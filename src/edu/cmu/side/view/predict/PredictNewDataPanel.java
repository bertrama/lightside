package edu.cmu.side.view.predict;

import java.io.File;

import javax.swing.JScrollPane;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.PredictLabelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.util.SelectPluginList;

public class PredictNewDataPanel extends GenericLoadPanel
{

	SelectPluginList textColumnsList = new SelectPluginList();
	JScrollPane textColumnsScroll = new JScrollPane(textColumnsList);

	public PredictNewDataPanel()
	{
		super("Unlabeled Data:");
		this.remove(save);
		chooser.setCurrentDirectory(new File("data"));
	}

	@Override
	public void setHighlight(Recipe r)
	{
		PredictLabelsControl.setHighlightedUnlabeledData(r);
		Workbench.update(this);
	}

	@Override
	public Recipe getHighlight()
	{
		return PredictLabelsControl.getHighlightedUnlabeledData();
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
}
