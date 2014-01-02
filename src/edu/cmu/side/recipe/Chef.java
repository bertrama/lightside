package edu.cmu.side.recipe;

import java.io.File;
<<<<<<< local

=======
>>>>>>> other
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import plugins.metrics.models.BasicModelEvaluations;
import edu.cmu.side.control.BuildModelControl;
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
import edu.cmu.side.recipe.converters.ConverterControl;
import edu.cmu.side.recipe.converters.ConverterControl.RecipeFileFormat;

/**
 * loads a model trained using LightSide and uses it to label new instances.
 * 
 * @author dadamson
 */
public class Chef
{
//	static
//	{
//		System.setProperty("java.awt.headless", "true");
//		System.out.println(java.awt.GraphicsEnvironment.isHeadless() ? "Running in headless mode." : "Not actually headless");
//	}
    
	static boolean quiet = true;

	static StatusUpdater textUpdater = new StatusUpdater()
	{

		@Override
		public void update(String updateSlot, int slot1, int slot2)
		{
			if(!quiet)
				System.out.println(updateSlot+": "+slot1 + "/"+slot2);
		}

		@Override
		public void update(String update)
		{
			if(!quiet)
				System.out.println(update);	
		}

		@Override
		public void reset()
		{
			// TODO Auto-generated method stub

		}
	};

	//Extract Features
	protected static void simmerFeatures(Recipe recipe, int threshold, String annotation, Type type)
	{		
<<<<<<< local
		DocumentList corpus = recipe.getDocumentList();
		Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
		OrderedPluginMap extractors = recipe.getExtractors();

		for (SIDEPlugin plug : extractors.keySet())
=======
		final DocumentList corpus = recipe.getDocumentList();
		final OrderedPluginMap extractors = recipe.getExtractors();
		final ConcurrentSkipListSet<String> hitChunks = new ConcurrentSkipListSet<String>();
		final Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
		
		for (final SIDEPlugin plug : extractors.keySet())
>>>>>>> other
		{
<<<<<<< local
			if(!quiet) System.out.println("Extracting features with "+plug+"...");
			//System.out.println("Extractor Settings: "+extractors.get(plug));
			Collection<FeatureHit> extractorHits = ((FeaturePlugin) plug).extractFeatureHits(corpus, extractors.get(plug), textUpdater);
			hits.addAll(extractorHits);
=======
//			new Thread()
//			{
//				public void run()
//				{
					if(!quiet) System.out.println("Chef: Simmering features with "+plug+"...");
					//System.out.println("Extractor Settings: "+extractors.get(plug));
					Collection<FeatureHit> extractorHits = ((FeaturePlugin) plug).extractFeatureHits(corpus, extractors.get(plug), textUpdater);
					
//					synchronized(hits)
//					{
//						hitChunks.add(plug.toString());
						hits.addAll(extractorHits);
//						hits.notifyAll();
//					}
//				}
//			}.start();	
			if (!quiet) System.out.println("Chef: Finished simmering with " + plug + "...");
>>>>>>> other
		}

		if (!quiet) System.out.println("Chef: Done simmering with plugins!");
//		while(hitChunks.size() < extractors.size())
//		{
//			synchronized(hits)
//			{
//				try
//				{
//					hits.wait();
//				}
//				catch (InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}

		if (!quiet) System.out.println("Chef: Building feature table...");

		FeatureTable ft = new FeatureTable(corpus, hits, threshold, annotation, type);
		recipe.setFeatureTable(ft);

<<<<<<< local
=======
		if (!quiet) System.out.println("Chef: Done building feature table!");

>>>>>>> other
		if(recipe.getStage().compareTo(Stage.MODIFIED_TABLE) >= 0) //recipe includes filtering
		{
			for (SIDEPlugin plug : recipe.getFilters().keySet())
			{
				if(!quiet) System.out.println("Restructuring features with "+plug+"...");
				ft = ((RestructurePlugin) plug).restructure(recipe.getTrainingTable(), recipe.getFilters().get(plug), textUpdater);
			}
			recipe.setFilteredTable(ft);
		}
		ft.setName(recipe.getRecipeName()+" features");
	}

	public static Recipe followSimmerSteps(Recipe originalRecipe, DocumentList corpus, Stage finalStage, int newThreshold){
		Recipe newRecipe = Recipe.copyEmptyRecipe(originalRecipe);

		prepareDocumentList(originalRecipe, corpus);
		newRecipe.setDocumentList(corpus);
		printMemoryUsage();
		
		if(finalStage == Stage.DOCUMENT_LIST)
			return newRecipe;

		String annotation = originalRecipe.getAnnotation();

		if(!corpus.allAnnotations().containsKey(annotation))
			annotation = null;

		simmerFeatures(newRecipe, newThreshold, annotation, originalRecipe.getClassValueType());

		return newRecipe;
	}

	public static Recipe followRecipeWithTestSet(Recipe originalRecipe, DocumentList corpus, DocumentList testSet, Stage finalStage, int newThreshold) throws Exception{
		Recipe newRecipe = followSimmerSteps(originalRecipe, corpus, finalStage, newThreshold);

		Map<String, Serializable> validationSettings = new TreeMap<String, Serializable>();
		validationSettings.put("test", Boolean.TRUE);
		validationSettings.put("type", "SUPPLY");
		
		// Creates a reconciled test set feature table.
		validationSettings = BuildModelControl.prepareDocuments(newRecipe, validationSettings, testSet);

		newRecipe.setValidationSettings(validationSettings);

		newRecipe = broilModel(newRecipe);
		return newRecipe;
	}

	//TODO: be more consistent in parameters to recipe stages
	public static Recipe followRecipe(Recipe originalRecipe, DocumentList corpus, Stage finalStage, int newThreshold) throws Exception
	{
		Recipe newRecipe = followSimmerSteps(originalRecipe, corpus, finalStage, newThreshold);

		if(finalStage.compareTo(Stage.TRAINED_MODEL) < 0)
			return newRecipe;

		broilModel(newRecipe);
		printMemoryUsage();
		return newRecipe;
	}

	/**
	 * Build model and update recipe settings to include the new classifier. 
	 * @param newRecipe
	 * @throws Exception
	 */
	//Build Model
	protected static Recipe broilModel(Recipe newRecipe) throws Exception
	{
		if(!quiet) System.out.println("Training model with "+newRecipe.getLearner()+"...");
		TrainingResult trainResult = newRecipe.getLearner().train(newRecipe.getTrainingTable(), newRecipe.getLearnerSettings(), newRecipe.getValidationSettings(), newRecipe.getWrappers(), textUpdater);
		newRecipe.setTrainingResult(trainResult);
		newRecipe.setLearnerSettings(newRecipe.getLearner().generateConfigurationSettings());
		return newRecipe;
	}

	/**
	 * @param originalRecipe
	 * @param corpus
	 */
	protected static void prepareDocumentList(Recipe originalRecipe, DocumentList corpus)
	{
		if(!quiet) System.out.println("Preparing documents...");
		DocumentList original = originalRecipe.getDocumentList();
		FeatureTable originalTable = originalRecipe.getTrainingTable();
		String currentAnnotation = originalTable.getAnnotation();
		if(corpus.allAnnotations().containsKey(currentAnnotation))
		{
			corpus.setCurrentAnnotation(currentAnnotation, originalRecipe.getClassValueType());
		}
		else
		{
			//System.err.println("Warning: data has no "+currentAnnotation+" annotation. You can't train a new model on this data (only predict)");
		}
		corpus.setLabelArray(originalRecipe.getLabelArray());
		corpus.setTextColumns(new HashSet<String>(originalRecipe.getTextColumns()));
	}

	public static Recipe loadRecipe(String recipePath) throws IOException, FileNotFoundException
	{

		return ConverterControl.loadRecipe(recipePath);
	}


	public static void saveRecipe(Recipe recipe, File target, RecipeFileFormat exportFormat) throws IOException
	{
		ConverterControl.writeRecipeToFile(target.getPath(), recipe, exportFormat);
	}

	public static void main(String[] args) throws Exception
	{
		
		String recipePath, outPath;
		if (args.length < 2)
		{
			System.err.println("usage: chef.sh saved/template.model.side saved/new.model.side data.csv...");
			return;
		}
		
		recipePath = args[0];
		outPath = args[1];

		Set<String> corpusFiles = new HashSet<String>();

		String dataFile = "data/MovieReviews.csv";
		if (args.length < 3) corpusFiles.add(dataFile);
		else for(int i = 2; i < args.length; i++)
		{
			corpusFiles.add(args[i]);
		}

		if(!quiet) System.out.println("Loading "+recipePath);
		Recipe recipe = loadRecipe(recipePath);
		printMemoryUsage();

		if(!quiet) System.out.println("Loading documents: "+corpusFiles);
		Recipe result = followRecipe(recipe, new DocumentList(corpusFiles), recipe.getStage(), recipe.getFeatureTable().getThreshold());
		
		if(result.getStage().compareTo(Stage.TRAINED_MODEL) >= 0)
		{
			displayTrainingResults(result);
		}

		System.out.println("Saving finished recipe to "+outPath);
		saveRecipe(result, new File(outPath), RecipeFileFormat.XML);
	}

	protected static void printMemoryUsage()
	{
		if(quiet) return;
		
		double gigs = 1024 * 1024 * 1024;
		MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

		double beanMax = usage.getMax() / gigs;
		double beanUsed = usage.getUsed() / gigs;

		System.out.println(String.format("%.1f/%.1f GB used", beanUsed, beanMax));
	}

	/**
	 * @param recipe
	 */
	protected static void displayTrainingResults(Recipe recipe)
	{
		if(recipe.getStage().compareTo(Stage.TRAINED_MODEL) >= 0)
		{
			TrainingResult trainingResult = recipe.getTrainingResult();
			System.out.println(trainingResult.getTextConfusionMatrix());
			BasicModelEvaluations eval = new BasicModelEvaluations();
			System.out.println("Accuracy\t"+eval.getAccuracy(trainingResult));
			System.out.println("Kappa\t"+eval.getKappa(trainingResult));
		}
	}
	
	public static void setQuiet(boolean b)
	{
		quiet = b;
	}

}
