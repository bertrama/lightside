package edu.cmu.side.view.extract;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.view.generic.ActionBar;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class ExtractActionPanel extends ActionBar{

	static JTextField threshold = new JTextField(2);

	public ExtractActionPanel(StatusUpdater update){
		super(update);
		name.setText("features");
		actionButton.setText("Extract");
		actionButton.setIcon(new ImageIcon("toolkits/icons/application_view_columns.png"));
		//Doesn't update the backend when the threshold changes!!
		threshold.setText("5");
		settings.add("left", new JLabel("Rare Threshold:"));
		settings.add("left", threshold);
		actionButton.addActionListener(new ExtractFeaturesControl.BuildTableListener(this, threshold, name));
	}
	
	public void refreshPanel(){
		super.refreshPanel();
		actionButton.setEnabled(ExtractFeaturesControl.hasHighlightedDocumentList());
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
