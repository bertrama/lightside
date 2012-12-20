package edu.cmu.side.genesis.view.build;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.view.ActionBar;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;

public class BuildActionPanel extends ActionBar {

	public BuildActionPanel(){
		add.setText("Train");
		add.addActionListener(new BuildModelControl.TrainModelListener(progressBar));
		name.setText("model");
		name.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ExtractFeaturesControl.setNewName(name.getText());
			}
		});
		JPanel updaterPanel = new JPanel(new BorderLayout());
		updaterPanel.setPreferredSize(new Dimension(400,30));
		updaterPanel.add(BorderLayout.CENTER, (SwingUpdaterLabel)BuildModelControl.getUpdater());
		updaters.add("right", updaterPanel);
	}

	public void refreshPanel(){
		add.setEnabled(BuildModelControl.hasHighlightedFeatureTableRecipe());
	}
}
