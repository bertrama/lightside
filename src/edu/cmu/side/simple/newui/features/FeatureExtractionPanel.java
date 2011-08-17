package edu.cmu.side.simple.newui.features;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import se.datadosen.component.RiverLayout;

public class FeatureExtractionPanel extends JPanel{
	private static final long serialVersionUID = -6256375665492797520L;

	/** Configuration panel for loading files, choosing plugins, and pulling up feature tables for analysis. */
	static FeatureLeftPanel left;
	
	/** Analysis panel for feature tables and feature construction */
	static FeatureRightPanel right;
	
	
	/** Construct the split pane and scroll panes for everything to sit in. */
	public FeatureExtractionPanel(){
		left = new FeatureLeftPanel(this);
		right = new FeatureRightPanel(this);
		JSplitPane split = new JSplitPane();
		setLayout(new RiverLayout());
		split.setLeftComponent(left);
		split.setRightComponent(right);
		add("hfill vfill", split);
	}

	public void refreshPanel() {
		left.refreshPanel();
		right.refreshPanel();
	}
}
