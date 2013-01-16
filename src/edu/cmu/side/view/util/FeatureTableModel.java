package edu.cmu.side.view.util;

import javax.swing.table.DefaultTableModel;

public class FeatureTableModel extends DefaultTableModel{
	private static final long serialVersionUID = -6623645069818166916L;

	@Override
	public Class<?> getColumnClass(int col){
		if(this.getRowCount()>0 && this.getValueAt(0,col) != null){
			return this.getValueAt(0, col).getClass();
		}
		else return Object.class;
	}
}
