package edu.cmu.side.view.predict;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.GenesisControl;
import edu.cmu.side.control.PredictLabelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.view.generic.ActionBar;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.util.DocumentListTableModel;
import edu.cmu.side.view.util.Refreshable;

public class PredictLabelsPane extends JPanel implements Refreshable
{

	JCheckBox useValidationBox = new JCheckBox("Add Validation Results to Test Data");
	GenericLoadPanel load = new GenericLoadPanel("Model to Apply:")
	{

		{
			checkChooser();
			chooser.addChoosableFileFilter(GenericLoadPanel.trainedFilter);
		}

		@Override
		public void setHighlight(Recipe r)
		{
			PredictLabelsControl.setHighlightedTrainedModelRecipe(r);
			Workbench.update(this);
		}

		@Override
		public Recipe getHighlight()
		{
			return PredictLabelsControl.getHighlightedTrainedModelRecipe();
		}

		@Override
		public void refreshPanel()
		{
			refreshPanel(Workbench.getRecipesByPane(RecipeManager.Stage.TRAINED_MODEL, RecipeManager.Stage.PREDICTION_ONLY));
		}

	};

	ActionBar actionBar = new PredictActionBar(PredictLabelsControl.getUpdater());

	PredictNewDataPanel newData = new PredictNewDataPanel();
	PredictOutputPanel output = new PredictOutputPanel();
	DocumentListTableModel docTableModel = new DocumentListTableModel(null);
	JTable docDisplay = new JTable(docTableModel);

	public PredictLabelsPane()
	{
		setLayout(new BorderLayout());
		JSplitPane pane = new JSplitPane();

		JPanel newDataPanel = new JPanel(new BorderLayout());
		newDataPanel.add(useValidationBox, BorderLayout.NORTH);
		newDataPanel.add(newData, BorderLayout.CENTER);
		useValidationBox.setToolTipText("Add the predictions from the trained-model validation to a copy of the test data.");
		useValidationBox.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean validation = useValidationBox.isSelected();
				newData.setEnabled(!validation);
				PredictLabelsControl.setUseValidationResults(validation);

				if (validation)
				{
					PredictLabelsControl.setHighlightedUnlabeledData(PredictLabelsControl.getHighlightedTrainedModelRecipe());
				}
				else
				{
					newData.refreshPanel();
					PredictLabelsControl.setHighlightedUnlabeledData(newData.getSelectedItem());
				}

				Workbench.update(newData);

			}
		});

		JPanel left = new JPanel(new GridLayout(2, 1));
		left.add(load);
		left.add(newDataPanel);
		pane.setLeftComponent(left);
		pane.setRightComponent(output);
		Dimension minimumSize = new Dimension(50, 200);
		left.setMinimumSize(minimumSize);
		output.setMinimumSize(minimumSize);
		pane.setDividerLocation(300);
		add(BorderLayout.CENTER, pane);
		add(BorderLayout.SOUTH, actionBar);

		// TODO: why can't these each be (parameterized) in genericLoadPane?
		GenesisControl.addListenerToMap(RecipeManager.Stage.TRAINED_MODEL, load);
		GenesisControl.addListenerToMap(RecipeManager.Stage.PREDICTION_ONLY, load);
		GenesisControl.addListenerToMap(RecipeManager.Stage.DOCUMENT_LIST, newData);
		GenesisControl.addListenerToMap(RecipeManager.Stage.DOCUMENT_LIST, output);
		GenesisControl.addListenerToMap(RecipeManager.Stage.PREDICTION_RESULT, actionBar);

		GenesisControl.addListenerToMap(load, actionBar);
		GenesisControl.addListenerToMap(newData, actionBar);
		GenesisControl.addListenerToMap(newData, output);
		GenesisControl.addListenerToMap(actionBar, output);

		GenesisControl.addListenerToMap(load, this);
		GenesisControl.addListenerToMap(RecipeManager.Stage.TRAINED_MODEL, this);
		GenesisControl.addListenerToMap(RecipeManager.Stage.PREDICTION_ONLY, this);

	}

	public void refreshPanel()
	{
//		load.refreshPanel();
//		newData.refreshPanel();
//		output.refreshPanel(PredictLabelsControl.getHighlightedUnlabeledData());
//		actionBar.refreshPanel();

		Recipe modelRecipe = PredictLabelsControl.getHighlightedTrainedModelRecipe();
		if (modelRecipe != null) 
		{
			if (modelRecipe.getStage() == Stage.PREDICTION_ONLY)
			{
				useValidationBox.setSelected(false);
				useValidationBox.setEnabled(false);
			}
			else
			{
				useValidationBox.setEnabled(true);
			}
		}
		
	}
}
