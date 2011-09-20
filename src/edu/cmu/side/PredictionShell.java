package edu.cmu.side;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;

public class PredictionShell 
{

	public static void main(String[] args)
	{
		Set<String> corpusFilenames = new HashSet<String>();
		String corpusCurrentAnnot = "class";
		String corpusText = "text";
		String predictionAnnotation = "predicted";
		SimpleDocumentList corpus = null;
		ArrayList<FeaturePlugin> extractors = new ArrayList<FeaturePlugin>();
		ArrayList<String> extractorConfigFiles = new ArrayList<String>();
		Integer threshold = 0;
		FeatureTable table = null;
		LearningPlugin learner = null;
		Map<String, String> config = new HashMap<String, String>();
		Map<Integer, Integer> foldsMap = null;
		String modelName = "model";
		String cvSetting = null;
		TrainingResultInterface result = null;
		File modelFile = new File("saved/model_23.0.ser");
		
		SIDEPlugin[] fex = SimpleWorkbench.getPluginsByType("feature_hit_extractor");
		for(int k = 0; k < fex.length; k++)
		{
			FeaturePlugin plug = (FeaturePlugin)fex[k];
			System.out.println(plug.getOutputName());
		}
		
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("-"))
			{
				if(args[i].contains("-dl"))
				{
					int j = i+1;
					while(j < args.length && !args[j].startsWith("-"))
					{
						corpusFilenames.add(args[j]);
						j++;
					}
					i = j-1;
				}
				else if(args[i].contains("-da"))
				{
					corpusCurrentAnnot = args[++i];
				}
				else if(args[i].contains("-dt"))
				{
					corpusText = args[++i];
				}
				else if(args[i].contains("-fe"))
				{
					int j = i+1;
					ArrayList<String> pluginNames = new ArrayList<String>();
					while(j < args.length && !args[j].startsWith("-") )
					{
						pluginNames.add(args[j]);
						j++;
					}
					i = j-1;
					SIDEPlugin[] featureExtractors = SimpleWorkbench.getPluginsByType("feature_hit_extractor");
					for(int k = 0; k < featureExtractors.length; k++)
					{
						FeaturePlugin plug = (FeaturePlugin)featureExtractors[k];
						if(pluginNames.contains(plug.getOutputName()))
						{
							extractors.add(plug);
						}
					}
				}
				else if(args[i].contains("-fc"))
				{
					for(int j = 0; j < extractors.size(); j++)
					{
						extractorConfigFiles.add(args[j+i+1]);
					}
					i += extractors.size();
				}
				else if(args[i].contains("-ff"))
				{
					threshold = Integer.parseInt(args[++i]);
				}
				else if (args[i].contains("-mf"))
				{
					modelFile = new File(args[i+1]);
					i++;
				}
				else if(args[i].contains("-mb"))
				{
					String learnerName = args[++i];
					SIDEPlugin[] learners = SimpleWorkbench.getPluginsByType("model_builder");
					for(int j = 0; j < learners.length; j++){
						LearningPlugin plug = (LearningPlugin)learners[j];
						System.out.println(plug.getOutputName());
						if(plug.getOutputName().equals(learnerName))
						{
							learner = plug;
						}
					}
				}
				else if(args[i].contains("-cv"))
				{
					cvSetting = args[++i];
				}
			}
		}
		
		
		if(corpusFilenames.size() != 0)
			corpus = new SimpleDocumentList(corpusFilenames, corpusCurrentAnnot, corpusText);
		else
		{
			BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
			ArrayList<String> instances = new ArrayList<String>();
			String line;
			try
			{
				while((line = inReader.readLine()) != null)
				{
					instances.add(line);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			corpus = new SimpleDocumentList(instances);
			//corpus = new SimpleDocumentList("foo bar bad");
		}
		
		if(corpus != null)
		{
			if(extractorConfigFiles.size() == extractors.size())
			{
				for(int i = 0; i < extractors.size(); i++)
				{
					extractors.get(i).configureFromFile(extractorConfigFiles.get(i));
				}
			}
			else
				System.out.println("warning - not enough config files for extractors! Give a list after -fc");
			
			table = new FeatureTable(extractors, corpus, threshold);
		}
		
		if(!modelFile.exists())
		{
			System.err.println("No model file at "+modelFile.getPath());
		}
		else if(table != null)
		{
			try
			{
				SimpleTrainingResult trained = new SimpleTrainingResult(modelFile);
				table = trained.predictLabels(predictionAnnotation, "class", table);

				table.getDocumentList().setCurrentAnnotation(predictionAnnotation);
				ArrayList<String> annotationList = table.getDocumentList().getAnnotationArray(predictionAnnotation);
				List<String> textList = corpus.getCoveredTextList();
				System.out.println(predictionAnnotation+"\ttext\n---------------------\n");
				for(int i = 0; i < corpus.getSize(); i++)
				{
					String text = textList.get(i);
					System.out.println(annotationList.get(i)+"\t"+text.substring(0, Math.min(100, text.length())));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}