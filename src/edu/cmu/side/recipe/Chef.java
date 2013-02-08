package edu.cmu.side.recipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;

import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.Feature.Type;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.RestructurePlugin;
import edu.cmu.side.plugin.SIDEPlugin;

/**
 * loads a model trained using lightSIDE uses it to label new instances.
 * 
 * @author dadamson
 */
public class Chef
{
	static boolean quiet = false;

	static StatusUpdater textUpdater = new StatusUpdater()
	{
		
		@Override
		public void update(String updateSlot, int slot1, int slot2)
		{
			if(!quiet)
				System.err.println(updateSlot+": "+slot1 + "/"+slot2);
		}

		@Override
		public void update(String update)
		{
			if(!quiet)
				System.err.println(update);	
		}

		@Override
		public void reset()
		{
			// TODO Auto-generated method stub
			
		}
	};


	protected static void simmerFeatures(Recipe recipe, int threshold, String annotation, Type type)
	{		
		DocumentList corpus = recipe.getDocumentList();
		Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
		OrderedPluginMap extractors = recipe.getExtractors();
		
		for (SIDEPlugin plug : extractors.keySet())
		{
			System.out.println("Extractor Settings: "+extractors.get(plug));
			Collection<FeatureHit> extractorHits = ((FeaturePlugin) plug).extractFeatureHits(corpus, extractors.get(plug), textUpdater);
			hits.addAll(extractorHits);
		}
		FeatureTable ft = new FeatureTable(corpus, hits, threshold, annotation, type);
		recipe.setFeatureTable(ft);
		
		if(recipe.getStage().compareTo(Stage.MODIFIED_TABLE) >= 0) //recipe includes filtering
		{
			for (SIDEPlugin plug : recipe.getFilters().keySet())
			{
				ft = ((RestructurePlugin) plug).filterTestSet(recipe.getTrainingTable(), ft, recipe.getFilters().get(plug), textUpdater);
			}
			recipe.setFilteredTable(ft);
		}
	}
	
	//TODO: be more consistent in parameters to recipe stages
	public static Recipe followRecipe(Recipe originalRecipe, DocumentList corpus, Stage finalStage) throws Exception
	{
		Recipe newRecipe = Recipe.copyEmptyRecipe(originalRecipe);
		
		prepareDocumentList(originalRecipe, corpus);
		newRecipe.setDocumentList(corpus);
		
		if(finalStage == Stage.DOCUMENT_LIST)
			return newRecipe;
		
		FeatureTable originalFeatures = originalRecipe.getFeatureTable();
		simmerFeatures(newRecipe, originalFeatures.getThreshold(), originalFeatures.getAnnotation(), originalFeatures.getClassValueType());
		
		if(finalStage.compareTo(Stage.TRAINED_MODEL) < 0)
			return newRecipe;
		
		broilModel(newRecipe);
		
		return newRecipe;
	}

	/**
	 * @param newRecipe
	 * @throws Exception
	 */
	protected static void broilModel(Recipe newRecipe) throws Exception
	{
		TrainingResult trainResult = newRecipe.getLearner().train(newRecipe.getTrainingTable(), newRecipe.getLearnerSettings(), newRecipe.getValidationSettings(), newRecipe.getWrappers(), textUpdater);
		newRecipe.setTrainingResult(trainResult);
	}

	/**
	 * @param originalRecipe
	 * @param corpus
	 */
	protected static void prepareDocumentList(Recipe originalRecipe, DocumentList corpus)
	{
		DocumentList original = originalRecipe.getDocumentList();
		String currentAnnotation = original.getCurrentAnnotation();
		if(corpus.allAnnotations().containsKey(currentAnnotation))
		{
			corpus.setCurrentAnnotation(currentAnnotation);
		}
		else
		{
			System.err.println("Warning: data has no "+currentAnnotation+" annotation. You can't train a new model on this data (only predict)");
		}
		corpus.setLabelArray(original.getLabelArray());
		corpus.setTextColumns(new HashSet<String>(original.getTextColumns()));
	}


	protected static Recipe loadRecipe(String recipePath) throws DeserializationException, FileNotFoundException
	{
		File recipeFile = new File(recipePath);
		if (!recipeFile.exists())
		{
			throw new FileNotFoundException("No model file at " + recipeFile.getPath());
		}
		else
		{

			try
			{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(recipeFile));
				Recipe recipe = (Recipe) in.readObject();
				return recipe;
			}
			catch (Exception e)
			{

				throw new DeserializationException(e);
			}
		}
	}


	public static void main(String[] args) throws Exception
	{
		String recipePath = "saved/bayes.model.side";
		if (args.length < 1)
		{
			System.err.println("usage: chef.sh path/to/my.model.side corpus.csv...");
		}
		else
			recipePath = args[0];

		Set<String> corpusFiles = new HashSet<String>();
		
		String dataFile = "data/MovieReviews.csv";
		if (args.length < 2) corpusFiles.add(dataFile);
		else for(int i = 1; i < args.length; i++)
		{
			corpusFiles.add(dataFile);
		}

		Recipe recipe = loadRecipe(recipePath);
		Recipe result = followRecipe(recipe, new DocumentList(corpusFiles), recipe.getStage());

		System.out.println("extracted "+result.getFeatureTable().getFeatureSet().size()+" features.");
		if(result.getStage().compareTo(Stage.TRAINED_MODEL) >= 0)
			System.out.println(result.getTrainingResult().getTextConfusionMatrix());
		
	}

}
