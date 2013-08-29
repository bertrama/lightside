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
import java.util.Set;

import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.Feature.Type;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.plugin.RestructurePlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.WrapperPlugin;

/**
 * A recipe stores the set of options that were chosen to get from an input file to whatever stage of machine learning you're currently at.
 * All subsequent fields will remain blank; for instance, if this recipe describes a feature table then the learner will be null.
 * 
 * Within the researcher UI, everything that points to a data structure points to a recipe containing all of the
 * steps that led to the creation of that feature table (in that case, a documentList, featureTable, and pluginmap of extractors.
 * 
 * @author emayfiel
 *
 */
public class Recipe implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	
	RecipeManager.Stage stage = null;
	private String recipeName = "";
	OrderedPluginMap extractors;
	OrderedPluginMap filters;
	OrderedPluginMap wrappers;
	LearningPlugin learner;
	Map<String, String> learnerSettings;
	Map<String, Serializable> validationSettings;

	DocumentList documentList;
	FeatureTable featureTable;
	FeatureTable filteredTable;
	TrainingResult trainedModel;
	PredictionResult predictionResult;
	
	public RecipeManager.Stage getStage()
	{
		if (stage == null)
		{
			if (predictionResult != null)
			{
				stage = RecipeManager.Stage.PREDICTION_RESULT;
			}
			else if (trainedModel != null)
			{
				stage = RecipeManager.Stage.TRAINED_MODEL;
			}
			else if (learnerSettings != null && learnerSettings.containsKey("classifier"))
			{
				stage = RecipeManager.Stage.PREDICTION_ONLY;
			}
			else if (filteredTable != null)
			{
				stage = RecipeManager.Stage.MODIFIED_TABLE;
			}
			else if (featureTable != null)
			{
				stage = RecipeManager.Stage.FEATURE_TABLE;
			}
			else if (documentList != null)
			{
				stage = RecipeManager.Stage.DOCUMENT_LIST;
			}
			else
				stage = RecipeManager.Stage.NONE;

		}
		return stage;
	}

	public void resetStage(){
		stage = null;
		stage = getStage();
	}

	@Override
	public String toString()
	{
		String out = "";
		if (RecipeManager.Stage.DOCUMENT_LIST.equals(stage))
		{
			out = documentList.getName();
		}
		else if (RecipeManager.Stage.FEATURE_TABLE.equals(stage))
		{
			out = featureTable.getName();
		}
		else if (RecipeManager.Stage.MODIFIED_TABLE.equals(stage))
		{
			out = filteredTable.getName();
		}
		else if (RecipeManager.Stage.TRAINED_MODEL.equals(stage))
		{
			out = trainedModel.getName();
		}
		else
		{
			out = ""+stage;
		}
		if (out == null) out = stage.toString();
		return out;
	}

	//Filtered tables may alter the document list being worked with.
	public DocumentList getDocumentList(){ 
		return filteredTable == null ? documentList : filteredTable.getDocumentList();
	}

	public FeatureTable getFeatureTable(){ return featureTable; }
	
	public FeatureTable getFilteredTable(){ return filteredTable; }
	
	public FeatureTable getTrainingTable(){
		if(filteredTable == null) return getFeatureTable(); else return getFilteredTable();
	}
	
	public TrainingResult getTrainingResult(){ return trainedModel; }
	
	public PredictionResult getPredictionResult(){ return predictionResult; }

	public OrderedPluginMap getExtractors(){ return extractors; }
	
	public OrderedPluginMap getFilters(){ return filters; }
	
	public OrderedPluginMap getWrappers(){ return wrappers; }
	
	public LearningPlugin getLearner(){ return learner; }

	
	/**
	 * Every time we add a new element to the recipe, we reset the stage that the recipe is at to reflect the new reality.
	 * @param sdl
	 */
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
	
	public void addExtractor(FeaturePlugin plug, Map<String, String> settings){
		if(settings == null)
			settings = plug.generateConfigurationSettings();
		extractors.put(plug, settings);
		resetStage();
	}
	
	public void addFilter(RestructurePlugin plug, Map<String, String> settings){
		if(settings == null)
			settings = plug.generateConfigurationSettings();
		filters.put(plug, settings);
		resetStage();
	}
	
	public void addWrapper(WrapperPlugin plug, Map<String, String> settings){
		if(settings == null)
			settings = plug.generateConfigurationSettings();
		wrappers.put(plug, settings);
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
	
	public Map<String, Serializable> getValidationSettings()
	{
		return validationSettings;
	}

	public void setValidationSettings(Map<String, Serializable> validationSettings)
	{
		this.validationSettings = validationSettings;
	}

	protected Recipe()
	{
		extractors = new OrderedPluginMap();
		filters= new OrderedPluginMap();
		wrappers = new OrderedPluginMap();
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
		}else if(stage.equals(RecipeManager.Stage.FEATURE_TABLE) || stage.equals(RecipeManager.Stage.MODIFIED_TABLE)){
			addRestructurePlugins(prior, newRecipe, (Collection<RestructurePlugin>)next);
		}
		return newRecipe;
	}
	
	public static Recipe addLearnerToRecipe(Recipe prior, LearningPlugin next, Map<String, String> settings){
		RecipeManager.Stage stage = prior.getStage();
		Recipe newRecipe = fetchRecipe();
		newRecipe.setDocumentList(prior.getDocumentList());
		for(SIDEPlugin plugin : prior.getExtractors().keySet()){
			newRecipe.addExtractor((FeaturePlugin)plugin, prior.getExtractors().get(plugin));
		}
		newRecipe.setFeatureTable(prior.getFeatureTable());
		for(SIDEPlugin plugin : prior.getFilters().keySet()){
			newRecipe.addFilter((RestructurePlugin)plugin, prior.getFilters().get(plugin));
		}
		newRecipe.setFilteredTable(prior.getFilteredTable());
		for(SIDEPlugin plugin : prior.getWrappers().keySet()){
			newRecipe.addWrapper((WrapperPlugin)plugin, prior.getWrappers().get(plugin));
		}
		newRecipe.setLearner(next);
		newRecipe.setLearnerSettings(settings);
		return newRecipe;
	}
	
	public static Recipe copyPredictionRecipe(Recipe prior)
	{
		Recipe newRecipe = fetchRecipe();
		
		Map<String, List<String>> textColumns = new HashMap<String, List<String>>();
		Map<String, List<String>> columns = new HashMap<String, List<String>>();
		DocumentList originalDocs = prior.getDocumentList();
		
		List<String> emptyList = new ArrayList<String>(0);
		for(String key : originalDocs.getCoveredTextList().keySet())
		{
			textColumns.put(key, emptyList);
		}
		for(String key : originalDocs.allAnnotations().keySet())
		{
			columns.put(key, emptyList);
		}
		
		DocumentList newDocs = new DocumentList(emptyList, textColumns, columns, prior.getFeatureTable().getAnnotation());
		newDocs.setLabelArray(prior.getFeatureTable().getLabelArray());
		
		
		newRecipe.setDocumentList(newDocs);
		
		for (SIDEPlugin plugin : prior.getExtractors().keySet())
		{
			newRecipe.addExtractor((FeaturePlugin) plugin, prior.getExtractors().get(plugin));
		}
		
		FeatureTable dummyTable = prior.getTrainingTable().predictionClone();
		
		newRecipe.setFeatureTable(dummyTable);
		
		for (SIDEPlugin plugin : prior.getFilters().keySet())
		{
			newRecipe.addFilter((RestructurePlugin) plugin, prior.getFilters().get(plugin));
		}
		
		for (SIDEPlugin plugin : prior.getWrappers().keySet()){
			newRecipe.addWrapper((WrapperPlugin) plugin, prior.getWrappers().get(plugin));
		}
		newRecipe.setLearner(prior.getLearner());
		newRecipe.setLearnerSettings(prior.getLearnerSettings());
		newRecipe.setValidationSettings(prior.getValidationSettings());
		newRecipe.setRecipeName(prior.getRecipeName());

		return newRecipe;
	}
	
	public static Recipe copyEmptyRecipe(Recipe prior)
	{
		RecipeManager.Stage stage = prior.getStage();
		Recipe newRecipe = fetchRecipe();
//		newRecipe.setDocumentList(prior.getDocumentList());
		for (SIDEPlugin plugin : prior.getExtractors().keySet())
		{
			newRecipe.addExtractor((FeaturePlugin) plugin, prior.getExtractors().get(plugin));
		}
//		newRecipe.setFeatureTable(prior.getFeatureTable());
		for (SIDEPlugin plugin : prior.getFilters().keySet())
		{
			newRecipe.addFilter((RestructurePlugin) plugin, prior.getFilters().get(plugin));
		}
		
		for (SIDEPlugin plugin : prior.getWrappers().keySet()){
			newRecipe.addWrapper((WrapperPlugin) plugin, prior.getWrappers().get(plugin));
		}
//		newRecipe.setFilteredTable(prior.getFilteredTable());
		newRecipe.setLearner(prior.getLearner());
		newRecipe.setLearnerSettings(prior.getLearnerSettings());
		newRecipe.setValidationSettings(prior.getValidationSettings());

		return newRecipe;
	}
	
	protected static void addFeaturePlugins(Recipe prior, Recipe newRecipe, Collection<FeaturePlugin> next){
		newRecipe.setDocumentList(prior.getDocumentList());
		for(FeaturePlugin plugin : next){
			assert next instanceof FeaturePlugin;
			newRecipe.addExtractor(plugin, prior.getExtractors().get(plugin));
		}
	}
	
	protected static void addRestructurePlugins(Recipe prior, Recipe newRecipe, Collection<RestructurePlugin> next){
		newRecipe.setDocumentList(prior.getDocumentList());
		for (SIDEPlugin plugin : prior.getExtractors().keySet())
		{
			newRecipe.addExtractor((FeaturePlugin) plugin, prior.getExtractors().get(plugin));
		}
		newRecipe.setFeatureTable(prior.getTrainingTable());
		
		for (RestructurePlugin plugin : next)
		{
			assert next instanceof RestructurePlugin;
			newRecipe.addFilter(plugin, plugin.generateConfigurationSettings());
		}
	}
	
	public String getAnnotation(){
		FeatureTable table = getTrainingTable();
		return table==null?null:table.getAnnotation();
	}
	public Type getClassValueType(){
		FeatureTable table = getTrainingTable();
		return table==null?null:table.getClassValueType();		
	}
	
	public String[] getLabelArray(){
		FeatureTable table = getTrainingTable();
		return table==null?null:table.getLabelArray();
	}
	public Set<String> getTextColumns(){
		FeatureTable table = getTrainingTable();
		return table==null?null:table.getDocumentList().getTextColumns();
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
				out = ""+stage;
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
		//System.out.println("reading "+this + " from "+in);
		stage = (RecipeManager.Stage) in.readObject();
		recipeName = (String) in.readObject();
		extractors = (OrderedPluginMap) in.readObject();
		filters = (OrderedPluginMap) in.readObject();
		wrappers = (OrderedPluginMap) in.readObject();
		learner = (LearningPlugin) SIDEPlugin.fromSerializable((Serializable) in.readObject()); //it's all for you!
		learnerSettings = (Map<String, String>) in.readObject();
		documentList = (DocumentList) in.readObject();
		featureTable = (FeatureTable) in.readObject();
		filteredTable = (FeatureTable) in.readObject();
		trainedModel = (TrainingResult)in.readObject();
		predictionResult = (PredictionResult) in.readObject();
		validationSettings = (Map<String, Serializable>) in.readObject();
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		//System.out.println("writing "+this + " to "+out);
		out.writeObject(stage);
		out.writeObject(recipeName);
		out.writeObject(extractors);
		out.writeObject(filters);
		out.writeObject(wrappers);
		out.writeObject(learner==null?null:learner.toSerializable());
		out.writeObject(learnerSettings);
		out.writeObject(documentList);
		out.writeObject(featureTable);
		out.writeObject(filteredTable);
		out.writeObject(trainedModel);
		out.writeObject(predictionResult);
		out.writeObject(validationSettings);
	}

}