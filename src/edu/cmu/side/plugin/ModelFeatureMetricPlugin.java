package edu.cmu.side.plugin;

import java.util.Map;

import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.Feature;

public abstract class ModelFeatureMetricPlugin<E extends Comparable<E>> extends FeatureMetricPlugin{

	
	public static String type = "model_feature_evaluation";
	
	public String getType() {
		return type;	
	}
	
	public abstract Map<Feature, E> evaluateModelFeatures(TrainingResult model, boolean[] mask, String eval, String target);

	public Map<Feature, E> evaluateFeatures(Recipe recipe, boolean[] mask, String eval, String target){
		return evaluateModelFeatures(recipe.getTrainingResult(), mask, eval, target);
	}
}
