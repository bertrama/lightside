package edu.cmu.side.view.util;

import javax.swing.JTable;

/**
 * This class exists to make small changes to the default behavior of JTables to fit the UI better.
 * @author emayfiel
 *
 */
public class SIDETable extends JTable{
	static final private SIDETableCellRenderer renderer = new SIDETableCellRenderer();
	static final private DoubleTroubleRenderer doubleRenderer = new DoubleTroubleRenderer();
	public SIDETable(){
		setDefaultRenderer(Object.class, renderer);
//		setDefaultRenderer(String.class, renderer);
//		setDefaultRenderer(Integer.class, renderer);
		setDefaultRenderer(Double.class, doubleRenderer);
//		setDefaultRenderer(RadioButtonListEntry.class, renderer);
//		setDefaultRenderer(CheckBoxListEntry.class, renderer);
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
