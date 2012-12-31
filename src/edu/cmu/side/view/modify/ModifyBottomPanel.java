package edu.cmu.side.view.modify;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.ModifyFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericTableMetricPanel;

public class ModifyBottomPanel extends JPanel{

	private GenericLoadPanel control = new GenericLoadPanel("Highlighted Filtered Table:") {
		
		@Override
		public void setHighlight(Recipe r) {
			ModifyFeaturesControl.setHighlightedFilterTableRecipe(r);
		}
		
		@Override
		public void refreshPanel() {
			refreshPanel(ModifyFeaturesControl.getFilterTables());
		}
		
		@Override
		public String getHighlightDescription() {
			return getHighlight().getFilteredTable().getDescriptionString();
		}
		
		@Override
		public Recipe getHighlight() {
			return ModifyFeaturesControl.getHighlightedFilterTableRecipe();
		}
	};
	private ModifyTableChecklistPanel checklist = new ModifyTableChecklistPanel();
	private GenericTableMetricPanel display = new GenericTableMetricPanel();

	public ModifyBottomPanel(){
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();
		pane.setLeftComponent(control);

		JSplitPane right = new JSplitPane();
		right.setLeftComponent(checklist);
		right.setRightComponent(display);
		right.setPreferredSize(new Dimension(650,200));
		pane.setRightComponent(right);
		control.setPreferredSize(new Dimension(275,200));		
		checklist.setPreferredSize(new Dimension(275,200));
		display.setPreferredSize(new Dimension(350, 200));

		add(BorderLayout.CENTER, pane);
	}
	
	public void refreshPanel(){
		control.refreshPanel();
		checklist.refreshPanel();
		if(ModifyFeaturesControl.hasHighlightedFilterTable()){
			display.refreshPanel(ModifyFeaturesControl.getHighlightedFilterTableRecipe().getFilteredTable(), ModifyFeaturesControl.getTableEvaluationPlugins());	
		}else{
			display.refreshPanel(null, ModifyFeaturesControl.getTableEvaluationPlugins());
		}
	}
}
