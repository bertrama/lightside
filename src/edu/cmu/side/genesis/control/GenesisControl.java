package edu.cmu.side.genesis.control;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Map;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.TableEvaluationPlugin;

public abstract class GenesisControl {


	public static class EvalCheckboxListener implements ItemListener{

		Map<? extends SIDEPlugin, Map<String, Boolean>>  plugins;
		public EvalCheckboxListener(Map<? extends SIDEPlugin, Map<String, Boolean>> p){
			plugins = p;
		}
		@Override
		public void itemStateChanged(ItemEvent ie) {
			String eval = ((CheckBoxListEntry)ie.getSource()).getValue().toString();
			for(SIDEPlugin plug : plugins.keySet()){
				if(plugins.get(plug).containsKey(eval)){
					boolean flip = !plugins.get(plug).get(eval);
					plugins.get(plug).put(eval, flip);
				}
			}
			GenesisWorkbench.update();
		}
	}
	
	public static class PluginCheckboxListener<E extends SIDEPlugin> implements ItemListener{

		Map<E, Boolean> plugins;
		public PluginCheckboxListener(Map<E, Boolean> p){
			plugins = p;
		}
		
		@Override
		public void itemStateChanged(ItemEvent ie) {
			E plug = (E)((CheckBoxListEntry)ie.getSource()).getValue();
			plugins.put(plug, !plugins.get(plug));
			GenesisWorkbench.update();
		}
	}
	
	public static Collection<GenesisRecipe> getDocumentLists(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.DOCUMENT_LIST_RECIPES);
	}
	
	public static Collection<GenesisRecipe> getFeatureTables(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.FEATURE_TABLE_RECIPES);
	}
	
	public static Collection<GenesisRecipe> getFilterTables(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.MODIFIED_TABLE_RECIPES);
	}
	
	public static Collection<GenesisRecipe> getTrainedModels(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.TRAINED_MODEL_RECIPES);
	}

	public static int numFeatureTables(){
		return getFeatureTables().size();
	}
	
	public static int numFilterTables(){
		return getFilterTables().size();
	}

	public static int numDocumentLists(){
		return getDocumentLists().size();
	}

	public static int numTrainedModels(){
		return getTrainedModels().size();
	}

}

