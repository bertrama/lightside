package edu.cmu.side.plugin;

import java.util.Collection;
import java.util.Map;

import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.TrainingResult;

public abstract class ModelMetricPlugin extends SIDEPlugin{

	public ModelMetricPlugin() {
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
	public abstract Map<String, String> evaluateModelFeatures(TrainingResult model, Map<String, String> settings);
}
