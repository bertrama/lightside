package edu.cmu.side.view.predict;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import edu.cmu.side.control.PredictLabelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.generic.GenericLoadPanel;
import edu.cmu.side.view.util.CheckBoxList;

public class PredictNewDataPanel extends GenericLoadPanel{

	CheckBoxList textColumnsList = new CheckBoxList();
	JScrollPane textColumnsScroll = new JScrollPane(textColumnsList);

	public PredictNewDataPanel(){
		super();
		add.setText("Load");
		add("left", new JLabel("Unlabeled Data:"));
		add("left", add);
		add("left", delete);
		add("br hfill", combo);
		add("br hfill vfill", describeScroll);
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
	public String getHighlightDescription() {
		return getHighlight().getDocumentList().getDescriptionString();
	}

	@Override
	public void refreshPanel() {
		refreshPanel(PredictLabelsControl.getUnlabeledDataRecipes());
	}

}
