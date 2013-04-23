package edu.cmu.side.view.generic;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;

import edu.cmu.side.view.util.RadioButtonListEntry;

public abstract class ToggleFeatureMetricPanel extends GenericFeatureMetricPanel
{

	ButtonGroup toggleButtons = new ButtonGroup();

	Object selectedObject;

	@Override
	public Object getCellObject(Object o)
	{
		RadioButtonListEntry tb = new RadioButtonListEntry(o, (o != null && o.equals(selectedObject)));
		if (tb.isSelected())
		{
			System.out.println(o + ", " + selectedObject + ", " + o.equals(selectedObject) + " TFMP21");
		}
		tb.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent arg0)
			{
				RadioButtonListEntry entry = ((RadioButtonListEntry) arg0.getSource());
				if (entry.isSelected())
				{
					selectedObject = entry.getValue();
				}
			}

		});
		toggleButtons.add(tb);
		return tb;
	}

	@Override
	public void refreshPanel()
	{
		super.refreshPanel();
		toggleButtons = new ButtonGroup();
	}
}
