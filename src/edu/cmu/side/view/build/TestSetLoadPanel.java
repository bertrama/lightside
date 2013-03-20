package edu.cmu.side.view.build;

import java.awt.Dimension;
import java.io.File;
import java.util.Collections;

import javax.swing.ImageIcon;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.control.GenesisControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.view.generic.GenericLoadCSVPanel;

public class TestSetLoadPanel extends GenericLoadCSVPanel
{

	public TestSetLoadPanel(String s)
	{
		super(s);
		load.setToolTipText("Load Test Set (CSV)");
		chooser.setCurrentDirectory(new File("data"));

//		Dimension v = combo.getPreferredSize();
//		v.width = 100;
//		combo.setPreferredSize(v);
		describePanel.setMinimumSize(new Dimension(120, 120));
//		describePanel.setPreferredSize(new Dimension(150, 150));

		GenesisControl.addListenerToMap(RecipeManager.Stage.DOCUMENT_LIST, this);
		GenesisControl.addListenerToMap(RecipeManager.Stage.FEATURE_TABLE, this);
		GenesisControl.addListenerToMap(RecipeManager.Stage.MODIFIED_TABLE, this);
	
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
		verifyTestSet();
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
		
		revalidate();
	}

	/**
	 * 
	 */
	protected void verifyTestSet()
	{
		Recipe trainRecipe = BuildModelControl.getHighlightedFeatureTableRecipe();
		DocumentList testList= (DocumentList) BuildModelControl.getValidationSettings().get("testSet");
		
		if(trainRecipe != null && testList != null)
		{
			DocumentList trainList = trainRecipe.getDocumentList();
			if(!Collections.disjoint(trainList.getFilenames(), testList.getFilenames()))
			{
				setWarning("Test set overlaps with training set.");
			}
			else
			{
				clearWarning();
			}
		}
		else
		{
			clearWarning();
		}
	}

	@Override
	public void loadNewItem()
	{
		loadNewDocumentsFromCSV();
	}
	
}
