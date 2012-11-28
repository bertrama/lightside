package edu.cmu.side.simple.newui;

import javax.swing.JTable;

import javax.swing.table.TableCellRenderer;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureTable;

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
		try{
			return getModel().getValueAt(getRowSorter().convertRowIndexToModel(row), col);						
		}catch(Exception e){
			return null;
		}
	}
	public void activateSelected(){
		boolean allActivated = true;
		int[] selRows = getSelectedRows();
		int colCnt = getColumnCount();
//		FeatureTable table = FeatureTableListPanel.getSelectedFeatureTable();
		for(int row : selRows){
			for(int i = 0; i < colCnt; i++){
				Object cell = getSortedValue(row,i);
				if(cell instanceof Feature){
//					if(!table.getActivated((Feature)cell)){
//						allActivated = false;
//					}
				}
			}
		}
		for(int row : selRows){
			for(int i = 0; i < colCnt; i++){
				Object cell = getSortedValue(row,i);
				if(cell instanceof Feature){
//					FeatureTableListPanel.getSelectedFeatureTable().setActivated((Feature)cell, !allActivated);
				}
			}
		}

	}
}
