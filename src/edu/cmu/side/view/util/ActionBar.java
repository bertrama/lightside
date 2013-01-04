package edu.cmu.side.view.util;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;

public abstract class ActionBar extends JPanel{
	
	protected JButton add = new JButton();
	protected JButton cancel = new JButton();
	
	protected JProgressBar progressBar = new JProgressBar();
	protected JTextField name = new JTextField(5);
	protected JLabel nameLabel = new JLabel("Name:");
	protected JPanel settings = new JPanel(new RiverLayout());
	protected JComboBox combo;
	protected JPanel updaters = new JPanel(new RiverLayout());
	Font font = new Font(Font.SANS_SERIF, Font.BOLD, 20);

	public ActionBar(){
		setLayout(new RiverLayout());
		setBackground(Color.white);
		add.setFont(font);
		setBorder(BorderFactory.createLineBorder(Color.gray));
		settings.setBackground(Color.white);
		updaters.setBackground(Color.white);
		settings.add("left", nameLabel);
		settings.add("left", name);
		updaters.add("center hfill", progressBar);
		ImageIcon iconCancel = new ImageIcon("toolkits/icons/cancel.png");
		cancel.setText("");
		cancel.setIcon(iconCancel);
		cancel.setEnabled(false);
		cancel.setToolTipText("Cancel");
		updaters.add("right", cancel);
		add("left", add);
		add("left", settings);
		add("hfill", updaters);
	}

	public void refreshPanel(){
	}
}
