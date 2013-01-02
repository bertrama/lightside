package edu.cmu.side.view.compare;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.control.CompareModelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.plugin.EvaluateTwoModelPlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericPluginChecklistPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;
import edu.cmu.side.view.util.AbstractListPanel;

public class CompareModelsPane extends AbstractListPanel{

	GenericLoadPanel loadBaseline = new GenericLoadPanel("Baseline:"){

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
	

	GenericLoadPanel loadCompetitor = new GenericLoadPanel("Competing:"){

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
	
	JPanel middle = new JPanel(new BorderLayout());
	public CompareModelsPane(){
		setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JPanel grid = new JPanel(new GridLayout(1,2));
		JPanel top = new JPanel(new RiverLayout());
		grid.add(loadBaseline);
		grid.add(loadCompetitor);
		top.add("hfill", grid);
		top.add("br left", new JLabel("Selected Comparison Plugin:"));
		top.add("hfill", combo);
		Workbench.reloadComboBoxContent(combo, CompareModelsControl.getModelComparisonPlugins(), null);
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					EvaluateTwoModelPlugin plug = (EvaluateTwoModelPlugin)combo.getSelectedItem();
					CompareModelsControl.setHighlightedModelComparisonPlugin(plug);
					middle.removeAll();
					middle.add(BorderLayout.CENTER, plug.getConfigurationUI());
					plug.refreshPanel();
				}
			}
		});
		if(combo.getModel().getSize() > 0){
			combo.setSelectedIndex(0);
		}
		JScrollPane scroll = new JScrollPane(middle);
		grid.setPreferredSize(new Dimension(950,200));
		top.setPreferredSize(new Dimension(950,250));
		scroll.setPreferredSize(new Dimension(950,400));
		split.setTopComponent(top);
		split.setBottomComponent(scroll);
		add(BorderLayout.CENTER, split);

	}
	
	public void refreshPanel(){
		loadBaseline.refreshPanel();
		loadCompetitor.refreshPanel();
		if(CompareModelsControl.getHighlightedModelComparisonPlugin() != null){
			CompareModelsControl.getHighlightedModelComparisonPlugin().refreshPanel();
		}
	}
}
