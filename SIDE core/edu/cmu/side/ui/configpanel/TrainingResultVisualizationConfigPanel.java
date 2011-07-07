package edu.cmu.side.ui.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.uima.jcas.JCas;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.RTTIToolkit;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.plugin.PluginWrapper;
import edu.cmu.side.plugin.VisualizationToolkit;
import edu.cmu.side.plugin.VisualizationToolkit.TrainingResultVisualizationPlugin;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.type.SIDEAnnotation;

public class TrainingResultVisualizationConfigPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	public TrainingResultVisualizationConfigPanel(){
		yeriInit();
	}
	
	private void yeriInit(){
		this.setLayout(new BorderLayout());
		
		JSplitPane splitPane = new JSplitPane();
		this.add(splitPane, BorderLayout.CENTER);
		
		JPanel leftPanel = new JPanel();
		splitPane.setLeftComponent(leftPanel);
		leftPanel.setLayout(new RiverLayout());
		
		trainingResultComboBox = Workbench.current.trainingResultListManager.createComboBox();
		leftPanel.add("br", Workbench.current.trainingResultListManager.createImageIconLabel());
		leftPanel.add(" hfill", trainingResultComboBox);
		
//		trainingResultManagerPanel = new TrainingResultManagerPanel();
//		leftPanel.add("br hfill", trainingResultManagerPanel);
		
		analyzeButton = new JButton("visualize");
		leftPanel.add("p hfill", analyzeButton);
		analyzeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				analyze();
			}
		});
		progressBar = new JProgressBar();
		leftPanel.add("br hfill", progressBar);
		
		rightTabbedPanel = new JTabbedPane();
		splitPane.setRightComponent(rightTabbedPanel);
	}
	
	private class AnalyzeTask extends OnPanelSwingTask{
//		private TrainingResult trainingResult;
		
		public AnalyzeTask(JProgressBar progressBar, TrainingResultInterface trainingResult){
			this.addProgressBar(progressBar);
//			this.trainingResult = trainingResult;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			try{
				rightTabbedPanel.removeAll();
				
				boolean runnable = true;
				List<CharSequence> errorMessageList = new ArrayList<CharSequence>();
				
				TrainingResult trainingResult = (TrainingResult)trainingResultComboBox.getSelectedItem();
				if(trainingResult==null){
					runnable = false;
					errorMessageList.add("Please select a model for visualization.");
				}
	
				List<PluginWrapper> pluginWrapperList = Workbench.current.pluginManager.getPluginWrapperCollectionByType(TrainingResultVisualizationPlugin.type);
				for(PluginWrapper pluginWrapper : pluginWrapperList){
					TrainingResultVisualizationPlugin vp = (TrainingResultVisualizationPlugin)pluginWrapper.getSIDEPlugin();
					Component c = vp.buildVisualizationPanel(trainingResult);
					rightTabbedPanel.addTab(RTTIToolkit.getClassNameWithoutParent(vp), c);
				}
				
				if(!runnable){
					AlertDialog.show("error", errorMessageList, rightTabbedPanel);
					return null;
				}
				
//				TrainingResultVisualizationPlugin[] array = null;
//				try{
//					array = new TrainingResultVisualizationPlugin[]{
//							new InteractiveVisualization(),
//							new PieChartVisualizationPlugin(),
//							new PeriodicVisualizationPlugin(),
//							new PredictionFeatureAnalysisPlugin(),
//							new TestErrorAnalysisPlugin(),
//					};
//				}catch(Throwable t){
//					YeriDebug.die(t);
//				}
				
				UIMAToolkit.removeSIDEAnnotationList(trainingResult.getDocumentList().getJCasList().toArray(new JCas[0]), SIDEAnnotation.type, VisualizationToolkit.visualizationSubtypeName);
				UIMAToolkit.addSelfPredictionAnnotation(trainingResult, VisualizationToolkit.visualizationSubtypeName);
//				for(TrainingResultVisualizationPlugin vp : array){
//					Component c = vp.buildVisualizationPanel(trainingResult);
//					rightTabbedPanel.addTab(RTTIToolkit.getClassNameWithoutParent(vp), c);
//				}
			}catch(Exception ex){
				YeriDebug.ASSERT(ex);
			}
			return null;
		}
	}
	
	private void analyze(){
		AnalyzeTask task = new AnalyzeTask(progressBar, (TrainingResultInterface)trainingResultComboBox.getSelectedItem());
		task.addPropertyChangeListener(task);
		task.execute();
	}
	
//	private TrainingResultManagerPanel trainingResultManagerPanel;
	private JComboBox trainingResultComboBox;
	private JButton analyzeButton;
	private JTabbedPane rightTabbedPanel;
	private JProgressBar progressBar;
	
	
	public static void main(String[] args){
		test01();
	}

	private static void test01() {
		TrainingResultVisualizationConfigPanel eacp = new TrainingResultVisualizationConfigPanel();
		SIDEToolkit.FileType.loadAll();
		
		TestFrame testFrame = new TestFrame(eacp);
		testFrame.setSize(new Dimension(1000,1000));
		testFrame.showFrame();
		
		eacp.trainingResultComboBox.setSelectedIndex(0);
		eacp.analyze();
	}
}
