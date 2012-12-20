package edu.cmu.side.simple;

import java.util.Collection;
import java.util.Map;

import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.SimpleTrainingResult;

public abstract class ModelEvaluationPlugin extends SIDEPlugin{

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

	public abstract Collection<String> getAvailableEvaluations(FeatureTable table);
	
	/**
	 * 
	 */
	public abstract Map<String, String> evaluateModelFeatures(SimpleTrainingResult model, Map<String, String> settings);
}
