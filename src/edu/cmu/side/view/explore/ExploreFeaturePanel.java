package edu.cmu.side.view.explore;

import java.awt.BorderLayout;

import edu.cmu.side.view.generic.GenericFeatureMetricPanel;
import edu.cmu.side.view.util.AbstractListPanel;

public class ExploreFeaturePanel extends AbstractListPanel{

	GenericFeatureMetricPanel display = new GenericFeatureMetricPanel() {
		
		@Override
		public String getTargetAnnotation() { return null; }
	};
	
	public ExploreFeaturePanel(){
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, display);
		
	}
}
