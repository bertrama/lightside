package edu.cmu.side.genesis.view.build;

import javax.swing.JPanel;

import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.view.generic.ActionBar;
import edu.cmu.side.genesis.view.generic.SwingUpdaterLabel;

public class BuildActionPanel extends ActionBar {

	public BuildActionPanel(){
		add.setText("Train");
		add.addActionListener(new BuildModelControl.TrainModelListener(progressBar));
		name.setText("model");
		updaters.add("left", (SwingUpdaterLabel)BuildModelControl.getUpdater());
	}
	
	public void refreshPanel(){
		add.setEnabled(BuildModelControl.hasHighlightedFeatureTableRecipe());
	}
}
