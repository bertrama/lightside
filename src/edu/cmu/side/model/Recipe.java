package edu.cmu.side.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.FilterPlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.plugin.SIDEPlugin;

public class Recipe implements Serializable
{

	RecipeManager.Stage stage = null;
	private String recipeName = "";
	OrderedPluginMap extractors;
	OrderedPluginMap filters;
	LearningPlugin learner;
	Map<String, String> learnerSettings;
	
	DocumentList documentList;
	FeatureTable featureTable;
	FeatureTable filteredTable;
	TrainingResult trainedModel;
	PredictionResult predictionResult;
	
	public RecipeManager.Stage getStage(){
		if(stage == null){
			if(predictionResult != null){
				stage = RecipeManager.Stage.PREDICTION_RESULT;
			}else if(trainedModel != null){
				stage = RecipeManager.Stage.TRAINED_MODEL;
			}else if(filteredTable != null){
				stage = RecipeManager.Stage.MODIFIED_TABLE;
			}else if(featureTable != null){
				stage = RecipeManager.Stage.FEATURE_TABLE;
			}else if(documentList != null){
				stage = RecipeManager.Stage.DOCUMENT_LIST;
			}else stage = RecipeManager.Stage.NONE;
			
		}
		return stage;
	}

	public void resetStage(){
		stage = null;
	}

	public String toString(){
		String out = "";
		if(RecipeManager.Stage.DOCUMENT_LIST.equals(stage)){
			out = documentList.getName();
		}else if(RecipeManager.Stage.FEATURE_TABLE.equals(stage)){
			out = featureTable.getName();
		}else if(RecipeManager.Stage.MODIFIED_TABLE.equals(stage)){
			out = filteredTable.getName();
		}else if(RecipeManager.Stage.TRAINED_MODEL.equals(stage)){
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

	private Recipe()
	{
		extractors = new OrderedPluginMap();
		filters= new OrderedPluginMap();
		getStage();
	}

	public static Recipe fetchRecipe(){
		return new Recipe();
	}

	public static Recipe addPluginsToRecipe(Recipe prior, Collection<? extends SIDEPlugin> next){
		RecipeManager.Stage stage = prior.getStage();
		Recipe newRecipe = fetchRecipe();
		if(stage.equals(RecipeManager.Stage.DOCUMENT_LIST)){
			addFeaturePlugins(prior, newRecipe, (Collection<FeaturePlugin>)next);
		}else if(stage.equals(RecipeManager.Stage.FEATURE_TABLE)){
			addFilterPlugins(prior, newRecipe, (Collection<FilterPlugin>)next);
		}
		return newRecipe;
	}
	
	public static Recipe addLearnerToRecipe(Recipe prior, LearningPlugin next, Map<String, String> settings){
		RecipeManager.Stage stage = prior.getStage();
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

	public String getRecipeName()
	{
		if(recipeName.isEmpty())
		{
			String out = "";
			if(RecipeManager.Stage.DOCUMENT_LIST.equals(stage)){
				out = documentList.getName();
			}else if(RecipeManager.Stage.FEATURE_TABLE.equals(stage)){
				out = featureTable.getName();
			}else if(RecipeManager.Stage.MODIFIED_TABLE.equals(stage)){
				out = filteredTable.getName();
			}else if(RecipeManager.Stage.TRAINED_MODEL.equals(stage)){
				out = trainedModel.getName();
			}else{
				out = "Default: " + stage;
			}
			return out+"."+stage.extension;
		}
		return recipeName;
	}
	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		System.out.println("reading "+this + " from "+in);
		stage = (RecipeManager.Stage) in.readObject();
		recipeName = (String) in.readObject();
		extractors = (OrderedPluginMap) in.readObject();
		filters = (OrderedPluginMap) in.readObject();
		learner = (LearningPlugin) SIDEPlugin.fromSerializable((Serializable) in.readObject()); //it's all for you!
		learnerSettings = (Map<String, String>) in.readObject();
		documentList = (DocumentList) in.readObject();
		featureTable = (FeatureTable) in.readObject();
		filteredTable = (FeatureTable) in.readObject();
		trainedModel = (TrainingResult)in.readObject();
		predictionResult = (PredictionResult) in.readObject();
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		System.out.println("writing "+this + " to "+out);
		out.writeObject(stage);
		out.writeObject(recipeName);
		out.writeObject(extractors);
		out.writeObject(filters);
		out.writeObject(learner==null?null:learner.toSerializable());
		out.writeObject(learnerSettings);
		out.writeObject(documentList);
		out.writeObject(featureTable);
		out.writeObject(filteredTable);
		out.writeObject(trainedModel);
		out.writeObject(predictionResult);
	}


}