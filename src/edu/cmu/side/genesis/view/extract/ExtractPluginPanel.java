package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;

import se.datadosen.component.RiverLayout;

public class ExtractPluginPanel extends JPanel{

	ExtractPluginChecklistPanel pluginChecklist;
	ExtractPluginConfigPanel pluginConfig;
	JSplitPane split;

	static JButton addButton = new JButton("Extract Features");
	public static JProgressBar progressBar = new JProgressBar();
	static JTextField tableName = new JTextField(5);
	static JTextField threshold = new JTextField(2);

	public ExtractPluginPanel(){
		setLayout(new BorderLayout());
		split = new JSplitPane();
		pluginChecklist = new ExtractPluginChecklistPanel();
		pluginConfig = new ExtractPluginConfigPanel();
		pluginChecklist.setPreferredSize(new Dimension(300, 450));
		pluginConfig.setPreferredSize(new Dimension(325,450));
		split.setLeftComponent(pluginChecklist);
		split.setRightComponent(new JScrollPane(pluginConfig));
		add(BorderLayout.CENTER, split);
		
		ExtractFeaturesControl.setThreshold(5);

		//Doesn't update the backend when the threshold changes!!
		threshold.setText(""+ExtractFeaturesControl.getThreshold());
		JPanel pan = new JPanel(new RiverLayout());
		pan.add("left", new JLabel("Table Name:"));
		pan.add("left", tableName);
		pan.add("left", new JLabel("Rare Threshold:"));
		pan.add("left", threshold);
		pan.add("left", progressBar);
		pan.add("left", addButton);
		addButton.addActionListener(new ExtractFeaturesControl.BuildTableListener(progressBar));
		add(BorderLayout.SOUTH, pan);
	}
	
	public void refreshPanel(){
		pluginChecklist.refreshPanel();
		pluginConfig.refreshPanel();
	}
}