package edu.cmu.side.simple.newui;

import javax.swing.table.DefaultTableModel;

/** This allows us to sort numerically, not just by toString() */
public class EvalTableModel extends DefaultTableModel{
	private static final long serialVersionUID = -6623645069818166916L;

	@Override
	public Class<?> getColumnClass(int col){
		return (col==0?Object.class:Double.class);
	}
}