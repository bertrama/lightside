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
		Double[] sum = parent.getSum();
		Component rend = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		rend.setBackground(Color.white);
		rend.setFocusable(false);

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
				String contents = ((SIDETable)table).getDeepValue(rowIndex, vColIndex).toString();
				double numberValue = 0.0;
				try{
					numberValue = (Double.parseDouble(contents));
				}catch(Exception e){}

				if(numberValue > 0){
					Double outValue = ((Double)(255.0*numberValue)) / sum[1];
					intensity = outValue.intValue();
					rend.setBackground(new Color(255-intensity, 255-intensity,255));
					rend.setForeground(intensity<128?Color.black:Color.white);
				}else if(numberValue < 0){
					Double outValue = -((Double)(255.0*numberValue)) / sum[1];
					intensity = outValue.intValue();
					rend.setBackground(new Color(255, (new Double(255-(intensity/2.0))).intValue(),255-intensity));
					rend.setForeground(intensity<128?Color.black:Color.white);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return rend;
	}
}
