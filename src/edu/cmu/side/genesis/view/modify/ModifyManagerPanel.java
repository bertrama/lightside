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
import edu.cmu.side.genesis.view.CheckBoxList;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;

public class ModifyManagerPanel extends AbstractListPanel {

	static FastListModel pluginsModel = new FastListModel();
	static CheckBoxList pluginsList = new CheckBoxList();
	JScrollPane pluginsScroll = new JScrollPane(pluginsList);

	public ModifyManagerPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Highlighted Feature Table:"));
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
		
		ArrayList<CheckBoxListEntry> pluginsToPass = new ArrayList<CheckBoxListEntry>();
		Map<FilterPlugin, Boolean> filterPlugins = ModifyFeaturesControl.getFilterPlugins();
		for(FilterPlugin plug : filterPlugins.keySet()){
			CheckBoxListEntry entry = new CheckBoxListEntry(plug, filterPlugins.get(plug));
			entry.addItemListener(new ModifyFeaturesControl.PluginCheckboxListener());
			pluginsToPass.add(entry);
		}
		pluginsModel.addAll(pluginsToPass.toArray(new CheckBoxListEntry[0]));
		pluginsList.setModel(pluginsModel);
		add.addActionListener(new ModifyFeaturesControl.AddFilterListener());
		add("br hfill vfill", describeScroll);
		JPanel pan = new JPanel(new BorderLayout());
		pan.add(BorderLayout.NORTH, new JLabel("Filters Available:"));
		pan.add(BorderLayout.CENTER, pluginsScroll);
		pan.add(BorderLayout.SOUTH, add);
		pan.setPreferredSize(new Dimension(250,250));
		add("br hfill", pan);
	}
	
	public void refreshPanel(){
		System.out.println(combo.getItemCount() + " combo " + ModifyFeaturesControl.numFeatureTables() + " feature tables MMP37");
		if(combo.getItemCount() != ModifyFeaturesControl.numFeatureTables()){
			GenesisWorkbench.reloadComboBoxContent(combo, ModifyFeaturesControl.getFeatureTables(), ModifyFeaturesControl.getHighlightedFeatureTableRecipe());
		}
		if(ModifyFeaturesControl.hasHighlightedFeatureTable()){
			description.setText(ModifyFeaturesControl.getHighlightedFeatureTableRecipe().getFeatureTable().getDescriptionString());
			combo.setSelectedItem(ModifyFeaturesControl.getHighlightedFeatureTableRecipe());
		}
	}
}
