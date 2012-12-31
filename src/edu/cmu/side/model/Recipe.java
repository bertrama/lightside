package edu.cmu.side.model;

import java.util.Collection;
import java.util.Map;

import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.FilterPlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.plugin.SIDEPlugin;

public class Recipe {

	String stage = null;
	String recipeName = null;
	OrderedPluginMap extractors;
	OrderedPluginMap filters;
	LearningPlugin learner;
	Map<String, String> learnerSettings;
	
	DocumentList documentList;
	FeatureTable featureTable;
	FeatureTable filteredTable;
	TrainingResult trainedModel;
	PredictionResult predictionResult;

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

	public DocumentList getDocumentList(){ return documentList; }

	public FeatureTable getFeatureTable(){ return featureTable; }
	
	public FeatureTable getFilteredTable(){ return filteredTable; }
	
	public FeatureTable getTrainingTable(){
		if(filteredTable == null) return getFeatureTable(); else return getFilteredTable();
	}
	
	public TrainingResult getTrainingResult(){ return trainedModel; }
	
	public PredictionResult getPredictionResult(){ return predictionResult; }

	public OrderedPluginMap getExtractors(){ return extractors; }
	
	public OrderedPluginMap getFilters(){ return filters; }
	
	public LearningPlugin getLearner(){ return learner; }

	public void setDocumentList(DocumentList sdl){
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
	
	public void setTrainingResult(TrainingResult tm){
		trainedModel = tm;
		resetStage();
	}
	
	public void setPredictionResult(PredictionResult pr){
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

	private Recipe(){
		recipeName = "Blank Recipe";
		extractors = new OrderedPluginMap();
		filters= new OrderedPluginMap();
		getStage();
	}

	public static Recipe fetchRecipe(){
		return new Recipe();
	}

	public static Recipe addPluginsToRecipe(Recipe prior, Collection<? extends SIDEPlugin> next){
		String stage = prior.getStage();
		Recipe newRecipe = fetchRecipe();
		if(stage.equals(RecipeManager.DOCUMENT_LIST_RECIPES)){
			addFeaturePlugins(prior, newRecipe, (Collection<FeaturePlugin>)next);
		}else if(stage.equals(RecipeManager.FEATURE_TABLE_RECIPES)){
			addFilterPlugins(prior, newRecipe, (Collection<FilterPlugin>)next);
		}
		return newRecipe;
	}
	
	public static Recipe addLearnerToRecipe(Recipe prior, LearningPlugin next, Map<String, String> settings){
		String stage = prior.getStage();
		Recipe newRecipe = fetchRecipe();
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
	
	protected static void addFeaturePlugins(Recipe prior, Recipe newRecipe, Collection<FeaturePlugin> next){
		newRecipe.setDocumentList(prior.getDocumentList());
		for(FeaturePlugin plugin : next){
			assert next instanceof FeaturePlugin;
			newRecipe.addExtractor(plugin);
		}
	}
	
	protected static void addFilterPlugins(Recipe prior, Recipe newRecipe, Collection<FilterPlugin> next){
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