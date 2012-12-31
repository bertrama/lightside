package edu.cmu.side.control;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.plugin.EvaluateOneModelPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class ExploreResultsControl extends GenesisControl{

	private static Recipe highlightedTrainedModel;
	
	private static Map<EvaluateOneModelPlugin, Boolean> modelAnalysisPlugins;
	private static StatusUpdater update = new SwingUpdaterLabel();
	private static EvalCheckboxListener eval;

	static{
		modelAnalysisPlugins = new HashMap<EvaluateOneModelPlugin, Boolean>();
		SIDEPlugin[] modelEvaluations = PluginManager.getSIDEPluginArrayByType("model_analysis");
		for(SIDEPlugin fe : modelEvaluations){
			modelAnalysisPlugins.put((EvaluateOneModelPlugin)fe, false);
		}
	}
	
	public static Map<EvaluateOneModelPlugin, Boolean> getModelAnalysisPlugins(){
		return modelAnalysisPlugins;
	}
	
	public static boolean hasHighlightedTrainedModelRecipe(){
		return highlightedTrainedModel!=null;
	}

	public static Recipe getHighlightedTrainedModelRecipe(){
		return highlightedTrainedModel;
	}

	public static void setHighlightedTrainedModelRecipe(Recipe highlight){
		highlightedTrainedModel = highlight;
		Workbench.update();
	}
}
