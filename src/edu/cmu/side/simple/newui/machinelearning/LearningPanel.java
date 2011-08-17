package edu.cmu.side.simple.newui.machinelearning;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import se.datadosen.component.RiverLayout;

/**
 * Holds everything in the machine learning tab.
 * @author emayfiel
 *
 */
public class LearningPanel extends JPanel{
	private static final long serialVersionUID = 4971966596966830913L;

	static LearningLeftPanel left;
	static LearningRightPanel right;
	public LearningPanel(){
		left = new LearningLeftPanel();
		right = new LearningRightPanel();
		setBorder(null);
		JSplitPane split = new JSplitPane();
		split.setBorder(null);
		split.setLeftComponent(left);
		split.setRightComponent(right);
		setLayout(new RiverLayout());
		add("hfill vfill", split);
	}

	public void refreshPanel() {
		left.refreshPanel();
		right.refreshPanel();
	}
}
