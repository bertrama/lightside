package edu.cmu.side.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.data.DocumentList;

public class RecipeManager{
	
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
	
	public boolean containsRecipe(Recipe rec){
		return recipes.contains(rec);
	}
	
	public void deleteRecipe(Recipe rec){
		recipes.remove(rec);
		Workbench.update(this);
	}
	
	public void addRecipe(Recipe rec){
		recipes.add(rec);
		Workbench.update(this);
	}
	
	
	public Recipe fetchDocumentListRecipe(DocumentList documents){
		Recipe recipe = Recipe.fetchRecipe();
		recipe.setDocumentList(documents);
		recipes.add(recipe);
		return recipe;
	}
	
	public RecipeManager(){

	}
}
