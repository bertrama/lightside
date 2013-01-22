package edu.cmu.side.view.explore;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.plugin.EvaluateOneModelPlugin;
import edu.cmu.side.view.generic.ActionBar;

public class ExploreActionBar extends ActionBar{
	
	public ExploreActionBar(StatusUpdater update){
		super(update);
		removeAll();
		setLayout(new RiverLayout());
		setBackground(Color.white);
		combo = new JComboBox();
		if(combo.getItemCount()>0 && combo.getSelectedIndex()==-1){
			combo.setSelectedIndex(0);
		}
		combo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(combo.getSelectedIndex() + " selected EAB28");
				ExploreResultsControl.setHighlightedModelAnalysisPlugin((EvaluateOneModelPlugin)combo.getSelectedItem());
				Workbench.update();
			}
		});
		add("left", new JLabel("Comparison Plugin:"));
		add("hfill", combo);
		EvaluateOneModelPlugin plug = (ExploreResultsControl.getModelAnalysisPlugins().keySet().size()>0?
				ExploreResultsControl.getModelAnalysisPlugins().keySet().toArray(new EvaluateOneModelPlugin[0])[0]:null);
		Workbench.reloadComboBoxContent(combo, ExploreResultsControl.getModelAnalysisPlugins().keySet(), plug);
	}

	@Override
	public void startedTask()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endedTask()
	{
		// TODO Auto-generated method stub
		
	}
	
}
