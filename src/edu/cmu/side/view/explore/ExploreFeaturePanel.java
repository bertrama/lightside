package edu.cmu.side.view.explore;

import java.awt.BorderLayout;

import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.view.generic.GenericFeatureMetricPanel;
import edu.cmu.side.view.util.AbstractListPanel;

public class ExploreFeaturePanel extends AbstractListPanel{

	ExploreFeatureMetricPanel display = new ExploreFeatureMetricPanel();
	
	public ExploreFeaturePanel(){
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, display);
	}
	
	public void refreshPanel(){
		Recipe target = ExploreResultsControl.getHighlightedTrainedModelRecipe();
		if(target != null){
			FeatureTable table = target.getTrainingResult().getEvaluationTable();
			boolean[] mask = new boolean[table.getDocumentList().getSize()];
			for(int i = 0; i < mask.length; i++) mask[i] = true;
			display.refreshPanel(target, ExploreResultsControl.getFeatureEvaluationPlugins(), mask);
		}else{
			display.refreshPanel(target, ExploreResultsControl.getFeatureEvaluationPlugins(), new boolean[0]);
		}
	}
}
