package edu.cmu.side.genesis.view.build;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class BuildResultPanel extends AbstractListPanel {

	public BuildResultPanel(){
		setLayout(new RiverLayout());
		add("hfill", combo);
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null && BuildModelControl.getHighlightedTrainedModelRecipe() != null){
					description.setText(BuildModelControl.getHighlightedTrainedModelRecipe().getTrainingResult().getEvaluations().get(combo.getSelectedItem()));
				}
			}
		});
		add("br hfill vfill", describeScroll);
	}
	
	public void refreshPanel(){
		if(BuildModelControl.hasHighlightedTrainedModelRecipe()){
			GenesisWorkbench.reloadComboBoxContent(combo, BuildModelControl.getHighlightedTrainedModelRecipe().getTrainingResult().getEvaluations().keySet(), "summary");
		}
	}
}
