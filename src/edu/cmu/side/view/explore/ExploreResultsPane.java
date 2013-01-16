package edu.cmu.side.view.explore;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.plugin.EvaluateOneModelPlugin;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.generic.GenericPluginConfigPanel;
import edu.cmu.side.view.generic.GenericTripleFrame;
import edu.cmu.side.view.util.AbstractListPanel;

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
	
	ExploreMetricChecklistPanel checklist = new ExploreMetricChecklistPanel();

	ExploreFeaturePanel features = new ExploreFeaturePanel();
	
	GenericTripleFrame triple;
	ExploreActionBar middle;

	
	GenericPluginConfigPanel<EvaluateOneModelPlugin> analysis = new GenericPluginConfigPanel<EvaluateOneModelPlugin>(false){
		public void refreshPanel(){
			refreshPanel(ExploreResultsControl.getModelAnalysisPlugins());
		}
	};
	
	
	public ExploreResultsPane(){
		setLayout(new BorderLayout());
		
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		matrix.setPreferredSize(new Dimension(325, 150));
		checklist.setPreferredSize(new Dimension(325, 150));

		features.setPreferredSize(new Dimension(325, 250));

		AbstractListPanel panel = new AbstractListPanel();
		panel.setPreferredSize(new Dimension(325,325));
		panel.removeAll();
		panel.setLayout(new BorderLayout());
		panel.add(BorderLayout.CENTER, matrix);
		
		
		panel.add(BorderLayout.SOUTH, checklist);
		triple = new GenericTripleFrame(load, panel, features);
		middle = new ExploreActionBar(ExploreResultsControl.getUpdater());
		
		JScrollPane scroll = new JScrollPane(analysis);
		
		JPanel top = new JPanel(new BorderLayout());
		

		triple.setPreferredSize(new Dimension(950,400));
		top.setPreferredSize(new Dimension(950,400));

		top.add(BorderLayout.CENTER, triple);
		top.add(BorderLayout.SOUTH, middle);

		left.setTopComponent(top);
		left.setBottomComponent(scroll);
		add(BorderLayout.CENTER, left);

	}
	
	public void refreshPanel(){
		if(!ExploreResultsControl.hasHighlightedTrainedModelRecipe()){
			ExploreResultsControl.setHighlightedCell(null, null);
		}

		load.refreshPanel();
		matrix.refreshPanel();
		features.refreshPanel();
		if(ExploreResultsControl.hasHighlightedTrainedModelRecipe()){
			checklist.refreshPanel(ExploreResultsControl.getHighlightedTrainedModelRecipe().getFeatureTable());			
		}else{
			checklist.refreshPanel(null);
		}
		analysis.refreshPanel();
		revalidate();
		repaint();
	}
}
