package edu.cmu.side.genesis.view.modify;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class ModifyRightPanel extends JSplitPane {

	private ModifyConfigPanel config = new ModifyConfigPanel();
	private ModifyDisplayPanel display = new ModifyDisplayPanel();
	
	public ModifyRightPanel(){
		config.setPreferredSize(new Dimension(300,675));
		display.setPreferredSize(new Dimension(325,675));
		setLeftComponent(config);
		setRightComponent(display);		
	}
	
	public void refreshPanel(){
		config.refreshPanel();
		display.refreshPanel();
	}
}
