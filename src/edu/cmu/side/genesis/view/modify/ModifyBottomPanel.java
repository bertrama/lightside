package edu.cmu.side.genesis.view.modify;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.view.generic.FeatureTableDisplayPanel;

public class ModifyBottomPanel extends JPanel{

	private ModifyControlPanel control = new ModifyControlPanel();
	private ModifyTableChecklistPanel checklist = new ModifyTableChecklistPanel();
	private FeatureTableDisplayPanel display = new FeatureTableDisplayPanel();

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
		}
	}
}
