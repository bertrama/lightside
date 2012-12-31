package edu.cmu.side.control;

import java.util.HashMap;
import java.util.Map;

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
	
	private static Map<EvaluateTwoModelPlugin, Boolean> modelComparisonPlugins;
	
	private static StatusUpdater update = new SwingUpdaterLabel();
	private static EvalCheckboxListener eval;


	static{
		modelComparisonPlugins = new HashMap<EvaluateTwoModelPlugin, Boolean>();
		SIDEPlugin[] modelComparisons = PluginManager.getSIDEPluginArrayByType("model_comparison");
		for(SIDEPlugin fe : modelComparisons){
			modelComparisonPlugins.put((EvaluateTwoModelPlugin)fe, false);
		}

	}

	public static Map<EvaluateTwoModelPlugin, Boolean> getModelComparisonPlugins(){
		return modelComparisonPlugins;
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
	
	private static double pairedTTest(){
		return 0.01;
	}
	
	private static double unpairedTTest(){
		return 1;
	}
}
