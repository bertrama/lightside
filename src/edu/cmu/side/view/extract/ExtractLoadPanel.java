package edu.cmu.side.view.extract;


import javax.swing.ImageIcon;
import javax.swing.JLabel;

import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.generic.GenericLoadPanel;

public class ExtractLoadPanel extends GenericLoadPanel{

	public ExtractLoadPanel(String s){
		super();		
		ImageIcon iconDelete = new ImageIcon("toolkits/icons/cross.png");
		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_table.png");
		delete.setText("");
		delete.setIcon(iconDelete);
		delete.setToolTipText("Delete");
		load.setText("");
		load.setToolTipText("Open");
		load.setIcon(iconLoad);

		load.addActionListener(new ExtractFeaturesControl.AddFilesListener(this));
		add("left", load);
		add("hfill", combo);
		add("left", delete);
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
