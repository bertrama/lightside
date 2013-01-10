package edu.cmu.side.view.extract;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.view.util.ActionBar;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class ExtractActionPanel extends ActionBar{

	static JTextField threshold = new JTextField(2);

	public ExtractActionPanel(){
		name.setText("features");
		add.setText("Extract");
		add.setIcon(new ImageIcon("toolkits/icons/application_view_columns.png"));
		//Doesn't update the backend when the threshold changes!!
		threshold.setText("5");
		settings.add("left", new JLabel("Rare Threshold:"));
		settings.add("left", threshold);
		add.addActionListener(new ExtractFeaturesControl.BuildTableListener(progressBar, threshold, name));
	}
	
	public void refreshPanel(){
		super.refreshPanel();
		add.setEnabled(ExtractFeaturesControl.hasHighlightedDocumentList());
	}
}
