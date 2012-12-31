package old;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.table.TableModel;

import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.view.util.FeatureTableModel;

public class ExportShell {
	public static void main(String[] args) throws Exception{
		Set<String> corpusFilenames = new HashSet<String>();
		String corpusCurrentAnnot = "class";
		String corpusText = "text";
		DocumentList corpus = null;
		ArrayList<FeaturePlugin> extractors = new ArrayList<FeaturePlugin>();
		ArrayList<String> extractorConfigFiles = new ArrayList<String>();
		Integer threshold = 1;
		FeatureTable table = null;
		LearningPlugin learner = null;
		Map<String, String> config = new HashMap<String, String>();
		Map<Integer, Integer> foldsMap = null;
		String modelName = "model";
		String cvSetting = null;
		String learnerConfig = null;
		TrainingResultInterface result = null;
		String outputFilename = null;
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
					for(int k = 0; k < featureExtractors.length; k++){
						FeaturePlugin plug = (FeaturePlugin)featureExtractors[k];
						if(pluginNames.contains(plug.getOutputName())){
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
				}else if(args[i].contains("-mb")){
					String learnerName = args[++i];
					SIDEPlugin[] learners = SimpleWorkbench.getPluginsByType("model_builder");
					for(int j = 0; j < learners.length; j++){
						LearningPlugin plug = (LearningPlugin)learners[j];
						if(plug.getOutputName().equals(learnerName)){
							learner = plug;
						}
					}
				}else if(args[i].contains("-mc")){
					learnerConfig = args[++i];
				}else if(args[i].contains("-cv")){
					cvSetting = args[++i];
				}else if(args[i].contains("-to")){
					outputFilename = args[++i];
				}
			}
		}
		if(corpusFilenames.size() != 0)
			corpus = new DocumentList(corpusFilenames, corpusCurrentAnnot, corpusText);
		if(corpus != null && extractors.size() > 0){
			for(int i = 0; i < extractors.size(); i++){
				extractors.get(i).configureFromFile(extractorConfigFiles.get(i));
			}
			table = new FeatureTable(extractors, corpus, threshold);
			table.defaultEvaluation();
		}
		if(learner != null && table != null){
			try{
				Integer folds = Integer.parseInt(cvSetting);
				foldsMap = corpus.getFoldsMapByNum(folds);
			}catch(Exception e){
				foldsMap = corpus.getFoldsMapByFile();
			}
			if(learnerConfig != null){
				learner.configureFromFile(learnerConfig);
			}
			result = learner.train(table, modelName, config, foldsMap, new JLabel());
			System.out.println(result.getSummary());
		}
		if(outputFilename != null && table != null){
			CSVExporter.exportToCSV(table, new File(outputFilename));
		}
	}

	public static TableModel getModelFromFeatureTable(FeatureTable table){
		FeatureTableModel tableModel = new FeatureTableModel();
		tableModel.addColumn("from");
		tableModel.addColumn("feature name");
		tableModel.addColumn("type");

		for(String eval : table.getConstantEvaluations()){
			tableModel.addColumn(eval);
		}
		List<String> otherEvals = new ArrayList<String>();
		for(String eval : table.getEvaluations().keySet()){
			boolean found = false;
			for(String s : table.getConstantEvaluations()){
				if(eval.equals(s)){ found = true; break; }
			}
			if(!found){
				otherEvals.add(eval); 
				tableModel.addColumn(eval);
			}
		}
		for(Feature f : table.getFeatureSet()){
			Object[] row = FeatureTablePanel.getFeatureDisplayRow(table, otherEvals, tableModel, f);
			tableModel.addRow(row);
		}
		return tableModel;
	}
}