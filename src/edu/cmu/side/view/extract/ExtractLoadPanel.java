package edu.cmu.side.view.extract;


import javax.swing.JLabel;

import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.generic.GenericLoadPanel;

public class ExtractLoadPanel extends GenericLoadPanel{

	public ExtractLoadPanel(String s){
		super();
		add.setText("New");
		label = new JLabel(s);
		add.addActionListener(new ExtractFeaturesControl.AddFilesListener(this));
		add("left", label);
		add("left", add);
		add("left", delete);
		add("br hfill", combo);
		add("br hfill vfill", describeScroll);
	}

	@Override
	public void setHighlight(Recipe r) {
		ExtractFeaturesControl.setHighlightedDocumentListRecipe(r);
	}

	@Override
	public Recipe getHighlight() {
		return ExtractFeaturesControl.getHighlightedDocumentListRecipe();
	}

	@Override
	public String getHighlightDescription() {
		return getHighlight().getDocumentList().getDescriptionString();
	}

	@Override
	public void refreshPanel() {		
		refreshPanel(ExtractFeaturesControl.getDocumentLists());
	}
}
