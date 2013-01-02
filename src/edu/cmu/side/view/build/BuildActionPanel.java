package edu.cmu.side.view.build;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.view.util.ActionBar;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class BuildActionPanel extends ActionBar {

	public BuildActionPanel(){
		add.setText("Train");
		add.addActionListener(new BuildModelControl.TrainModelListener(progressBar, name));
		name.setText("model");
	}
	
	public class NameListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent ae) {
			if(ae.getSource() instanceof JComboBox){
				Object o = ((JComboBox)ae.getSource()).getSelectedItem();
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
