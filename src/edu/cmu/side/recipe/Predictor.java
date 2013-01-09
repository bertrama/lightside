package edu.cmu.side.recipe;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;

import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.Recipe;
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
	
	StatusUpdater textUpdater = new StatusUpdater()
	{
		boolean quiet = true;
		
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

	public Predictor(Map<String, String> params) throws DeserializationException
	{
		System.out.println(params);

		this.modelPath = params.get("path");
		this.predictionAnnotation = params.get("prediction");
		this.corpusCurrentAnnot = params.get("currentAnnotation");
		loadModel();
	}

	public Predictor(String modelPath, String annotationName) throws DeserializationException
	{
		this.modelPath = modelPath;
		this.predictionAnnotation = "predicted";
		this.corpusCurrentAnnot = annotationName;

		loadModel();
	}

	protected FeatureTable prepareTestSet( DocumentList test)
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

	/**
	 * @param corpus
	 * @return
	 */
	protected PredictionResult predict(DocumentList corpus)
	{
		FeatureTable table = prepareTestSet(corpus);

		PredictionResult result = recipe.getLearner().predict(recipe.getFeatureTable(), table, recipe.getLearnerSettings(), textUpdater);
		return result;	
	}
	
	public String prettyPredict(String instance)
	{

		DocumentList corpus = null;
		corpus = new DocumentList(instance);
		String prediction = "?";
	
		PredictionResult predictionResult = predict(corpus);
		
		prediction = predictionResult.getPredictions().get(0).toString();
		if(predictionResult.getDistributions() != null)
		{
			prediction = prediction + "\t "+ (int)(predictionResult.getDistributions().get(0).get(prediction)*100) + "%";
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
	 * 
	 */
	protected void loadModel() throws DeserializationException
	{
		File modelFile = new File(modelPath);
		if (!modelFile.exists())
		{
			System.err.println("No model file at " + modelFile.getPath());
		}
		else
		{

			try
			{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelFile));
				recipe = (Recipe) in.readObject();

				recipe.getLearner().loadClassifierFromSettings(recipe.getLearnerSettings());
			}

			catch (Exception e)
			{

				// TODO Auto-generated catch block
				throw new DeserializationException(e);
			}
		}
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
		String modelPath = "saved/logit.model.side";
		if (args.length < 1)
		{
			System.err.println("usage: just_predict.sh path/to/my.model.side [annotation_name]");
		}
		else
			modelPath = args[0];

		String annotation = "class";
		if (args.length > 1) annotation = args[1];

		//to swallow all output except for the classifications
//		PrintStream actualOut = System.out;
//
//		try
//		{
//			String outLogFilename = "simple_side_predict.log";
//			PrintStream logPrintStream = new PrintStream(outLogFilename);
//			System.setOut(logPrintStream);
//		}
//		catch (FileNotFoundException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


		Predictor predict = new Predictor(modelPath, annotation);
		Scanner input = new Scanner(System.in);

		while (input.hasNextLine())
		{
			String sentence = input.nextLine();
			String answer = predict.prettyPredict(sentence);
//			actualOut.println(answer);
			System.out.println(answer + "\t" + sentence.substring(0, Math.min(sentence.length(), 100)));
		}
	}

}
