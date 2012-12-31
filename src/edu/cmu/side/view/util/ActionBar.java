package edu.cmu.side.view.util;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;

public abstract class ActionBar extends JPanel{
	
	protected JButton add = new JButton();
	protected JProgressBar progressBar = new JProgressBar();
	protected JTextField name = new JTextField(5);
	protected JLabel nameLabel = new JLabel("Name:");
	protected JPanel settings = new JPanel(new RiverLayout());
	protected JPanel updaters = new JPanel(new RiverLayout());
	
	public ActionBar(){
		setLayout(new RiverLayout());
		settings.add("left", nameLabel);
		settings.add("left", name);
		updaters.add("center hfill", progressBar);
		
		add("left", settings);
		add("hfill", updaters);
		add("left", add);
	}
	
	public String getName(){
		return name.getText();
	}
	public abstract void refreshPanel();
}
