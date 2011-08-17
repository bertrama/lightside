package edu.cmu.side.simple.newui;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.newui.features.FeatureTableListPanel;

/**
 * This class exists to make small changes to the default behavior of JTables to fit the UI better.
 * @author emayfiel
 *
 */
public class SIDETable extends JTable{

	@Override
	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	/**
	 * Corrects the getValueAt method for when the rows in the table have been sorted.
	 */
	public Object getSortedValue(int row, int col){
		return getModel().getValueAt(getRowSorter().convertRowIndexToModel(row), col);
	}

	public void deactivateSelected(){
		for(int row : getSelectedRows()){
			Object cell = getSortedValue(row,1);
			if(cell instanceof Feature){
				FeatureTableListPanel.getSelectedFeatureTable().setActivated((Feature)cell, false);
			}
		}
	}

	public void activateSelected(){
		for(int row : getSelectedRows()){
			for(int i = 0; i < getColumnCount(); i++){
				Object cell = getSortedValue(row,i);
				if(cell instanceof Feature){
					FeatureTableListPanel.getSelectedFeatureTable().setActivated((Feature)cell, true);
				}
			}
		}
	}
}
