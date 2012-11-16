package edu.cmu.side.simple;

import java.io.File;


import java.io.Serializable;
import java.util.Map;

import javax.swing.JLabel;

import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureTable;

public abstract class LearningPlugin extends SIDEPlugin implements Serializable{
	private static final long serialVersionUID = -7928450759075851993L;

	protected static boolean halt = false;
	
	public static String type = "model_builder";
	
	public String getType() {
		return type;
	}
	
	public SimpleTrainingResult train(String name, FeatureTable table, Map<String, String> configuration, GenesisUpdater progressIndicator) throws Exception{
		this.configureFromSettings(configuration);
		return trainForSubclass(name, table, progressIndicator);
	}
	
	protected abstract SimpleTrainingResult trainForSubclass(String name, FeatureTable table, GenesisUpdater progressIndicator) throws Exception;
	
	public SimplePredictionResult predict(String name, FeatureTable newData, Map<String, String> configuration, GenesisUpdater progressIndicator){
		this.configureFromSettings(configuration);
		return predictForSubclass(name, newData, progressIndicator);
	}
	
	protected abstract SimplePredictionResult predictForSubclass(String name, FeatureTable newData, GenesisUpdater progressIndicator);
	
	public void stopWhenPossible(){
		halt = true;
	}
}
