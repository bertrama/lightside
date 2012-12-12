package edu.cmu.side.genesis.view.modify;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.genesis.view.generic.GenericTripleFrame;

public class ModifyFeaturesPane extends JPanel{

	private static GenericTripleFrame top;
	private static ModifyActionPanel action = new ModifyActionPanel();
	private static ModifyBottomPanel bottom = new ModifyBottomPanel();

	public ModifyFeaturesPane(){
		setLayout(new BorderLayout());
		top = new GenericTripleFrame(new ModifyLoadPanel(), new ModifyChecklistPanel(), new ModifyConfigPanel());
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.CENTER, top);
		panel.add(BorderLayout.SOUTH, action);
		pane.setTopComponent(panel);
		pane.setBottomComponent(bottom);
		top.setPreferredSize(new Dimension(950,450));
		bottom.setPreferredSize(new Dimension(950,200));
		add(BorderLayout.CENTER, pane);
	}
	
	public void refreshPanel(){
		top.refreshPanel();
		action.refreshPanel();
		bottom.refreshPanel();
	}
}
