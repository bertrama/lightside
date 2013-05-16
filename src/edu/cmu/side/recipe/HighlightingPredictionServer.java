/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.side.recipe;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Map;

import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import plugins.analysis.one.display.SentenceEvaluator;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.PredictionResult;

/**
 * loads a model trained using lightSIDE uses it to label new instances via the
 * web. TODO (maybe): allow classification of multiple instances at once, or by
 * multiple classifiers, upload new trained models (possible?)
 * 
 * @author dadamson
 */
public class HighlightingPredictionServer extends PredictionServer
{
	private String styleLink = "<link href=\"http://lightsidelabs.com/wp-content/uploads/pagelines/" +
			"compiled-css-1366064275.css\" media=\"screen,projection\" type=\"text/css\" rel=\"stylesheet\">";
	
	public HighlightingPredictionServer(int size)
	{
		super(size);
	}
	

	protected String handleGetUpload(Request request, Response response)
	{
		return styleLink+"\n"+super.handleGetUpload(request, response);
	}

	@Override
	protected String handleGetEvaluate(Request request, Response response, String header)
	{
		System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024) + " M used");
		return styleLink+"\n"+super.handleGetEvaluate(request, response, header);
	}
	
	@Override
	protected String handleEvaluate(Request request, Response response) throws IOException, FileNotFoundException
	{
		Part part = request.getPart("sample");
		String sample = part.getContent();
		String highlightedSample = "";
		String header = "";

		if (!sample.isEmpty())
		{

			String modelName = request.getPath().getPath(1).substring(1);
			String answer = checkModel(response, modelName);
			if (!answer.equals("OK")) { return answer; }

			DocumentList sampleDoc = new DocumentList(sample);
			Predictor predictor = predictors.get(modelName);
			PredictionResult prediction = predictor.predict(sampleDoc);

			Map<String, Double> scores = prediction.getDistributionMapForInstance(0);

			for (String label : scores.keySet())
			{
				header += String.format("%s: %.1f%%<br>", label, 100 * scores.get(label));
			}

			sampleDoc.setLabelArray(predictor.getLabelArray());
			
			SentenceEvaluator sentenceEvaluator = new SentenceEvaluator(sampleDoc, predictor, 0);

			sentenceEvaluator.evaluate();

			String label = prediction.getPredictions().get(0).toString();
			for (int i = 0; i < sentenceEvaluator.getNumSentences(); i++)
			{
				String sentence = sentenceEvaluator.getSentenceText(i);
				double score = sentenceEvaluator.getSentenceScore(label, i);

				float deviationColorValue = (float) Math.pow(Math.min(Math.max(Math.abs(score), 0.0), 1.0), 2);
				Color color = Color.getHSBColor(score > 0 ? 0.05f : 0.6f, 0.5f * deviationColorValue, 1.0f);

				highlightedSample += String.format("<font style=\"background-color:%02X%02X%02X\">%s</font> ", color.getRed(), color.getGreen(),
						color.getBlue(), sentence);
//				System.out.println(String.format("%s\t%02X%02X%02X\t%s ", deviationColorValue, color.getRed(), color.getGreen(), color.getBlue(), sentence));
			}
		}

		return handleGetEvaluate(request, response, "<h3>" + header + "</h3><p style=\"max-width:400px\">" + highlightedSample + "</p>");
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			printUsage();
		}

		initSIDE();
		int port = 8000;

		int start = 0;
		if (args.length > 0 && !args[0].contains(":"))
		{
			try
			{
				start = 1;
				port = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException e)
			{
				printUsage();
			}
		}

		for (int i = start; i < args.length; i++)
		{

			String[] modelConfig = args[i].split(":");
			String modelNick = modelConfig[0];
			String modelPath = modelConfig[1];

			attachModel(modelNick, modelPath);
		}

		if (predictors.isEmpty())
		{
			System.out.println("Warning: no models attached yet. Use http://localhost:" + port + "/upload");
		}

		HighlightingPredictionServer.serve(port, 5);
	}

	public static void serve(int port, int threads) throws Exception
	{
		Container container = new HighlightingPredictionServer(threads);

		Server server = new ContainerServer(container);
		Connection connection = new SocketConnection(server);
		SocketAddress address = new InetSocketAddress(port);

		connection.connect(address);
		System.out.println("Started server on port " + port + ".");
	}
}
