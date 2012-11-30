package edu.cmu.side.genesis.view.modify;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ModifyFeaturesPane extends JPanel{

	private ModifyManagerPanel manage = new ModifyManagerPanel();
	private ModifyRightPanel right = new ModifyRightPanel();
	
	public ModifyFeaturesPane(){
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();
		pane.setLeftComponent(manage);
		pane.setRightComponent(right);
		manage.setPreferredSize(new Dimension(275, 675));
		right.setPreferredSize(new Dimension(650, 675));
		add(BorderLayout.CENTER, pane);
	}
	
	public void refreshPanel(){
		manage.refreshPanel();
		right.refreshPanel();
	}
}
