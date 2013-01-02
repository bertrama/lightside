package edu.cmu.side.view.predict;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import edu.cmu.side.control.PredictLabelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.util.SelectPluginList;

public class PredictNewDataPanel extends GenericLoadPanel{

	SelectPluginList textColumnsList = new SelectPluginList();
	JScrollPane textColumnsScroll = new JScrollPane(textColumnsList);

	public PredictNewDataPanel(){
		super();
		add.setText("Load");
		add("left", new JLabel("Unlabeled Data:"));
		add("left", add);
		add("left", delete);
		add("br hfill", combo);
		describeScroll = new JScrollPane();
		describePanel.add(BorderLayout.CENTER, describeScroll);
		add("br hfill vfill", describePanel);

	}
	@Override
	public void setHighlight(Recipe r) {
		PredictLabelsControl.setHighlightedUnlabeledData(r);
	}

	@Override
	public Recipe getHighlight() {
		return PredictLabelsControl.getHighlightedUnlabeledData();
	}
	
	@Override
	public void refreshPanel() {
		refreshPanel(PredictLabelsControl.getUnlabeledDataRecipes());
	}

}
