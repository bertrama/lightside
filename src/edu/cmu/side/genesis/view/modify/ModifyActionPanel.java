package edu.cmu.side.genesis.view.modify;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.view.generic.ActionBar;
import edu.cmu.side.genesis.view.generic.SwingUpdaterLabel;

public class ModifyActionPanel extends ActionBar{

	public ModifyActionPanel(){
		super();
		add.setText("Filter");
		add.addActionListener(new ModifyFeaturesControl.FilterTableListener(progressBar));
		name.setText("filtered");
		updaters.add("left", (SwingUpdaterLabel)ModifyFeaturesControl.getUpdater());
	}
	
	public void refreshPanel(){
		add.setEnabled(ModifyFeaturesControl.getFilterPlugins().values().contains(Boolean.TRUE));
	}
}
