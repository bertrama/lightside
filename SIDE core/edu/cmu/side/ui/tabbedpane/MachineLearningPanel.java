package edu.cmu.side.ui.tabbedpane;

import java.awt.Dimension;

import javax.swing.JTabbedPane;

import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.ui.configpanel.DocumentReaderConfigPanel;
import edu.cmu.side.ui.configpanel.FeatureAnalyzerConfigPanel;
import edu.cmu.side.ui.configpanel.FeatureTableConfigPanel;
import edu.cmu.side.ui.configpanel.MLAPluginConfigPanel;
import edu.cmu.side.ui.configpanel.TrainingResultVisualizationConfigPanel;
import edu.cmu.side.viewer.AnnotationEditor;

public class MachineLearningPanel extends JTabbedPane{
	private static final long serialVersionUID = 1L;
	
	public MachineLearningPanel(){
		super();
		yeriInit();
	}

	private void yeriInit(){
		documentReaderConfigPanel = new DocumentReaderConfigPanel();
		featureTableConfigPanel = new FeatureTableConfigPanel();
		mlaPluginConfigPanel = new MLAPluginConfigPanel();
		featureAnalyzerConfigPanel = new FeatureAnalyzerConfigPanel();
		annotationEditor = new AnnotationEditor();
		summaryPanel = new SummaryBuilder();
		this.addTab("Convert Data", documentReaderConfigPanel);
		this.addTab("Extract Features", featureTableConfigPanel);
		this.addTab("Build Model", mlaPluginConfigPanel);
		this.addTab("Analyze Features", featureAnalyzerConfigPanel);
		this.addTab("Annotate Data", annotationEditor);
		this.addTab("Summarize Data", summaryPanel);
	}
	
	private DocumentReaderConfigPanel documentReaderConfigPanel;
	private FeatureTableConfigPanel featureTableConfigPanel;
	private MLAPluginConfigPanel mlaPluginConfigPanel;
	private FeatureAnalyzerConfigPanel featureAnalyzerConfigPanel;
	private AnnotationEditor annotationEditor;
	private SummaryBuilder summaryPanel;
	
	
	protected void printTrainError(String message){
		AlertDialog.show("Error!", message, this);
	}
	
}
