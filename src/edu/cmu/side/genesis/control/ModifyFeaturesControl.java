package edu.cmu.side.genesis.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

import javax.swing.JProgressBar;

import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;


import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.GenesisControl.EvalCheckboxListener;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.OrderedPluginMap;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.generic.CheckBoxListEntry;
import edu.cmu.side.genesis.view.generic.SwingUpdaterLabel;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.TableEvaluationPlugin;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public class ModifyFeaturesControl extends GenesisControl{

	private static GenesisRecipe highlightedFeatureTable;
	private static GenesisRecipe highlightedFilterTable;

	private static OrderedPluginMap highlightedFilters;
	private static Map<TableEvaluationPlugin, Map<String, Boolean>> tableEvaluationPlugins;
	private static GenesisUpdater update = new SwingUpdaterLabel();
	static Map<FilterPlugin, Boolean> filterPlugins;
	private static EvalCheckboxListener eval;

	static{
		filterPlugins = new HashMap<FilterPlugin, Boolean>();
		SIDEPlugin[] filterExtractors = PluginManager.getSIDEPluginArrayByType("filter_extractor");
		for(SIDEPlugin fe : filterExtractors){
			filterPlugins.put((FilterPlugin)fe, false);
		}
		highlightedFilters = new OrderedPluginMap();
		tableEvaluationPlugins = new HashMap<TableEvaluationPlugin, Map<String, Boolean>>();
		SIDEPlugin[] tableEvaluations = PluginManager.getSIDEPluginArrayByType("table_evaluation");
		for(SIDEPlugin fe : tableEvaluations){
			tableEvaluationPlugins.put((TableEvaluationPlugin)fe, new TreeMap<String, Boolean>());
		}
		eval = new GenesisControl.EvalCheckboxListener(tableEvaluationPlugins);

	}

	public static void setUpdater(GenesisUpdater up){
		update = up;
	}
	
	public static GenesisUpdater getUpdater(){
		return update;
	}

	public static Map<TableEvaluationPlugin, Map<String, Boolean>> getTableEvaluationPlugins(){
		return tableEvaluationPlugins;
	}

	
	public static EvalCheckboxListener getEvalCheckboxListener(){
		return eval;
	}
	
	public static void clearTableEvaluationPlugins(){
		for(TableEvaluationPlugin p : tableEvaluationPlugins.keySet()){
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
			System.out.println(ft + ", " + ie.getStateChange() + ", " + ie.SELECTED + " MFC64");		
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
			GenesisRecipe newRecipe = GenesisRecipe.addPluginsToRecipe(getHighlightedFeatureTableRecipe(), plugins);
			ModifyFeaturesControl.FilterTableTask task = new ModifyFeaturesControl.FilterTableTask(progress, newRecipe);
			task.execute();
		}
		
	}

	private static class FilterTableTask extends OnPanelSwingTask{
		
		GenesisRecipe plan;
		
		public FilterTableTask(JProgressBar progressBar, GenesisRecipe newRecipe){
			this.addProgressBar(progressBar);
			plan = newRecipe;
		}

		@Override
		protected Void doInBackground(){
			try{
				FeatureTable current = plan.getFeatureTable();
				System.out.println(current.getFeatureSet().size() + " features pre-filter MFC120");
				for(SIDEPlugin plug : plan.getFilters().keySet()){
					current = ((FilterPlugin)plug).filter(current, plan.getFilters().get(plug), update);					
				}
				current.setName(plan.getFeatureTable().getName());
				System.out.println(current.getFeatureSet().size() + " features post-filter MFC125");
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
	
	public static GenesisRecipe getHighlightedFeatureTableRecipe(){
		return highlightedFeatureTable;
	}
	
	public static void setHighlightedFeatureTableRecipe(GenesisRecipe highlight){
		highlightedFeatureTable = highlight;
		GenesisWorkbench.update();
	}

	public static OrderedPluginMap getSelectedFilters(){
		return highlightedFilters;
	}
	
	public static boolean hasHighlightedFilterTable(){
		return highlightedFilterTable != null;
	}
	
	public static GenesisRecipe getHighlightedFilterTableRecipe(){
		return highlightedFilterTable;
	}
	
	public static void setHighlightedFilterTableRecipe(GenesisRecipe highlight){
		highlightedFilterTable = highlight;
		GenesisWorkbench.update();
	}
}
