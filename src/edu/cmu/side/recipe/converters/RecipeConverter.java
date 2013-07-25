package edu.cmu.side.recipe.converters;

import java.io.Serializable;
import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.plugin.LearningPlugin;

public class RecipeConverter implements Converter{

	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(Recipe.class);
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext context) {
/*
 * RecipeManager.Stage stage = null;
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
 */
		Recipe recipe = (Recipe) obj;
		writer.addAttribute("Recipe Name", recipe.getRecipeName());
		writer.startNode("Extractors");
		
		context.convertAnother(recipe.getExtractors()!=null?recipe.getExtractors():"");
		writer.endNode();
		
		writer.startNode("Filters");
		context.convertAnother(recipe.getFilters()!=null?recipe.getFilters():"");
		writer.endNode();
		
		writer.startNode("Wrappers");
		context.convertAnother(recipe.getWrappers());
		writer.endNode();
		
		writer.startNode("Learner");
		context.convertAnother(recipe.getLearner()!=null?recipe.getLearner():"");
		writer.endNode();
		
		writer.startNode("Learner Settings");
		context.convertAnother(recipe.getLearnerSettings()!=null?recipe.getLearnerSettings():"");
		writer.endNode();
		
		writer.startNode("Validation Settings");
		context.convertAnother(recipe.getValidationSettings()!=null?recipe.getValidationSettings():"");
		writer.endNode();
//		
//		DocumentList documentList;
//		FeatureTable featureTable;
//		FeatureTable filteredTable;
//		TrainingResult trainedModel;
//		PredictionResult predictionResult;
		writer.startNode("Document List");
		context.convertAnother(recipe.getDocumentList()!=null?recipe.getDocumentList():"");
		writer.endNode();
		
		writer.startNode("Feature Table");
		context.convertAnother(recipe.getFeatureTable()!=null?recipe.getFeatureTable():"");
		writer.endNode();
	
		writer.startNode("Filtered Table");
		context.convertAnother(recipe.getFilteredTable()!=null?recipe.getFilteredTable():"");
		writer.endNode();
		
		writer.startNode("Trained Model");
		context.convertAnother(recipe.getTrainingResult()!=null?recipe.getTrainingResult():"");
		writer.endNode();
		
		writer.startNode("Prediction Results");
		context.convertAnother(recipe.getPredictionResult()!=null?recipe.getPredictionResult():"");
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
