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

import javax.swing.JTextField;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.plugin.RestructurePlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.TableFeatureMetricPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.generic.ActionBar;
import edu.cmu.side.view.generic.ActionBarTask;
import edu.cmu.side.view.util.CheckBoxListEntry;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class RestructureTablesControl extends GenesisControl{

	private static Recipe highlightedFeatureTable;
	private static Recipe highlightedFilterTable;

	private static OrderedPluginMap highlightedFilters;
	private static Map<TableFeatureMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins;
	private static StatusUpdater update = new SwingUpdaterLabel();
	static Map<RestructurePlugin, Boolean> filterPlugins;
	private static EvalCheckboxListener eval;
	private static String targetAnnotation;
	
	static{
		filterPlugins = new HashMap<RestructurePlugin, Boolean>();
		SIDEPlugin[] filterExtractors = PluginManager.getSIDEPluginArrayByType("restructure_table");
		for(SIDEPlugin fe : filterExtractors){
			filterPlugins.put((RestructurePlugin)fe, false);
		}
		highlightedFilters = new OrderedPluginMap();
		tableEvaluationPlugins = new HashMap<TableFeatureMetricPlugin, Map<String, Boolean>>();
		SIDEPlugin[] tableEvaluations = PluginManager.getSIDEPluginArrayByType("table_feature_evaluation");
		for(SIDEPlugin fe : tableEvaluations){
			tableEvaluationPlugins.put((TableFeatureMetricPlugin)fe, new TreeMap<String, Boolean>());
		}
		eval = new GenesisControl.EvalCheckboxListener(tableEvaluationPlugins);

	}
	
	public static void setUpdater(StatusUpdater up){
		update = up;
	}
	
	public static StatusUpdater getUpdater(){
		return update;
	}

	public static Map<TableFeatureMetricPlugin, Map<String, Boolean>> getTableEvaluationPlugins(){
		return tableEvaluationPlugins;
	}

	
	public static EvalCheckboxListener getEvalCheckboxListener(){
		return eval;
	}
	
	public static void clearTableEvaluationPlugins(){
		for(TableFeatureMetricPlugin p : tableEvaluationPlugins.keySet()){
			tableEvaluationPlugins.put(p, new TreeMap<String, Boolean>());
		}
	}
	
	public static Map<RestructurePlugin, Boolean> getFilterPlugins(){
		return filterPlugins;
	}

	public static class PluginCheckboxListener implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent ie) {
			RestructurePlugin ft = (RestructurePlugin)((CheckBoxListEntry)ie.getSource()).getValue();
			filterPlugins.put(ft, !filterPlugins.get(ft));
			if(filterPlugins.get(ft)){
				highlightedFilters.put(ft, ft.generateConfigurationSettings());				
			}else{
				highlightedFilters.remove(ft);
			}

			Workbench.update();
		}
	}
	
	public static class FilterTableListener implements ActionListener{
		
		private ActionBar actionBar;
		private JTextField name;
		
		public FilterTableListener(ActionBar action, JTextField n){
			actionBar = action;
			name = n;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			actionBar.setVisible(true);
			Collection<RestructurePlugin> plugins = new HashSet<RestructurePlugin>();
			for(RestructurePlugin plugin : RestructureTablesControl.getFilterPlugins().keySet()){
				if(RestructureTablesControl.getFilterPlugins().get(plugin)){
					plugins.add(plugin);
				}
			}
			Recipe newRecipe = Recipe.addPluginsToRecipe(getHighlightedFeatureTableRecipe(), plugins);
			RestructureTablesControl.FilterTableTask task = new RestructureTablesControl.FilterTableTask(actionBar, newRecipe, name.getText());
			task.execute();
		}
		
	}

	private static class FilterTableTask extends ActionBarTask{
		
		Recipe plan;
		String name;

		
		public FilterTableTask(ActionBar progressBar, Recipe newRecipe, String n){
			super(progressBar);
			plan = newRecipe;
			name = n;
		}

		@Override
		protected void doTask(){
			try
			{
				FeatureTable current = plan.getFeatureTable();
				for (SIDEPlugin plug : plan.getFilters().keySet())
				{
					current = ((RestructurePlugin) plug).restructure(current, plan.getFilters().get(plug), update);
				}
				current.setName(name);
				plan.setFilteredTable(current);
				setHighlightedFilterTableRecipe(plan);
				RecipeManager.addRecipe(plan);
				Workbench.update();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}			
		}

		@Override
		public void requestCancel()
		{
			//TODO: halt nicely.
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
		Workbench.update();
	}

	public static OrderedPluginMap getSelectedFilters(){
		return highlightedFilters;
	}
	
	public static boolean hasHighlightedFilterTable(){
		return highlightedFilterTable != null;
	}

	public static void setTargetAnnotation(String s){
		targetAnnotation = s;
	}
	
	public static String getTargetAnnotation(){
		return targetAnnotation;
	}
	
	
	public static Recipe getHighlightedFilterTableRecipe(){
		return highlightedFilterTable;
	}
	
	public static void setHighlightedFilterTableRecipe(Recipe highlight){
		highlightedFilterTable = highlight;
		Workbench.update();
	}
}
