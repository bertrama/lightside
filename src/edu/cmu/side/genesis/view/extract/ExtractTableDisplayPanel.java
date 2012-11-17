package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.simple.newui.AbstractListPanel;

public class ExtractTableDisplayPanel extends AbstractListPanel {
	
	public ExtractTableDisplayPanel(){
		setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane();
		JPanel checklist = new JPanel(new RiverLayout());
		JPanel explore = new JPanel(new RiverLayout());
		checklist.add("left", new JLabel("Evaluations to Display:"));
		explore.add("left", new JLabel("Features in Table:"));
		split.setLeftComponent(checklist);
		split.setRightComponent(explore);
		checklist.setPreferredSize(new Dimension(275,200));
		explore.setPreferredSize(new Dimension(350, 200));
		add(BorderLayout.CENTER, split);
	}
	
	public void refreshPanel(){
		
	}
}
