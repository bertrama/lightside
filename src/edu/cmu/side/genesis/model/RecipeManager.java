package edu.cmu.side.genesis.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimplePredictionResult;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;

public class RecipeManager {

	public static final String DOCUMENT_LIST_RECIPES = "document_lists";
	public static final String FEATURE_TABLE_RECIPES = "feature_tables";
	public static final String MODIFIED_TABLE_RECIPES = "modified_tables";
	public static final String TRAINED_MODEL_RECIPES = "trained_models";
	public static final String PREDICTION_RESULT_RECIPES = "prediction_results";
	
	static List<GenesisRecipe> recipes = new ArrayList<GenesisRecipe>();

	public Collection<GenesisRecipe> getRecipeCollectionByType(String type){
		List<GenesisRecipe> filtered = new ArrayList<GenesisRecipe>();
		for(GenesisRecipe recipe : recipes){
			String stage = recipe.getStage();
			if(stage.equals(type)){
				filtered.add(recipe);
			}
		}
		System.out.println(filtered.size() + " recipes of type " + type + " RM33");
		return filtered;
	}
	
	public static GenesisRecipe fetchDocumentListRecipe(SimpleDocumentList documents){
		GenesisRecipe recipe = GenesisRecipe.fetchRecipe();
		recipe.setDocumentList(documents);
		recipes.add(recipe);
		return recipe;
	}
	
	public static void removeRecipe(GenesisRecipe rec){
		recipes.remove(rec);
		GenesisWorkbench.update();
	}
	
	public RecipeManager(){

	}
}
