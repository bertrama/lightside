package edu.cmu.side.view.extract;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.plugin.FeatureMetricPlugin;
import edu.cmu.side.plugin.TableFeatureMetricPlugin;
import edu.cmu.side.view.generic.GenericFeatureMetricPanel;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericMetricChecklistPanel;

public class ExtractBottomPanel extends JPanel{

	GenericLoadPanel control = new GenericLoadPanel("Feature Table:") {	
		
		@Override
		public void setHighlight(Recipe r) {
			ExtractFeaturesControl.setHighlightedFeatureTableRecipe(r);
		}

		@Override
		public void refreshPanel() {
			refreshPanel(ExtractFeaturesControl.getFeatureTables());
		}

		@Override
		public Recipe getHighlight() {
			return ExtractFeaturesControl.getHighlightedFeatureTableRecipe();
		}
	};

	GenericMetricChecklistPanel checklist = new GenericMetricChecklistPanel<TableFeatureMetricPlugin>(){
		@Override
		public Map<TableFeatureMetricPlugin, Map<String, Boolean>> getEvaluationPlugins() {
			return ExtractFeaturesControl.getTableEvaluationPlugins();
		}

		@Override
		public ItemListener getCheckboxListener() {
			return ExtractFeaturesControl.getEvalCheckboxListener();
		}

		@Override
		public void setTargetAnnotation(String s) {
			ExtractFeaturesControl.setTargetAnnotation(s);
		}
	};
	GenericFeatureMetricPanel display = new GenericFeatureMetricPanel(){

		@Override
		public String getTargetAnnotation() {
			return ExtractFeaturesControl.getTargetAnnotation();
		}
		
	};

	public ExtractBottomPanel(){
		setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(control);
		split.setBorder(BorderFactory.createEmptyBorder());
		JSplitPane displaySplit = new JSplitPane();
		displaySplit.setLeftComponent(checklist);
		displaySplit.setRightComponent(display);
		displaySplit.setBorder(BorderFactory.createEmptyBorder());
		displaySplit.setPreferredSize(new Dimension(650,200));
		checklist.setPreferredSize(new Dimension(275,200));
		display.setPreferredSize(new Dimension(350, 200));
		split.setRightComponent(displaySplit);
		control.setPreferredSize(new Dimension(275,200));
		add(BorderLayout.CENTER, split);
	}

	public void refreshPanel(){
		control.refreshPanel();
		
		if(ExtractFeaturesControl.hasHighlightedFeatureTable()){     
			FeatureTable table = ExtractFeaturesControl.getHighlightedFeatureTableRecipe().getFeatureTable();
			checklist.refreshPanel(table);
			boolean[] mask = new boolean[table.getDocumentList().getSize()];
			for(int i = 0; i < mask.length; i++) mask[i] = true;
			display.refreshPanel(ExtractFeaturesControl.getHighlightedFeatureTableRecipe(), ExtractFeaturesControl.getTableEvaluationPlugins(), mask);
		}else{
			display.refreshPanel(null, ExtractFeaturesControl.getTableEvaluationPlugins(), new boolean[0]);
		}
	}
}