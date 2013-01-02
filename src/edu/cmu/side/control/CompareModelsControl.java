package edu.cmu.side.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.plugin.EvaluateTwoModelPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class CompareModelsControl extends GenesisControl{

	private static Recipe baselineModel;
	private static Recipe competingModel;
	
	private static Set<EvaluateTwoModelPlugin> modelComparisonPlugins;
	private static EvaluateTwoModelPlugin highlightedModelComparisonPlugin;
	
	private static StatusUpdater update = new SwingUpdaterLabel();
	private static EvalCheckboxListener eval;


	static{
		modelComparisonPlugins = new HashSet<EvaluateTwoModelPlugin>();
		SIDEPlugin[] modelComparisons = PluginManager.getSIDEPluginArrayByType("model_comparison");
		for(SIDEPlugin fe : modelComparisons){
			modelComparisonPlugins.add((EvaluateTwoModelPlugin)fe);
		}
	}

	public static void setHighlightedModelComparisonPlugin(EvaluateTwoModelPlugin plug){
		highlightedModelComparisonPlugin = plug;
		
	}
	
	public static Set<EvaluateTwoModelPlugin> getModelComparisonPlugins(){
		return modelComparisonPlugins;
	}
	
	public static EvaluateTwoModelPlugin getHighlightedModelComparisonPlugin(){
		return highlightedModelComparisonPlugin;
	}

	public static void setUpdater(StatusUpdater up){
		update = up;
	}

	public static EvalCheckboxListener getEvalCheckboxListener(){
		return eval;
	}
	

	public static boolean hasBaselineTrainedModelRecipe(){
		return baselineModel != null;
	}

	public static Recipe getBaselineTrainedModelRecipe(){
		return baselineModel;
	}

	public static void setBaselineTrainedModelRecipe(Recipe highlight){
		baselineModel = highlight;
		Workbench.update();
	}

	public static boolean hasCompetingTrainedModelRecipe(){
		return competingModel != null;
	}

	public static Recipe getCompetingTrainedModelRecipe(){
		return competingModel;
	}

	public static void setCompetingTrainedModelRecipe(Recipe highlight){
		competingModel = highlight;
		Workbench.update();
	}
}
