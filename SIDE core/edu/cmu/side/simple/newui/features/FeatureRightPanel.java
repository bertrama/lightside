package edu.cmu.side.simple.newui.features;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class FeatureRightPanel extends JPanel{
	private static final long serialVersionUID = -1567608872469676950L;

	private FeatureTablePanel tablePanel = new FeatureTablePanel();
	private FeatureZoomPanel zoomPanel = new FeatureZoomPanel();
	
	public FeatureRightPanel(FeatureExtractionPanel p){
		setPreferredSize(new Dimension(800,725));
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JScrollPane top = new JScrollPane(tablePanel);
		top.setPreferredSize(new Dimension(750, 400));
		
		JScrollPane bottom = new JScrollPane(zoomPanel);
		bottom.setPreferredSize(new Dimension(750, 300));
		split.setTopComponent(top);
		split.setBottomComponent(bottom);
		
		add(split);
	}
	
	public void refreshPanel(){
		tablePanel.refreshPanel();
		zoomPanel.refreshPanel();
		repaint();
	}
}
