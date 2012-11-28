package edu.cmu.side.genesis.view.extract;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.CheckBoxList;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.TableEvaluationPlugin;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;

import se.datadosen.component.RiverLayout;

public class ExtractTableChecklistPanel extends AbstractListPanel{

	static FastListModel pluginsModel = new FastListModel();
	static CheckBoxList pluginsList = new CheckBoxList();
	JScrollPane pluginsScroll = new JScrollPane(pluginsList);

	public ExtractTableChecklistPanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Evaluations to Display:"));
		add("br hfill vfill", pluginsScroll);
	}

	public void refreshPanel(){
		GenesisRecipe recipe = ExtractFeaturesControl.getHighlightedFeatureTableRecipe();
		if(recipe != null && recipe.getStage().equals(RecipeManager.FEATURE_TABLE_RECIPES) && recipe.getFeatureTable() != null){
			FeatureTable ft = recipe.getFeatureTable();
			boolean refresh = false;
			Map<TableEvaluationPlugin, Map<String, Boolean>> evalPlugins = ExtractFeaturesControl.getTableEvaluationPlugins();
			for(TableEvaluationPlugin plug : evalPlugins.keySet()){
				Map<String, Boolean> opts = evalPlugins.get(plug);
				if(opts == null){
					System.out.println("ETCP Refreshing at line 45");
					refresh = true;
				}
				for(Object s : plug.getAvailableEvaluations(ft)){
					if(!opts.containsKey(s.toString())){
						System.out.println("ETCP Refreshing at line 50");
						refresh = true;
					}
				}
			}
			if(refresh){
				ArrayList pluginsToPass = new ArrayList();
				ExtractFeaturesControl.clearTableEvaluationPlugins();
				pluginsModel = new FastListModel();
				for(TableEvaluationPlugin plug : evalPlugins.keySet()){
					pluginsToPass.add(plug);
					Map<String, Boolean> opts = new TreeMap<String, Boolean>();
					for(Object s : plug.getAvailableEvaluations(ft)){
						opts.put(s.toString(), false);
						CheckBoxListEntry entry = new CheckBoxListEntry(s, false);					
						entry.addItemListener(new ExtractFeaturesControl.EvalCheckboxListener());
						pluginsToPass.add(entry);					
					}
					ExtractFeaturesControl.getTableEvaluationPlugins().put(plug, opts);
				}
				pluginsModel.addAll(pluginsToPass.toArray(new Object[0]));
				pluginsList.setModel(pluginsModel);
			}
		}
	}
}
