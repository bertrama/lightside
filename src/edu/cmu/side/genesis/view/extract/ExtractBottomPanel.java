package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import edu.cmu.side.simple.newui.features.FeatureTableListPanel;
import edu.cmu.side.simple.newui.features.FeatureTablePanel;

public class ExtractBottomPanel extends JPanel{
	
	ExtractTableControlPanel control = new ExtractTableControlPanel();
	ExtractTableDisplayPanel display = new ExtractTableDisplayPanel();
	
	public ExtractBottomPanel(){
		setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(control);
		split.setRightComponent(display);
		control.setPreferredSize(new Dimension(275,200));
		display.setPreferredSize(new Dimension(650,200));
		add(BorderLayout.CENTER, split);
	}
	
	public void refreshPanel(){
		control.refreshPanel();
		display.refreshPanel();
	}
}
