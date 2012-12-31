package edu.cmu.side.control;

import java.util.ArrayList;
import java.util.Collection;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class PredictLabelsControl extends GenesisControl{

	private static Recipe trainedModel;
	private static Recipe highlightedUnlabeledData;
	
	private static Collection<Recipe> unlabeledDataRecipes = new ArrayList<Recipe>();
	
	private static StatusUpdater update = new SwingUpdaterLabel();

	public static boolean hasHighlightedTrainedModelRecipe(){
		return trainedModel!=null;
	}

	public static Recipe getHighlightedTrainedModelRecipe(){
		return trainedModel;
	}

	public static void setHighlightedTrainedModelRecipe(Recipe highlight){
		trainedModel = highlight;
		Workbench.update();
	}
	
	public static boolean hasHighlightedUnlabeledData(){
		return highlightedUnlabeledData != null;
	}
	
	public static Recipe getHighlightedUnlabeledData(){
		return highlightedUnlabeledData;
	}
	
	public static void setHighlightedUnlabeledData(Recipe r){
		highlightedUnlabeledData = r;
		Workbench.update();
	}
	
	public static Collection<Recipe> getUnlabeledDataRecipes(){
		return unlabeledDataRecipes;
	}
	
}
