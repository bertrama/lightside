package edu.cmu.side.view.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.cmu.side.view.util.AbstractListPanel;

public class GenericTripleFrame extends JPanel{

	JSplitPane bigSplit = new JSplitPane();
	JSplitPane smallSplit = new JSplitPane();
	JScrollPane scroll;
	ArrayList<AbstractListPanel> panels = new ArrayList<AbstractListPanel>();

	public GenericTripleFrame(AbstractListPanel chooseData, AbstractListPanel choosePlugin, AbstractListPanel chooseSettings){
		bigSplit.setLeftComponent(chooseData);
		smallSplit.setLeftComponent(choosePlugin);
		scroll = new JScrollPane(chooseSettings);
		smallSplit.setRightComponent(scroll);
		bigSplit.setRightComponent(smallSplit);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		bigSplit.setBorder(BorderFactory.createEmptyBorder());
		smallSplit.setBorder(BorderFactory.createEmptyBorder());
		scroll.setBackground(new Color(246,246,246));
		chooseData.setPreferredSize(new Dimension(275, 450));
		smallSplit.setPreferredSize(new Dimension(650, 450));
		choosePlugin.setPreferredSize(new Dimension(300, 450));
		scroll.setPreferredSize(new Dimension(325,450));

		panels.add(chooseData);
		panels.add(choosePlugin);
		panels.add(chooseSettings);
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, bigSplit);
	}
	
	public void refreshPanel(){
		for(AbstractListPanel panel : panels){
			panel.refreshPanel();
		}
		scroll.repaint();
	}
}
