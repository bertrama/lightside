package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

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
		split.setRightComponent(pluginConfig);
		add(BorderLayout.CENTER, split);
		
		threshold.setText("5");

		JPanel pan = new JPanel(new RiverLayout());
		pan.add("left", new JLabel("Table Name:"));
		pan.add("left", tableName);
		pan.add("left", new JLabel("Rare Threshold:"));
		pan.add("left", threshold);
		pan.add("left", progressBar);
		pan.add("left", addButton);
		add(BorderLayout.SOUTH, pan);
	}
	
	public void refreshPanel(){
		
	}
}
