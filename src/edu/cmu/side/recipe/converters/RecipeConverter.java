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

		Recipe recipe = (Recipe) obj;
		writer.addAttribute("name", recipe.getRecipeName());

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

		Recipe r = Recipe.fetchRecipe();
		String name = reader.getAttribute("name");
		r.setRecipeName(name);
		reader.moveDown();
//		OrderedPluginMap map = (OrderedPluginMap) context.convertAnother(reader.getValue(), OrderedPluginMap.class);
		System.out.println(reader.getNodeName());
		System.out.println(reader.getAttributeCount());
		//		r.setExtractors((OrderedPluginMap) context.convertAnother(reader.getValue(), OrderedPluginMap.class));
		reader.moveUp();
		//		reader.moveDown();
		//		r.setFilters((OrderedPluginMap) context.convertAnother(reader.getValue(),OrderedPluginMap.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setWrappers((OrderedPluginMap) context.convertAnother(reader.getValue(),OrderedPluginMap.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setLearner((LearningPlugin) context.convertAnother(reader.getValue(), LearningPlugin.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setLearnerSettings((Map<String,String>) context.convertAnother(reader.getValue(), Map.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setValidationSettings((Map<String,Serializable>) context.convertAnother(reader.getValue(), Map.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setDocumentList((DocumentList) context.convertAnother(reader.getValue(),DocumentList.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setFeatureTable((FeatureTable) context.convertAnother(reader.getValue(),FeatureTable.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setFilteredTable((FeatureTable) context.convertAnother(reader.getValue(),FeatureTable.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setTrainingResult((TrainingResult) context.convertAnother(reader.getValue(),TrainingResult.class));
		//		reader.moveUp();
		//		reader.moveDown();
		//		r.setPredictionResult((PredictionResult) context.convertAnother(reader.getValue(), PredictionResult.class));
		//		reader.moveUp();
		return r;
	}

}
