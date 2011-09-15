package edu.cmu.side.simple.newui.prediction;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import se.datadosen.component.RiverLayout;

public class PredictionPanel extends JPanel{

	PredictionLeftPanel left;
	PredictionRightPanel right;
	/** Construct the split pane and scroll panes for everything to sit in. */
	public PredictionPanel(){
		left = new PredictionLeftPanel(this);
		right = new PredictionRightPanel();
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
