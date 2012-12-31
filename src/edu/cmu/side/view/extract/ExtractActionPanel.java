package edu.cmu.side.view.extract;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
		name.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ExtractFeaturesControl.setNewName(name.getText());
			}
		});
		add.setText("Extract");
		//Doesn't update the backend when the threshold changes!!
		threshold.setText("5");
		settings.add("left", new JLabel("Rare Threshold:"));
		settings.add("left", threshold);
		JPanel updaterPanel = new JPanel(new BorderLayout());
		updaterPanel.setPreferredSize(new Dimension(150,30));
		updaterPanel.add(BorderLayout.CENTER, (SwingUpdaterLabel)ExtractFeaturesControl.getUpdater());
		updaters.add("right", updaterPanel);
		add.addActionListener(new ExtractFeaturesControl.BuildTableListener(progressBar, threshold));
	}
	
	public void refreshPanel(){
		add.setEnabled(ExtractFeaturesControl.hasHighlightedDocumentList());
	}
}
