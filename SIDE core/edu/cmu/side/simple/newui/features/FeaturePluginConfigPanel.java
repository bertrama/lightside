package edu.cmu.side.simple.newui.features;

import java.awt.Component;

import javax.swing.JPanel;

public class FeaturePluginConfigPanel extends JPanel{
	private static final long serialVersionUID = -7483758967397007423L;

	/**
	 * This gets replaced with the plugin's component when it is selected in the left-hand list.
	 */
	Component activeComponent = null;
	
	public FeaturePluginConfigPanel(){
	}
	
	public void refreshPanel(){
		activeComponent = FeaturePluginPanel.getSelectedPluginComponent();
		if(activeComponent != null){
			this.removeAll();
			add(activeComponent);
			this.repaint();
		}
	}

}
