package edu.cmu.side.simple.newui.prediction;

import java.awt.Dimension;

import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

public class PredictionLeftPanel extends JPanel{

	PredictionFileSelectPanel filePanel;
	PredictionConfigPanel configPanel;

	public PredictionLeftPanel(PredictionPanel p){
		setPreferredSize(new Dimension(300,600));
		setLayout(new RiverLayout());
		filePanel = new PredictionFileSelectPanel();
		configPanel = new PredictionConfigPanel();
		add("left", filePanel);
		add("br left", configPanel);
		
	}
	
	public void refreshPanel(){
		filePanel.refreshPanel();
		configPanel.refreshPanel();
		repaint();
	}
}
