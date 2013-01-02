package edu.cmu.side.view.modify;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.ModifyFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.plugin.FilterPlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericPluginChecklistPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;
import edu.cmu.side.view.generic.GenericTripleFrame;

public class ModifyFeaturesPane extends JPanel{

	private static GenericTripleFrame top;
	private static ModifyActionPanel action = new ModifyActionPanel();
	private static ModifyBottomPanel bottom = new ModifyBottomPanel();

	public ModifyFeaturesPane(){
		setLayout(new BorderLayout());
		GenericLoadPanel load = new GenericLoadPanel("Modify:"){

			@Override
			public void setHighlight(Recipe r) {
				ModifyFeaturesControl.setHighlightedFeatureTableRecipe(r);
			}

			@Override
			public Recipe getHighlight() {
				return ModifyFeaturesControl.getHighlightedFeatureTableRecipe();
			}

			@Override
			public String getHighlightDescription() {
				return getHighlight().getFeatureTable().getDescriptionString();
			}

			@Override
			public void refreshPanel() {
				refreshPanel(ModifyFeaturesControl.getFeatureTables());
			}
			
		};
		
		GenericPluginChecklistPanel<FilterPlugin> checklist = new GenericPluginChecklistPanel<FilterPlugin>("Filters Available:"){
			@Override
			public Map<FilterPlugin, Boolean> getPlugins() {
				return ModifyFeaturesControl.getFilterPlugins();
			}
		};
		
		GenericPluginConfigPanel<FilterPlugin> config = new GenericPluginConfigPanel<FilterPlugin>(){
			@Override
			public void refreshPanel() {
				refreshPanel(ModifyFeaturesControl.getFilterPlugins());
			}
		};
		
		top = new GenericTripleFrame(load, checklist, config);
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.CENTER, top);
		panel.add(BorderLayout.SOUTH, action);
		pane.setTopComponent(panel);
		pane.setBottomComponent(bottom);
		top.setPreferredSize(new Dimension(950,450));
		bottom.setPreferredSize(new Dimension(950,200));
		add(BorderLayout.CENTER, pane);
	}
	
	public void refreshPanel(){
		top.refreshPanel();
		action.refreshPanel();
		bottom.refreshPanel();
	}
}
