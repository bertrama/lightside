package edu.cmu.side.recipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;

import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.RestructurePlugin;
import edu.cmu.side.plugin.SIDEPlugin;

/**
 * loads a model trained using lightSIDE uses it to label new instances.
 * 
 * @author dadamson
 */
public class Predictor
{
	/**
	 * 
	 * @param modelFilePath
	 *            the path to the SIDE model file
	 */
	String modelPath;
	String predictionAnnotation = "predicted";
	String corpusCurrentAnnot = "class";

	// File name/location is defined in parameter map
	Recipe recipe;
	private boolean quiet = true;

	StatusUpdater textUpdater = new StatusUpdater()
	{

		@Override
		public void update(String updateSlot, int slot1, int slot2)
		{
			if (!isQuiet()) System.err.println(updateSlot + ": " + slot1 + "/" + slot2);
		}

		@Override
		public void update(String update)
		{
			if (!isQuiet()) System.err.println(update);
		}

		@Override
		public void reset()
		{

		}
	};

	public Predictor(Recipe r, String p)
	{
		this.recipe = r;
		this.predictionAnnotation = p;
		setQuiet(true);
	}

	public Predictor(Map<String, String> params) throws DeserializationException, FileNotFoundException
	{

		if (!isQuiet()) System.out.println(params);

		this.modelPath = params.get("path");
		this.predictionAnnotation = params.get("prediction");
		this.corpusCurrentAnnot = params.get("currentAnnotation");
		loadModel();
	}

	public Predictor(String modelPath, String annotationName) throws DeserializationException, FileNotFoundException
	{
		this.modelPath = modelPath;
		this.predictionAnnotation = "predicted";
		this.corpusCurrentAnnot = annotationName;

		loadModel();
	}

	protected FeatureTable prepareTestSet(DocumentList test)
	{
		test.setLabelArray(recipe.getDocumentList().getLabelArray());

		Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
		OrderedPluginMap extractors = recipe.getExtractors();
		for (SIDEPlugin plug : extractors.keySet())
		{
			Collection<FeatureHit> extractorHits = ((FeaturePlugin) plug).extractFeatureHits(test, extractors.get(plug), textUpdater);
			hits.addAll(extractorHits);
		}
		FeatureTable ft = new FeatureTable(test, hits, 0);
		for (SIDEPlugin plug : recipe.getFilters().keySet())
		{
			ft = ((RestructurePlugin) plug).filterTestSet(recipe.getTrainingTable(), ft, recipe.getFilters().get(plug), textUpdater);
		}
		return ft;
	}

	/**
	 * 
	 * @param instance
	 *            the string to classify.
	 * 
	 * @return a map of predicted category-labels to associated probabilities.
	 *         Note that SIDE doesn't yet expose the probability distribution of
	 *         its predictions, so this might just be a single entry
	 *         (predicted_label, 1.0)
	 */
	public List<? extends Comparable> predict(List<String> instances)
	{

		DocumentList corpus = null;
		corpus = new DocumentList(instances);

		return predict(corpus).getPredictions();

	}

	public double predictScore(String instance, String label)
	{
		DocumentList corpus = null;
		corpus = new DocumentList(instance);

		PredictionResult predictionResult = predict(corpus);

		return predictionResult.getDistributions().get(label).get(0);
	}

	/**
	 * @param corpus
	 * @return
	 */
	public PredictionResult predict(DocumentList corpus)
	{
		PredictionResult result = null;
		try
		{
			Chef.quiet = isQuiet();
			Recipe newRecipe = Chef.followRecipe(recipe, corpus, Stage.MODIFIED_TABLE, 0);
			FeatureTable predictTable = newRecipe.getTrainingTable();
			System.out.println(predictTable.getFeatureSet().size() + " features total");

			if (!isQuiet())
			{
				System.out.println(predictTable.getHitsForDocument(0).size() + " feature hits in document 0");
			}

			result = predictFromTable(predictTable);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public PredictionResult predictFromTable(FeatureTable predictTable)
	{
		PredictionResult result = null;
		FeatureTable trainingTable = recipe.getTrainingTable();
		predictTable.reconcileFeatures(trainingTable);

		if (!isQuiet())
		{
			System.out.println(predictTable.getHitsForDocument(0).size() + " feature hits in document 0 after reconciliation");
			System.out.println(predictTable.getFeatureSet().size() + " features total");
		}

		result = recipe.getLearner().predict(trainingTable, predictTable, recipe.getLearnerSettings(), textUpdater, recipe.getWrappers());

		return result;
	}

	public String prettyPredict(String instance)
	{

		DocumentList corpus = null;
		corpus = new DocumentList(instance);
		String prediction = "?";

		PredictionResult predictionResult = predict(corpus);

		prediction = predictionResult.getPredictions().get(0).toString();
		if (predictionResult.getDistributions() != null)
		{
			prediction = prediction + "\t " + (int) (predictionResult.getDistributions().get(prediction).get(0) * 100) + "%";
		}

		return prediction;

	}

	public String predict(String instance)
	{

		DocumentList corpus = null;
		corpus = new DocumentList(instance);
		String prediction = "?";

		PredictionResult predictionResult = predict(corpus);
		prediction = predictionResult.getPredictions().get(0).toString();

		return prediction;

	}

	/**
	 * @throws FileNotFoundException
	 * 
	 */
	protected void loadModel() throws DeserializationException, FileNotFoundException
	{
		recipe = Chef.loadRecipe(modelPath);
	}

	public String getModelPath()
	{
		return modelPath;
	}

	public void setModelPath(String modelPath)
	{
		this.modelPath = modelPath;
	}

	public String getPredictionAnnotation()
	{
		return predictionAnnotation;
	}

	public void setPredictionAnnotation(String predictionAnnotation)
	{
		this.predictionAnnotation = predictionAnnotation;
	}

	public static void main(String[] args) throws Exception
	{
		String modelPath = "saved/bayes.model.side";
		if (args.length < 1)
		{
			System.err.println("usage: just_predict.sh path/to/my.model.side [annotation_name]");
		}
		else
			modelPath = args[0];

		String annotation = "class";
		if (args.length > 1) annotation = args[1];

		// to swallow all output except for the classifications
		// PrintStream actualOut = System.out;
		//
		// try
		// {
		// String outLogFilename = "simple_side_predict.log";
		// PrintStream logPrintStream = new PrintStream(outLogFilename);
		// System.setOut(logPrintStream);
		// }
		// catch (FileNotFoundException e)
		// {
		// e.printStackTrace();
		// }

		Predictor predictor = new Predictor(modelPath, annotation);
		Scanner input = new Scanner(System.in);

		while (input.hasNextLine())
		{
			String sentence = input.nextLine();
			String answer = predictor.prettyPredict(sentence);
			// actualOut.println(answer);
			System.out.println(answer + "\t" + sentence.substring(0, Math.min(sentence.length(), 100)));
		}
	}

	public boolean isQuiet()
	{
		return quiet;
	}

	public void setQuiet(boolean quiet)
	{
		this.quiet = quiet;
	}

	public Map<String, Double> getScores(String sample)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
