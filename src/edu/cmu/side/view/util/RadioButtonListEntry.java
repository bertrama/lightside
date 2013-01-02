package edu.cmu.side.view.util;

import javax.swing.JRadioButton;

public class RadioButtonListEntry extends JRadioButton{


	  private Object value = null;

	  private boolean red = false;

	  public RadioButtonListEntry(Object itemValue, boolean selected) {
	    super(itemValue == null ? "" : "" + itemValue, selected);
	    setValue(itemValue);
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

	  public boolean isRed() {
	    return red;
	  }

	  public void setRed(boolean red) {
	    this.red = red;
	  }

}
