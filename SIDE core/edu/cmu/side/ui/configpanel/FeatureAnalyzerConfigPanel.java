package edu.cmu.side.ui.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Element;

import com.mysterion.xml.XMLBoss;
import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTableKey;
import edu.cmu.side.ml.PredictionToolkit.PredictionResult;
import edu.cmu.side.plugin.FeatureAnalysisToolkit.FeatureAnalysisPlugin;

public class FeatureAnalyzerConfigPanel extends JPanel{
	private static final long serialVersionUID = 1L;

	public FeatureAnalyzerConfigPanel(){
		yeriInit();
	}
	
	private Map<String,Object> optionMap;

	public void memoryToUIAll(){

		FeatureAnalysisPlugin[] pluginArray = this.getPluginArray();
		for(FeatureAnalysisPlugin plugin : pluginArray){
			plugin.memoryToUI();
		}
	}		

	private void yeriInit() {
		optionMap = new HashMap<String,Object>();
		
		this.setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		this.add(tabbedPane, BorderLayout.CENTER);
		
		FeatureAnalysisPlugin[] pluginArray = this.getPluginArray();
		
		String[] tabNameArray = new String[pluginArray.length];
		Component[] componentArray = new Component[pluginArray.length];
		
		for(int i=0; i<pluginArray.length; i++){
			FeatureAnalysisPlugin plugin = pluginArray[i];
			tabNameArray[i] = plugin.getTitleString();
			
			plugin.setOptionMap(this.optionMap);
			plugin.setConfigPanel(this);
//			plugin.memoryToUI();
			componentArray[i] = plugin.getUI();
		}
		SwingToolkit.reloadTabbedPaneByTabName(tabbedPane, tabNameArray, componentArray, true);
		
		memoryToUIAll();
		
//		JPanel rightPanel = new JPanel();
//		rightPanel.setLayout(new RiverLayout());
		
//		analysisResultPanel = new JPanel();
//		retrainProgressBar = new JProgressBar();
//		retrainButton = new JButton("retrain");
//		retrainButton.addActionListener(new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				TrainingResult trainingResult = (TrainingResult)FeatureAnalyzerConfigPanel.this.trainingResultComboBox.getSelectedItem();
//				RetrainTask fat = new RetrainTask(retrainProgressBar, trainingResult);
//				fat.execute();
//			}
//		});
		
		
//		rightTabbedPane.addTab("basic", analysisResultPanel);
//		rightPanel.add("br hfill", retrainProgressBar);
//		rightPanel.add("right", retrainButton);
	
//		splitPane.setTopComponent(leftPanel);
//		splitPane.setBottomComponent(rightTabbedPane);
	}
	
//	private class RetrainTask extends OnPanelSwingTask{
//		private TrainingResult trainingResult;
//		
//		public RetrainTask(JProgressBar progressBar, TrainingResult trainingResult){
//			this.addProgressBar(progressBar);
//			this.trainingResult = trainingResult;
//		}
//		
//		@Override
//		protected Void doInBackground() throws Exception {
//			FeatureTable featureTable = trainingResult.getFeatureTable().clone();
//			
////			FeatureTableKey[] featureTableKeyArray = model.getCheckedFeatureTableKey();
//			
////			featureTable.setFeatureFilterer(new FeatureTable.NameFeatureFilterer(featureTableKeyArray));
//			
//			MLAPlugin mlaPlugin = (MLAPlugin)trainingResult.getMlaPlugin().clone();
//			try { mlaPlugin.train(featureTable, trainingResult.getName(), trainingResult.getSegmenterPlugin(), trainingResult.getFold()); }
//			catch (Exception ex) { throw new RuntimeException(ex); }
//            return null;
//		}
//	}
	
	
//	private class FeatureAnalysisTask extends OnPanelSwingTask{
//		private TrainingResult trainingResult;
//		
//		public FeatureAnalysisTask(JProgressBar progressBar, TrainingResult trainingResult){
//			this.addProgressBar(progressBar);
//			this.trainingResult = trainingResult;
//		}
//		
//		@Override
//		protected Void doInBackground() throws Exception {
//			analyzeAll(trainingResult, null, null);
//            return null;
//		}
//	}
	
	
	private transient FeatureAnalysisPlugin[] pluginArray = null;
	protected FeatureAnalysisPlugin[] getPluginArray(){
		if(this.pluginArray==null){
			pluginArray = Workbench.current.pluginManager.getPluginCollectionByType(FeatureAnalysisPlugin.type).toArray(new FeatureAnalysisPlugin[0]);
		}
		return pluginArray;
	}
//	protected void analyzeAll(TrainingResult trainingResult, Map<String,Object> optionMap){
//		FeatureAnalysisPlugin[] pluginArray = this.getPluginArray();
//		
//		String[] tabNameArray = new String[pluginArray.length];
//		Component[] componentArray = new Component[pluginArray.length];
//		
//		for(int i=0; i<pluginArray.length; i++){
//			FeatureAnalysisPlugin plugin = pluginArray[i];
//			tabNameArray[i] = plugin.getTitleString();
//			plugin.setOptionMap(this.optionMap);
//			componentArray[i] = plugin.refreshPanel();
//			plugin.setConfigPanel(this);
//		}
//		SwingToolkit.reloadTabbedPaneByTabName(rightTabbedPane, tabNameArray, componentArray, true);
//	}
	
	public static class AnalysisTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;

		private static String[] columnNames = {"remove?", "feature", "correct value", "incorrect value"};
        
        private boolean[] checked;
        private FeatureTableKey[] featureTableKeyArray;
        private double[] valueOfIncorrect;
		private double[] valueOfCorrect;
		
		public FeatureTableKey[] getCheckedFeatureTableKey(){
			return CollectionsToolkit.getItemListOfValue(featureTableKeyArray, checked, true).toArray(new FeatureTableKey[0]);
		}
		
		
        public AnalysisTableModel(List<FeatureTableKey> featureTableKeyList, double[] valueOfIncorrect, double[] valueOfCorrect){
        	YeriDebug.ASSERT_compareInteger(featureTableKeyList.size(), valueOfCorrect.length);
        	YeriDebug.ASSERT_compareInteger(valueOfIncorrect.length, valueOfCorrect.length);
        	
        	this.checked = new boolean[valueOfIncorrect.length];
        	Arrays.fill(this.checked, false);
        	
        	this.featureTableKeyArray = featureTableKeyList.toArray(new FeatureTableKey[0]);
        	this.valueOfIncorrect = valueOfIncorrect;
        	this.valueOfCorrect = valueOfCorrect;
        }

        public int getColumnCount() { return columnNames.length; }
        public int getRowCount() { return valueOfCorrect.length; }
        public String getColumnName(int col) { return columnNames[col]; }

        public Object getValueAt(int row, int col) {
        	if(col==0){ return checked[row]; }
        	else if(col==1){ return featureTableKeyArray[row].getFeatureIdentifyingString(); }
        	else if(col==2){ return valueOfIncorrect[row]; }
        	else if(col==3){ return valueOfCorrect[row]; }
        	else{ throw new UnsupportedOperationException(); }
        }

        public Class<?> getColumnClass(int c) { return getValueAt(0, c).getClass(); }
        public boolean isCellEditable(int row, int col) { return col==0; }

        public void setValueAt(Object value, int row, int col) {
        	if(col==0){ checked[row] = (Boolean)value; }
        	else{ throw new UnsupportedOperationException(); }
            fireTableCellUpdated(row, col);
        }
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		test01();
	}
	protected static void test01() throws FileNotFoundException, IOException{
		SIDEToolkit.FileType.loadAll();
		
		FeatureAnalyzerConfigPanel featureAnalyzerConfigPanel = new FeatureAnalyzerConfigPanel();
//		List<PluginWrapper> list = Workbench.current.pluginManager.getPluginWrapperCollectionByType(MLAPlugin.type);
		Iterator<TrainingResult> iterator = Workbench.current.trainingResultListManager.iterator();
		
		featureAnalyzerConfigPanel.optionMap.put("training_result_option_key", iterator.next());
		featureAnalyzerConfigPanel.memoryToUIAll();
		
		TestFrame testFrame = new TestFrame(featureAnalyzerConfigPanel);
		testFrame.setSize(new Dimension(1200, 900));

		TrainingResult trainingResult = Workbench.current.trainingResultListManager.iterator().next();
		File predictionFile = new File(SIDEToolkit.predictionResultFolder, "test.xml");
		PredictionResult predictionResult;
		if(predictionFile.exists()){
			Element root = XMLBoss.XMLFromFile(predictionFile).getDocumentElement();
			predictionResult = PredictionResult.create(root);
//			trainingResult.setSelfPredictionResult(predictionResult);
		}else{
			predictionFile.createNewFile();

			predictionResult = trainingResult.getSelfPredictionResult();
			FileToolkit.writeTo(predictionFile, predictionResult.toXML());
		}
		
//		Iterator<TrainingResult> trainingResultIterator = Workbench.current.trainingResultListManager.iterator();
//		if(trainingResultIterator.hasNext()){
//			featureAnalyzerConfigPanel.optionMap.put(FeatureAnalysisToolkit.trainingResultOptionKey, trainingResultIterator.next()); }
//		featureAnalyzerConfigPanel.rightTabbedPane.setSelectedIndex(3);
//		featureAnalyzerConfigPanel.featureComboBox.setSelectedIndex(1);
//		featureAnalyzerConfigPanel.featureComboBox.setSelectedIndex(anIndex)
		
		testFrame.showFrame();
	}
	
//	private TrainingResultManagerPanel trainingResultManagerPanel;
//	private JComboBox trainingResultComboBox;
//	private JComboBox featureComboBox;
//	private JComboBox annotationComboBox;
	
	private JTabbedPane tabbedPane; 
//	private JPanel analysisResultPanel;
//	private JButton analyzeButton;
//	private JProgressBar analyzeProgressBar;
//	private JProgressBar retrainProgressBar;
	
//	private JButton retrainButton;
	

	
}
