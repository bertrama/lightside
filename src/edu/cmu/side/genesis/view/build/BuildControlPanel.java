package edu.cmu.side.genesis.view.build;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class BuildControlPanel extends AbstractListPanel {

	public BuildControlPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Highlighted Trained Model:"));
		add("left", delete);
		add("br hfill", combo);
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					GenesisRecipe r = (GenesisRecipe)combo.getSelectedItem();
					BuildModelControl.setHighlightedTrainedModelRecipe(r);
				}
				GenesisWorkbench.update();
			}
		});

		add("br hfill vfill", describeScroll);

	}

	public void refreshPanel(){
		if(combo.getItemCount() != BuildModelControl.numTrainedModels()){
			GenesisWorkbench.reloadComboBoxContent(combo, BuildModelControl.getTrainedModels(), BuildModelControl.getHighlightedTrainedModelRecipe());
		}
		if(BuildModelControl.hasHighlightedTrainedModelRecipe()){
			description.setText(BuildModelControl.getHighlightedTrainedModelRecipe().getTrainingResult().getDescriptionString());
			combo.setSelectedItem(BuildModelControl.getHighlightedTrainedModelRecipe());
		}

	}
}
