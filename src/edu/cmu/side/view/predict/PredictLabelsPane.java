package edu.cmu.side.view.predict;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.GenesisControl;
import edu.cmu.side.control.PredictLabelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.generic.ActionBar;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.util.DocumentListTableModel;

public class PredictLabelsPane extends JPanel{


	GenericLoadPanel load = new GenericLoadPanel("Model to Apply:"){

		@Override
		public void setHighlight(Recipe r) {
			PredictLabelsControl.setHighlightedTrainedModelRecipe(r);
			Workbench.update(this);
		}

		@Override
		public Recipe getHighlight() {
			return PredictLabelsControl.getHighlightedTrainedModelRecipe();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(PredictLabelsControl.getTrainedModels());
		}
		
	};
	

	ActionBar actionBar = new PredictActionBar(PredictLabelsControl.getUpdater());
	
	PredictNewDataPanel newData = new PredictNewDataPanel();
	PredictOutputPanel output = new PredictOutputPanel();
	DocumentListTableModel docTableModel =new DocumentListTableModel(null);
	JTable docDisplay = new JTable(docTableModel);
	
	public PredictLabelsPane(){
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();
		
		
		
		JPanel left = new JPanel(new GridLayout(2,1));
		left.add(load);
		left.add(newData);
		pane.setLeftComponent(left);
		pane.setRightComponent(output);
		add(BorderLayout.CENTER, pane);
		add(BorderLayout.SOUTH, actionBar);
		
		GenesisControl.addListenerToMap(Workbench.getRecipeManager(), load);
		GenesisControl.addListenerToMap(Workbench.getRecipeManager(), output);
		GenesisControl.addListenerToMap(Workbench.getRecipeManager(), actionBar);

		GenesisControl.addListenerToMap(newData, newData);
		GenesisControl.addListenerToMap(newData, actionBar);
		GenesisControl.addListenerToMap(newData, output);
		
		GenesisControl.addListenerToMap(actionBar, output);
	}
	
}
