package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ExtractTopPanel extends JPanel{

	ExtractLoadPanel control = new ExtractLoadPanel();
	ExtractPluginPanel display = new ExtractPluginPanel();
	
	public ExtractTopPanel(){
		setLayout(new BorderLayout());
		setBackground(Color.black);
		JSplitPane pane = new JSplitPane();
		pane.setLeftComponent(control);
		pane.setRightComponent(display);
		control.setPreferredSize(new Dimension(275, 450));
		display.setPreferredSize(new Dimension(650, 450));
		add(BorderLayout.CENTER, pane);
	}
	public void refreshPanel(){
		control.refreshPanel();
		display.refreshPanel();
	}
}
