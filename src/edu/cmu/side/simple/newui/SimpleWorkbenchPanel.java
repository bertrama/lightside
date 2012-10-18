package edu.cmu.side.simple.newui;

import java.awt.event.ActionEvent;


import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.cmu.side.simple.newui.analysis.FeatureAnalysisPanel;
import edu.cmu.side.simple.newui.features.FeatureExtractionPanel;
import edu.cmu.side.simple.newui.machinelearning.LearningPanel;
import edu.cmu.side.simple.newui.prediction.PredictionFileSelectPanel;
import edu.cmu.side.simple.newui.prediction.PredictionPanel;

/**
 * Holds the three main components of LightSIDE.
 * @author emayfiel
 *
 */
public class SimpleWorkbenchPanel extends JTabbedPane implements ActionListener{
	private static final long serialVersionUID = 3984473901629072916L;

	private boolean warned = false;
	private void init(){
		featureTableConfigPanel = new FeatureExtractionPanel();
		machineLearningConfigPanel = new LearningPanel();
		predictionConfigPanel = new PredictionPanel();
		this.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if(!warned & SimpleWorkbenchPanel.this.getSelectedIndex()==2){
					JOptionPane.showMessageDialog(SimpleWorkbenchPanel.this, "The Predict Labels pane is only for unannotated data. It will give you no performance statistics for evaluating your model.", "Warning", JOptionPane.WARNING_MESSAGE);
					warned = true;
				}
			}
		});
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
