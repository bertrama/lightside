package edu.cmu.side.genesis.view.extract;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class ExtractTableControlPanel extends AbstractListPanel{

	public ExtractTableControlPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Highlighted Feature Table"));
		add("left", delete);
		add("br hfill", combo);
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					GenesisRecipe r = (GenesisRecipe)combo.getSelectedItem();
					ExtractFeaturesControl.setHighlightedFeatureTableRecipe(r);
				}
				GenesisWorkbench.update();
			}
		});
		add("br hfill vfill", describeScroll);
	}
	
	public void refreshPanel(){
		System.out.println(combo.getItemCount() + " combo " + ExtractFeaturesControl.numFeatureTables() + " feature tables ETCP21");
		if(combo.getItemCount() != ExtractFeaturesControl.numFeatureTables()){
			GenesisWorkbench.reloadComboBoxContent(combo, ExtractFeaturesControl.getFeatureTables(), ExtractFeaturesControl.getHighlightedFeatureTableRecipe());
			
		}
		if(ExtractFeaturesControl.hasHighlightedFeatureTable()){
			description.setText(ExtractFeaturesControl.getHighlightedFeatureTableRecipe().getFeatureTable().getDescriptionString());
		}
	}
}
