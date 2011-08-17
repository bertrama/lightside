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
		addTab("Configure Plugin", configPanel);
		addTab("Feature Lab", labPanel);
	}
	
	public void refreshPanel(){
		configPanel.refreshPanel();
		int start = labPanel.getVisibleFeatures();
		labPanel.refreshPanel();
		int end = labPanel.getVisibleFeatures();
		if(start != end){
			setSelectedIndex(1);
		}
		repaint();
	}

	private FeaturePluginConfigPanel configPanel;
	private FeatureLabPanel labPanel;
}
