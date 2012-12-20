package edu.cmu.side.genesis.view.build;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.view.generic.GenericLoadPanel;
import edu.cmu.side.genesis.view.generic.GenericMatrixPanel;

public class BuildBottomPanel extends JPanel {

	private GenericLoadPanel control = new GenericLoadPanel("Highlighted Trained Model:"){

		@Override
		public void setHighlight(GenesisRecipe r) {
			BuildModelControl.setHighlightedTrainedModelRecipe(r);
		}

		@Override
		public GenesisRecipe getHighlight() {
			return BuildModelControl.getHighlightedTrainedModelRecipe();
		}

		@Override
		public String getHighlightDescription() {
			return getHighlight().getTrainingResult().getDescriptionString();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(BuildModelControl.getTrainedModels());
		}
	};

	private GenericMatrixPanel confusion = new GenericMatrixPanel(){

		@Override
		public void refreshPanel() {
			if(BuildModelControl.hasHighlightedTrainedModelRecipe()){
				refreshPanel(BuildModelControl.getHighlightedTrainedModelRecipe().getTrainingResult().getConfusionMatrix());				
			}else{
				refreshPanel(new TreeMap<String, Map<String, List<Integer>>>());
			}
		}

	};
	private BuildResultPanel result = new BuildResultPanel();

	public BuildBottomPanel(){
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();
		pane.setLeftComponent(control);

		JSplitPane right = new JSplitPane();
		right.setLeftComponent(result);
		right.setRightComponent(confusion);
		right.setPreferredSize(new Dimension(650,200));
		pane.setRightComponent(right);
		control.setPreferredSize(new Dimension(275,200));		
		confusion.setPreferredSize(new Dimension(275,200));
		result.setPreferredSize(new Dimension(350, 200));

		add(BorderLayout.CENTER, pane);
	}

	public void refreshPanel(){
		control.refreshPanel();
		confusion.refreshPanel();
		result.refreshPanel();	
	}
}
