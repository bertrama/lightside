package edu.cmu.side.view.compare;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.control.CompareModelsControl;
import edu.cmu.side.plugin.EvaluateTwoModelPlugin;
import edu.cmu.side.view.util.ActionBar;

public class CompareActionBar extends ActionBar {

	public CompareActionBar(){
		removeAll();
		setLayout(new RiverLayout());
		setBackground(Color.white);
		combo = new JComboBox();
		combo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CompareModelsControl.setHighlightedModelComparisonPlugin((EvaluateTwoModelPlugin)combo.getSelectedItem());
			}
		});
		add("left", new JLabel("Comparison Plugin:"));
		add("hfill", combo);
		EvaluateTwoModelPlugin plug = (CompareModelsControl.getModelComparisonPlugins().keySet().size()>0?
				CompareModelsControl.getModelComparisonPlugins().keySet().toArray(new EvaluateTwoModelPlugin[0])[0]:null);
		Workbench.reloadComboBoxContent(combo, CompareModelsControl.getModelComparisonPlugins().keySet(), plug);
	}
	
}
