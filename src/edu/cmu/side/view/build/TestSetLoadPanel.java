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
		super(s);
		ImageIcon iconDelete = new ImageIcon("toolkits/icons/cross.png");
		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_table.png");
		delete.setText("");
		delete.setIcon(iconDelete);
		delete.setToolTipText("Delete Document List");
		delete.setBorderPainted(true);
		load.setText("");
		load.setToolTipText("Load Test Set");
		load.setIcon(iconLoad);
		chooser = new JFileChooser("data");

		describePanel.setPreferredSize(new Dimension(120, 120));
		remove(save);
		revalidate();

		// load.addActionListener(new
		// ExtractFeaturesControl.AddFilesListener(this));
		// load.setBorderPainted(true);
		// label = new JLabel(s);
		// add("hfill", label);
		// add("right", load);
		// add("br hfill", combo);
		// add("left", delete);
		// add("br hfill vfill", describePanel);

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
		chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[] { "csv" }, true));
		chooser.setMultiSelectionEnabled(true);
		int result = chooser.showOpenDialog(TestSetLoadPanel.this);
		if (result != JFileChooser.APPROVE_OPTION) { return; }

		File[] selectedFiles = chooser.getSelectedFiles();
		HashSet<String> docNames = new HashSet<String>();

		for (File f : selectedFiles)
		{
			docNames.add(f.getPath());
		}

		DocumentList testDocs = new DocumentList(docNames);
		Recipe r = RecipeManager.fetchDocumentListRecipe(testDocs);
		setHighlight(r);

		Workbench.update();
	}
}
