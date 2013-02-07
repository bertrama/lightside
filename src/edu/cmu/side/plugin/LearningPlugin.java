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

	public TrainingResult train(FeatureTable table, Map<String, String> configuration, Map<String, Serializable> map, OrderedPluginMap wrappers, StatusUpdater progressIndicator) throws Exception{
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
		
		
		DocumentList sdl = (DocumentList)map.get("testSet");
		TrainingResult result = null;
		if (Boolean.TRUE.toString().equals(map.get("test")))
		{
			if (map.get("type").equals("CV"))
			{
				Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
				int numFolds = -1;
				try
				{
					numFolds = Integer.parseInt(map.get("numFolds").toString());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				progressIndicator.update("Generating Folds Map", 0, 0);
				if (map.get("source").equals("RANDOM"))
				{
					if(map.get("foldMethod").equals("AUTO"))
					{
						numFolds = Math.min(10, sdl.getSize());
					}
					foldsMap = BuildModelControl.getFoldsMapRandom(sdl, numFolds);
				}
				else if (map.get("source").equals("ANNOTATIONS"))
				{
					String annotation = map.get("annotation").toString();
					if(map.get("foldMethod").equals("AUTO"))
					{
						numFolds = sdl.getPossibleAnn(annotation).size();
					}
					foldsMap = BuildModelControl.getFoldsMapByAnnotation(sdl, annotation, numFolds);
				}
				else if (map.get("source").equals("FILES"))
				{
					if(map.get("foldMethod").equals("AUTO"))
					{
						numFolds = sdl.getFilenames().size();
					}
					foldsMap = BuildModelControl.getFoldsMapByFile(sdl, numFolds);
				}
				
				result = evaluateCrossValidation(table, foldsMap, wrappers, progressIndicator);
				
				progressIndicator.update("Training final model on all data");
				for(SIDEPlugin wrapper : wrappers.keySet()){
					((WrapperPlugin)wrapper).learnFromTrainingData(table, 1, defaultFoldMapZero, progressIndicator);
				}
				FeatureTable wrappedTable = table;
				for(SIDEPlugin wrapper : wrappers.keySet()){
					wrappedTable = ((WrapperPlugin)wrapper).wrapTableBefore(wrappedTable, 1, defaultFoldMapZero, progressIndicator);
				}
				prepareAndTrainAgainstFold(wrappedTable, 1, defaultFoldMapZero, progressIndicator);
			}
			else if (map.get("type").equals("SUPPLY"))
			{
				progressIndicator.update("Training model");
				for(SIDEPlugin wrapper : wrappers.keySet()){
					((WrapperPlugin)wrapper).learnFromTrainingData(table, 1, defaultFoldMapZero, progressIndicator);
				}
				FeatureTable pass = table;
				for(SIDEPlugin wrapper : wrappers.keySet()){
					pass = ((WrapperPlugin)wrapper).wrapTableBefore(pass, 1, defaultFoldMapZero, progressIndicator);
				}
				prepareAndTrainAgainstFold(pass, 1, defaultFoldMapZero, progressIndicator);
				progressIndicator.update("Testing model");
				FeatureTable passTest = (FeatureTable) map.get("testFeatureTable");
				for(SIDEPlugin wrapper : wrappers.keySet()){
					passTest = ((WrapperPlugin)wrapper).wrapTableBefore(passTest, 0, defaultFoldMapZero, progressIndicator);
				}
				result = evaluateTestSet(pass, passTest, wrappers, progressIndicator);
			}
		}
		result.setLongDescriptionString(getLongDescriptionString());
		return result;
	}

	protected TrainingResult evaluateCrossValidation(FeatureTable table, Map<Integer, Integer> foldsMap, OrderedPluginMap wrappers, StatusUpdater progressIndicator)
	{
		DocumentList localDocuments = table.getDocumentList();
		Comparable[] predictions = new Comparable[localDocuments.getSize()];
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
					(times.size() > 0 ? print.format(average) + "sec per fold,\t" : "") 
					+ "Training fold", (fold + 1), folds.size());
			
			for(SIDEPlugin wrapper : wrappers.keySet())
			{
				((WrapperPlugin)wrapper).learnFromTrainingData(table, fold, foldsMap, progressIndicator);
			}
			FeatureTable pass = table;
			for(SIDEPlugin wrapper : wrappers.keySet())
			{
				pass = ((WrapperPlugin)wrapper).wrapTableBefore(pass, fold, foldsMap, progressIndicator);
			}
			
			try
			{
				prepareAndTrainAgainstFold(pass, fold, foldsMap, updater);
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
			PredictionResult predictionResult = predictOnFold(pass, pass, fold, foldsMap, updater);
			
			List<? extends Comparable<?>> predictionsList = predictionResult.getPredictions();

			int predictionIndex = 0;
			for (int i = 0; i < predictionsList.size(); i++)
			{
				if(foldsMap.get(i).equals(fold))
				{
					predictions[predictionIndex] = predictionsList.get(i);
				}
				predictionIndex++;
			}
			double timeB = System.currentTimeMillis();
			times.add((timeB - timeA) / 1000.0);
		}
		if(!halt)
		{
			progressIndicator.update("Generating confusion matrix");
			List<Comparable<Comparable>> predictionsList = new ArrayList<Comparable<Comparable>>();
			for(Comparable s : predictions) predictionsList.add(s);
			return new TrainingResult(table, predictionsList);
		}
		else return null;
	}

	protected TrainingResult evaluateTestSet(FeatureTable train, FeatureTable testSet, OrderedPluginMap wrappers, StatusUpdater updater){
		boolean[] mask = new boolean[testSet.getSize()];
		DefaultMap<Integer, Integer> defaultFoldMap = new DefaultMap<Integer, Integer>(0);
		PredictionResult predictions = predictOnFold(train, testSet, 0, defaultFoldMap, updater);
		for(SIDEPlugin wrapper : wrappers.keySet()){
			predictions = ((WrapperPlugin)wrapper).wrapResultAfter(predictions, 0, defaultFoldMap, updater);
		}
		TrainingResult training = new TrainingResult(train, testSet, predictions.getPredictions());
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

	public PredictionResult predict(FeatureTable originalData, FeatureTable newData, Map<String, String> configuration, StatusUpdater progressIndicator){
		this.loadClassifierFromSettings(configuration);
		return predictOnFold(originalData, newData, 0, new DefaultMap<Integer, Integer>(0), progressIndicator);
	}

	protected  PredictionResult predictOnFold(FeatureTable originalData, FeatureTable newData, int fold, Map<Integer, Integer> foldsMap, StatusUpdater progressIndicator)
	{
		Object predictionContext = prepareToPredict(originalData, newData, fold, foldsMap);
		//Instances inst = WekaTools.getInstances(originalData, newData, mask);
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
	 * predict the nominal/boolean class value of instance i in newData
	 * @param i
	 * @param originalData
	 * @param newData
	 * @param mask
	 * @param predictionContext
	 * @return a label distribution
	 */
	protected abstract double[] predictLabel(int i, FeatureTable originalData, FeatureTable newData, Object predictionContext) throws Exception;

	/**
	 * predict the numeric value of instance i in newData
	 * @param i
	 * @param originalData
	 * @param newData
	 * @param mask
	 * @param predictionContext
	 * @return the predicted value for this instance
	 */
	protected abstract double predictNumeric(int i, FeatureTable originalData, FeatureTable newData, Object predictionContext)  throws Exception;
	
	/**
	 * do any setup before prediction, and keep the context of that setup in the returned object
	 * @param originalData - a reference to the training data
	 * @param newData - the data to predict on
	 * @param mask - a mask of the document instances that are actually being classified on - does anybody use this?
	 * @param progressIndicator
	 * @return a context object that the subclass may use to inform its per-document predictions
	 */
	protected abstract Object prepareToPredict(FeatureTable originalData, FeatureTable newData, int i, Map<Integer, Integer> foldsMap);

	public void stopWhenPossible(){
		halt = true;
	}
	
	public abstract String getLongDescriptionString();
	public abstract void loadClassifierFromSettings(Map<String, String> settings);

}
