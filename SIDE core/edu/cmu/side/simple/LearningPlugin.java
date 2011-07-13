package edu.cmu.side.simple;

import java.awt.Component;

import java.io.File;
import java.util.Map;

import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureTable;

public abstract class LearningPlugin extends SIDEPlugin {

	/**
	 * Kept for legacy reasons, implemented in this class so the developer doesn't have to.
	 */
	@Override
	public boolean doValidation(StringBuffer msg) {
		return true;
	}

	@Override
	public void memoryToUI() {}

	/**
	 * Called before extracting features; implementation should ensure that 
	 * all settings in the UI are transferred to model settings in this method.
	 */
	@Override
	public void uiToMemory() {}
	
	public LearningPlugin(){
		super();
	}
	
	public LearningPlugin(File root){
		super(root);
	}
	

	public static String type = "model_builder";
	
	public String getType() {
		return type;
	}
		
	/**
	 * @return A short prefix string for the plugin name.
	 */
	public abstract String getOutputName();
	
	public abstract TrainingResultInterface train(FeatureTable featureTable, String desiredName, Map<String, String> evaluationSettings, Map<Integer, Integer> foldsMap);


}
