package edu.cmu.side.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import com.yerihyo.yeritools.collections.CountMap;

import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTableKey;
import edu.cmu.side.ml.PredictionToolkit.PredictionResult;
import edu.cmu.side.ui.configpanel.FeatureAnalyzerConfigPanel;
import edu.cmu.side.uima.DocumentListInterface;
import edu.cmu.side.uima.type.SIDEAnnotation;

public class FeatureAnalysisToolkit {

	public static final String trainingResultOptionKey = "training_result_option_key";
	public static final String evaluationResultOptionKey = "evaluation_result_option_key";
	public static final String featureTableKeyOptionKey = "feature_table_key_option_key";
	public static final String selectedLabelOptionKey = "selected_label_option_key";
	public static final String selectedValueOptionKey = "selected_value_option_key";
	public static final String selectedCellOptionKey = "selected_cell_option_key";
	public static final String selectedComparisonOptionKey = "selected_comparison_option_key";
	
	
	public static abstract class FeatureAnalysisPlugin extends SIDEPlugin implements ActionListener{
		public static String type = "feature_analysis";
		protected Map<String,Object> optionMap;
		protected FeatureAnalyzerConfigPanel configPanel;
//		protected transient TrainingResult trainingResult;
		
		@Override
		protected Component getConfigurationUIForSubclass() {
			return null;
		}
	
		@Override
		public String getType() { return type; }
	
		@Override
		public void fromXML(Element element) throws Exception {
			throw new UnsupportedOperationException(); 
		}
	
		@Override
		public String toXML() {
			throw new UnsupportedOperationException(); 
		}
		
		public abstract String getTitleString();
		public abstract void memoryToUI();
		public abstract void uiToMemory();
		public abstract Component getUI();
		
		protected void variableChangePerformed(){
			if(this.configPanel==null){ return; }
			
			this.uiToMemory();
			this.getConfigPanel().memoryToUIAll();
		}
		
		@Override
		public void actionPerformed(ActionEvent evt){ variableChangePerformed(); }
		
		public static Map<Comparable,CountMap<Double>> createMap(TrainingResultInterface trainingResult, FeatureTableKey featureTableKey, String selectedLabel){
			
			DocumentListInterface documentList = trainingResult.getDocumentList();
			String[] labelArray = trainingResult.getDocumentList().getLabelArray();
			
			Map<Comparable,CountMap<Double>> map = new TreeMap<Comparable,CountMap<Double>>();
			if(selectedLabel==null){
				for(boolean b : new boolean[]{true, false}){
					CountMap<Double> countMap = new CountMap<Double>();
					map.put(b, countMap);
				}
			}else{
				for(String label : labelArray){
					CountMap<Double> countMap = new CountMap<Double>();
					map.put(label, countMap);
				}
			}
			
			PredictionResult predictionResult = ((TrainingResult)trainingResult).getSelfPredictionResult();
			FeatureTable predictionFeatureTable = predictionResult.getFeatureTable();
			
			int docIndex = 0;
			int validDocIndex = 0;
			int[] result = new int[2];
			for(Iterator<Object> iterator = documentList.iterator(); iterator.hasNext(); docIndex++){
				SIDEAnnotation sideAnnotation = (SIDEAnnotation)iterator.next();
				String labelString = sideAnnotation.getLabelString();
				
				String mostLikelyLabelString = predictionResult.getMostLikelyLabel(docIndex);
				Comparable key;
				if(selectedLabel==null){
					if(labelString==null){ System.out.println("null!"); }
					key = mostLikelyLabelString.equals(labelString);
					validDocIndex++;
				}
				else if(labelString.equals(selectedLabel)){
					key = mostLikelyLabelString;
					validDocIndex++;
				}else{ continue; }
				
				CountMap<Double> countMap = map.get(key);
				Number value = predictionFeatureTable.getValue(featureTableKey, docIndex);
				countMap.increCount(value.doubleValue());
				
				result[mostLikelyLabelString.equals(labelString)?0:1]++;
			}
			
//			System.out.println("total - o:"+result[0]+", x:"+result[1]);
			return map;
		}

		public FeatureAnalyzerConfigPanel getConfigPanel() {
			return configPanel;
		}

		public void setConfigPanel(
				FeatureAnalyzerConfigPanel featureAnalyzerConfigPanel) {
			this.configPanel = featureAnalyzerConfigPanel;
		}

		public void setOptionMap(Map<String, Object> optionMap) {
			this.optionMap = optionMap;
		}

		public Map<String, Object> getOptionMap() {
			return optionMap;
		}
	}
}
