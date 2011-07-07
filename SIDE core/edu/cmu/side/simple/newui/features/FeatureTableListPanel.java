package edu.cmu.side.simple.newui.features;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JLabel;
import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class FeatureTableListPanel extends AbstractListPanel{
	private static final long serialVersionUID = -2125231290325317847L;

	static FeatureTable clickedFeatureTable;

	public FeatureTableListPanel(){
		/** List model initialized and defined in the abstract class */
		super();
		
		add("hfill", new JLabel("feature tables:"));
		scroll.setPreferredSize(new Dimension(250,150));
		add("br hfill", scroll);
		
		list.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt){
				int index = list.locationToIndex(evt.getPoint());
				if(index == -1){ return; }
				clickedFeatureTable = ((FeatureTable)listModel.get(index));
				fireActionEvent();
			}
		});
	}
	
	/**
	 * Finds the feature table currently highlighted in the Swing UI.
	 * @return That feature table.
	 */
	public static FeatureTable getSelectedFeatureTable(){
		return clickedFeatureTable;
	}

	/**
	 * Grab all of the currently active feature tables from the Workbench and refresh the Swing UI to match.
	 */
	public void refreshPanel(){
		List<FeatureTable> featureTables = SimpleWorkbench.getFeatureTables();
		if(featureTables.size() != listModel.getSize()){
			listModel.removeAllElements();
			for(FeatureTable table : featureTables){
				listModel.addElement(table);
			}
		}
		super.refreshPanel();
		clickedFeatureTable = (FeatureTable)list.getSelectedValue();
	}
}
