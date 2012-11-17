package edu.cmu.side.genesis.control;

import java.util.*;

import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.SimpleDocumentList;

public class ModifyFeaturesControl extends GenesisControl{

	List<GenesisRecipe> filterRecipes = new ArrayList<GenesisRecipe>();
	
	List<FilterPlugin> plugins;
	
	public void filter(GenesisRecipe recipe, GenesisUpdater update){
		assert recipe.getOriginalTable() != null;
		GenesisRecipe newRecipe = GenesisRecipe.createAppendedRecipe(recipe, plugins);
		newRecipe.modify(update);
		filterRecipes.add(newRecipe);
	}
	
	public List<GenesisRecipe> getFilterRecipes(){
		return filterRecipes;
	}
	
}
