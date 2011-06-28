package edu.cmu.side.newui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import edu.cmu.side.ui.configpanel.DocumentListConfigPanel;

import se.datadosen.component.RiverLayout;

/**
 * 
 */
public class FeatureLeftPanel extends JPanel{

	DocumentListConfigPanel filePanel = new DocumentListConfigPanel();
	FeaturePluginPanel pluginPanel = new FeaturePluginPanel();
	FeatureTableListPanel listPanel = new FeatureTableListPanel();
	
	public FeatureLeftPanel(){
		setPreferredSize(new Dimension(300,650));
		setLayout(new RiverLayout());
		add("left", filePanel);
		add("br left", pluginPanel);
		add("br left", listPanel);
	}
}
