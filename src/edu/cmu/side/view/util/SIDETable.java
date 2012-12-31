package edu.cmu.side.view.util;

import javax.swing.JTable;

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
}
