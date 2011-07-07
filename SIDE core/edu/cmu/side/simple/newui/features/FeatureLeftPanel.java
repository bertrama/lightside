package edu.cmu.side.simple.newui.features;

import java.awt.Dimension;
import javax.swing.JPanel;
import se.datadosen.component.RiverLayout;

/**
 * Stores the list panels on the left hand side of the feature extraction UI.
 */
public class FeatureLeftPanel extends JPanel{
	private static final long serialVersionUID = -4501647608770871857L;
	
	public FeatureFileManagerPanel filePanel = new FeatureFileManagerPanel();
	public FeaturePluginPanel pluginPanel = new FeaturePluginPanel();
	public FeatureTableListPanel listPanel = new FeatureTableListPanel();
	
	public FeatureLeftPanel(FeatureExtractionPanel p){
		setPreferredSize(new Dimension(300,650));
		setLayout(new RiverLayout());
		add("left", filePanel);
		add("br left", pluginPanel);
		add("br left", listPanel);
		
	}
	
	public void refreshPanel(){
		filePanel.refreshPanel();
		pluginPanel.refreshPanel();
		listPanel.refreshPanel();
		repaint();
	}
}
