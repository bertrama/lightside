package edu.cmu.side.view.build;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;
import edu.cmu.side.view.generic.GenericTripleFrame;

public class BuildModelPane extends JPanel{

	private static GenericTripleFrame top;
	private static BuildActionPanel action = new BuildActionPanel(BuildModelControl.getUpdater());
	private static BuildBottomPanel bottom = new BuildBottomPanel();

	private static GenericPluginConfigPanel<LearningPlugin> config = new GenericPluginConfigPanel<LearningPlugin>(){

		@Override
		public void refreshPanel() {
			refreshPanel(BuildModelControl.getLearningPlugins());
		}
		
	};
	public BuildModelPane(){
		setLayout(new BorderLayout());
		GenericLoadPanel load = new GenericLoadPanel("Feature Tables:") {
			@Override
			public void setHighlight(Recipe r) {
				BuildModelControl.setHighlightedFeatureTableRecipe(r);
			}
			
			@Override
			public Recipe getHighlight() {
				return BuildModelControl.getHighlightedFeatureTableRecipe();
			}

			@Override
			public void refreshPanel() {
				Collection<Recipe> recipes = new ArrayList<Recipe>();
				recipes.addAll(BuildModelControl.getFeatureTables());
				recipes.addAll(BuildModelControl.getFilterTables());
				refreshPanel(recipes);
			}
		};
		
		top = new GenericTripleFrame(load, new BuildPluginPanel(action.new NameListener()), config);
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.CENTER, top);
		panel.add(BorderLayout.SOUTH, action);
		pane.setTopComponent(panel);
		pane.setBottomComponent(bottom);
		bottom.setPreferredSize(new Dimension(950,200));
		top.setPreferredSize(new Dimension(950,500));
		pane.setDividerLocation(500);
		add(BorderLayout.CENTER, pane);
	}
	
	public void refreshPanel(){

		top.refreshPanel();
		action.refreshPanel();
		bottom.refreshPanel();
	}
}
