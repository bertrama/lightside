package edu.cmu.side.plugin;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.yerihyo.yeritools.math.StatisticsToolkit;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.FeatureHit;

public abstract class LearningPlugin extends SIDEPlugin implements Serializable{
	private static final long serialVersionUID = -7928450759075851993L;

	protected static boolean halt = false;

	public static String type = "model_builder";

	public static StatusUpdater updater;

	public String getType() {
		return type;
	}

	public TrainingResult train(FeatureTable table, Map<String, String> configuration, Map<String, Serializable> map, StatusUpdater progressIndicator) throws Exception{

		halt = false;
		if(table == null){
			return null;
		}
		updater = progressIndicator;

		this.configureFromSettings(configuration);
		boolean[] mask = new boolean[table.getDocumentList().getSize()];
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
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				progressIndicator.update("Generating Folds Map", 0, 0);
				if (map.get("source").equals("RANDOM"))
				{
					foldsMap = BuildModelControl.getFoldsMapRandom(sdl, numFolds);
				}
				else if (map.get("source").equals("ANNOTATIONS"))
				{
					if(map.get("foldMethod").equals("AUTO"))
					{
						numFolds = sdl.getPossibleAnn(sdl.getCurrentAnnotation()).size();
					}
					foldsMap = BuildModelControl.getFoldsMapByAnnotation(sdl, map.get("annotation").toString(), numFolds);
				}
				else if (map.get("source").equals("FILES"))
				{
					if(map.get("foldMethod").equals("AUTO"))
					{
						numFolds = sdl.getFilenames().size();
					}
					foldsMap = BuildModelControl.getFoldsMapByFile(sdl, numFolds);
				}
				result = evaluateCrossValidation(table, foldsMap, progressIndicator);
				trainWithMaskForSubclass(table, mask, progressIndicator);
			}
			else if (map.get("type").equals("SUPPLY"))
			{
				trainWithMaskForSubclass(table, mask, progressIndicator);
				result = evaluateTestSet(table, (FeatureTable) map.get("testFeatureTable"), progressIndicator);
			}
		}
		return result;
	}

	protected TrainingResult evaluateCrossValidation(FeatureTable table, Map<Integer, Integer> foldsMap, StatusUpdater progressIndicator){
		boolean[] mask = new boolean[table.getDocumentList().getSize()];
		String[] predictions = new String[table.getDocumentList().getSize()];
		Set<Integer> folds = new TreeSet<Integer>();
		for(Integer key : foldsMap.keySet()){
			folds.add(foldsMap.get(key));
		}
		ArrayList<Double> times = new ArrayList<Double>();
		DecimalFormat print = new DecimalFormat("#.###");
		for(Integer fold : folds){
			for(Integer key : foldsMap.keySet()){
				mask[key] = !foldsMap.get(key).equals(fold);
			}
			double average = StatisticsToolkit.getAverage(times);
			double timeA = System.currentTimeMillis();
			try{
				progressIndicator.update((times.size()>0?"Time per fold: " + print.format(average)+", ":"") + "Training fold", (fold+1), folds.size());
				trainWithMaskForSubclass(table, mask, updater);				
			}catch(Exception e){
				e.printStackTrace();
			}
			for(int i = 0; i < mask.length; i++){
				mask[i] = !mask[i];
			}
			progressIndicator.update("Testing fold", fold, folds.size());
			PredictionResult preds = predictWithMaskForSubclass(table, table, mask, updater);
			int predictionIndex = 0;
			for(Comparable pred : preds.getPredictions()){
				while(!mask[predictionIndex]){
					predictionIndex++;
				}
				predictions[predictionIndex] = pred.toString();
				predictionIndex++;
			}
			double timeB = System.currentTimeMillis();
			times.add((timeB-timeA)/1000.0);
			if(halt)
			{
				break;
			}
		}
		if(!halt)
		{
			ArrayList<String> predictionsList = new ArrayList<String>();
			for(String s : predictions) predictionsList.add(s);
			return new TrainingResult(table, predictionsList);
		}
		else return null;
	}

	protected TrainingResult evaluateTestSet(FeatureTable train, FeatureTable testSet,StatusUpdater updater){
		boolean[] mask = new boolean[testSet.getDocumentList().getSize()];
		for(int i = 0; i < mask.length; i++) mask[i] = true;
		PredictionResult predictions = predictWithMaskForSubclass(train, testSet, mask, updater);
		TrainingResult training = new TrainingResult(train, testSet, predictions.getPredictions());
		return training;
	}

	protected abstract void trainWithMaskForSubclass(FeatureTable table, boolean[] mask, StatusUpdater progressIndicator) throws Exception;

	public PredictionResult predict(FeatureTable originalData, FeatureTable newData, Map<String, String> configuration, StatusUpdater progressIndicator){
		this.loadClassifierFromSettings(configuration);
		boolean[] mask = new boolean[newData.getDocumentList().getSize()];
		for(int i = 0; i < mask.length; i++) mask[i] = true;
		return predictWithMaskForSubclass(originalData, newData, mask, progressIndicator);
	}

	protected abstract PredictionResult predictWithMaskForSubclass(FeatureTable originalData, FeatureTable newData, boolean[] mask, StatusUpdater progressIndicator);

	public void stopWhenPossible(){
		halt = true;
	}
	
	public boolean isStopped()
	{
		return halt;
	}
	
	public abstract void loadClassifierFromSettings(Map<String, String> settings);
}
