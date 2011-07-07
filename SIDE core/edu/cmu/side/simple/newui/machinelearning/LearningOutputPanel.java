package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Color;

import javax.swing.JPanel;

/** In progress. Will hold the output of the model building process, including accuracy, kappa, etc. */
public class LearningOutputPanel extends JPanel{
	private static final long serialVersionUID = -3937539493535019798L;

	public LearningOutputPanel(){
		setBackground(Color.red);
	}

	public void refreshPanel(){}
}
