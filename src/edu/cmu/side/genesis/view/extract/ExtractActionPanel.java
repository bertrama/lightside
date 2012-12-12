package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.view.generic.ActionBar;
import edu.cmu.side.genesis.view.generic.SwingUpdaterLabel;

import se.datadosen.component.RiverLayout;

public class ExtractActionPanel extends ActionBar{

	static JTextField threshold = new JTextField(2);

	public ExtractActionPanel(){
		name.setText("features");
		add.setText("Extract");
		//Doesn't update the backend when the threshold changes!!
		threshold.setText("5");
		settings.add("left", new JLabel("Rare Threshold:"));
		settings.add("left", threshold);
		updaters.add("left", (SwingUpdaterLabel)ExtractFeaturesControl.getUpdater());
		add.addActionListener(new ExtractFeaturesControl.BuildTableListener(progressBar, name, threshold));
	}
	
	public void refreshPanel(){
		add.setEnabled(ExtractFeaturesControl.hasHighlightedDocumentList());
	}
}
