package edu.cmu.side.view.restructure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.RestructureTablesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.plugin.TableFeatureMetricPlugin;
import edu.cmu.side.view.generic.GenericFeatureMetricPanel;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericMetricChecklistPanel;

public class RestructureBottomPanel extends JPanel{

	private GenericLoadPanel control = new GenericLoadPanel("Restructured Tables:") {
		
		@Override
		public void setHighlight(Recipe r) {
			RestructureTablesControl.setHighlightedFilterTableRecipe(r);
		}
		
		@Override
		public void refreshPanel() {
			refreshPanel(RestructureTablesControl.getFilterTables());
		}
		
		@Override
		public Recipe getHighlight() {
			return RestructureTablesControl.getHighlightedFilterTableRecipe();
		}
	};

	GenericMetricChecklistPanel checklist = new GenericMetricChecklistPanel<TableFeatureMetricPlugin>(){
		@Override
		public Map<TableFeatureMetricPlugin, Map<String, Boolean>> getEvaluationPlugins() {
			return RestructureTablesControl.getTableEvaluationPlugins();
		}

		@Override
		public ItemListener getCheckboxListener() {
			return RestructureTablesControl.getEvalCheckboxListener();
		}

		@Override
		public void setTargetAnnotation(String s) {
			RestructureTablesControl.setTargetAnnotation(s);
		}
	};
	private GenericFeatureMetricPanel display = new GenericFeatureMetricPanel(){

		@Override
		public String getTargetAnnotation() {
			return RestructureTablesControl.getTargetAnnotation();
		}

	};

	public RestructureBottomPanel(){
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
		if(RestructureTablesControl.hasHighlightedFilterTable()){
			FeatureTable table = RestructureTablesControl.getHighlightedFilterTableRecipe().getFilteredTable();
			checklist.refreshPanel(table);
			boolean[] mask = new boolean[table.getDocumentList().getSize()];
			for(int i = 0; i < mask.length; i++) mask[i] = true;
			display.refreshPanel(RestructureTablesControl.getHighlightedFilterTableRecipe(), RestructureTablesControl.getTableEvaluationPlugins(), mask);	
		}else{
			checklist.refreshPanel(null);
			display.refreshPanel(null, RestructureTablesControl.getTableEvaluationPlugins(), new boolean[0]);
		}
	}
}
