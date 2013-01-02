package edu.cmu.side.view.build;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.view.util.AbstractListPanel;

public class BuildPluginPanel extends AbstractListPanel {

	public JPanel panel = new JPanel();
	public JPanel middle = new JPanel();
	public BuildPluginPanel(BuildActionPanel.NameListener listener){
		setLayout(new BorderLayout());
		middle.setLayout(new BorderLayout());
		panel.setLayout(new RiverLayout());
		panel.add("left", new JLabel("Selected Learner:"));
		panel.add("br hfill", combo);
		Workbench.reloadComboBoxContent(combo, BuildModelControl.getLearningPlugins(), null);
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					LearningPlugin r = (LearningPlugin)combo.getSelectedItem();
					BuildModelControl.setHighlightedLearningPlugin(r);
					middle.removeAll();
					middle.add(BorderLayout.CENTER, r.getConfigurationUI());	
				}
			}
		});
		combo.addActionListener(listener);
		if(combo.getModel().getSize() > 0){
			combo.setSelectedIndex(0);
		}
		add(BorderLayout.NORTH, panel);
		add(BorderLayout.CENTER, middle);
	}
	
	public void refreshPanel(){
	}
}
