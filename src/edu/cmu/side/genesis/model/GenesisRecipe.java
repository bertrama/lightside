package edu.cmu.side.genesis.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import javax.swing.JLabel;

import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimplePredictionResult;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.features.FeaturePluginPanel;
import edu.cmu.side.genesis.control.GenesisUpdater;

public class GenesisRecipe {
	
		String stage = null;
		String recipeName = null;
		Map<String, Map<String, String>> extractors;
		Map<String, Map<String, String>> filters;
		Map<String, Map<String, String>> learners;

		SimpleDocumentList documentList;
		FeatureTable featureTable;
		FeatureTable filteredTable;
		SimpleTrainingResult trainedModel;
		SimplePredictionResult predictionResult;
		
		String recipeID;

		public String getStage(){
			if(stage == null){
				if(predictionResult != null){
					stage = RecipeManager.PREDICTION_RESULT_RECIPES;
				}else if(trainedModel != null){
					stage = RecipeManager.TRAINED_MODEL_RECIPES;
				}else if(filteredTable != null){
					stage = RecipeManager.MODIFIED_TABLE_RECIPES;
				}else if(featureTable != null){
					stage = RecipeManager.FEATURE_TABLE_RECIPES;
				}else if(documentList != null){
					stage = RecipeManager.DOCUMENT_LIST_RECIPES;
				}else stage = "";
			}
			return stage;
		}
		
		public String toString(){
			getStage();
			String out = "";
			if(RecipeManager.DOCUMENT_LIST_RECIPES.equals(stage)){
				String name = "Docs ";
				for(String s : documentList.getFilenames()){
					name += s + ", ";
				}
				name = name.substring(0, name.length()-2);
				out = name;
			}
			return out;
		}
		
		public SimpleDocumentList getDocumentList(){
			return documentList;
		}
		
		public FeatureTable getFeatureTable(){
			return featureTable;
		}
		
		public void setDocumentList(SimpleDocumentList sdl){
			documentList = sdl;
		}
		
		public void setFeatureTable(FeatureTable ft){
			featureTable = ft;
		}
		
		private GenesisRecipe(){
			recipeName = "Blank Recipe";
		}
		
		public static GenesisRecipe fetchRecipe(){
			return new GenesisRecipe();
		}
		
}