package edu.cmu.side.plugin;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
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

		this.configureFromSettings(configuration);
		for(SIDEPlugin wrapper : wrappers.keySet()){
			((WrapperPlugin)wrapper).configureFromSettings(wrappers.get(wrapper));
		}
		boolean[] mask = new boolean[table.getSize()];
		for(int i = 0; i < mask.length; i++) mask[i] = true;
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
					System.out.println("LP 57: user wants "+numFolds+" folds");
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
					((WrapperPlugin)wrapper).learnFromTrainingData(table, mask, progressIndicator);
				}
				FeatureTable pass = table;
				for(SIDEPlugin wrapper : wrappers.keySet()){
					pass = ((WrapperPlugin)wrapper).wrapTableBefore(pass, mask, progressIndicator);
				}
				trainWithMaskForSubclass(pass, mask, progressIndicator);
			}
			else if (map.get("type").equals("SUPPLY"))
			{
				progressIndicator.update("Training model");
				for(SIDEPlugin wrapper : wrappers.keySet()){
					((WrapperPlugin)wrapper).learnFromTrainingData(table, mask, progressIndicator);
				}
				FeatureTable pass = table;
				for(SIDEPlugin wrapper : wrappers.keySet()){
					pass = ((WrapperPlugin)wrapper).wrapTableBefore(pass, mask, progressIndicator);
				}
				trainWithMaskForSubclass(pass, mask, progressIndicator);
				progressIndicator.update("Testing model");
				FeatureTable passTest = (FeatureTable) map.get("testFeatureTable");
				for(SIDEPlugin wrapper : wrappers.keySet()){
					passTest = ((WrapperPlugin)wrapper).wrapTableBefore(passTest, mask, progressIndicator);
				}
				result = evaluateTestSet(pass, passTest, wrappers, progressIndicator);
			}
		}
		result.setLongDescriptionString(getLongDescriptionString());
		return result;
	}

	protected TrainingResult evaluateCrossValidation(FeatureTable table, Map<Integer, Integer> foldsMap, OrderedPluginMap wrappers, StatusUpdater progressIndicator){
		DocumentList localDocuments = table.getDocumentList();
		boolean[] mask = new boolean[localDocuments.getSize()];
		Comparable[] predictions = new Comparable[localDocuments.getSize()];
		Set<Integer> folds = new TreeSet<Integer>();
		for(Integer key : foldsMap.keySet()){
			folds.add(foldsMap.get(key));
		}
		System.out.println("LP 128: Number of folds = "+folds.size());

		ArrayList<Double> times = new ArrayList<Double>();
		DecimalFormat print = new DecimalFormat("#.###");
		for(Integer fold : folds)
		{
			if(halt)
			{
				break;
			}
			
			for (Integer key : foldsMap.keySet())
			{
				mask[key] = !foldsMap.get(key).equals(fold);
			}
			double average = StatisticsToolkit.getAverage(times);
			double timeA = System.currentTimeMillis();
			progressIndicator
					.update((times.size() > 0 ? "Time per fold: " + print.format(average) + ", " : "") + "Training fold", (fold + 1), folds.size());
			for(SIDEPlugin wrapper : wrappers.keySet()){
				((WrapperPlugin)wrapper).learnFromTrainingData(table, mask, progressIndicator);
			}
			FeatureTable pass = table;
			for(SIDEPlugin wrapper : wrappers.keySet()){
				pass = ((WrapperPlugin)wrapper).wrapTableBefore(pass, mask, progressIndicator);
			}
			try
			{
				trainWithMaskForSubclass(pass, mask, updater);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			for (int i = 0; i < mask.length; i++)
			{
				mask[i] = !mask[i];
			}

			if (halt)
			{
				break;
			}

			progressIndicator.update("Testing fold", (fold+1), folds.size());
			PredictionResult preds = predictWithMaskForSubclass(pass, pass, mask, updater);
			
			int predictionIndex = 0;
			for (Comparable pred : preds.getPredictions())
			{
				while (!mask[predictionIndex])
				{
					predictionIndex++;
				}
				predictions[predictionIndex] = pred;
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
		for(int i = 0; i < mask.length; i++) mask[i] = true;
		PredictionResult predictions = predictWithMaskForSubclass(train, testSet, mask, updater);
		for(SIDEPlugin wrapper : wrappers.keySet()){
			predictions = ((WrapperPlugin)wrapper).wrapResultAfter(predictions, mask, updater);
		}
		TrainingResult training = new TrainingResult(train, testSet, predictions.getPredictions());
		return training;
	}

	protected abstract void trainWithMaskForSubclass(FeatureTable table, boolean[] mask, StatusUpdater progressIndicator) throws Exception;

	public PredictionResult predict(FeatureTable originalData, FeatureTable newData, Map<String, String> configuration, StatusUpdater progressIndicator){
		this.loadClassifierFromSettings(configuration);
		boolean[] mask = new boolean[newData.getSize()];
		for(int i = 0; i < mask.length; i++) mask[i] = true;
		return predictWithMaskForSubclass(originalData, newData, mask, progressIndicator);
	}

	protected abstract PredictionResult predictWithMaskForSubclass(FeatureTable originalData, FeatureTable newData, boolean[] mask, StatusUpdater progressIndicator);

	public void stopWhenPossible(){
		halt = true;
	}
	
	public abstract String getLongDescriptionString();
	public abstract void loadClassifierFromSettings(Map<String, String> settings);
}
