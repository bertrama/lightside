package edu.cmu.side.simple.newui;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultFormatterFactory;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.newui.features.FeatureTableListPanel;

public class FeatureActivationCell extends DefaultTableCellRenderer{

	NumberFormat fmt= new DecimalFormat("#.###");
    
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		FeatureActivationCell c = (FeatureActivationCell)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if(table instanceof SIDETable && table.getColumnCount() > -1 && FeatureTableListPanel.getSelectedFeatureTable() != null){
			Object f = ((SIDETable)table).getSortedValue(row, 1);
			if(f instanceof Feature && !FeatureTableListPanel.getSelectedFeatureTable().getActivated((Feature)f)){
				c.setForeground(Color.red);				
			}else{
				c.setForeground(isSelected?table.getSelectionForeground():table.getForeground());
			}
			if(table.getModel().getColumnClass(column).equals(Double.class)){
		         Number num = (Number) value;
		         String text;
		         try{
		        	 text = fmt.format(num);
		         } catch (Exception x){
		        	 text = "";
		         }
		        c.setText(text);
			}
		}
		return c;
	}
}
