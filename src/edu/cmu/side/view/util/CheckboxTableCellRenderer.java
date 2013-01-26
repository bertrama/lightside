package edu.cmu.side.view.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class CheckboxTableCellRenderer extends DefaultTableCellRenderer{

	private int cutoff = 25;

	public CheckboxTableCellRenderer(int cutoff)
	{
		super();
		this.cutoff = cutoff;
	}
	
	public CheckboxTableCellRenderer()
	{
		super();
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
		AbstractButton rend = (AbstractButton) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		rend.setBackground(Color.white);

		if(value instanceof RadioButtonListEntry){
			RadioButtonListEntry radioButton = (RadioButtonListEntry) value;
			radioButton.setEnabled(isEnabled());
			radioButton.setFont(getFont());
			radioButton.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			rend = radioButton;
		}
		if(value instanceof CheckBoxListEntry){
			CheckBoxListEntry checkButton = (CheckBoxListEntry) value;
			checkButton.setEnabled(isEnabled());
			checkButton.setFont(getFont());
			checkButton.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			rend = checkButton;
			
		}
		
		
        if(rend.getText().length() > cutoff )
        {
        	rend.setText(rend.getText().substring(0, cutoff)+"...");
        	
        }
        
		return rend;
	}
}
