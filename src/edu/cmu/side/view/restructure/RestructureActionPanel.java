package edu.cmu.side.view.restructure;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import edu.cmu.side.control.RestructureTablesControl;
import edu.cmu.side.view.util.ActionBar;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class RestructureActionPanel extends ActionBar{

	public RestructureActionPanel(){
		super();
		add.setText("Restructure");
		add.addActionListener(new RestructureTablesControl.FilterTableListener(progressBar, name));
		name.setText("restructured");
	}

	public void refreshPanel(){
		super.refreshPanel();
		add.setEnabled(RestructureTablesControl.getFilterPlugins().values().contains(Boolean.TRUE));
	}
}
