package edu.cmu.side.view.generic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.view.util.AbstractListPanel;

public abstract class ActionBar extends AbstractListPanel{
	
	protected JButton actionButton = new JButton();
	protected JButton cancel = new JButton();
	
	protected JProgressBar progressBar = new JProgressBar();
	protected JTextField name = new JTextField(5);
	protected JLabel nameLabel = new JLabel("Name:");
	protected JPanel settings = new JPanel(new RiverLayout());
	protected JComboBox combo;
	protected JPanel updaters = new JPanel(new RiverLayout());
	Font font = new Font(Font.SANS_SERIF, Font.BOLD, 20);

	JPanel left = new JPanel(new RiverLayout());
	JPanel middle = new JPanel(new RiverLayout());
	JPanel right = new JPanel(new RiverLayout());
	
	protected StatusUpdater update;

	public ActionBar(StatusUpdater update)
	{
		this.update = update;
		
		setLayout(new RiverLayout());
		actionButton.setFont(font);
		setBackground(Color.white);
		setBorder(BorderFactory.createLineBorder(Color.gray));
		settings.setBackground(Color.white);
		updaters.setBackground(Color.white);
		settings.add("left", nameLabel);
		settings.add("left", name);
		progressBar.setPreferredSize(new Dimension(50,25));
		updaters.add("hfill", (Component)update);
		updaters.add("right", progressBar);
		progressBar.setVisible(false);
		ImageIcon iconCancel = new ImageIcon("toolkits/icons/cancel.png");
		cancel.setText("");
		cancel.setIcon(iconCancel);
		cancel.setEnabled(false);
		cancel.setToolTipText("Cancel");
		right.add("hfill", updaters);
		right.add("left", cancel);
		right.setBackground(Color.white);
		add("left", actionButton);
		add("hfill", settings);
		add("left", right);
	}

	public void refreshPanel(){
	}

	public abstract void startedTask();

	public abstract void endedTask();
}
