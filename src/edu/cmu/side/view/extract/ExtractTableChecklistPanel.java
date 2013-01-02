package edu.cmu.side.view.extract;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.plugin.TableMetricPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.SelectPluginList;
import edu.cmu.side.view.util.CheckBoxListEntry;
import edu.cmu.side.view.util.FastListModel;

public class ExtractTableChecklistPanel extends AbstractListPanel{

	static FastListModel pluginsModel = new FastListModel();
	static SelectPluginList pluginsList = new SelectPluginList();
	JScrollPane pluginsScroll = new JScrollPane(pluginsList);

	public ExtractTableChecklistPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Evaluations to Display:"));
		add("br hfill vfill", pluginsScroll);
	}

	public void refreshPanel(){
		Recipe recipe = ExtractFeaturesControl.getHighlightedFeatureTableRecipe();
		if(recipe != null && recipe.getStage().equals(RecipeManager.FEATURE_TABLE_RECIPES) && recipe.getFeatureTable() != null){
			FeatureTable ft = recipe.getFeatureTable();
			boolean refresh = false;
			Map<TableMetricPlugin, Map<String, Boolean>> evalPlugins = ExtractFeaturesControl.getTableEvaluationPlugins();
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
				ArrayList pluginsToPass = new ArrayList();
				ExtractFeaturesControl.clearTableEvaluationPlugins();
				pluginsModel = new FastListModel();
				for(TableMetricPlugin plug : evalPlugins.keySet()){
					pluginsToPass.add(plug);
					Map<String, Boolean> opts = new TreeMap<String, Boolean>();
					for(Object s : plug.getAvailableEvaluations(ft)){
						opts.put(s.toString(), false);
						CheckBoxListEntry entry = new CheckBoxListEntry(s, false);
						entry.addItemListener(ExtractFeaturesControl.getEvalCheckboxListener());
						pluginsToPass.add(entry);					
					}
					ExtractFeaturesControl.getTableEvaluationPlugins().put(plug, opts);
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
