package edu.cmu.side.simple.newui;

import java.awt.event.ActionEvent;


import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;

import edu.cmu.side.simple.newui.analysis.FeatureAnalysisPanel;
import edu.cmu.side.simple.newui.features.FeatureExtractionPanel;
import edu.cmu.side.simple.newui.machinelearning.LearningPanel;
import edu.cmu.side.simple.newui.prediction.PredictionPanel;

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
		predictionConfigPanel = new PredictionPanel();
		
		this.addTab("Extract Features", featureTableConfigPanel);
		this.addTab("Build Model", machineLearningConfigPanel);
		this.addTab("Predict Labels", predictionConfigPanel);
	}
	
	public SimpleWorkbenchPanel(){
		init();
	}
	
	private FeatureExtractionPanel featureTableConfigPanel;
	private LearningPanel machineLearningConfigPanel;
	private PredictionPanel predictionConfigPanel;
	@Override
	public void actionPerformed(ActionEvent e) {
		featureTableConfigPanel.refreshPanel();
		machineLearningConfigPanel.refreshPanel();
		predictionConfigPanel.refreshPanel();
	}	
	
}
