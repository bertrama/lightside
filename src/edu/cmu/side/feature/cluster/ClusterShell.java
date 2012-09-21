package edu.cmu.side.feature.cluster;
import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.feature.LocalFeatureHit;

public class ClusterShell {
	public static void main(String[] args) throws Exception{
		Set<String> corpusFilenames = new HashSet<String>();
		String corpusCurrentAnnot = "class";
		String corpusText = "text";
		SimpleDocumentList corpus = null;
		ArrayList<FeaturePlugin> extractors = new ArrayList<FeaturePlugin>();
		ArrayList<String> extractorConfigFiles = new ArrayList<String>();
		Integer threshold = 10;
		FeatureTable table = null;
		LearningPlugin learner = null;
		Map<String, String> config = new HashMap<String, String>();
		Map<Integer, Integer> foldsMap = null;
		String modelName = "model";
		String cvSetting = null;
		TrainingResultInterface result = null;
		
		double similarityWindow = 0.05;
		double clusterSimilarity = 0.05;
		int chunks = 10;
		boolean localClusters = false;
		
		for(int i = 0; i < args.length; i++){
			if(args[i].startsWith("-")){
				if(args[i].contains("-dl")){
					int j = i+1;
					while(!args[j].startsWith("-")){
						corpusFilenames.add(args[j]);
						j++;
					}
					i = j-1;
				}else if(args[i].contains("-da")){
					corpusCurrentAnnot = args[++i];
				}else if(args[i].contains("-dt")){
					corpusText = args[++i];
				}else if(args[i].contains("-fe")){
					int j = i+1;
					ArrayList<String> pluginNames = new ArrayList<String>();
					while(!args[j].startsWith("-")){
						pluginNames.add(args[j]);
						j++;
					}
					i = j-1;
					SIDEPlugin[] featureExtractors = SimpleWorkbench.getPluginsByType("feature_hit_extractor");
					for(int k = 0; k < featureExtractors.length; k++)
					{
						System.out.println(featureExtractors[k]);
						
						FeaturePlugin plug = (FeaturePlugin)featureExtractors[k];
						if(pluginNames.contains(plug.getOutputName()))
						{
							extractors.add(plug);
						}
					}
				}else if(args[i].contains("-fc")){
					for(int j = 0; j < extractors.size(); j++){
						extractorConfigFiles.add(args[j+i+1]);
					}
					i += extractors.size();
				}else if(args[i].contains("-ff")){
					threshold = Integer.parseInt(args[++i]);
				}/*else if(args[i].contains("-mb")){
					String learnerName = args[++i];
					SIDEPlugin[] learners = SimpleWorkbench.getPluginsByType("model_builder");
					for(int j = 0; j < learners.length; j++){
						LearningPlugin plug = (LearningPlugin)learners[j];
						if(plug.getOutputName().equals(learnerName)){
							learner = plug;
						}
					}
				}*/else if(args[i].contains("-cv")){
					cvSetting = args[++i];
				}
				else if(args[i].contains("--minsimilarity")){
					clusterSimilarity = Double.parseDouble(args[++i]);
				}
				else if(args[i].contains("--similaritywindow")){
					similarityWindow = Double.parseDouble(args[++i]);
				}
				else if(args[i].contains("--chunks")){
					chunks = Integer.parseInt(args[++i]);
				}
				else if(args[i].contains("--localclusters")){
					localClusters = true;
				}
			}
		}
		if(corpusFilenames.size() != 0)
			corpus = new SimpleDocumentList(corpusFilenames, corpusCurrentAnnot, corpusText);
		if(corpus != null){
//			for(int i = 0; i < extractors.size(); i++){
//				extractors.get(i).configureFromFile(extractorConfigFiles.get(i));
//			}
			System.out.println("ff:"+threshold+", clusterSim:"+clusterSimilarity+", simWindow:"+similarityWindow+", chunks"+chunks+" localClusters:"+localClusters);
			
			Collection hits = extractors.get(0).extractFeatureHits(corpus, new SwingUpdaterLabel(){public void setText(String s){System.out.println(s);}});
			
			corpus = null;
			
			List<Cluster> clusters = new ArrayList<Cluster>(Cluster.makeClusters((List<FeatureHit>) hits, threshold, clusterSimilarity, similarityWindow, chunks, localClusters));
			
			for(int i = 0; i < clusters.size(); i++)
			{
				Cluster c = clusters.get(i);
				System.out.println("#"+i+":\n"+c + "\n");
			}
			
			System.out.println(clusters.size()+" top-level clusters");
			
			//table = new FeatureTable(extractors, corpus, threshold);
			
		}
//		if(learner != null && table != null){
//			try{
//				Integer folds = Integer.parseInt(cvSetting);
//				foldsMap = corpus.getFoldsMapByNum(folds);
//			}catch(Exception e){
//				foldsMap = corpus.getFoldsMapByFile();
//			}
//			result = learner.train(table, modelName, config, foldsMap, new JLabel());
//			System.out.println(result.getSummary());
//		}
	}
}