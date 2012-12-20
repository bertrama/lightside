package edu.cmu.side.genesis.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import edu.cmu.side.genesis.control.GenesisControl.EvalCheckboxListener;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.ModelComparisonPlugin;
import edu.cmu.side.simple.ModelEvaluationPlugin;

public class CompareModelsControl extends GenesisControl{

	private static GenesisRecipe highlightedModelA;
	private static GenesisRecipe highlightedModelB;
	
	private static Map<ModelComparisonPlugin, Boolean> modelComparisonPlugins;
	
	private static GenesisUpdater update = new SwingUpdaterLabel();
	private static EvalCheckboxListener eval;


	static{
		modelComparisonPlugins = new HashMap<ModelComparisonPlugin, Boolean>();
		SIDEPlugin[] modelComparisons = PluginManager.getSIDEPluginArrayByType("model_comparison");
		for(SIDEPlugin fe : modelComparisons){
			modelComparisonPlugins.put((ModelComparisonPlugin)fe, false);
		}

	}

	public static Map<ModelComparisonPlugin, Boolean> getModelComparisonPlugins(){
		return modelComparisonPlugins;
	}

	public static void setUpdater(GenesisUpdater up){
		update = up;
	}

	public static EvalCheckboxListener getEvalCheckboxListener(){
		return eval;
	}
	
	private static double pairedTTest(){
		return 0.01;
	}
	
	private static double unpairedTTest(){
		return 1;
	}
}
