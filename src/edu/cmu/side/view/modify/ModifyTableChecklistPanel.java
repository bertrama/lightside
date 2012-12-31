package edu.cmu.side.view.modify;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.control.ModifyFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.plugin.TableMetricPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.CheckBoxList;
import edu.cmu.side.view.util.CheckBoxListEntry;
import edu.cmu.side.view.util.FastListModel;

public class ModifyTableChecklistPanel extends AbstractListPanel{


	static FastListModel pluginsModel = new FastListModel();
	static CheckBoxList pluginsList = new CheckBoxList();
	JScrollPane pluginsScroll = new JScrollPane(pluginsList);

	public ModifyTableChecklistPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Evaluations to Display:"));
		add("br hfill vfill", pluginsScroll);
	}

	public void refreshPanel(){
		Recipe recipe = ModifyFeaturesControl.getHighlightedFilterTableRecipe();
		if(recipe != null && recipe.getStage().equals(RecipeManager.MODIFIED_TABLE_RECIPES) && recipe.getFilteredTable() != null){
			FeatureTable ft = recipe.getFilteredTable();
			boolean refresh = false;
			Map<TableMetricPlugin, Map<String, Boolean>> evalPlugins = ModifyFeaturesControl.getTableEvaluationPlugins();
			for(TableMetricPlugin plug : evalPlugins.keySet()){
				Map<String, Boolean> opts = evalPlugins.get(plug);
				if(opts == null){
					refresh = true;
				}
				for(Object s : plug.getAvailableEvaluations(ft)){
					if(!opts.containsKey(s.toString())){
						refresh = true;
					}
				}
			}
			if(refresh){
				ArrayList<Object> pluginsToPass = new ArrayList<Object>();
				ModifyFeaturesControl.clearTableEvaluationPlugins();
				pluginsModel = new FastListModel();
				for(TableMetricPlugin plug : evalPlugins.keySet()){
					pluginsToPass.add(plug);
					Map<String, Boolean> opts = new TreeMap<String, Boolean>();
					for(Object s : plug.getAvailableEvaluations(ft)){
						opts.put(s.toString(), false);
						CheckBoxListEntry entry = new CheckBoxListEntry(s, false);					
						entry.addItemListener(ModifyFeaturesControl.getEvalCheckboxListener());
						pluginsToPass.add(entry);					
					}
					ModifyFeaturesControl.getTableEvaluationPlugins().put(plug, opts);
				}
				pluginsModel.addAll(pluginsToPass.toArray(new Object[0]));
				pluginsList.setModel(pluginsModel);
			}
		}else{
			pluginsModel = new FastListModel();
			pluginsList.setModel(pluginsModel);
		}
	}
}
