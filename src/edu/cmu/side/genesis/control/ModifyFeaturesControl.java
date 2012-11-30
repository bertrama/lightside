package edu.cmu.side.genesis.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

import javax.swing.JProgressBar;

import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;


import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.OrderedPluginMap;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public class ModifyFeaturesControl extends GenesisControl{

	List<GenesisRecipe> filterRecipes = new ArrayList<GenesisRecipe>();
	private static GenesisRecipe highlightedFeatureTable;
	private static GenesisRecipe highlightedFilterTable;

	private static OrderedPluginMap highlightedFilters;
	private static GenesisUpdater update = new SwingUpdaterLabel();
	static Map<FilterPlugin, Boolean> filterPlugins;

	static{
		System.out.println("Attempting to get plugins");
		filterPlugins = new HashMap<FilterPlugin, Boolean>();
		SIDEPlugin[] tableEvaluations = PluginManager.getSIDEPluginArrayByType("filter_extractor");
		for(SIDEPlugin fe : tableEvaluations){
			filterPlugins.put((FilterPlugin)fe, false);
		}
		highlightedFilters = new OrderedPluginMap();
	}

	public static void setUpdater(GenesisUpdater up){
		update = up;
	}
	
	public static GenesisUpdater getUpdater(){
		return update;
	}
	
	
	public static Map<FilterPlugin, Boolean> getFilterPlugins(){
		return filterPlugins;
	}
	
	public static Collection<GenesisRecipe> getFeatureTables(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.FEATURE_TABLE_RECIPES);
	}
	
	public static Collection<GenesisRecipe> getFilterTables(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.MODIFIED_TABLE_RECIPES);
	}

	public static class PluginCheckboxListener implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent ie) {
			FilterPlugin plug = (FilterPlugin)((CheckBoxListEntry)ie.getSource()).getValue();
			System.out.println(plug + ", " + ie.getStateChange() + ", " + ie.SELECTED + " MFC64");		
			filterPlugins.put(plug, !filterPlugins.get(plug));
			GenesisWorkbench.update();
		}
	}
	
	public static class AddFilterListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Testing filter plugins!");
			for(FilterPlugin ft : filterPlugins.keySet()){
				System.out.println(ft.toString() + " being tested " + filterPlugins.get(ft) + " MFC84");
				if(filterPlugins.get(ft)){
					highlightedFilters.put(ft, ft.generateConfigurationSettings());
					System.out.println(highlightedFilters.size() + " size of filters MFC88");
				}
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
					System.out.println("Adding filter plugin " + plugin.getOutputName() + " MFC61");
					plugins.add(plugin);
				}
			}
			GenesisRecipe newRecipe = GenesisRecipe.addPluginsToRecipe(getHighlightedFeatureTableRecipe(), plugins);
			System.out.println(newRecipe.getStage() + " " + newRecipe.getDocumentList().getCurrentAnnotation() + " " + newRecipe.getDocumentList().getSize() + " " + newRecipe.getFeatureTable().getFeatureSet().size() + " MFC68");
			ModifyFeaturesControl.FilterTableTask task = new ModifyFeaturesControl.FilterTableTask(progress, newRecipe);
			task.execute();
			System.out.println("Executed EFC165");
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
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;				
		}
	}
	
	public List<GenesisRecipe> getFilterRecipes(){
		return filterRecipes;
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


	public static int numFeatureTables(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.FEATURE_TABLE_RECIPES).size();
	}
	
	public static int numFilterTables(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.MODIFIED_TABLE_RECIPES).size();
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
