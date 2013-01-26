package edu.cmu.side.control;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.plugin.EvaluateOneModelPlugin;
import edu.cmu.side.plugin.ModelFeatureMetricPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class ExploreResultsControl extends GenesisControl{

	private static Recipe highlightedTrainedModel;
	
	private static Map<EvaluateOneModelPlugin, Boolean> modelAnalysisPlugins;
	private static Map<ModelFeatureMetricPlugin, Map<String, Boolean>> featureEvaluationPlugins;
	private static StatusUpdater update = new SwingUpdaterLabel();
	private static String highlightedRow; private static String highlightedColumn;
	private static Feature highlightedFeature;
	

	static{
		modelAnalysisPlugins = new HashMap<EvaluateOneModelPlugin, Boolean>();
		SIDEPlugin[] modelEvaluations = PluginManager.getSIDEPluginArrayByType("model_analysis");
		for(SIDEPlugin fe : modelEvaluations){
			modelAnalysisPlugins.put((EvaluateOneModelPlugin)fe, false);
		}

		featureEvaluationPlugins = new HashMap<ModelFeatureMetricPlugin, Map<String, Boolean>>();
		SIDEPlugin[] tableEvaluations = PluginManager.getSIDEPluginArrayByType("model_feature_evaluation");
		for(SIDEPlugin fe : tableEvaluations){
			ModelFeatureMetricPlugin plugin = (ModelFeatureMetricPlugin)fe;
			featureEvaluationPlugins.put(plugin, new TreeMap<String, Boolean>());
			for(Object s : plugin.getAvailableEvaluations()){
				featureEvaluationPlugins.get(plugin).put(s.toString(), false);
			}
		}
	}
	
	public static StatusUpdater getUpdater(){
		return update;
	}
	public static EvalCheckboxListener getCheckboxListener(AbstractListPanel source){
		return new EvalCheckboxListener(source, featureEvaluationPlugins, ExploreResultsControl.getUpdater());
	}

	public static void setHighlightedCell(String row, String col){
		highlightedRow = row;
		highlightedColumn = col;
	}
	
	public static String getHighlightedRow(){
		return highlightedRow;
	}
	public static String getHighlightedColumn(){
		return highlightedColumn;
	}
	
	public static Map<EvaluateOneModelPlugin, Boolean> getModelAnalysisPlugins(){
		return modelAnalysisPlugins;
	}

	public static Map<ModelFeatureMetricPlugin, Map<String, Boolean>> getFeatureEvaluationPlugins(){
		return featureEvaluationPlugins;
	}
	
	public static Feature getHighlightedFeature(){
		return highlightedFeature;
	}
	
	public static void setHighlightedFeature(Feature f){
		highlightedFeature = f;
	}
	
	
	public static void setHighlightedModelAnalysisPlugin(EvaluateOneModelPlugin plug){
		for(EvaluateOneModelPlugin plugin : modelAnalysisPlugins.keySet()){
			modelAnalysisPlugins.put(plugin, plugin==plug);
		}
	}
		
	public static boolean hasHighlightedTrainedModelRecipe(){
		return highlightedTrainedModel!=null;
	}

	public static Recipe getHighlightedTrainedModelRecipe(){
		return highlightedTrainedModel;
	}

	public static void setHighlightedTrainedModelRecipe(Recipe highlight){
		highlightedTrainedModel = highlight;
	}
}
