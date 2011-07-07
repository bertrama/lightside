package edu.cmu.side.simple.newui.features;

import javax.swing.JTabbedPane;

/**
 * Contains the panels in the bottom right side of the features window.
 * @author emayfiel
 *
 */
public class FeatureZoomPanel extends JTabbedPane{
	private static final long serialVersionUID = 38888773979094143L;

	public FeatureZoomPanel(){
		configPanel = new FeaturePluginConfigPanel();
		labPanel = new FeatureLabPanel();
		evalPanel = new FeatureEvaluationPanel();
		
		addTab("Configure Plugin", configPanel);
		addTab("Feature Lab", labPanel);
		addTab("Evaluate Feature Table", evalPanel);
	}
	
	public void refreshPanel(){
		configPanel.refreshPanel();
		labPanel.refreshPanel();
		evalPanel.refreshPanel();
		repaint();
	}

	private FeaturePluginConfigPanel configPanel;
	private FeatureLabPanel labPanel;
	private FeatureEvaluationPanel evalPanel;
}
