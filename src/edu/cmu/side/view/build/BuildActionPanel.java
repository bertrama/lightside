package edu.cmu.side.view.build;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.WrapperPlugin;
import edu.cmu.side.view.generic.ActionBar;
import edu.cmu.side.view.util.RadioButtonListEntry;

public class BuildActionPanel extends ActionBar {

	JLabel trainingLabel = new JLabel();

	public BuildActionPanel(StatusUpdater update){
		super("model", Stage.TRAINED_MODEL, update);
		actionButton.setText("Train");
		actionButton.setIcon(new ImageIcon("toolkits/icons/chart_curve.png"));
		actionButton.setIconTextGap(10);
		actionButton.addActionListener(new BuildModelControl.TrainModelListener(this, name));
		
		Collection<WrapperPlugin> wrappers = new TreeSet<WrapperPlugin>(BuildModelControl.getWrapperPlugins().keySet());
		for(WrapperPlugin wrapper : wrappers){
			settings.add("left", wrapper.getConfigurationUI());
		}

		trainingLabel.setIcon(new ImageIcon("toolkits/icons/training.gif"));
		trainingLabel.setVisible(false);
		settings.add("left", trainingLabel);
		
		updaters.removeAll();
		updaters.add("right", (Component) update);
		updaters.add("right", trainingLabel);
		name.setText(getDefaultName());
		
		right.remove(cancel);
	}
	
	public class NameListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent ae) {
			if(ae.getSource() instanceof RadioButtonListEntry){
				Object o = ((RadioButtonListEntry)ae.getSource()).getValue();
				if(o instanceof SIDEPlugin){
					setDefaultName(((SIDEPlugin)o).getOutputName());
					refreshPanel();
				}
			}
		}
	}

	public void refreshPanel(){
		super.refreshPanel();
		
		Recipe table = BuildModelControl.getHighlightedFeatureTableRecipe();
		LearningPlugin learner = BuildModelControl.getHighlightedLearningPlugin();
		actionButton.setEnabled(table != null && learner.supportsClassType(table.getTrainingTable().getClassValueType()));
	}

	@Override
	public void startedTask()
	{
		trainingLabel.setVisible(true);
	}

	@Override
	public void endedTask()
	{
		trainingLabel.setVisible(false);
	}

}
