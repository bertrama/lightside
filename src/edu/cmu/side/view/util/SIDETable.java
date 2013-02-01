package edu.cmu.side.view.util;

import javax.swing.JTable;

/**
 * This class exists to make small changes to the default behavior of JTables to fit the UI better.
 * @author emayfiel
 *
 */
public class SIDETable extends JTable{

	public SIDETable(){
		setDefaultRenderer(Object.class, new SIDETableCellRenderer());
	}
	
	@Override
	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	public Object getDeepValue(int row, int col){
		Object o = this.getValueAt(row, col);
		if(o instanceof RadioButtonListEntry){
			return ((RadioButtonListEntry)o).getValue();
		}else if(o instanceof ToggleButtonTableEntry){
			return ((ToggleButtonTableEntry)o).getValue();
		}else return o;

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
