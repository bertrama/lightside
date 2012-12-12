package edu.cmu.side.genesis.view.modify;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.view.generic.CheckBoxList;
import edu.cmu.side.genesis.view.generic.CheckBoxListEntry;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;

public class ModifyChecklistPanel extends AbstractListPanel{
	static FastListModel pluginsModel = new FastListModel();
	static CheckBoxList pluginsList = new CheckBoxList();
	JScrollPane pluginsScroll = new JScrollPane(pluginsList);

	public ModifyChecklistPanel(){
		setLayout(new RiverLayout());
		ArrayList<CheckBoxListEntry> pluginsToPass = new ArrayList<CheckBoxListEntry>();
		Map<FilterPlugin, Boolean> filterPlugins = ModifyFeaturesControl.getFilterPlugins();
		for(FilterPlugin plug : filterPlugins.keySet()){
			CheckBoxListEntry entry = new CheckBoxListEntry(plug, filterPlugins.get(plug));
			entry.addItemListener(new ModifyFeaturesControl.PluginCheckboxListener());
			pluginsToPass.add(entry);
		}
		pluginsModel.addAll(pluginsToPass.toArray(new CheckBoxListEntry[0]));
		pluginsList.setModel(pluginsModel);
		add("left", new JLabel("Filters Available:"));
		add("br hfill vfill", pluginsScroll);
	}

	public void refreshPanel(){
		
	}
}