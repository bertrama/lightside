package edu.cmu.side.genesis.view.modify;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.view.ActionBar;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;

public class ModifyActionPanel extends ActionBar{

	public ModifyActionPanel(){
		super();
		add.setText("Filter");
		add.addActionListener(new ModifyFeaturesControl.FilterTableListener(progressBar));
		name.setText("filtered");
		name.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ExtractFeaturesControl.setNewName(name.getText());
			}
		});
		JPanel updaterPanel = new JPanel(new BorderLayout());
		updaterPanel.setPreferredSize(new Dimension(150,30));
		updaterPanel.add((SwingUpdaterLabel)ModifyFeaturesControl.getUpdater());
		updaters.add("right", updaterPanel);
	}

	public void refreshPanel(){
		add.setEnabled(ModifyFeaturesControl.getFilterPlugins().values().contains(Boolean.TRUE));
	}
}
