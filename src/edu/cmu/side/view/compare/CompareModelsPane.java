package edu.cmu.side.view.compare;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.cmu.side.control.CompareModelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.plugin.EvaluateTwoModelPlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericPluginChecklistPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;

public class CompareModelsPane extends JPanel{

	GenericLoadPanel loadBaseline = new GenericLoadPanel("Baseline Model:"){

		@Override
		public void setHighlight(Recipe r) {
			CompareModelsControl.setBaselineTrainedModelRecipe(r);
		}

		@Override
		public Recipe getHighlight() {
			return CompareModelsControl.getBaselineTrainedModelRecipe();
		}

		@Override
		public String getHighlightDescription() {
			return getHighlight().getTrainingResult().getDescriptionString();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(CompareModelsControl.getTrainedModels());
		}

	};
	

	GenericLoadPanel loadCompetitor = new GenericLoadPanel("Competing Model:"){

		@Override
		public void setHighlight(Recipe r) {
			CompareModelsControl.setCompetingTrainedModelRecipe(r);
		}

		@Override
		public Recipe getHighlight() {
			return CompareModelsControl.getCompetingTrainedModelRecipe();
		}

		@Override
		public String getHighlightDescription() {
			return getHighlight().getTrainingResult().getDescriptionString();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(CompareModelsControl.getTrainedModels());
		}
	};
	
	GenericPluginChecklistPanel<EvaluateTwoModelPlugin> checklist = new GenericPluginChecklistPanel<EvaluateTwoModelPlugin>("Model Comparison Plugins:"){
		@Override
		public Map<EvaluateTwoModelPlugin, Boolean> getPlugins() {
			return CompareModelsControl.getModelComparisonPlugins();
		}
	};
	
	GenericPluginConfigPanel<EvaluateTwoModelPlugin> analysis = new GenericPluginConfigPanel<EvaluateTwoModelPlugin>(){
		public void refreshPanel(){
			refreshPanel(CompareModelsControl.getModelComparisonPlugins());
		}
	};
	
	public CompareModelsPane(){
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();
		
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel top = new JPanel(new GridLayout(2,1));
		top.add(loadBaseline);
		top.add(loadCompetitor);
		left.setTopComponent(top);
		left.setBottomComponent(checklist);
		
		JScrollPane scroll = new JScrollPane(analysis);
		top.setPreferredSize(new Dimension(275,500));
		checklist.setPreferredSize(new Dimension(275,150));
		scroll.setPreferredSize(new Dimension(650,700));

		pane.setLeftComponent(left);
		pane.setRightComponent(scroll);
		add(BorderLayout.CENTER, pane);

	}
	
	public void refreshPanel(){
		loadBaseline.refreshPanel();
		loadCompetitor.refreshPanel();
		checklist.refreshPanel();
		analysis.refreshPanel();
	}
}
