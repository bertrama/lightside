package edu.cmu.side.genesis.view.modify;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.model.OrderedPluginMap;
import edu.cmu.side.genesis.view.generic.SwingUpdaterLabel;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class ModifyConfigPanel extends AbstractListPanel {

	Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
	
	static List<FilterPlugin> visiblePlugins = new ArrayList<FilterPlugin>();
	JProgressBar progress = new JProgressBar();
	
	public ModifyConfigPanel(){
		setLayout(new RiverLayout());
	}
	
	public static void removePlugin(int i){
		visiblePlugins.remove(i);
	}
	
	public void refreshPanel(){
		OrderedPluginMap plugins = ModifyFeaturesControl.getSelectedFilters();

		System.out.println(plugins.size() + ", " + visiblePlugins.size() + "MCP35");
		if(visiblePlugins.size() != plugins.size()){
			visiblePlugins.clear();
			this.removeAll();
			int i = 0;
			for(SIDEPlugin p : plugins.keySet()){
				FilterPlugin plug = (FilterPlugin)p;
				visiblePlugins.add(plug);
				JLabel label = new JLabel(plug.toString());
				label.setFont(font);
				JButton clear = new JButton("Clear");
				clear.addActionListener(new ModifyConfigPanel.ClearButtonListener(i++));
				add("br left", clear);
				add("left", label);
				add("br hfill", plug.getConfigurationUI());
			}
		}

	}
	
	public class ClearButtonListener implements ActionListener{
		
		int index;
		
		public ClearButtonListener(int i){
			index = i;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ModifyFeaturesControl.getSelectedFilters().remove(visiblePlugins.get(index));
			/** Visible plugins will be updated when the panel is refreshed */
			GenesisWorkbench.update();
		}
		
	}
}

