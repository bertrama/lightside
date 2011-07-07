package edu.cmu.side.simple.newui;

import java.awt.event.ActionEvent;


import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;

import edu.cmu.side.simple.newui.analysis.FeatureAnalysisPanel;
import edu.cmu.side.simple.newui.features.FeatureExtractionPanel;
import edu.cmu.side.simple.newui.machinelearning.LearningPanel;

/**
 * Holds the three main components of LightSIDE.
 * @author emayfiel
 *
 */
public class SimpleWorkbenchPanel extends JTabbedPane implements ActionListener{
	private static final long serialVersionUID = 3984473901629072916L;

	private void init(){
		featureTableConfigPanel = new FeatureExtractionPanel();
		machineLearningConfigPanel = new LearningPanel();
		featureAnalyzerConfigPanel = new FeatureAnalysisPanel();
		
		this.addTab("Extract Features", featureTableConfigPanel);
		this.addTab("Build Model", machineLearningConfigPanel);
		this.addTab("Analyze Features", featureAnalyzerConfigPanel);
	}
	
	public SimpleWorkbenchPanel(){
		init();
	}
	
	private FeatureExtractionPanel featureTableConfigPanel;
	private LearningPanel machineLearningConfigPanel;
	private FeatureAnalysisPanel featureAnalyzerConfigPanel;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		featureTableConfigPanel.refreshPanel();
		machineLearningConfigPanel.refreshPanel();
	}	
	
}
