package edu.cmu.side.view.predict;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.PredictLabelsControl;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.view.util.ActionBar;

public class PredictActionBar extends ActionBar
{
	JCheckBox showDistsBox = new JCheckBox("Show Model Score");

	public PredictActionBar(StatusUpdater update)
	{
		super(update);
		actionButton.setIcon(new ImageIcon("toolkits/icons/application_form_edit.png"));
		actionButton.setText("Predict");
		nameLabel.setText("New Annotation Name:");
		name.setText("predicted");
		settings.add("left", showDistsBox);
		
		
		actionButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				PredictLabelsControl.executePredictTask(PredictActionBar.this, name.getText(), showDistsBox.isSelected());
			}
		});
		
	}

	@Override
	public void startedTask()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void endedTask()
	{
		Workbench.update();
	}
	
	@Override
	public void refreshPanel()
	{
		actionButton.setEnabled(PredictLabelsControl.hasHighlightedTrainedModelRecipe() && PredictLabelsControl.hasHighlightedUnlabeledData());
	}

}
