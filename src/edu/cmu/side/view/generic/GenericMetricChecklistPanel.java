package edu.cmu.side.view.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.plugin.FeatureMetricPlugin;
import edu.cmu.side.view.util.AbbreviatedComboBoxCellRenderer;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.CheckBoxListEntry;
import edu.cmu.side.view.util.FastListModel;
import edu.cmu.side.view.util.SelectPluginList;

public abstract class GenericMetricChecklistPanel<E extends FeatureMetricPlugin> extends AbstractListPanel{

	FastListModel pluginsModel = new FastListModel();
	SelectPluginList pluginsList = new SelectPluginList();

	FeatureTable localTable;
	
	public GenericMetricChecklistPanel(){
		setLayout(new RiverLayout());
		pluginsModel = new FastListModel();
		Map<E, Map<String, Boolean>> evalPlugins = getEvaluationPlugins();
		ArrayList pluginsToPass = new ArrayList();
		for(E plug : evalPlugins.keySet()){
			pluginsToPass.add(plug);
			Map<String, Boolean> opts = new TreeMap<String, Boolean>();
			for(Object s : plug.getAvailableEvaluations().keySet()){
				opts.put(s.toString(), false);
				CheckBoxListEntry entry = new CheckBoxListEntry(s, false);
				entry.addItemListener(getCheckboxListener());
				pluginsToPass.add(entry);					
			}
			evalPlugins.put(plug, opts);
		}
		pluginsModel.addAll(pluginsToPass.toArray(new Object[0]));			
		pluginsList.setModel(pluginsModel);

		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(combo.getSelectedItem() != null){
					setTargetAnnotation(combo.getSelectedItem().toString());					
				}
				Workbench.update(GenericMetricChecklistPanel.this);
			}
		});
		//combo.setRenderer(new AbbreviatedComboBoxCellRenderer(30));
		add("left", new JLabel("Evaluations to Display:"));
		add("br left", new JLabel("Target:"));
		add("hfill", combo);
		describeScroll = new JScrollPane(pluginsList);
		add("br hfill vfill", describeScroll);
	}

	public void refreshPanel(FeatureTable table){
		if(table != localTable){
			localTable = table;
			Set<String> keysLocal = new HashSet<String>();
			Set<String> keysNew = new HashSet<String>();
			if(table != null)
			{
				for(String s : table.getDocumentList().getLabelArray()){
					keysNew.add(s);
				}
			}
			Feature.Type activeType = (localTable == null?null: localTable.getClassValueType());
			Workbench.reloadComboBoxContent(combo, keysNew, (keysNew.size()>0?keysNew.toArray(new String[0])[0]:null));
			for(int i = 0; i < pluginsModel.getSize(); i++){
				if(pluginsModel.get(i) instanceof CheckBoxListEntry){
					CheckBoxListEntry check = ((CheckBoxListEntry)pluginsModel.get(i));
					String label = check.getValue().toString();
					Map<E, Map<String, Boolean>> evalPlugins = getEvaluationPlugins();
					for (E plug : evalPlugins.keySet())
					{
						Collection<Feature.Type> types = (Collection<Feature.Type>) plug.getAvailableEvaluations().get(label);
						if (types != null && activeType != null && types.contains(activeType))
						{
							check.setEnabled(true);
						}
						else
						{
							check.setSelected(false);
							check.setEnabled(false);
						}

					}
				}
			}
			for(int i = 0; i < pluginsModel.getSize(); i++){
				if(pluginsModel.get(i) instanceof CheckBoxListEntry){
					CheckBoxListEntry check = ((CheckBoxListEntry)pluginsModel.get(i));
				}
			}
			pluginsList.setModel(pluginsModel);
			revalidate();
			repaint();
		}
	}
	
	public abstract ItemListener getCheckboxListener();

	public abstract Map<E, Map<String, Boolean>> getEvaluationPlugins();
	
	public abstract void setTargetAnnotation(String s);
}
