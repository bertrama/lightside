package edu.cmu.side.simple;

import java.util.Map;

import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.SimpleTrainingResult;

public abstract class ModelEvaluationPlugin extends SIDEPlugin{

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

	public ModelEvaluationPlugin() {
		super();
	}
	
	public static String type = "model_evaluation";
	
	public String getType() {
		return type;
	}
		
	/**
	 * @return A short prefix string for the plugin name.
	 */
	public abstract String getOutputName();

	/**
	 * 
	 */
	public abstract Map<Feature, Double> evaluateModelFeatures(SimpleTrainingResult model, Map<String, String> settings);
}
