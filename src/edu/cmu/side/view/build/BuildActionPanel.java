package edu.cmu.side.view.build;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.view.util.ActionBar;
import edu.cmu.side.view.util.RadioButtonListEntry;

public class BuildActionPanel extends ActionBar {

	public BuildActionPanel(StatusUpdater update){
		super(update);
		add.setText("Train");
		add.setIcon(new ImageIcon("toolkits/icons/chart_curve.png"));
		add.setIconTextGap(10);
		add.addActionListener(new BuildModelControl.TrainModelListener(progressBar, name));
		name.setText("model");
	}
	
	public class NameListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent ae) {
			if(ae.getSource() instanceof RadioButtonListEntry){
				Object o = ((RadioButtonListEntry)ae.getSource()).getValue();
				if(o instanceof SIDEPlugin){
					name.setText(((SIDEPlugin)o).getOutputName());
				}
			}
		}
		
	}

	public void refreshPanel(){
		super.refreshPanel();
		add.setEnabled(BuildModelControl.hasHighlightedFeatureTableRecipe());
	}
}
