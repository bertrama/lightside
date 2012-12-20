package edu.cmu.side.genesis.control;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.GenesisControl.EvalCheckboxListener;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.ModelAnalysisPlugin;
import edu.cmu.side.simple.ModelComparisonPlugin;
import edu.cmu.side.simple.ModelEvaluationPlugin;

public class ExploreResultsControl extends GenesisControl{

	private static GenesisRecipe highlightedTrainedModel;
	
	private static Map<ModelAnalysisPlugin, Boolean> modelAnalysisPlugins;
	private static GenesisUpdater update = new SwingUpdaterLabel();
	private static EvalCheckboxListener eval;

	static{
		modelAnalysisPlugins = new HashMap<ModelAnalysisPlugin, Boolean>();
		SIDEPlugin[] modelEvaluations = PluginManager.getSIDEPluginArrayByType("model_analysis");
		for(SIDEPlugin fe : modelEvaluations){
			modelAnalysisPlugins.put((ModelAnalysisPlugin)fe, false);
		}
	}
	
	public static Map<ModelAnalysisPlugin, Boolean> getModelAnalysisPlugins(){
		return modelAnalysisPlugins;
	}
	
	public static boolean hasHighlightedTrainedModelRecipe(){
		return highlightedTrainedModel!=null;
	}

	public static GenesisRecipe getHighlightedTrainedModelRecipe(){
		return highlightedTrainedModel;
	}

	public static void setHighlightedTrainedModelRecipe(GenesisRecipe highlight){
		highlightedTrainedModel = highlight;
		GenesisWorkbench.update();
	}
}
