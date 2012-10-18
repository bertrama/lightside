package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Dimension;

import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

/**
 * Holds all the configuration option panels for doing machine learning.
 * @author emayfiel
 *
 */
public class LearningLeftPanel extends JPanel {
	private static final long serialVersionUID = -3562041875235989332L;
	public LearningPluginPanel pluginPanel = new LearningPluginPanel();
	public LearningConfigPanel configPanel = new LearningConfigPanel();
	public ModelListPanel listPanel = new ModelListPanel();
	
	public LearningLeftPanel(){
		setLayout(new RiverLayout());
		setPreferredSize(new Dimension(300,500));
		pluginPanel.setPreferredSize(new Dimension(280, 120));
		add("left", pluginPanel);
		configPanel.setPreferredSize(new Dimension(280, 300));
		add("br left", configPanel);
		add("br left", listPanel);
	}
	

	public void refreshPanel(){
		pluginPanel.refreshPanel();
		configPanel.refreshPanel();
		listPanel.refreshPanel();
		repaint();
	}
}
