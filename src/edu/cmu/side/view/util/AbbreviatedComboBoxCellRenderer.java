package edu.cmu.side.view.util;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class AbbreviatedComboBoxCellRenderer extends DefaultListCellRenderer
{
	private int cutoff = 15;

	public AbbreviatedComboBoxCellRenderer(int cutoff)
	{
		super();
		this.cutoff = cutoff;
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		String text;
		if(value == null) 
			text = "";
		else text = value.toString();
		
		if(text == null)
			text = "WTF";
		
		if(text.length() > cutoff )
			text = text.substring(0, cutoff)+"...";
		Component cell = super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
		return cell;
	}
}
