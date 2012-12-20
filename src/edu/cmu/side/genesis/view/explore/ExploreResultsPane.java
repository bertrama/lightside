package edu.cmu.side.genesis.view.explore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.cmu.side.genesis.control.ExploreResultsControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.view.generic.GenericLoadPanel;
import edu.cmu.side.genesis.view.generic.GenericPluginChecklistPanel;
import edu.cmu.side.genesis.view.generic.GenericPluginConfigPanel;
import edu.cmu.side.simple.ModelAnalysisPlugin;
import edu.cmu.side.simple.ModelEvaluationPlugin;

public class ExploreResultsPane extends JPanel{

	GenericLoadPanel load = new GenericLoadPanel(){

		@Override
		public void setHighlight(GenesisRecipe r) {
			ExploreResultsControl.setHighlightedTrainedModelRecipe(r);
		}

		@Override
		public GenesisRecipe getHighlight() {
			return ExploreResultsControl.getHighlightedTrainedModelRecipe();
		}

		@Override
		public String getHighlightDescription() {
			return getHighlight().getTrainingResult().getDescriptionString();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(ExploreResultsControl.getTrainedModels());
		}
		
	};
	
	GenericPluginChecklistPanel<ModelAnalysisPlugin> checklist = new GenericPluginChecklistPanel<ModelAnalysisPlugin>("Model Analysis Plugins:"){
		@Override
		public Map<ModelAnalysisPlugin, Boolean> getPlugins() {
			return ExploreResultsControl.getModelAnalysisPlugins();
		}
	};
	
	GenericPluginConfigPanel<ModelAnalysisPlugin> analysis = new GenericPluginConfigPanel<ModelAnalysisPlugin>(){
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
