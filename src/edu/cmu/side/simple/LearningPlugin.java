package edu.cmu.side.simple;

import java.io.File;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JLabel;

import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public abstract class LearningPlugin extends SIDEPlugin implements Serializable{
	private static final long serialVersionUID = -7928450759075851993L;

	protected static boolean halt = false;

	public static String type = "model_builder";

	public static GenesisUpdater updater;

	public String getType() {
		return type;
	}

	public SimpleTrainingResult train(FeatureTable table, Map<String, String> configuration, Map<String, Object> validationSettings, GenesisUpdater progressIndicator) throws Exception{

		if(table == null){
			return null;
		}
		updater = progressIndicator;

		this.configureFromSettings(configuration);
		boolean[] mask = new boolean[table.getDocumentList().getSize()];
		for(int i = 0; i < mask.length; i++) mask[i] = true;
		SimpleDocumentList sdl = (SimpleDocumentList)validationSettings.get("testSet");
		SimpleTrainingResult result = null;
		if(Boolean.TRUE.toString().equals(validationSettings.get("test"))){
			if(validationSettings.get("type").equals("CV")){
				Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
				int numFolds = -1;
				try{
					numFolds = Integer.parseInt(validationSettings.get("numFolds").toString());
				}catch(Exception e){
					e.printStackTrace();
				}
				if(validationSettings.get("source").equals("RANDOM")){
					foldsMap = BuildModelControl.getFoldsMapRandom(sdl, numFolds);
				}else if(validationSettings.get("source").equals("ANNOTATIONS")){
					foldsMap = BuildModelControl.getFoldsMapByAnnotation(sdl, validationSettings.get("annotation").toString(), numFolds);
				}else if(validationSettings.get("source").equals("FILES")){
					foldsMap = BuildModelControl.getFoldsMapByFile(sdl, numFolds);
				}
				result = evaluateCrossValidation(table, foldsMap);
				trainWithMaskForSubclass(table, mask, progressIndicator);
			}else if(validationSettings.get("type").equals("SUPPLY")){
				trainWithMaskForSubclass(table, mask, progressIndicator);
				result = evaluateTestSet(table, (FeatureTable)validationSettings.get("supplied"));
			}
		}
		return result;
	}

	protected SimpleTrainingResult evaluateCrossValidation(FeatureTable table, Map<Integer, Integer> foldsMap){
		boolean[] mask = new boolean[table.getDocumentList().getSize()];
		String[] predictions = new String[table.getDocumentList().getSize()];
		Set<Integer> folds = new TreeSet<Integer>();
		for(Integer key : foldsMap.keySet()){
			folds.add(foldsMap.get(key));
		}
		for(Integer fold : folds){
			for(Integer key : foldsMap.keySet()){
				mask[key] = !foldsMap.get(key).equals(fold);
			}			
			try{
				trainWithMaskForSubclass(table, mask, updater);				
			}catch(Exception e){
				e.printStackTrace();
			}
			for(int i = 0; i < mask.length; i++){
				mask[i] = !mask[i];
			}
			SimplePredictionResult preds = predictWithMaskForSubclass(table, table, mask, updater);
			int predictionIndex = 0;
			for(Comparable pred : preds.getPredictions()){
				while(!mask[predictionIndex]){
					predictionIndex++;
				}
				predictions[predictionIndex] = pred.toString();
				predictionIndex++;

			}
		}
		ArrayList<String> predictionsList = new ArrayList<String>();
		for(String s : predictions) predictionsList.add(s);
		return new SimpleTrainingResult(table, predictionsList);
	}

	protected SimpleTrainingResult evaluateTestSet(FeatureTable train, FeatureTable testSet){
		Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
		boolean[] mask = new boolean[testSet.getDocumentList().getSize()];
		for(int i = 0; i < mask.length; i++) mask[i] = true;
		SimplePredictionResult predictions = predictWithMaskForSubclass(train, testSet, mask, updater);
		SimpleTrainingResult training = new SimpleTrainingResult(train, testSet, predictions.getPredictions());
		return training;
	}

	protected abstract void trainWithMaskForSubclass(FeatureTable table, boolean[] mask, GenesisUpdater progressIndicator) throws Exception;

	public SimplePredictionResult predict(FeatureTable originalData, FeatureTable newData, Map<String, String> configuration, GenesisUpdater progressIndicator){
		this.configureFromSettings(configuration);
		boolean[] mask = new boolean[newData.getDocumentList().getSize()];
		for(int i = 0; i < mask.length; i++) mask[i] = true;
		return predictWithMaskForSubclass(originalData, newData, mask, progressIndicator);
	}

	protected abstract SimplePredictionResult predictWithMaskForSubclass(FeatureTable originalData, FeatureTable newData, boolean[] mask, GenesisUpdater progressIndicator);


	public void stopWhenPossible(){
		halt = true;
	}
}
