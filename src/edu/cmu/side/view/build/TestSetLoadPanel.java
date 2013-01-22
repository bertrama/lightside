package edu.cmu.side.view.build;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import com.yerihyo.yeritools.io.FileToolkit;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.view.generic.GenericLoadPanel;

public class TestSetLoadPanel extends GenericLoadPanel
{

	public TestSetLoadPanel(String s)
	{
		super(s, true, true, false);
		load.setToolTipText("Load Test Set");
		chooser.setCurrentDirectory(new File("data"));

		describePanel.setPreferredSize(new Dimension(120, 120));
		remove(save);
		revalidate();

	}

	@Override
	public void setHighlight(Recipe r)
	{
		if(r != null)
		{
			BuildModelControl.updateValidationSetting("testRecipe", r);
			BuildModelControl.updateValidationSetting("testSet", r.getDocumentList());
		}
		else
		{
			BuildModelControl.updateValidationSetting("testRecipe", null);
			BuildModelControl.updateValidationSetting("testSet", null);
		}
	}

	@Override
	public Recipe getHighlight()
	{
		return (Recipe) BuildModelControl.getValidationSettings().get("testRecipe");
	}

	@Override
	public void refreshPanel()
	{
		refreshPanel(BuildModelControl.getDocumentLists());
	}

	@Override
	public void loadNewItem()
	{
		loadNewDocumentsFromCSV();
	}
	
}
