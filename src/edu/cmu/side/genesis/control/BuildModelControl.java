package edu.cmu.side.genesis.control;

import java.util.*;

import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.LearningPlugin;

public class BuildModelControl extends GenesisControl{

	List<GenesisRecipe> modelRecipes = new ArrayList<GenesisRecipe>();
	
	LearningPlugin plugin;
	
	public void train(GenesisRecipe recipe, GenesisUpdater update){
		assert recipe.getModifiedTable() != null;
		List<LearningPlugin> list = new ArrayList<LearningPlugin>();
		list.add(plugin);
		GenesisRecipe newRecipe = GenesisRecipe.createAppendedRecipe(recipe, list);
		newRecipe.train(update);
		modelRecipes.add(newRecipe);
	}
	
	public List<GenesisRecipe> getFilterRecipes(){
		return modelRecipes;
	}
	
}
