package edu.cmu.side.view.explore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.plugin.EvaluateOneModelPlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericPluginChecklistPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;

public class ExploreResultsPane extends JPanel{

	GenericLoadPanel load = new GenericLoadPanel("Highlight:"){

		@Override
		public void setHighlight(Recipe r) {
			ExploreResultsControl.setHighlightedTrainedModelRecipe(r);
		}

		@Override
		public Recipe getHighlight() {
			return ExploreResultsControl.getHighlightedTrainedModelRecipe();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(ExploreResultsControl.getTrainedModels());
		}
		
	};
	
	GenericPluginChecklistPanel<EvaluateOneModelPlugin> checklist = new GenericPluginChecklistPanel<EvaluateOneModelPlugin>("Model Analysis Plugins:"){
		@Override
		public Map<EvaluateOneModelPlugin, Boolean> getPlugins() {
			return ExploreResultsControl.getModelAnalysisPlugins();
		}
	};
	
	GenericPluginConfigPanel<EvaluateOneModelPlugin> analysis = new GenericPluginConfigPanel<EvaluateOneModelPlugin>(){
		public void refreshPanel(){
			refreshPanel(ExploreResultsControl.getModelAnalysisPlugins());
		}
	};
	
	public ExploreResultsPane(){
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();
		
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		left.setTopComponent(load);
		left.setBottomComponent(checklist);
		
		JScrollPane scroll = new JScrollPane(analysis);
		load.setPreferredSize(new Dimension(275,450));
		checklist.setPreferredSize(new Dimension(275,200));
		scroll.setPreferredSize(new Dimension(650,700));

		pane.setLeftComponent(left);
		pane.setRightComponent(scroll);
		add(BorderLayout.CENTER, pane);

	}
	
	public void refreshPanel(){
		load.refreshPanel();
		checklist.refreshPanel();
		analysis.refreshPanel();
	}
}
