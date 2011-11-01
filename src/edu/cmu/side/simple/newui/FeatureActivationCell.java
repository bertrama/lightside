package edu.cmu.side.simple.newui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.newui.features.FeatureTableListPanel;

public class FeatureActivationCell extends DefaultTableCellRenderer{

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if(table instanceof SIDETable && table.getColumnCount() > -1 && FeatureTableListPanel.getSelectedFeatureTable() != null){
			Feature f = (Feature)((SIDETable)table).getSortedValue(row, 1);
			c.setForeground(FeatureTableListPanel.getSelectedFeatureTable().getActivated(f)?
					(isSelected?table.getSelectionForeground():table.getForeground()):Color.red);
		}
		return c;
	}
}
