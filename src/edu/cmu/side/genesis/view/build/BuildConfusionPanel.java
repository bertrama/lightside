package edu.cmu.side.genesis.view.build;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.view.generic.ConfusionMatrixPanel;
import edu.cmu.side.simple.newui.SIDETable;

public class BuildConfusionPanel extends JPanel {

	ConfusionMatrixPanel panel = new ConfusionMatrixPanel();
	
	public BuildConfusionPanel(){
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, panel);
	}

	public void refreshPanel(){
		if(BuildModelControl.hasHighlightedTrainedModelRecipe()){			
			panel.refreshPanel(BuildModelControl.getHighlightedTrainedModelRecipe().getTrainingResult().getConfusionMatrix());
		}
	}
}
