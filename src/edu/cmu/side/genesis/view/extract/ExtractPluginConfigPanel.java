package edu.cmu.side.genesis.view.extract;

import java.awt.Font;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.simple.FeaturePlugin;

public class ExtractPluginConfigPanel extends JPanel {

	Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
	
	Set<FeaturePlugin> visiblePlugins = new TreeSet<FeaturePlugin>();
	
	public ExtractPluginConfigPanel(){
		setLayout(new RiverLayout());
	}
	public void refreshPanel(){
		Map<FeaturePlugin, Boolean> plugins = ExtractFeaturesControl.getFeaturePlugins();
		Set<FeaturePlugin> localSet = new HashSet<FeaturePlugin>();
		for(FeaturePlugin plugin : plugins.keySet()){
			if(plugins.get(plugin)){
				localSet.add(plugin);
			}
		}
		System.out.println(localSet.size() + ", " + visiblePlugins.size() + "EPConP35");
		if(!localSet.equals(visiblePlugins)){
			visiblePlugins = localSet;
			this.removeAll();
			for(FeaturePlugin plugin : visiblePlugins){
				JLabel label = new JLabel(plugin.toString());
				label.setFont(font);
				this.add("br left", label);
				this.add("br hfill", plugin.getConfigurationUI());
			}
		}
	}
	
}
