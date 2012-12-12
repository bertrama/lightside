package edu.cmu.side.genesis.view.build;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class BuildLoadPanel extends AbstractListPanel {

	public BuildLoadPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Selected Feature Table:"));
		add("left", delete);
		add("br hfill", combo);
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					GenesisRecipe r = (GenesisRecipe)combo.getSelectedItem();
					BuildModelControl.setHighlightedFeatureTableRecipe(r);
				}
				GenesisWorkbench.update();
			}
		});
		add("br hfill vfill", describeScroll);
	}
	
	public void refreshPanel(){
		if(combo.getItemCount() != (BuildModelControl.numFeatureTables() + BuildModelControl.numFilterTables())){
			Collection<GenesisRecipe> tableRecipes = BuildModelControl.getFeatureTables();
			tableRecipes.addAll(BuildModelControl.getFilterTables());
			GenesisWorkbench.reloadComboBoxContent(combo,tableRecipes, BuildModelControl.getHighlightedFeatureTableRecipe());
		}
		if(BuildModelControl.hasHighlightedFeatureTableRecipe()){
			GenesisRecipe highlight = BuildModelControl.getHighlightedFeatureTableRecipe();
			combo.setSelectedItem(highlight);				
			FeatureTable display = highlight.getFilteredTable()==null?highlight.getFeatureTable():highlight.getFilteredTable();
			if(display != null){
				description.setText(display.getDescriptionString());
			}else{
				description.setText("");
			}
		}
	}
}
