package edu.cmu.side.genesis.view.modify;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class ModifyControlPanel extends AbstractListPanel{

	public ModifyControlPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Highlighted Filtered Table:"));
		add("left", delete);
		add("br hfill", combo);
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					GenesisRecipe r = (GenesisRecipe)combo.getSelectedItem();
					ModifyFeaturesControl.setHighlightedFilterTableRecipe(r);
				}
				GenesisWorkbench.update();
			}
		});
		
		add("br hfill vfill", describeScroll);

	}
	
	public void refreshPanel(){
		System.out.println(combo.getItemCount() + " combo " + ModifyFeaturesControl.numFilterTables() + " filter tables MDP38");
		if(combo.getItemCount() != ModifyFeaturesControl.numFilterTables()){
			GenesisWorkbench.reloadComboBoxContent(combo, ModifyFeaturesControl.getFilterTables(), ModifyFeaturesControl.getHighlightedFilterTableRecipe());
		}
		if(ModifyFeaturesControl.hasHighlightedFilterTable()){
			description.setText(ModifyFeaturesControl.getHighlightedFilterTableRecipe().getFilteredTable().getDescriptionString());
			combo.setSelectedItem(ModifyFeaturesControl.getHighlightedFilterTableRecipe());
		}
	
	}
}
