package edu.cmu.side.genesis.view.build;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;

public class BuildBottomPanel extends JPanel {

	private BuildControlPanel control = new BuildControlPanel();
	private BuildConfusionPanel confusion = new BuildConfusionPanel();
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
		if(BuildModelControl.hasHighlightedTrainedModelRecipe()){
			confusion.refreshPanel();
			result.refreshPanel();	
		}
	}
}
