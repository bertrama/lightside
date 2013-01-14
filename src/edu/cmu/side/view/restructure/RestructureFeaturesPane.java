package edu.cmu.side.view.restructure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.RestructureTablesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.plugin.RestructurePlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericPluginChecklistPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;
import edu.cmu.side.view.generic.GenericTripleFrame;

public class RestructureFeaturesPane extends JPanel{

	private static GenericTripleFrame top;
	private static RestructureActionPanel action = new RestructureActionPanel(RestructureTablesControl.getUpdater());
	private static RestructureBottomPanel bottom = new RestructureBottomPanel();

	public RestructureFeaturesPane(){
		setLayout(new BorderLayout());
		GenericLoadPanel load = new GenericLoadPanel("Feature Tables:"){

			@Override
			public void setHighlight(Recipe r) {
				RestructureTablesControl.setHighlightedFeatureTableRecipe(r);
			}

			@Override
			public Recipe getHighlight() {
				return RestructureTablesControl.getHighlightedFeatureTableRecipe();
			}

			@Override
			public void refreshPanel() {
				refreshPanel(RestructureTablesControl.getFeatureTables());
			}
			
		};
		
		GenericPluginChecklistPanel<RestructurePlugin> checklist = new GenericPluginChecklistPanel<RestructurePlugin>("Filters Available:"){
			@Override
			public Map<RestructurePlugin, Boolean> getPlugins() {
				return RestructureTablesControl.getFilterPlugins();
			}
		};
		
		GenericPluginConfigPanel<RestructurePlugin> config = new GenericPluginConfigPanel<RestructurePlugin>(){
			@Override
			public void refreshPanel() {
				refreshPanel(RestructureTablesControl.getFilterPlugins());
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
