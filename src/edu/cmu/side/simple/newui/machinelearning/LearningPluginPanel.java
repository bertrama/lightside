package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.newui.AbstractListPanel;

/**
 * Pick a plugin for machine learning. Currently only weka and svmlight.
 * @author emayfiel
 *
 */
public class LearningPluginPanel extends AbstractListPanel{
	private static final long serialVersionUID = -7148504589131163340L;

	JComboBox pluginList = new JComboBox();
	DefaultComboBoxModel listModel = new DefaultComboBoxModel();
	static LearningPlugin clickedPlugin = null;
	JPanel configPanel = new JPanel();
	
	public LearningPluginPanel(){
		pluginList.setModel(listModel);
		add("left", new JLabel("Model Building Plugin: "));
		add("br left hfill", pluginList);
		add("br left hfill", configPanel);
		refreshPanel();
		if(pluginList.getSelectedItem() != null){
			configPanel.add(((LearningPlugin)pluginList.getSelectedItem()).getConfigurationUI());
		}
		pluginList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				clickedPlugin = (LearningPlugin)pluginList.getSelectedItem();
				if(clickedPlugin != null){
					configPanel.removeAll();
					Component c = clickedPlugin.getConfigurationUI();
					configPanel.add(c);
				}
			}
		});
		pluginList.addActionListener(this);
	}
	
	public void refreshPanel(){
		SIDEPlugin[] learners = SimpleWorkbench.getPluginsByType("model_builder");
		if(learners.length != listModel.getSize()){
			listModel.removeAllElements();
			for(SIDEPlugin learner : learners){
				listModel.addElement(learner);
			}
		}
		if(listModel.getSize() > 0 && listModel.getSelectedItem() == null){
			listModel.setSelectedItem(listModel.getElementAt(0));
		}
		clickedPlugin = (LearningPlugin)pluginList.getSelectedItem();
		
		repaint();
	}
	
	public static LearningPlugin getSelectedPlugin(){
		return clickedPlugin;
	}
	
}
