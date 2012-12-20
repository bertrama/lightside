package edu.cmu.side.genesis.view.generic;

import java.awt.Font;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JLabel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.newui.AbstractListPanel;

public abstract class GenericPluginConfigPanel<E extends SIDEPlugin> extends AbstractListPanel {

	Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
	Set<E> visiblePlugins = new TreeSet<E>();
	
	public GenericPluginConfigPanel(){
		setLayout(new RiverLayout());
	}

	@Override
	public abstract void refreshPanel();
	
	public void refreshPanel(Map<E, Boolean> plugins){
		Set<E> localSet = new HashSet<E>();
		for(E plugin : plugins.keySet()){
			if(plugins.get(plugin)){
				localSet.add(plugin);
			}
		}
		if(!localSet.equals(visiblePlugins)){
			visiblePlugins = localSet;
			this.removeAll();
			for(E plugin : visiblePlugins){
				JLabel label = new JLabel(plugin.toString());
				label.setFont(font);
				this.add("br left", label);
				this.add("br hfill", plugin.getConfigurationUI());
			}
		}
	}
}
