package edu.cmu.side.genesis.view.extract;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.view.generic.GenericLoadPanel;

public class ExtractLoadPanel extends GenericLoadPanel{

	public ExtractLoadPanel(String s){
		super();
		add.setText("New");
		add.addActionListener(new ExtractFeaturesControl.AddFilesListener(this));
		add("left", new JLabel(s));
		add("left", add);
		add("left", delete);
		add("br hfill", combo);
		add("br hfill vfill", describeScroll);
	}

	@Override
	public void setHighlight(GenesisRecipe r) {
		ExtractFeaturesControl.setHighlightedDocumentListRecipe(r);
	}

	@Override
	public GenesisRecipe getHighlight() {
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
