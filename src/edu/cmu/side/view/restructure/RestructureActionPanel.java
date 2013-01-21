package edu.cmu.side.view.restructure;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.control.RestructureTablesControl;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.view.util.ActionBar;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class RestructureActionPanel extends ActionBar{

	public RestructureActionPanel(StatusUpdater update){
		super(update);
		actionButton.setText("Restructure");
		actionButton.setIcon(new ImageIcon("toolkits/icons/application_side_expand.png"));
		actionButton.setIconTextGap(10);
		actionButton.addActionListener(new RestructureTablesControl.FilterTableListener(this, name));
		name.setText("restructured");
	}

	public void refreshPanel(){
		super.refreshPanel();
		actionButton.setEnabled(RestructureTablesControl.getFilterPlugins().values().contains(Boolean.TRUE));
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
