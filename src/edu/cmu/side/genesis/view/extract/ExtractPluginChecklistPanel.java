package edu.cmu.side.genesis.view.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.view.CheckBoxList;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;

public class ExtractPluginChecklistPanel extends AbstractListPanel {

	static FastListModel pluginsModel = new FastListModel();
	static CheckBoxList pluginsList = new CheckBoxList();
	JScrollPane pluginsScroll = new JScrollPane(pluginsList);

	public ExtractPluginChecklistPanel(){
		setLayout(new RiverLayout());
		ArrayList<CheckBoxListEntry> pluginsToPass = new ArrayList<CheckBoxListEntry>();
		Map<FeaturePlugin, Boolean> featurePlugins = ExtractFeaturesControl.getFeaturePlugins();
		for(FeaturePlugin plug : featurePlugins.keySet()){
			CheckBoxListEntry entry = new CheckBoxListEntry(plug, featurePlugins.get(plug));
			entry.addItemListener(new ExtractFeaturesControl.PluginCheckboxListener());
			pluginsToPass.add(entry);
		}
		pluginsModel.addAll(pluginsToPass.toArray(new CheckBoxListEntry[0]));
		pluginsList.setModel(pluginsModel);
		add("left", new JLabel("Feature Extractor Plugins:"));
		add("br hfill vfill", pluginsScroll);
	}
	
	
	public void refreshPanel(){
		
	}
}
