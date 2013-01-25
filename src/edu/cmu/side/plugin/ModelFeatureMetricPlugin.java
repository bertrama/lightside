package edu.cmu.side.plugin;

import java.util.Map;

import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.Feature;

public abstract class ModelFeatureMetricPlugin<E extends Comparable<E>> extends FeatureMetricPlugin{

	
	public static String type = "model_feature_evaluation";
	
	public String getType() {
		return type;	
	}

	public Map<Feature, E> evaluateModelFeatures(TrainingResult model, boolean[] mask, String eval, StatusUpdater update) {
		String act = ExploreResultsControl.getHighlightedRow();
		String pred = ExploreResultsControl.getHighlightedColumn();
		return evaluateModelFeatures(model, mask, eval, pred, act, update);
	}
	
	public Map<Feature, E> evaluateFeatures(Recipe recipe, boolean[] mask, String eval, String target, StatusUpdater update){
		return evaluateModelFeatures(recipe.getTrainingResult(), mask, eval, update);
	}
	
	public abstract Map<Feature, E> evaluateModelFeatures(TrainingResult model, boolean[] mask, String eval, String pred, String act, StatusUpdater update);

}
