package edu.cmu.side.view.restructure;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.control.RestructureTablesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.view.generic.ActionBar;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class RestructureActionPanel extends ActionBar{

	static JTextField threshold = new JTextField(2);

	public RestructureActionPanel(StatusUpdater update){
		super("restructure",Stage.MODIFIED_TABLE, update);
		actionButton.setText("Restructure");
		actionButton.setIcon(new ImageIcon("toolkits/icons/application_side_expand.png"));
		actionButton.setIconTextGap(10);
		actionButton.addActionListener(new RestructureTablesControl.FilterTableListener(this, name));

		threshold.setText("5");
		settings.add("left", new JLabel("Rare Threshold:"));
		settings.add("left", threshold);
		name.setText(getDefaultName());
	}

	public void refreshPanel(){
		super.refreshPanel();
		actionButton.setEnabled(RestructureTablesControl.getFilterPlugins().values().contains(Boolean.TRUE)
								&& RestructureTablesControl.getHighlightedFeatureTableRecipe() != null);
	}

	@Override
	public void startedTask()
	{
	}

	@Override
	public void endedTask()
	{
	}
}
