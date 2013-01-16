package edu.cmu.side.view.build;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.view.util.ActionBar;
import edu.cmu.side.view.util.RadioButtonListEntry;

public class BuildActionPanel extends ActionBar {

	JCheckBox featureSelection = new JCheckBox("Feature Selection?");
	JTextField numFeatures = new JTextField(5);
	JLabel numLabel = new JLabel("#:");

	public BuildActionPanel(StatusUpdater update){
		super(update);
		add.setText("Train");
		add.setIcon(new ImageIcon("toolkits/icons/chart_curve.png"));
		add.setIconTextGap(10);
		add.addActionListener(new BuildModelControl.TrainModelListener(progressBar, name));
		settings.add("left", featureSelection);
		settings.add("left", numLabel);
		settings.add("left", numFeatures);
		numLabel.setVisible(false);
		numFeatures.setVisible(false);
		featureSelection.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				numFeatures.setVisible(featureSelection.isSelected());
				numLabel.setVisible(featureSelection.isSelected());
			}
		});
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
