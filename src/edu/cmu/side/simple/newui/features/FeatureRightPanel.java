package edu.cmu.side.simple.newui.features;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import se.datadosen.component.RiverLayout;

public class FeatureRightPanel extends JPanel{
	private static final long serialVersionUID = -1567608872469676950L;

	private FeatureTablePanel tablePanel = new FeatureTablePanel();
	private FeatureZoomPanel zoomPanel = new FeatureZoomPanel();
	
	public FeatureRightPanel(FeatureExtractionPanel p){
		setBorder(null);
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JScrollPane top = new JScrollPane(tablePanel);
		split.setBorder(null);
		tablePanel.setBorder(null);
		zoomPanel.setBorder(null);		
		JScrollPane bottom = new JScrollPane(zoomPanel);
		split.setTopComponent(top);
		split.setBottomComponent(bottom);
		setLayout(new RiverLayout());
		add("hfill", split);
	}
	
	public void refreshPanel(){
		tablePanel.refreshPanel();
		zoomPanel.refreshPanel();
		repaint();
	}
}
