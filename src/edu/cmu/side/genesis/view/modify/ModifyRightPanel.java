package edu.cmu.side.genesis.view.modify;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ModifyRightPanel extends JSplitPane {

	private ModifyChecklistPanel checklist = new ModifyChecklistPanel();
	private ModifyConfigPanel config = new ModifyConfigPanel();

	public ModifyRightPanel(){
		setLeftComponent(checklist);
		setRightComponent(config);
		checklist.setPreferredSize(new Dimension(300, 450));
		config.setPreferredSize(new Dimension(325,450));

	}
	
	public void refreshPanel(){
		checklist.refreshPanel();
		config.refreshPanel();
	}
}
