package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

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
		JSplitPane split = new JSplitPane();
		JScrollPane scrollLeft = new JScrollPane(left);
		JScrollPane scrollRight = new JScrollPane(right);
		scrollLeft.setPreferredSize(new Dimension(300,750));
		scrollRight.setPreferredSize(new Dimension(775,750));
		split.setLeftComponent(scrollLeft);
		split.setRightComponent(scrollRight);
		add(split);
	}

	public void refreshPanel() {
		left.refreshPanel();
		right.refreshPanel();
	}
}
