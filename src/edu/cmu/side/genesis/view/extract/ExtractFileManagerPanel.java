package edu.cmu.side.genesis.view.extract;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.CheckBoxList;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;

public class ExtractFileManagerPanel extends AbstractListPanel{

	public ExtractFileManagerPanel(){
		super();
		add.setText("New");
		add.addActionListener(new ExtractFeaturesControl.AddFilesListener(this));
		delete.addActionListener(new ExtractFeaturesControl.DeleteFilesListener(this));
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					GenesisRecipe r = (GenesisRecipe)combo.getSelectedItem();
					ExtractFeaturesControl.setHighlightedDocumentList(r);
				}
				GenesisWorkbench.update();
			}
		});
		add("left", new JLabel("CSV Files:"));
		add("left", add);
		add("left", delete);
		add("br hfill", combo);
		add("br hfill vfill", describeScroll);
	}
	
	public void refreshPanel(){
		System.out.println("EFMP53 " + ExtractFeaturesControl.numDocumentLists());
		if(combo.getItemCount() != ExtractFeaturesControl.numDocumentLists()){
			System.out.println("EFMP54 " + ExtractFeaturesControl.numDocumentLists() + " Doc Lists");
			GenesisWorkbench.reloadComboBoxContent(combo, ExtractFeaturesControl.getDocumentLists(), ExtractFeaturesControl.getHighlightedDocumentListRecipe());
		}
		if(ExtractFeaturesControl.hasHighlightedDocumentList()){
			description.setText(ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList().getDescriptionString());			
		}else{
			description.setText("");
		}
	}

}
