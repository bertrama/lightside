package edu.cmu.side;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;


/**
 * 
 * @author dadamson
 * for example, 
 * CLASSPATH=bin:lib/jfreechart-1.0.11.jar:lib/lingpipe-2.3.0.jar:lib/riverlayout.jar:lib/stanford-postagger-2010-05-26.jar:lib/trove.jar:lib/weka.jar:lib/XMLBoss.jar:lib/xmlparserv2.jar:lib/yeritools.jar edu.cmu.side.SimpleWorkbench
 * 
 * java -classpath $CLASSPATH edu.cmu.side.PredictionShell -mf myModel.ser < cat myPlainText.txt
 * 
 * or
 * 
 * java -classpath $CLASSPATH edu.cmu.side.PredictionShell -mf myModel.ser -dl myCSV.csv
 * 
 */
public class PredictionShell 
{

	public static void main(String[] args)
	{
		Set<String> corpusFilenames = new HashSet<String>();
		String corpusCurrentAnnot = "class";
		String corpusText = "text";
		String predictionAnnotation = "predicted";
		SimpleDocumentList corpus = null;
		Collection<FeaturePlugin> extractors = new ArrayList<FeaturePlugin>();
		Integer threshold = 0;
		FeatureTable table = null;
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
				else if (args[i].contains("-mf"))
				{
					modelFile = new File(args[i+1]);
					i++;
				}
			}
		}
		
		//---end command-line configuration
		
		//--build a document list
		
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
		
//		//-----configure extractors and build feature table
//		
//		if(corpus != null && !extractors.isEmpty())
//		{
//			//not every extractor exposes a save-config-file interface.
//			if(extractorConfigFiles.size() == extractors.size())
//			{
//				for(int i = 0; i < extractors.size(); i++)
//				{
//					extractors.get(i).configureFromFile(extractorConfigFiles.get(i));
//				}
//			}
////			else
////				System.out.println("warning - not enough config files for extractors! Give a list after -fc");
//			
//			table = new FeatureTable(extractors, corpus, threshold);
//		}
//		
		//------load the model and predict
		if(!modelFile.exists())
		{
			System.err.println("No model file at "+modelFile.getPath());
		}
		else
		{
			System.out.println("Using model file "+modelFile.getPath());
			try
			{
				SimpleTrainingResult trained = new SimpleTrainingResult(modelFile);
				extractors = trained.getFeatureTable().getExtractors();
				table = new FeatureTable(extractors, corpus, threshold);
				table = trained.predictLabels(predictionAnnotation, corpusCurrentAnnot, table);

				
				table.getDocumentList().setCurrentAnnotation(predictionAnnotation);
				ArrayList<String> annotationList = table.getDocumentList().getAnnotationArray(predictionAnnotation);
				List<String> textList = corpus.getCoveredTextList();
				System.out.println(predictionAnnotation+"\ttext\n---------------------\n");
				
				//annotationList.get(i) is the predicted label for document #i
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