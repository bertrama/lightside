package edu.cmu.side.view.util;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import edu.cmu.side.view.generic.GenericMatrixPanel;

public class ConfusionCellRenderer  extends DefaultTableCellRenderer{
	GenericMatrixPanel parent;

	DecimalFormat print = new DecimalFormat("#.###");
	public ConfusionCellRenderer(GenericMatrixPanel p){
		parent = p;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
		Double[] sum = parent.getSum();
		Component rend = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		
		rend.setFocusable(false);
		rend.setBackground(Color.white);
		rend.setForeground(Color.black);

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
				Object deep = ((SIDETable)table).getDeepValue(rowIndex, vColIndex);
		        double numberValue = 0.0;
		        boolean success = true;
				if(deep != null){
					String contents = deep.toString();					
					try{
						numberValue = (Double.parseDouble(contents));
					}catch(Exception e){
						success = false;
					}
				}

				if(success){
					if(rend instanceof DefaultTableCellRenderer){
				        ((DefaultTableCellRenderer)rend).setText(print.format(numberValue));						
					}
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
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return rend;
	}
}
