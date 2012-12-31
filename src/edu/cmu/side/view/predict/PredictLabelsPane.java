package edu.cmu.side.view.predict;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.control.PredictLabelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.generic.GenericLoadPanel;

public class PredictLabelsPane extends JPanel{


	GenericLoadPanel load = new GenericLoadPanel("Model to Apply:"){

		@Override
		public void setHighlight(Recipe r) {
			PredictLabelsControl.setHighlightedTrainedModelRecipe(r);
		}

		@Override
		public Recipe getHighlight() {
			return PredictLabelsControl.getHighlightedTrainedModelRecipe();
		}

		@Override
		public String getHighlightDescription() {
			return getHighlight().getTrainingResult().getDescriptionString();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(PredictLabelsControl.getTrainedModels());
		}
		
	};
	
	PredictNewDataPanel newData = new PredictNewDataPanel();
	PredictOutputPanel output = new PredictOutputPanel();
	
	public PredictLabelsPane(){
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();
		
		JPanel left = new JPanel(new GridLayout(2,1));
		left.add(load);
		left.add(newData);
		pane.setLeftComponent(left);
		pane.setRightComponent(output);
		add(BorderLayout.CENTER, pane);
	}
	
	public void refreshPanel(){
		load.refreshPanel();
		newData.refreshPanel();
		output.refreshPanel();
	}
}
