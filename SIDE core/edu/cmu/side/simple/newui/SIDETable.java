package edu.cmu.side.simple.newui;

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
}
