package edu.cmu.side.genesis.view.modify;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.view.generic.CheckBoxList;
import edu.cmu.side.genesis.view.generic.CheckBoxListEntry;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;

public class ModifyLoadPanel extends AbstractListPanel {

	public ModifyLoadPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Selected Feature Table:"));
		add("left", delete);
		add("br hfill", combo);
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					GenesisRecipe r = (GenesisRecipe)combo.getSelectedItem();
					ModifyFeaturesControl.setHighlightedFeatureTableRecipe(r);
				}
				GenesisWorkbench.update();
			}
		});
		add("br hfill vfill", describeScroll);
	}
	
	public void refreshPanel(){
		if(combo.getItemCount() != ModifyFeaturesControl.numFeatureTables()){
			GenesisWorkbench.reloadComboBoxContent(combo, ModifyFeaturesControl.getFeatureTables(), ModifyFeaturesControl.getHighlightedFeatureTableRecipe());
		}
		if(ModifyFeaturesControl.hasHighlightedFeatureTable()){
			description.setText(ModifyFeaturesControl.getHighlightedFeatureTableRecipe().getFeatureTable().getDescriptionString());
			combo.setSelectedItem(ModifyFeaturesControl.getHighlightedFeatureTableRecipe());
		}
	}
}
