package edu.cmu.side.simple.newui;

import javax.swing.table.DefaultTableModel;

public class FeatureTableModel extends DefaultTableModel{
	private static final long serialVersionUID = -6623645069818166916L;

	@Override
	public Class<?> getColumnClass(int col){
		return (col > 3?Double.class:Object.class);
	}
}
