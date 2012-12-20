package edu.cmu.side.genesis.view.generic;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.control.GenesisControl;
import edu.cmu.side.genesis.view.CheckBoxList;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;

public abstract class GenericPluginChecklistPanel<E extends SIDEPlugin> extends AbstractListPanel {
	FastListModel pluginsModel = new FastListModel();
	CheckBoxList pluginsList = new CheckBoxList();
	JScrollPane pluginsScroll = new JScrollPane(pluginsList);

	public GenericPluginChecklistPanel(String label){
		setLayout(new RiverLayout());
		ArrayList<CheckBoxListEntry> pluginsToPass = new ArrayList<CheckBoxListEntry>();
		Map<E, Boolean> plugins = getPlugins();
		System.out.println("Instantiating " + label + " plugins " + plugins.keySet().size());
		for(E plug : plugins.keySet()){
			CheckBoxListEntry entry = new CheckBoxListEntry(plug, plugins.get(plug));
			entry.addItemListener(new GenesisControl.PluginCheckboxListener<E>(plugins));
			pluginsToPass.add(entry);
		}
		pluginsModel.addAll(pluginsToPass.toArray(new CheckBoxListEntry[0]));
		pluginsList.setModel(pluginsModel);
		System.out.println(pluginsModel.size() + "GPCP35");
		add("left", new JLabel(label));
		add("br hfill vfill", pluginsScroll);
	}
	
	public abstract Map<E, Boolean> getPlugins();
}
