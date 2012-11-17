package edu.cmu.side.genesis.view.extract;

import javax.swing.JLabel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class ExtractTableControlPanel extends AbstractListPanel{

	public ExtractTableControlPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Highlighted Feature Table"));
		add("left", delete);
		add("br hfill", combo);
		add("br hfill vfill", describeScroll);
	}
	
	public void refreshPanel(){
		if(combo.getItemCount() != ExtractFeaturesControl.numFeatureTables()){
			GenesisWorkbench.reloadComboBoxContent(combo, ExtractFeaturesControl.getFeatureTables(), ExtractFeaturesControl.getHighlightedFeatureTableRecipe());
		}
		if(ExtractFeaturesControl.hasHighlightedFeatureTable()){
			description.setText(ExtractFeaturesControl.getHighlightedFeatureTableRecipe().getFeatureTable().getDescriptionString());
		}
	}
}
