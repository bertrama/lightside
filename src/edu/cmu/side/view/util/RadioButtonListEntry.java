package edu.cmu.side.view.util;

import javax.swing.JRadioButton;

public class RadioButtonListEntry extends JRadioButton{


	  private Object value = null;

	  public RadioButtonListEntry(Object itemValue, boolean selected) {
	    super(itemValue == null ? "" : "" + itemValue, selected);
	    setValue(itemValue);
	    setBackground(null);
	  }

	  public Object getValue() {
	    return value;
	  }

	  public void setValue(Object value) {
	    this.value = value;
	  }

}
