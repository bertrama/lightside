package edu.cmu.side.view.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import edu.cmu.side.view.generic.GenericMatrixPanel;

public class ConfusionCellRenderer  extends DefaultTableCellRenderer{
	GenericMatrixPanel parent;
	
	public ConfusionCellRenderer(GenericMatrixPanel p){
		parent = p;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
		double sum = parent.getSum();
		Component rend = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		rend.setBackground(Color.white);
		
		if(value instanceof RadioButtonListEntry){
			RadioButtonListEntry radioButton = (RadioButtonListEntry) value;
			radioButton.setEnabled(isEnabled());
			radioButton.setFont(getFont());
			radioButton.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			rend = radioButton;
		}
		
		if(vColIndex > 0){
			Integer intensity = 0;
			try{
				intensity = 
						((Double)(255.0*
								(Double.parseDouble(
										((SIDETable)table).getDeepValue(rowIndex, vColIndex).toString()
												   ))/sum)).intValue();
			}catch(Exception e){
				e.printStackTrace();
			}
			rend.setBackground(new Color(255-intensity, 255-intensity,255));
			rend.setForeground(Color.black);
		}
		
		return rend;
	}
}
