package edu.cmu.side.view.generic;

import java.io.File;

import javax.swing.ImageIcon;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;

public abstract class GenericLoadCSVPanel extends GenericLoadPanel
{
	public GenericLoadCSVPanel(String title)
	{
		super(title);
		configureLoadCSVPanel();
	}

	protected void configureLoadCSVPanel()
	{
		this.remove(save);
		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_page.png");
		load.setIcon(iconLoad);
	}
	
	public GenericLoadCSVPanel(String title, boolean showLoad, boolean showDelete, boolean showSave, boolean showDescription)
	{
		super(title, showLoad, showDelete, showSave, showDescription);
		configureLoadCSVPanel();
	}

	@Override
	public void refreshPanel()
	{
		refreshPanel(Workbench.getRecipesByPane(Stage.DOCUMENT_LIST));
	}

	@Override
	public void loadNewItem()
	{
		loadNewDocumentsFromCSV();
	}

	@Override
	public void checkChooser()
	{
		if(chooser == null)
		{
			super.checkChooser();
			chooser.setCurrentDirectory(new File("data"));
		}
	}

}
