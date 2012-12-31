package edu.cmu.side.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JProgressBar;

import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.GenesisWorkbench;
import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.plugin.FilterPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.TableMetricPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.util.CheckBoxListEntry;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class ModifyFeaturesControl extends GenesisControl{

	private static Recipe highlightedFeatureTable;
	private static Recipe highlightedFilterTable;

	private static OrderedPluginMap highlightedFilters;
	private static Map<TableMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins;
	private static StatusUpdater update = new SwingUpdaterLabel();
	static Map<FilterPlugin, Boolean> filterPlugins;
	private static EvalCheckboxListener eval;
	private static String newName = "filtered";

	static{
		filterPlugins = new HashMap<FilterPlugin, Boolean>();
		SIDEPlugin[] filterExtractors = PluginManager.getSIDEPluginArrayByType("filter_extractor");
		for(SIDEPlugin fe : filterExtractors){
			filterPlugins.put((FilterPlugin)fe, false);
		}
		highlightedFilters = new OrderedPluginMap();
		tableEvaluationPlugins = new HashMap<TableMetricPlugin, Map<String, Boolean>>();
		SIDEPlugin[] tableEvaluations = PluginManager.getSIDEPluginArrayByType("table_evaluation");
		for(SIDEPlugin fe : tableEvaluations){
			tableEvaluationPlugins.put((TableMetricPlugin)fe, new TreeMap<String, Boolean>());
		}
		eval = new GenesisControl.EvalCheckboxListener(tableEvaluationPlugins);

	}
	public static void setNewName(String n){
		newName = n;
	}
	
	public static String getNewName(){
		return newName;
	}

	public static void setUpdater(StatusUpdater up){
		update = up;
	}
	
	public static StatusUpdater getUpdater(){
		return update;
	}

	public static Map<TableMetricPlugin, Map<String, Boolean>> getTableEvaluationPlugins(){
		return tableEvaluationPlugins;
	}

	
	public static EvalCheckboxListener getEvalCheckboxListener(){
		return eval;
	}
	
	public static void clearTableEvaluationPlugins(){
		for(TableMetricPlugin p : tableEvaluationPlugins.keySet()){
			tableEvaluationPlugins.put(p, new TreeMap<String, Boolean>());
		}
	}
	
	public static Map<FilterPlugin, Boolean> getFilterPlugins(){
		return filterPlugins;
	}

	public static class PluginCheckboxListener implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent ie) {
			FilterPlugin ft = (FilterPlugin)((CheckBoxListEntry)ie.getSource()).getValue();
			filterPlugins.put(ft, !filterPlugins.get(ft));
			if(filterPlugins.get(ft)){
				highlightedFilters.put(ft, ft.generateConfigurationSettings());				
			}else{
				highlightedFilters.remove(ft);
			}

			GenesisWorkbench.update();
		}
	}
	
	public static class FilterTableListener implements ActionListener{
		
		private JProgressBar progress;
		
		public FilterTableListener(JProgressBar pr){
			progress = pr;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Collection<FilterPlugin> plugins = new HashSet<FilterPlugin>();
			for(FilterPlugin plugin : ModifyFeaturesControl.getFilterPlugins().keySet()){
				if(ModifyFeaturesControl.getFilterPlugins().get(plugin)){
					plugins.add(plugin);
				}
			}
			Recipe newRecipe = Recipe.addPluginsToRecipe(getHighlightedFeatureTableRecipe(), plugins);
			ModifyFeaturesControl.FilterTableTask task = new ModifyFeaturesControl.FilterTableTask(progress, newRecipe);
			task.execute();
		}
		
	}

	private static class FilterTableTask extends OnPanelSwingTask{
		
		Recipe plan;
		
		public FilterTableTask(JProgressBar progressBar, Recipe newRecipe){
			this.addProgressBar(progressBar);
			plan = newRecipe;
		}

		@Override
		protected Void doInBackground(){
			try{
				FeatureTable current = plan.getFeatureTable();
				for(SIDEPlugin plug : plan.getFilters().keySet()){
					current = ((FilterPlugin)plug).filter(current, plan.getFilters().get(plug), update);					
				}
				current.setName(ModifyFeaturesControl.getNewName());
				plan.setFilteredTable(current);
				setHighlightedFilterTableRecipe(plan);
				RecipeManager.addRecipe(plan);
				GenesisWorkbench.update();
				update.reset();
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;				
		}
	}

	public static boolean hasHighlightedFeatureTable(){
		return highlightedFeatureTable != null;
	}
	
	public static Recipe getHighlightedFeatureTableRecipe(){
		return highlightedFeatureTable;
	}
	
	public static void setHighlightedFeatureTableRecipe(Recipe highlight){
		highlightedFeatureTable = highlight;
		GenesisWorkbench.update();
	}

	public static OrderedPluginMap getSelectedFilters(){
		return highlightedFilters;
	}
	
	public static boolean hasHighlightedFilterTable(){
		return highlightedFilterTable != null;
	}
	
	public static Recipe getHighlightedFilterTableRecipe(){
		return highlightedFilterTable;
	}
	
	public static void setHighlightedFilterTableRecipe(Recipe highlight){
		highlightedFilterTable = highlight;
		GenesisWorkbench.update();
	}
}
