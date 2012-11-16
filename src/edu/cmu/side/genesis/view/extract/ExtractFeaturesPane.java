package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ExtractFeaturesPane extends JPanel{

	static ExtractTopPanel top = new ExtractTopPanel();
	static ExtractBottomPanel bottom = new ExtractBottomPanel();
	
	public ExtractFeaturesPane(){
		setLayout(new BorderLayout());
		setBackground(Color.white);
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		pane.setTopComponent(top);
		pane.setBottomComponent(bottom);
//		this.setPreferredSize(new Dimension(950,675));
		top.setPreferredSize(new Dimension(950,450));
		bottom.setPreferredSize(new Dimension(950,200));
		add(BorderLayout.CENTER, pane);
	}

	public void refreshPanel() {
		top.refreshPanel();
		bottom.refreshPanel();
	}
}
