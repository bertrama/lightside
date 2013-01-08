package edu.cmu.side.view.modify;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.ModifyFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.plugin.TableFeatureMetricPlugin;
import edu.cmu.side.view.generic.GenericFeatureMetricPanel;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericMetricChecklistPanel;

public class ModifyBottomPanel extends JPanel{

	private GenericLoadPanel control = new GenericLoadPanel("Filtered Tables:") {
		
		@Override
		public void setHighlight(Recipe r) {
			ModifyFeaturesControl.setHighlightedFilterTableRecipe(r);
		}
		
		@Override
		public void refreshPanel() {
			refreshPanel(ModifyFeaturesControl.getFilterTables());
		}
		
		@Override
		public Recipe getHighlight() {
			return ModifyFeaturesControl.getHighlightedFilterTableRecipe();
		}
	};

	GenericMetricChecklistPanel checklist = new GenericMetricChecklistPanel<TableFeatureMetricPlugin>(){
		@Override
		public Map<TableFeatureMetricPlugin, Map<String, Boolean>> getEvaluationPlugins() {
			return ModifyFeaturesControl.getTableEvaluationPlugins();
		}

		@Override
		public ItemListener getCheckboxListener() {
			return ModifyFeaturesControl.getEvalCheckboxListener();
		}

		@Override
		public void setTargetAnnotation(String s) {
			ModifyFeaturesControl.setTargetAnnotation(s);
		}
	};
	private GenericFeatureMetricPanel display = new GenericFeatureMetricPanel(){

		@Override
		public String getTargetAnnotation() {
			return ModifyFeaturesControl.getTargetAnnotation();
		}

	};

	public ModifyBottomPanel(){
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();
		pane.setLeftComponent(control);

		pane.setBorder(BorderFactory.createEmptyBorder());
		JSplitPane right = new JSplitPane();
		right.setLeftComponent(checklist);
		right.setRightComponent(display);
		right.setBorder(BorderFactory.createEmptyBorder());
		right.setPreferredSize(new Dimension(650,200));
		pane.setRightComponent(right);
		control.setPreferredSize(new Dimension(275,200));		
		checklist.setPreferredSize(new Dimension(275,200));
		display.setPreferredSize(new Dimension(350, 200));

		add(BorderLayout.CENTER, pane);
	}
	
	public void refreshPanel(){
		control.refreshPanel();
		if(ModifyFeaturesControl.hasHighlightedFilterTable()){
			FeatureTable table = ModifyFeaturesControl.getHighlightedFilterTableRecipe().getFilteredTable();
			checklist.refreshPanel(table);
			boolean[] mask = new boolean[table.getDocumentList().getSize()];
			for(int i = 0; i < mask.length; i++) mask[i] = true;
			display.refreshPanel(ModifyFeaturesControl.getHighlightedFilterTableRecipe(), ModifyFeaturesControl.getTableEvaluationPlugins(), mask);	
		}else{
			checklist.refreshPanel(null);
			display.refreshPanel(null, ModifyFeaturesControl.getTableEvaluationPlugins(), new boolean[0]);
		}
	}
}
