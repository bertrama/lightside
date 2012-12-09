package edu.cmu.side.genesis.view.modify;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.control.ModifyFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.generic.CheckBoxList;
import edu.cmu.side.genesis.view.generic.CheckBoxListEntry;
import edu.cmu.side.simple.TableEvaluationPlugin;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;

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
		GenesisRecipe recipe = ModifyFeaturesControl.getHighlightedFilterTableRecipe();
		if(recipe != null && recipe.getStage().equals(RecipeManager.MODIFIED_TABLE_RECIPES) && recipe.getFilteredTable() != null){
			FeatureTable ft = recipe.getFilteredTable();
			boolean refresh = false;
			Map<TableEvaluationPlugin, Map<String, Boolean>> evalPlugins = ModifyFeaturesControl.getTableEvaluationPlugins();
			for(TableEvaluationPlugin plug : evalPlugins.keySet()){
				Map<String, Boolean> opts = evalPlugins.get(plug);
				if(opts == null){
					System.out.println("MTCP Refreshing at line 45");
					refresh = true;
				}
				for(Object s : plug.getAvailableEvaluations(ft)){
					if(!opts.containsKey(s.toString())){
						System.out.println("MTCP Refreshing at line 50");
						refresh = true;
					}
				}
			}
			if(refresh){
				ArrayList pluginsToPass = new ArrayList();
				ModifyFeaturesControl.clearTableEvaluationPlugins();
				pluginsModel = new FastListModel();
				for(TableEvaluationPlugin plug : evalPlugins.keySet()){
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
		}
	}
}
