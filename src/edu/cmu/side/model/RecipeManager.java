package edu.cmu.side.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.data.DocumentList;

public class RecipeManager {
//
//	public static final String DOCUMENT_LIST_RECIPES = "document_lists";
//	public static final String FEATURE_TABLE_RECIPES = "feature_tables";
//	public static final String MODIFIED_TABLE_RECIPES = "modified_tables";
//	public static final String TRAINED_MODEL_RECIPES = "trained_models";
//	public static final String PREDICTION_RESULT_RECIPES = "prediction_results";
	
	public enum Stage 
	{
		NONE("blank.side"),
		DOCUMENT_LIST("docs.side"), 
		FEATURE_TABLE("table.side"), 
		MODIFIED_TABLE("struct.side"), 
		TRAINED_MODEL("model.side"), 
		PREDICTION_RESULT("pred.side");
		
		public final String extension;
		
		private Stage(String ext)
		{
			this.extension = ext;
		}
	}

	static List<Recipe> recipes = new ArrayList<Recipe>();

	public Collection<Recipe> getRecipeCollectionByType(Stage type){
		List<Recipe> filtered = new ArrayList<Recipe>();
		for(Recipe recipe : recipes){
			Stage stage = recipe.getStage();
			if(stage.equals(type)){
				filtered.add(recipe);
			}
		}
		return filtered;
	}
	
	public static boolean containsRecipe(Recipe rec){
		return recipes.contains(rec);
	}
	
	public static void deleteRecipe(Recipe rec){
		recipes.remove(rec);
		Workbench.update();
	}
	
	public static Recipe fetchDocumentListRecipe(DocumentList documents){
		Recipe recipe = Recipe.fetchRecipe();
		recipe.setDocumentList(documents);
		recipes.add(recipe);
		return recipe;
	}
	
	public static void addRecipe(Recipe rec){
		recipes.add(rec);
		Workbench.update();
	}
	
	public static void removeRecipe(Recipe rec){
		recipes.remove(rec);
		Workbench.update();
	}
	
	public RecipeManager(){

	}
}
