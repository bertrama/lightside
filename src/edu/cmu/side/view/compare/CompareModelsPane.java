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
import edu.cmu.side.control.GenesisControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.plugin.EvaluateTwoModelPlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericPluginChecklistPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;
import edu.cmu.side.view.util.AbstractListPanel;

public class CompareModelsPane extends AbstractListPanel{

	GenericLoadPanel loadBaseline = new GenericLoadPanel("Baseline Model:"){

		@Override
		public void setHighlight(Recipe r) {
			CompareModelsControl.setBaselineTrainedModelRecipe(r);
			Workbench.update(this);
		}

		@Override
		public Recipe getHighlight() {
			return CompareModelsControl.getBaselineTrainedModelRecipe();
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
			Workbench.update(this);
		}

		@Override
		public Recipe getHighlight() {
			return CompareModelsControl.getCompetingTrainedModelRecipe();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(CompareModelsControl.getTrainedModels());
		}
	};
	
	JPanel middle = new JPanel(new BorderLayout());
	CompareActionBar dropdown = new CompareActionBar(CompareModelsControl.getUpdater());

	public CompareModelsPane(){
		setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JPanel grid = new JPanel(new GridLayout(1,2));
		JPanel top = new JPanel(new BorderLayout());
		grid.add(loadBaseline);
		grid.add(loadCompetitor);
		top.add(BorderLayout.CENTER, grid);
		top.add(BorderLayout.SOUTH, dropdown);
		Workbench.reloadComboBoxContent(combo, CompareModelsControl.getModelComparisonPlugins().keySet(), null);
		if(combo.getItemCount() > 0){
			System.out.println("Setting combo!");
			combo.setSelectedIndex(0);
		}
		JScrollPane scroll = new JScrollPane(middle);
		grid.setPreferredSize(new Dimension(950,200));
		top.setPreferredSize(new Dimension(950,250));
		scroll.setPreferredSize(new Dimension(950,400));
		split.setTopComponent(top);
		split.setBottomComponent(scroll);
		add(BorderLayout.CENTER, split);

		GenesisControl.addListenerToMap(RecipeManager.Stage.TRAINED_MODEL, loadBaseline);
		GenesisControl.addListenerToMap(RecipeManager.Stage.TRAINED_MODEL, loadCompetitor);
		GenesisControl.addListenerToMap(loadBaseline, this);
		GenesisControl.addListenerToMap(loadCompetitor, this);
		GenesisControl.addListenerToMap(dropdown, this);

	}
	
	public void refreshPanel(){
		dropdown.refreshPanel();
		if(CompareModelsControl.getHighlightedModelComparisonPlugin() != null){
			middle.removeAll();
			middle.add(BorderLayout.CENTER, CompareModelsControl.getHighlightedModelComparisonPlugin().getConfigurationUI());
			CompareModelsControl.getHighlightedModelComparisonPlugin().refreshPanel();
			middle.revalidate();
			middle.repaint();
		}
	}
}
