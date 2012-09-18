package edu.cmu.side.genesis.control;

import java.util.*;

import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.SimpleDocumentList;

public class ExtractFeaturesControl extends GenesisControl{

	List<GenesisRecipe> documentRecipes = new ArrayList<GenesisRecipe>();
	List<GenesisRecipe> extractRecipes = new ArrayList<GenesisRecipe>();
	
	List<FeaturePlugin> plugins;
	
	public void extract(GenesisRecipe recipe, GenesisUpdater update){
		assert recipe.getDocumentList() != null;
		GenesisRecipe newRecipe = GenesisRecipe.createAppendedRecipe(recipe, plugins);
		newRecipe.extract(update);
		extractRecipes.add(newRecipe);
	}
	
	public List<GenesisRecipe> getDocumentRecipes(){
		return documentRecipes;
	}
	
	public List<GenesisRecipe> getExtractRecipes(){
		return extractRecipes;
	}
	
	public void addDocumentRecipe(SimpleDocumentList documents){
		assert documents != null;
		GenesisRecipe recipe = new GenesisRecipe(documents);
		documentRecipes.add(recipe);
	}

}
