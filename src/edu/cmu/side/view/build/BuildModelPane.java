package edu.cmu.side.view.build;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericTripleFrame;

public class BuildModelPane extends JPanel{

	private static GenericTripleFrame top;
	private static BuildActionPanel action = new BuildActionPanel();
	private static BuildBottomPanel bottom = new BuildBottomPanel();

	public BuildModelPane(){
		setLayout(new BorderLayout());
		GenericLoadPanel load = new GenericLoadPanel("Highlight:") {
			@Override
			public void setHighlight(Recipe r) {
				BuildModelControl.setHighlightedFeatureTableRecipe(r);
			}
			
			@Override
			public String getHighlightDescription() {
				return getHighlight().getTrainingTable().getDescriptionString();
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
		
		top = new GenericTripleFrame(load, new BuildPluginPanel(action.new NameListener()), new BuildTestingPanel());
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
