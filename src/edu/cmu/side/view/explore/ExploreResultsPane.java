package edu.cmu.side.view.explore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.plugin.EvaluateOneModelPlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericMatrixPanel;
import edu.cmu.side.view.generic.GenericPluginChecklistPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;
import edu.cmu.side.view.generic.GenericTripleFrame;

public class ExploreResultsPane extends JPanel{

	GenericLoadPanel load = new GenericLoadPanel("Highlight:"){

		@Override
		public void setHighlight(Recipe r) {
			ExploreResultsControl.setHighlightedTrainedModelRecipe(r);
		}

		@Override
		public Recipe getHighlight() {
			return ExploreResultsControl.getHighlightedTrainedModelRecipe();
		}

		@Override
		public void refreshPanel() {
			refreshPanel(ExploreResultsControl.getTrainedModels());
		}
		
	};
	
	ExploreMatrixPanel matrix = new ExploreMatrixPanel();
	
	ExploreFeaturePanel features = new ExploreFeaturePanel();
	
	GenericTripleFrame top;
	ExploreActionBar middle;
	
	GenericPluginConfigPanel<EvaluateOneModelPlugin> analysis = new GenericPluginConfigPanel<EvaluateOneModelPlugin>(false){
		public void refreshPanel(){
			refreshPanel(ExploreResultsControl.getModelAnalysisPlugins());
		}
	};
	
	
	public ExploreResultsPane(){
		setLayout(new BorderLayout());
		
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		top = new GenericTripleFrame(load, matrix, features);
		middle = new ExploreActionBar();
		
		JScrollPane scroll = new JScrollPane(analysis);
		load.setPreferredSize(new Dimension(275,350));
		
		JPanel corner = new JPanel(new BorderLayout());
		
		corner.add(BorderLayout.CENTER, top);
		corner.add(BorderLayout.SOUTH, middle);
		left.setTopComponent(corner);
		left.setBottomComponent(scroll);
		add(BorderLayout.CENTER, left);

	}
	
	public void refreshPanel(){
		load.refreshPanel();
		matrix.refreshPanel();
		features.refreshPanel();
		analysis.refreshPanel();
	}
}
