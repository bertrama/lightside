package edu.cmu.side.genesis.view.build;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.genesis.view.generic.GenericTripleFrame;

public class BuildModelPane extends JPanel{

	private static GenericTripleFrame top;
	private static BuildActionPanel action = new BuildActionPanel();
	private static BuildBottomPanel bottom = new BuildBottomPanel();

	public BuildModelPane(){
		setLayout(new BorderLayout());
		top = new GenericTripleFrame(new BuildLoadPanel(), new BuildTestingPanel(), new BuildPluginPanel());
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
