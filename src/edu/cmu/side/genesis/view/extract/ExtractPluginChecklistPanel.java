package edu.cmu.side.genesis.view.extract;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.SimpleWorkbench;
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
		CheckBoxListEntry[] checkboxes = new CheckBoxListEntry[featureExtractors.length];
		for(int i = 0 ; i < featureExtractors.length; i++){
			checkboxes[i] = new CheckBoxListEntry(featureExtractors[i], false);
			checkboxes[i].addActionListener(new ItemListener()
		}
		pluginsModel.addAll(checkboxes);
		pluginsList.setModel(pluginsModel);
		add("left", new JLabel("Feature Extractor Plugins:"));
		add("br hfill vfill", pluginsScroll);
	}
	
	
	public void refreshPanel(){
		
	}
}
