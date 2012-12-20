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
	OrderedPluginMap extractors;
	OrderedPluginMap filters;
	LearningPlugin learner;
	Map<String, String> learnerSettings;
	
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

	public void resetStage(){
		stage = null;
	}

	public String toString(){
		String out = "";
		if(RecipeManager.DOCUMENT_LIST_RECIPES.equals(stage)){
			out = documentList.getName();
		}else if(RecipeManager.FEATURE_TABLE_RECIPES.equals(stage)){
			out = featureTable.getName();
		}else if(RecipeManager.MODIFIED_TABLE_RECIPES.equals(stage)){
			out = filteredTable.getName();
		}else if(RecipeManager.TRAINED_MODEL_RECIPES.equals(stage)){
			out = trainedModel.getName();
		}else{
			out = "Default: " + stage;
		}
		return out;
	}

	public SimpleDocumentList getDocumentList(){ return documentList; }

	public FeatureTable getFeatureTable(){ return featureTable; }
	
	public FeatureTable getFilteredTable(){ return filteredTable; }
	
	public FeatureTable getTrainingTable(){
		if(filteredTable == null) return getFeatureTable(); else return getFilteredTable();
	}
	
	public SimpleTrainingResult getTrainingResult(){ return trainedModel; }
	
	public SimplePredictionResult getPredictionResult(){ return predictionResult; }

	public OrderedPluginMap getExtractors(){ return extractors; }
	
	public OrderedPluginMap getFilters(){ return filters; }
	
	public LearningPlugin getLearner(){ return learner; }

	public void setDocumentList(SimpleDocumentList sdl){
		documentList = sdl;
		resetStage();
	}

	public void setFeatureTable(FeatureTable ft){
		featureTable = ft;
		resetStage();
	}

	public void setFilteredTable(FeatureTable ft){
		filteredTable = ft;
		resetStage();
	}
	
	public void setTrainingResult(SimpleTrainingResult tm){
		trainedModel = tm;
		resetStage();
	}
	
	public void setPredictionResult(SimplePredictionResult pr){
		predictionResult = pr;
		resetStage();
	}
	
	public void addExtractor(FeaturePlugin plug){
		extractors.put(plug, plug.generateConfigurationSettings());
		resetStage();
	}
	
	public void addFilter(FilterPlugin plug){
		filters.put(plug, plug.generateConfigurationSettings());
		resetStage();
	}
	
	public void setLearner(LearningPlugin plug){
		learner = plug;
		resetStage();
	}
	
	public void setLearnerSettings(Map<String, String> settings){
		learnerSettings = settings;
	}
	
	public Map<String, String> getLearnerSettings(){
		return learnerSettings;
	}

	private GenesisRecipe(){
		recipeName = "Blank Recipe";
		extractors = new OrderedPluginMap();
		filters= new OrderedPluginMap();
		getStage();
	}

	public static GenesisRecipe fetchRecipe(){
		return new GenesisRecipe();
	}

	public static GenesisRecipe addPluginsToRecipe(GenesisRecipe prior, Collection<? extends SIDEPlugin> next){
		String stage = prior.getStage();
		GenesisRecipe newRecipe = fetchRecipe();
		if(stage.equals(RecipeManager.DOCUMENT_LIST_RECIPES)){
			addFeaturePlugins(prior, newRecipe, (Collection<FeaturePlugin>)next);
		}else if(stage.equals(RecipeManager.FEATURE_TABLE_RECIPES)){
			addFilterPlugins(prior, newRecipe, (Collection<FilterPlugin>)next);
		}
		return newRecipe;
	}
	
	public static GenesisRecipe addLearnerToRecipe(GenesisRecipe prior, LearningPlugin next, Map<String, String> settings){
		String stage = prior.getStage();
		GenesisRecipe newRecipe = fetchRecipe();
		newRecipe.setDocumentList(prior.getDocumentList());
		for(SIDEPlugin plugin : prior.getExtractors().keySet()){
			newRecipe.addExtractor((FeaturePlugin)plugin);
		}
		newRecipe.setFeatureTable(prior.getFeatureTable());
		for(SIDEPlugin plugin : prior.getFilters().keySet()){
			newRecipe.addFilter((FilterPlugin)plugin);
		}
		newRecipe.setFilteredTable(prior.getFilteredTable());
		newRecipe.setLearner(next);
		newRecipe.setLearnerSettings(settings);
		return newRecipe;
	}
	
	protected static void addFeaturePlugins(GenesisRecipe prior, GenesisRecipe newRecipe, Collection<FeaturePlugin> next){
		newRecipe.setDocumentList(prior.getDocumentList());
		for(FeaturePlugin plugin : next){
			assert next instanceof FeaturePlugin;
			newRecipe.addExtractor(plugin);
		}
	}
	
	protected static void addFilterPlugins(GenesisRecipe prior, GenesisRecipe newRecipe, Collection<FilterPlugin> next){
		newRecipe.setDocumentList(prior.getDocumentList());
		for(SIDEPlugin fp : prior.getExtractors().keySet()){
			newRecipe.addExtractor((FeaturePlugin)fp);
		}
		newRecipe.setFeatureTable(prior.getFeatureTable());
		for(FilterPlugin plugin : next){
			assert next instanceof FilterPlugin;
			newRecipe.addFilter(plugin);
		}
	}
	
}