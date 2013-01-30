package edu.cmu.side.view.util;

import javax.swing.JCheckBox;

/**
 * 
 * @author gtoffoli
 */
public class CheckBoxListEntry extends JCheckBox{

  private Object value = null;

  public CheckBoxListEntry(Object itemValue, boolean selected) {
    super(itemValue == null ? "" : "" + itemValue, selected);
    setValue(itemValue);
    setBackground(null);
  }

  public boolean isSelected() {
    return super.isSelected();
  }

  public void setSelected(boolean selected) {
    super.setSelected(selected);
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }
  
  public String toString()
  {
	  if(value == null) return "NULL";
	  return value.toString();
  }

}