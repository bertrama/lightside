package edu.cmu.side.plugin;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.yerihyo.yeritools.math.StatisticsToolkit;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.view.util.DefaultMap;

public abstract class LearningPlugin extends SIDEPlugin implements Serializable{
	private static final long serialVersionUID = -7928450759075851993L;

	public static String type = "model_builder";

	public static StatusUpdater updater;

	public String getType() {
		return type;
	}

	public TrainingResult train(FeatureTable table, Map<String, String> configuration, Map<String, Serializable> validationSettings, OrderedPluginMap wrappers, StatusUpdater progressIndicator) throws Exception{
		halt = false;
		if(table == null){
			return null;
		}
		updater = progressIndicator;
		Map<Integer, Integer> defaultFoldMapZero = new DefaultMap<Integer, Integer>(0);

		this.configureFromSettings(configuration);
		for(SIDEPlugin wrapper : wrappers.keySet()){
			((WrapperPlugin)wrapper).configureFromSettings(wrappers.get(wrapper));
		}
		
		
		DocumentList sdl = (DocumentList)validationSettings.get("testSet");
		TrainingResult result = null;
		if (Boolean.TRUE.toString().equals(validationSettings.get("test")))
		{
			if (validationSettings.get("type").equals("CV"))
			{
				Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
				int numFolds = -1;
				try
				{
					numFolds = Integer.parseInt(validationSettings.get("numFolds").toString());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				progressIndicator.update("Generating Folds Map", 0, 0);
				if (validationSettings.get("source").equals("RANDOM"))
				{
					if(validationSettings.get("foldMethod").equals("AUTO"))
					{
						numFolds = Math.min(10, sdl.getSize());
					}
					foldsMap = BuildModelControl.getFoldsMapRandom(sdl, numFolds);
				}
				else if (validationSettings.get("source").equals("ANNOTATIONS"))
				{
					String annotation = validationSettings.get("annotation").toString();
					if(validationSettings.get("foldMethod").equals("AUTO"))
					{
						numFolds = sdl.getPossibleAnn(annotation).size();
					}
					foldsMap = BuildModelControl.getFoldsMapByAnnotation(sdl, annotation, numFolds);
				}
				else if (validationSettings.get("source").equals("FILES"))
				{
					if(validationSettings.get("foldMethod").equals("AUTO"))
					{
						numFolds = sdl.getFilenames().size();
					}
					foldsMap = BuildModelControl.getFoldsMapByFile(sdl, numFolds);
				}
				
				result = evaluateCrossValidation(table, foldsMap, wrappers, progressIndicator);
				
				progressIndicator.update("Training final model on all data");
				FeatureTable wrappedTable = wrapAndTrain(table, wrappers, progressIndicator, defaultFoldMapZero, 1);
			}
			else if (validationSettings.get("type").equals("SUPPLY"))
			{
				progressIndicator.update("Training model");
				FeatureTable pass = wrapAndTrain(table, wrappers, progressIndicator, defaultFoldMapZero, 1);
				progressIndicator.update("Testing model");
				FeatureTable passTest = (FeatureTable) validationSettings.get("testFeatureTable");
				result = evaluateTestSet(pass, passTest, wrappers, progressIndicator);
			}
		}else{
			FeatureTable wrappedTable = wrapAndTrain(table, wrappers, progressIndicator, defaultFoldMapZero, 1);
			List<Comparable<Comparable>> blankPredictions = new ArrayList<Comparable<Comparable>>();
			switch(wrappedTable.getClassValueType()){
			case NOMINAL:
			case BOOLEAN:
			case STRING:
				for(int i = 0; i < wrappedTable.getSize(); i++){ blankPredictions.add((Comparable)wrappedTable.getDocumentList().getLabelArray()[0]); }
				break;
			case NUMERIC:
				for(int i = 0; i < wrappedTable.getSize(); i++){ blankPredictions.add((Comparable)new Double(0.0)); }
				break;
			}
			result = new TrainingResult(wrappedTable, blankPredictions);
		}
		result.setLongDescriptionString(getLongDescriptionString());
		return result;
	}

	/**
	 * @param wrappers
	 * @param progressIndicator
	 * @param defaultFoldMapZero
	 * @param pass
	 * @param passTest
	 * @return
	 */
//	protected TrainingResult wrapAndTest(OrderedPluginMap wrappers, StatusUpdater progressIndicator, Map<Integer, Integer> defaultFoldMapZero,
//			FeatureTable pass, FeatureTable passTest)
//	{
//		TrainingResult result = evaluateTestSet(pass, passTest, wrappers, progressIndicator);
//		return result;
//	}

	/**
	 * @param table
	 * @param wrappers
	 * @param progressIndicator
	 * @param foldMap
	 * @return
	 * @throws Exception
	 */
	public FeatureTable
			wrapAndTrain(FeatureTable table, OrderedPluginMap wrappers, StatusUpdater progressIndicator, Map<Integer, Integer> foldMap, int fold)
					throws Exception
	{
		FeatureTable wrapped = wrapTableBefore(table, fold, foldMap, progressIndicator, wrappers, true);
		prepareAndTrainAgainstFold(wrapped, fold, foldMap, progressIndicator);
		return wrapped;
	}

	public TrainingResult evaluateCrossValidation(FeatureTable table, Map<Integer, Integer> foldsMap, OrderedPluginMap wrappers, StatusUpdater progressIndicator)
	{
		DocumentList localDocuments = table.getDocumentList();
		Comparable[] predictions = new Comparable[localDocuments.getSize()];
		String[] labelArray = localDocuments.getLabelArray();
		Map<String, List<Double>> distributions = null;
		
		Set<Integer> folds = new TreeSet<Integer>();
		for(Integer key : foldsMap.keySet()){
			folds.add(foldsMap.get(key));
		}

		ArrayList<Double> times = new ArrayList<Double>();
		DecimalFormat print = new DecimalFormat("#.###");
		for(Integer fold : folds)
		{
			if(halt)
			{
				break;
			}
			
			double average = StatisticsToolkit.getAverage(times);
			double timeA = System.currentTimeMillis();
			progressIndicator.update(
					(times.size() > 0 ? print.format(average) + " sec per fold,\t" : "") 
					+ "Training fold", (fold + 1), folds.size());
			
			FeatureTable pass = table;
			
			try
			{
				pass = wrapAndTrain(table, wrappers, progressIndicator, foldsMap, fold);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (halt)
			{
				break;
			}

			progressIndicator.update(
					(times.size() > 0 ? print.format(average) + " sec per fold,\t" : "") 
					+ "Testing fold", (fold+1), folds.size());
			
			PredictionResult predictionResult = predictOnFold(pass, pass, fold, foldsMap, updater, wrappers);
			
			List<? extends Comparable<?>> predictionsList = predictionResult.getPredictions();

			//int predictionIndex = 0;
			double correct = 0;
			double total = 0;
			
			if(distributions == null) distributions = new HashMap<String, List<Double>>(predictionResult.getDistributions());
			
			for (int i = 0; i < predictionsList.size(); i++)
			{
				if(foldsMap.get(i).equals(fold))
				{
					for(String label : labelArray)
					{
						distributions.get(label).set(i, predictionResult.getDistributions().get(label).get(i));
					}
					
					predictions[i] = predictionsList.get(i);
					
					if(predictions[i].equals(localDocuments.getAnnotationArray().get(i)))
						correct++;
					total++;
				}
				//predictionIndex++;
			}
			System.out.println("accuracy for fold #"+fold+": "+(100*correct/total)+"%");
			double timeB = System.currentTimeMillis();
			times.add((timeB - timeA) / 1000.0);
		}
		if(!halt)
		{
			progressIndicator.update("Generating confusion matrix");
			List<Comparable<Comparable>> predictionsList = new ArrayList<Comparable<Comparable>>();

			PredictionResult pred = new PredictionResult(predictionsList, distributions);
			for(Comparable s : predictions) predictionsList.add(s);
			return new TrainingResult(table, pred);
		}
		else 
		{
			for(SIDEPlugin wrapper : wrappers.keySet())
			{
				wrapper.stopWhenPossible();
			}
			return null;
		}
	}

	protected TrainingResult evaluateTestSet(FeatureTable train, FeatureTable testSet, OrderedPluginMap wrappers, StatusUpdater updater){
		DefaultMap<Integer, Integer> defaultFoldMap = new DefaultMap<Integer, Integer>(0);
		PredictionResult predictions = predictOnFold(train, testSet, 0, defaultFoldMap, updater, wrappers);
		TrainingResult training = new TrainingResult(train, testSet, predictions);
		return training;
	}

	protected void prepareAndTrainAgainstFold(FeatureTable table, int fold, Map<Integer, Integer> foldsMap, StatusUpdater progressIndicator) throws Exception
	{
		for(int i = 0; i < table.getSize(); i++)
		{
			if(!foldsMap.get(i).equals(fold))
			{
				for(FeatureHit hit : table.getHitsForDocument(i))
				{
					hit.prepareForTraining(fold, foldsMap, table);
				}
			}
		}
		trainAgainstFold(table, fold, foldsMap, progressIndicator);
	}
	protected abstract void trainAgainstFold(FeatureTable table, int fold, Map<Integer, Integer> foldsMap, StatusUpdater progressIndicator) throws Exception;

	public PredictionResult predict(FeatureTable originalData, FeatureTable newData, Map<String, String> configuration, StatusUpdater progressIndicator, OrderedPluginMap wrappers){
		this.loadClassifierFromSettings(configuration);
		return predictOnFold(originalData, newData, 0, new DefaultMap<Integer, Integer>(0), progressIndicator, wrappers);
	}

	protected  PredictionResult predictOnFold(FeatureTable originalData, FeatureTable newData, int fold, Map<Integer, Integer> foldsMap, StatusUpdater progressIndicator, OrderedPluginMap wrappers)
	{
		newData = wrapTableBefore(newData, fold, foldsMap, progressIndicator, wrappers, false);
		
		Object predictionContext = prepareToPredict(originalData, newData, fold, foldsMap);
		
		PredictionResult prediction = actuallyPredict(originalData, newData, fold, foldsMap, predictionContext);
		
		prediction = wrapTableAfter(fold, foldsMap, wrappers, newData, prediction);

		return prediction;

	}

	/**
	 * @param fold
	 * @param foldsMap
	 * @param wrappers
	 * @param prediction
	 * @return
	 */
	protected PredictionResult wrapTableAfter(int fold, Map<Integer, Integer> foldsMap, OrderedPluginMap wrappers, FeatureTable table, PredictionResult prediction)
	{
		for (SIDEPlugin wrapper : wrappers.keySet())
		{
			prediction = ((WrapperPlugin) wrapper).wrapResultAfter(prediction, table, fold, foldsMap, updater);
		}
		return prediction;
	}

	/**
	 * @param originalData
	 * @param newData
	 * @param fold
	 * @param foldsMap
	 * @param predictionContext
	 * @param prediction
	 * @return
	 */
	protected PredictionResult actuallyPredict(FeatureTable originalData, FeatureTable newData, int fold, Map<Integer, Integer> foldsMap,
			Object predictionContext)
	{
		PredictionResult prediction = null;
		switch (originalData.getClassValueType())
		{
			case NUMERIC:
				List<Double> numericPredictions = new ArrayList<Double>();
				for (int i = 0; i < newData.getSize(); i++)
				{
					if(foldsMap.get(i).equals(fold))
					{
						for(FeatureHit hit : newData.getHitsForDocument(i))
						{
							hit.prepareToPredict(fold, foldsMap, newData, numericPredictions);
						}
						
						try
						{
							//numericPredictions.add(classifier.classifyInstance(instance));
							numericPredictions.add(predictNumeric(i, originalData, newData, predictionContext));
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
						numericPredictions.add(0.0);
				}
				prediction = new PredictionResult(numericPredictions);
				break;
			case NOMINAL:
			case BOOLEAN:
				List<String> predictions = new ArrayList<String>();
				Map<String, List<Double>> distributions = new HashMap<String, List<Double>>();

				String[] labelArray = originalData.getDocumentList().getLabelArray();
				for (String possible : labelArray)
				{
					distributions.put(possible, new ArrayList<Double>());
				}

				try
				{
					for (int i = 0; i < newData.getSize(); i++)
					{
//						Instance instance = inst.instance(i);
//						double[] distro = classifier.distributionForInstance(instance);
						
						double[] distro;
						if(foldsMap.get(i).equals(fold))
						{
							for(FeatureHit hit : newData.getHitsForDocument(i))
							{
								hit.prepareToPredict(fold, foldsMap, newData, predictions);
							}
							
							distro = predictLabel(i, originalData, newData, predictionContext);
						}
						else
						{
							distro = new double[labelArray.length];
						}
						
						int index = -1;
						double max = Double.NEGATIVE_INFINITY;
						for (int j = 0; j < distro.length; j++)
						{
							if (distro[j] > max)
							{
								max = distro[j];
								index = j;
							}
							distributions.get(labelArray[j]).add(distro[j]);
						}
						predictions.add(labelArray[index]);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				prediction = new PredictionResult(predictions, distributions);
				
			
		}
		return prediction;
	}

	/**
	 * @param newData
	 * @param fold
	 * @param foldsMap
	 * @param progressIndicator
	 * @param wrappers
	 * @return
	 */
	protected FeatureTable wrapTableBefore(FeatureTable newData, int fold, Map<Integer, Integer> foldsMap, StatusUpdater progressIndicator,
			OrderedPluginMap wrappers, boolean learn)
	{

		
		for (SIDEPlugin wrapper : wrappers.keySet())
		{
			//System.out.println("wrapper " + fold + ": " + wrappers.get(wrapper));
			wrapper.configureFromSettings(wrappers.get(wrapper));
			if(learn)
				((WrapperPlugin) wrapper).learnFromTrainingData(newData, fold, foldsMap, progressIndicator);
		
			newData = ((WrapperPlugin) wrapper).wrapTableBefore(newData, fold, foldsMap, progressIndicator);
		}
		return newData;
	}

	/**
	 * predict the nominal/boolean class value of instance i in newData
	 * @param i
	 * @param originalData
	 * @param newData
	 * @param mask
	 * @param predictionContext
	 * @return a label distribution
	 */
	public abstract double[] predictLabel(int i, FeatureTable originalData, FeatureTable newData, Object predictionContext) throws Exception;

	/**
	 * predict the numeric value of instance i in newData
	 * @param i
	 * @param originalData
	 * @param newData
	 * @param mask
	 * @param predictionContext
	 * @return the predicted value for this instance
	 */
	public abstract double predictNumeric(int i, FeatureTable originalData, FeatureTable newData, Object predictionContext)  throws Exception;
	
	/**
	 * do any setup before prediction, and keep the context of that setup in the returned object
	 * @param originalData - a reference to the training data
	 * @param newData - the data to predict on
	 * @param mask - a mask of the document instances that are actually being classified on - does anybody use this?
	 * @param progressIndicator
	 * @return a context object that the subclass may use to inform its per-document predictions
	 */
	public abstract Object prepareToPredict(FeatureTable originalData, FeatureTable newData, int i, Map<Integer, Integer> foldsMap);

	public void stopWhenPossible(){
		halt = true;
	}
	
	public abstract String getLongDescriptionString();
	public abstract void loadClassifierFromSettings(Map<String, String> settings);

}
