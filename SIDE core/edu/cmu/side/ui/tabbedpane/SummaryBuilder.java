package edu.cmu.side.ui.tabbedpane;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.ui.configpanel.SummaryConfigPanel;
import edu.cmu.side.ui.configpanel.TextRecipeConfigPanel;

public class SummaryBuilder extends JPanel{
	private static final long serialVersionUID = 1L;
	
	public SummaryBuilder(){
		yeriInit();
	}
	
	private void yeriInit(){
		this.setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		this.add(tabbedPane);
		
		SummaryConfigPanel summaryConfigPanel = new SummaryConfigPanel();
		TextRecipeConfigPanel textRecipeConfigPanel = new TextRecipeConfigPanel();
		tabbedPane.addTab("text recipe", textRecipeConfigPanel);
		tabbedPane.addTab("summary", summaryConfigPanel);
		
		
		// visualization
//		JTabbedPane visualizationTabbedPane = new JTabbedPane();
//		
////		String[] tabTitleArray = new String[]{
////				"interactive",
////				"pie",
////		};
//		
//		List<PluginWrapper> pluginWrapperList = Workbench.current.pluginManager.getPluginWrapperCollectionByType(VisualizationPlugin.type);
////		VisualConfigPanel[] vcpArray = new VisualConfigPanel[]{
////				new InteractiveVisualization(),
////				new PieChartVisualization(),
////		};
//		
//		for(int i=0; i<pluginWrapperList.size(); i++){
//			VisualizationPlugin plugin = (VisualizationPlugin)pluginWrapperList.get(i).getSIDEPlugin();
////			dlcp.addActionListner(new DocumentListListenerForVisualConfigPanel(vcp));
////			visualizationTabbedPane.addTab(plugin.getClass().getName(), plugin.buildErrorAnalysisPanel(documentList, predictionResult));
//		}
//		tabbedPane.addTab("feature analyzer", new FeatureAnalyzerConfigPanel());
//		tabbedPane.addTab("visualization", new TrainingResultVisualizationConfigPanel());
	}
	
//	private static class DocumentListListenerForVisualConfigPanel implements ActionListener{
//		private VisualConfigPanel vcp;
//		public DocumentListListenerForVisualConfigPanel(VisualConfigPanel vcp){
//			this.vcp = vcp;
//		}
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			DocumentListConfigPanel dlcp = (DocumentListConfigPanel)e.getSource();
//			vcp.setDocumentList(dlcp.getDocumentList());
//		}
//		
//	}
	
	public static void main(String[] args){
		test01();
	}
	
	private static void test01(){
		SIDEToolkit.FileType.loadAll();
		
		SummaryBuilder summaryBuilder = new SummaryBuilder();
		
		TestFrame testFrame = new TestFrame(summaryBuilder);
		testFrame.setSize(new Dimension(600,800));
		testFrame.showFrame();
	}
	private JTabbedPane tabbedPane;
}
