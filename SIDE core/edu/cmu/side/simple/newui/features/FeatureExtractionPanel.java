package edu.cmu.side.simple.newui.features;

import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class FeatureExtractionPanel extends JPanel{
	private static final long serialVersionUID = -6256375665492797520L;

	/** Configuration panel for loading files, choosing plugins, and pulling up feature tables for analysis. */
	static FeatureLeftPanel left;
	
	/** Analysis panel for feature tables and feature construction */
	static FeatureRightPanel right;
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		FeatureExtractionPanel fep = new FeatureExtractionPanel();
		fep.setPreferredSize(new Dimension(1100,700));
		frame.add(fep);
		frame.setSize(1100, 700);
		frame.setVisible(true);	
	}
	
	/** Construct the split pane and scroll panes for everything to sit in. */
	public FeatureExtractionPanel(){
		left = new FeatureLeftPanel(this);
		right = new FeatureRightPanel(this);
		JSplitPane split = new JSplitPane();
		JScrollPane scrollLeft = new JScrollPane(left);
		JScrollPane scrollRight = new JScrollPane(right);
		scrollLeft.setPreferredSize(new Dimension(300,750));
		scrollRight.setPreferredSize(new Dimension(775,750));
		split.setLeftComponent(scrollLeft);
		split.setRightComponent(scrollRight);
		add(split);
	}

	public void refreshPanel() {
		left.refreshPanel();
		right.refreshPanel();
	}
}
