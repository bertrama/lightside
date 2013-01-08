package edu.cmu.side.view.explore;

import java.awt.event.ItemListener;
import java.util.Map;

import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.plugin.ModelFeatureMetricPlugin;
import edu.cmu.side.view.generic.GenericMetricChecklistPanel;

public class ExploreMetricChecklistPanel extends GenericMetricChecklistPanel<ModelFeatureMetricPlugin>{

		public ExploreMetricChecklistPanel(){
			super();
			this.removeAll();
			add("br hfill vfill", describeScroll);
		}
		
		@Override
		public ItemListener getCheckboxListener() {
			return ExploreResultsControl.getCheckboxListener();
		}

		@Override
		public Map<ModelFeatureMetricPlugin, Map<String, Boolean>> getEvaluationPlugins() {
			return ExploreResultsControl.getFeatureEvaluationPlugins();
		}

		@Override
		public void setTargetAnnotation(String s) {}
	
	
}