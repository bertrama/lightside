package edu.cmu.side.view.explore;

import javax.swing.JLabel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.view.util.AbstractListPanel;

public class ExploreFeaturePanel extends AbstractListPanel{

	public ExploreFeaturePanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Feature Highlight:"));
		
	}
}
