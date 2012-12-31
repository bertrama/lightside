package edu.cmu.side.view.generic;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.control.GenesisControl;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.CheckBoxList;
import edu.cmu.side.view.util.CheckBoxListEntry;
import edu.cmu.side.view.util.FastListModel;

public abstract class GenericPluginChecklistPanel<E extends SIDEPlugin> extends AbstractListPanel {
	FastListModel pluginsModel = new FastListModel();
	CheckBoxList pluginsList = new CheckBoxList();
	JScrollPane pluginsScroll = new JScrollPane(pluginsList);

	public GenericPluginChecklistPanel(String label){
		setLayout(new RiverLayout());
		ArrayList<CheckBoxListEntry> pluginsToPass = new ArrayList<CheckBoxListEntry>();
		Map<E, Boolean> plugins = getPlugins();
		for(E plug : plugins.keySet()){
			CheckBoxListEntry entry = new CheckBoxListEntry(plug, plugins.get(plug));
			entry.addItemListener(new GenesisControl.PluginCheckboxListener<E>(plugins));
			pluginsToPass.add(entry);
		}
		pluginsModel.addAll(pluginsToPass.toArray(new CheckBoxListEntry[0]));
		pluginsList.setModel(pluginsModel);
		add("left", new JLabel(label));
		add("br hfill vfill", pluginsScroll);
	}
	
	public abstract Map<E, Boolean> getPlugins();
}
