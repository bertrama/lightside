package edu.cmu.side.view.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.plugin.FeatureMetricPlugin;
import edu.cmu.side.view.util.CheckBoxListEntry;

public abstract class FeatureMetricCheckboxPanel extends GenericFeatureMetricPanel
{

	Set<Feature> selectedFeatures = new TreeSet<Feature>();

	final static String selectKey = "FeatureMetricCheckBoxPanelSelectActionKey";
	final static String deselectKey = "FeatureMetricCheckBoxPanelDeselectActionKey";

	public FeatureMetricCheckboxPanel()
	{
		super();
		
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		KeyStroke space = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
		KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		featureTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, selectKey);
		featureTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(space, selectKey);
		featureTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(backspace, deselectKey);
		featureTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(delete, deselectKey);
		featureTable.getActionMap().put(selectKey, new AbstractAction() {

		    @Override
		    public void actionPerformed(ActionEvent e) 
		    {
		    	int[] rows = featureTable.getSelectedRows();
				
				boolean willSelect = false;
				
				for(int row : rows)
				{
					CheckBoxListEntry entry = (CheckBoxListEntry) display.getValueAt(featureTable.convertRowIndexToModel(row), 0);
					willSelect = willSelect || !entry.isSelected();
				}
				
				for(int row : rows)
				{
					CheckBoxListEntry entry = (CheckBoxListEntry) display.getValueAt(featureTable.convertRowIndexToModel(row), 0);
					entry.setSelected(willSelect);
				}	
				
				featureTable.repaint();
		    }
		});
		
		featureTable.getActionMap().put(deselectKey, new AbstractAction() {

		    @Override
		    public void actionPerformed(ActionEvent e) 
		    {
		    	int[] rows = featureTable.getSelectedRows();
				
				for(int row : rows)
				{
					CheckBoxListEntry entry = (CheckBoxListEntry) display.getValueAt(featureTable.convertRowIndexToModel(row), 0);
					entry.setSelected(false);
				}	
				
				featureTable.repaint();
		    }
		});
		
		
	}
	
	@Override
	public Object getCellObject(Object o)
	{
		CheckBoxListEntry tb = new CheckBoxListEntry(o, (o != null && selectedFeatures.contains(o)));
		tb.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent arg0)
			{
				CheckBoxListEntry entry = ((CheckBoxListEntry) arg0.getSource());
				Object value = entry.getValue();
				if (entry.isSelected())
				{
					if(value instanceof Feature)
						selectedFeatures.add((Feature) value);
					else
						System.out.println(value+" in list is not a Feature!");
				}
				else
				{
					selectedFeatures.remove(value);
				}
				
				selectedFeaturesChanged();
			}

		});
		return tb;
	}

	public abstract void selectedFeaturesChanged();
	
	public Collection<Feature> getSelectedFeatures()
	{
		return selectedFeatures;
	}
	
	@Override
	public void refreshPanel(Recipe recipe, Map<? extends FeatureMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins, boolean[] mask)
	{
		FeatureTable newTable = (recipe == null ? null : recipe.getTrainingTable());
		if(localTable != newTable)
		{
			selectedFeatures.clear();
		}
		super.refreshPanel(recipe, tableEvaluationPlugins, mask);
	}
}
