package edu.cmu.side.view.explore;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.plugin.EvaluateOneModelPlugin;
import edu.cmu.side.view.util.ActionBar;

public class ExploreActionBar extends ActionBar{
	
	public ExploreActionBar(){
		removeAll();
		setLayout(new RiverLayout());
		setBackground(Color.white);
		combo = new JComboBox();
		combo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ExploreResultsControl.setHighlightedModelAnalysisPlugin((EvaluateOneModelPlugin)combo.getSelectedItem());
			}
		});
		add("left", new JLabel("Analysis Plugin:"));
		add("hfill", combo);
		EvaluateOneModelPlugin plug = (ExploreResultsControl.getModelAnalysisPlugins().keySet().size()>0?
				ExploreResultsControl.getModelAnalysisPlugins().keySet().toArray(new EvaluateOneModelPlugin[0])[0]:null);
		Workbench.reloadComboBoxContent(combo, ExploreResultsControl.getModelAnalysisPlugins().keySet(), plug);
	}
	
}
