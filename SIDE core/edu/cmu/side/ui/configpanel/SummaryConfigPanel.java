/*
 * MLAPluginConfigPanel.java
 *
 * Created on __DATE__, __TIME__
 */

package edu.cmu.side.ui.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.apache.uima.jcas.JCas;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;
import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.Recipe;
import edu.cmu.side.dataitem.Summary;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.VisualizationToolkit.JCasListVisualizationPlugin;
import edu.cmu.side.ui.managerpanel.RecipeManagerPanel;
import edu.cmu.side.ui.managerpanel.SummaryManagerPanel;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;


/**
 * 
 * @author __USER__
 */
public class SummaryConfigPanel extends javax.swing.JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;

	/** Creates new form MLAPluginConfigPanel */
	public SummaryConfigPanel() {
		yeriInit();
	}
	
	private void createSummary(){
		boolean valid = true;
		StringBuilder builder = new StringBuilder();

		Object[] objectArray = recipeManagerPanel.getSelectedItems();
		if(objectArray.length==0){
			valid = false;
			builder.append("No recipe!");
		}
		
		String summaryName = summaryNameTextField.getText().trim();
		if(summaryName.length()==0){
			valid = false;
			builder.append("Please type in summary name");
		}
		
		if(!valid){
			AlertDialog.show("Error!", builder, this);
			return;
		}
		
		Recipe[] recipeArray = new Recipe[objectArray.length];
		for(int i=0; i<objectArray.length; i++){
			recipeArray[i] = (Recipe)objectArray[i];
		}
		
		Summary summary = new Summary(recipeArray);
		summary.setName(summaryName);
		Workbench.current.summaryListManager.add(summary);
	}
	
	private class SummarizeTask extends OnPanelSwingTask{
		public SummarizeTask(JProgressBar progressBar){
			this.addProgressBar(progressBar);
		}

		@Override
		protected Void doInBackground() {
			boolean runnable = true;
			List<CharSequence> commentList = new ArrayList<CharSequence>();
			
			Summary summary = summaryManagerPanel.getSelectedItem();
			DocumentList documentList = documentListConfigPanel.getDocumentList();
			
			runnable = YeriDebug.updateValidity(runnable, documentList!=null, commentList, "no target file selected");
			runnable = YeriDebug.updateValidity(runnable, summary!=null, commentList, "no summary selected");
			if(!runnable){
				AlertDialog.show("error", commentList, SummaryConfigPanel.this);
				return null;
			}
			
			Component c = summary.summarize(documentList);
			SwingToolkit.attachAndSetComponentVisible(desktopPane, summary.getDisplayText(), c);
			return null;
		}
	}
	private class VisualizationTask extends OnPanelSwingTask{
		public VisualizationTask(JProgressBar progressBar){
			this.addProgressBar(progressBar);
		}

		@Override
		protected Void doInBackground() throws Exception {
			boolean runnable = true;
			List<CharSequence> commentList = new ArrayList<CharSequence>();
			
			JCasListVisualizationPlugin visuPlugin = (JCasListVisualizationPlugin)visualizationComboBox.getSelectedItem();
			DocumentList documentList = documentListConfigPanel.getDocumentList();
			Summary summary = summaryManagerPanel.getSelectedItem();
			
			runnable = YeriDebug.updateValidity(runnable, documentList!=null, commentList, "no target file selected");
			runnable = YeriDebug.updateValidity(runnable, visuPlugin!=null, commentList, "no visualization plugin selected");
			runnable = YeriDebug.updateValidity(runnable, summary!=null, commentList, "no summary selected");

			if(!runnable){
				AlertDialog.show("error", commentList, SummaryConfigPanel.this);
				return null;
			}
			
			visuPlugin.setColumnNameMap(summary.createColumnNameMap(documentList));
//			visuPlugin.setVisibleSubtypeSet(renderer.getMap().keySet());
			
			Component c = visuPlugin.buildVisualizationPanel(documentList.getJCasList().toArray(new JCas[0]));
			SwingToolkit.attachAndSetComponentVisible(desktopPane, visuPlugin.getClass().getName(), c);
			
			return null;
		}
	}
	
	private void yeriInit() {
		
		JPanel leftPanel = new JPanel();
		
		leftPanel.setLayout(new RiverLayout());
		
		// filePanel
		documentListConfigPanel = new DocumentListConfigPanel();
		documentListConfigPanel.setDocumentListType(DocumentListConfigPanel.DocumentListType.BASE);
		leftPanel.add("br hfill", documentListConfigPanel);
		documentListConfigPanel.setNullSubtypeEnabled(true);
		
		leftPanel.add("br hfill", new JSeparator());
		
		// recipeManagerPanel
		recipeManagerPanel = new RecipeManagerPanel();
		recipeManagerPanel.addActionListener(this);
		leftPanel.add("br hfill", recipeManagerPanel);
		
		// summary button
		
		summaryNameTextField = new JTextField();
		leftPanel.add("p", new JLabel("summary name:"));
		leftPanel.add("hfill", summaryNameTextField);
		
		createSummaryButton = new JButton("create summary object");
		createSummaryButton.setMnemonic(KeyEvent.VK_C);
		createSummaryButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				createSummary();
			}
		});
		
		leftPanel.add("p hfill", createSummaryButton);
		
		summaryManagerPanel = new SummaryManagerPanel();
		leftPanel.add("p hfill", summaryManagerPanel);
		
		
		leftPanel.add("br hfill", new JSeparator());
		
		summarizeButton = new JButton("summarize");
		summarizeButton.setMnemonic(KeyEvent.VK_S);
		leftPanel.add("br hfill", summarizeButton);
		summarizeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				(new SummarizeTask(summarizationProgressBar)).execute();
			}
		});
		summarizationProgressBar = new JProgressBar();
		leftPanel.add("br hfill", summarizationProgressBar);
		
//		GridLayout visualizePanelLayout = new GridLayout(3,1);
//		visualizePanelLayout.setVgap(1);
//		JPanel visualizePanel = new JPanel(visualizePanelLayout);
		
		SIDEPlugin[] visuPluginArray = Workbench.current.pluginManager.getPluginCollectionByType(JCasListVisualizationPlugin.type).toArray(new SIDEPlugin[0]);
		visualizationComboBox = SwingToolkit.createSimpleClassNameComboBox(visuPluginArray);
		visualizeButton = new JButton("visualize");
		visualizeButton.setMnemonic(KeyEvent.VK_V);
		visualizeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				(new VisualizationTask(visualizationProgressBar)).execute();
			}
		});
		
		
		leftPanel.add("p hfill", new JLabel("visualization:"));
		leftPanel.add("br hfill", visualizationComboBox);
		leftPanel.add("br hfill", visualizeButton);
		
		visualizationProgressBar = new JProgressBar();
		leftPanel.add("br hfill", visualizationProgressBar);
		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(400);
		
		JScrollPane leftScrollPane = new JScrollPane(leftPanel);
		SwingToolkit.adjustScrollBar(leftScrollPane, JScrollBar.VERTICAL);
		splitPane.setLeftComponent(leftScrollPane);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new RiverLayout());
		
		
		desktopPane = new JDesktopPane();
		rightPanel.add("br hfill", new JLabel("results:"));
		rightPanel.add("br hfill vfill", new JScrollPane(desktopPane));
		
		splitPane.setRightComponent(rightPanel);
		
		this.setLayout(new BorderLayout());
		this.add(splitPane);
		
		this.refreshPanel();
	}
	
	public void refreshPanel() {
	}
	
	private DocumentListConfigPanel documentListConfigPanel;
	private RecipeManagerPanel recipeManagerPanel;
	
	private JTextField summaryNameTextField;
	private JButton createSummaryButton;
	private SummaryManagerPanel summaryManagerPanel;
	private JButton summarizeButton;
	private JProgressBar summarizationProgressBar;
	private JComboBox visualizationComboBox;
	private JButton visualizeButton;
	private JProgressBar visualizationProgressBar;
	
//	private JTextArea descriptionTextArea;
	private JDesktopPane desktopPane;

	@Override
	public void actionPerformed(ActionEvent e) {
		this.refreshPanel();
	}

	public DocumentListConfigPanel getDocumentListConfigPanel() {
		return documentListConfigPanel;
	}

}