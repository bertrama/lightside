package edu.cmu.side.view.modify;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import edu.cmu.side.control.ModifyFeaturesControl;
import edu.cmu.side.view.util.ActionBar;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class ModifyActionPanel extends ActionBar{

	public ModifyActionPanel(){
		super();
		add.setText("Filter");
		add.addActionListener(new ModifyFeaturesControl.FilterTableListener(progressBar, name));
		name.setText("filtered");
		JPanel updaterPanel = new JPanel(new BorderLayout());
		updaterPanel.setPreferredSize(new Dimension(150,30));
		updaterPanel.add((SwingUpdaterLabel)ModifyFeaturesControl.getUpdater());
		updaters.add("right", updaterPanel);
	}

	public void refreshPanel(){
		add.setEnabled(ModifyFeaturesControl.getFilterPlugins().values().contains(Boolean.TRUE));
	}
}
