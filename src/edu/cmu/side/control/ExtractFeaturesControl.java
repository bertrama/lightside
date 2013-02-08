package edu.cmu.side.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.yerihyo.yeritools.io.FileToolkit;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.TableFeatureMetricPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.extract.ExtractCombinedLoadPanel;
import edu.cmu.side.view.generic.ActionBar;
import edu.cmu.side.view.generic.ActionBarTask;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.FastListModel;
import edu.cmu.side.view.util.Refreshable;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class ExtractFeaturesControl extends GenesisControl{

	private static Recipe highlightedDocumentList;
	private static Recipe highlightedFeatureTable;
	private static StatusUpdater update = new SwingUpdaterLabel();
	private static Map<FeaturePlugin, Boolean> featurePlugins;
	private static Map<TableFeatureMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins;
	private static String targetAnnotation;
	
	static{
		featurePlugins = new HashMap<FeaturePlugin, Boolean>();
		SIDEPlugin[] featureExtractors = PluginManager.getSIDEPluginArrayByType("feature_hit_extractor");
		boolean selected = true;
		for(SIDEPlugin fe : featureExtractors){
			featurePlugins.put((FeaturePlugin)fe, selected);
			selected = false;
		}
		tableEvaluationPlugins = new HashMap<TableFeatureMetricPlugin, Map<String, Boolean>>();
		SIDEPlugin[] tableEvaluations = PluginManager.getSIDEPluginArrayByType("table_feature_evaluation");
		for(SIDEPlugin fe : tableEvaluations){
			tableEvaluationPlugins.put((TableFeatureMetricPlugin)fe, new TreeMap<String, Boolean>());
		}
	}

	public static class AddFilesListener implements ActionListener{
		private AbstractListPanel parentComponent;
		private FastListModel model;
		private JFileChooser chooser = new JFileChooser(Workbench.csvFolder);

		public AddFilesListener(AbstractListPanel parentComponent){
			this.parentComponent = parentComponent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			chooser.setFileFilter(FileToolkit
					.createExtensionListFileFilter(new String[] { "csv" }, true));
			chooser.setMultiSelectionEnabled(true);
			int result = chooser.showOpenDialog(parentComponent);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			Recipe plan = generateDocumentListRecipe(chooser.getSelectedFiles());
			plan.getDocumentList().guessTextAndAnnotationColumns();
			
			Workbench.update(RecipeManager.Stage.DOCUMENT_LIST);
			setHighlightedDocumentListRecipe(plan);
			Workbench.update(parentComponent);
		}
	}
	

	public static Recipe generateDocumentListRecipe(File[] files){
		TreeSet<String> pass = new TreeSet<String>();
		for(File f : files){
			pass.add(f.getAbsolutePath());
		}
		return generateDocumentListRecipe(pass);
	}
	
	public static Recipe generateDocumentListRecipe(File file){
		TreeSet<String> pass = new TreeSet<String>();
		pass.add(file.getAbsolutePath());
		return generateDocumentListRecipe(pass);
	}
	
	public static Recipe generateDocumentListRecipe(Set<String> files){
		DocumentList sdl = new DocumentList(files);
		Recipe plan = Workbench.getRecipeManager().fetchDocumentListRecipe(sdl);
		return plan;
	}
	
	public static Map<FeaturePlugin, Boolean> getFeaturePlugins(){
		return featurePlugins;
	}
	
	public static EvalCheckboxListener getEvalCheckboxListener(Refreshable source){
		return new EvalCheckboxListener(source, tableEvaluationPlugins, ExtractFeaturesControl.getUpdater());
	}
	
	public static Map<TableFeatureMetricPlugin, Map<String, Boolean>> getTableEvaluationPlugins(){
		return tableEvaluationPlugins;
	}
	
	public static void clearTableEvaluationPlugins(){
		for(TableFeatureMetricPlugin p : tableEvaluationPlugins.keySet()){
			tableEvaluationPlugins.put(p, new TreeMap<String, Boolean>());
		}
	}
	
	public static void setTargetAnnotation(String s){
		targetAnnotation = s;
	}
	
	public static String getTargetAnnotation(){
		return targetAnnotation;
	}
	
	public static class AnnotationComboListener implements ActionListener{
		private ExtractCombinedLoadPanel parentComponent;

		public AnnotationComboListener(ExtractCombinedLoadPanel parentComponent){
			this.parentComponent = parentComponent;
		}
		
		public void actionPerformed(ActionEvent ae){
			if(ExtractFeaturesControl.hasHighlightedDocumentList() && parentComponent.getAnnotationField().getSelectedItem() != null && 
					ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList().allAnnotations().containsKey(
							parentComponent.getAnnotationField().getSelectedItem().toString())){
				DocumentList sdl = ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList();
				String annot = parentComponent.getAnnotationField().getSelectedItem().toString();
				if(sdl.getTextColumns().contains(annot)){
					sdl.setTextColumn(annot, false);					
				}
				sdl.setCurrentAnnotation(annot);  //because this modifies a recipe, should it notify the recipemanager?
				Map<String, Boolean> columns = new TreeMap<String, Boolean>();
				for(String s : sdl.allAnnotations().keySet()){
					if(!sdl.getCurrentAnnotation().equals(s)) columns.put(s,  false);
				}
				for(String s : sdl.getTextColumns()){
					columns.put(s,  true);
				}
				parentComponent.reloadCheckBoxList(columns);
				Workbench.update(RecipeManager.Stage.DOCUMENT_LIST);
				Workbench.update(parentComponent);			
			}
		}
	}
	
	
	public static class BuildTableListener implements ActionListener{
		
		private ActionBar actionBar;
		private JTextField threshold;
		private JTextField name;
		
		public BuildTableListener(ActionBar action, JTextField thr, JTextField n){
			actionBar = action;
			threshold = thr;
			name = n;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			Collection<FeaturePlugin> plugins = new HashSet<FeaturePlugin>();
			for (FeaturePlugin plugin : ExtractFeaturesControl.getFeaturePlugins().keySet())
			{
				if (ExtractFeaturesControl.getFeaturePlugins().get(plugin))
				{
					plugins.add(plugin);
				}
			}
			int thresh = 1;
			try
			{
				thresh = Integer.parseInt(threshold.getText());
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(threshold, "Threshold value is not an integer!", "Warning", JOptionPane.WARNING_MESSAGE);
			}

			Recipe newRecipe = Recipe.addPluginsToRecipe(getHighlightedDocumentListRecipe(), plugins);
			ExtractFeaturesControl.BuildTableTask task = new ExtractFeaturesControl.BuildTableTask(actionBar, newRecipe, name.getText(), thresh);
			task.execute();
		}
		
	}

	private static class BuildTableTask extends ActionBarTask
	{
		
		Recipe plan;
		String name;
		Integer threshold;
		FeaturePlugin activeExtractor =  null;
		
		public BuildTableTask(ActionBar action, Recipe newRecipe, String n, int t){
			super(action);
			plan = newRecipe;
			name = n;
			threshold = t;

		}

		@Override
		protected void doTask(){
			try
			{
				
				Collection<FeatureHit> hits = new HashSet<FeatureHit>();
				for (SIDEPlugin plug : plan.getExtractors().keySet())
				{
					if(!halt)
					{
						activeExtractor = (FeaturePlugin) plug;
						hits.addAll(activeExtractor.extractFeatureHits(plan.getDocumentList(), plan.getExtractors().get(plug), update));
					}
				}
				if(!halt)
				{
					FeatureTable ft = new FeatureTable(plan.getDocumentList(), hits, threshold);
					ft.setName(name);
					plan.setFeatureTable(ft);
					setHighlightedFeatureTableRecipe(plan);
					RestructureTablesControl.setHighlightedFeatureTableRecipe(plan);
					BuildModelControl.setHighlightedFeatureTableRecipe(plan);
					Workbench.getRecipeManager().addRecipe(plan);
				}
			}
			catch (Exception e)
			{
				// JTextArea text = new JTextArea();
				// text.setText(e.toString());
				// JOptionPane.showMessageDialog(FeaturePluginPanel.this, new
				// JScrollPane(text), "Feature Extraction Failed",
				// JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}				
		}

		@Override
		public void requestCancel()
		{
			System.out.println("stopping extraction...");
			if(activeExtractor != null && !activeExtractor.isStopped())
				activeExtractor.stopWhenPossible();
		}
	}

	public static Recipe getHighlightedDocumentListRecipe(){
		return highlightedDocumentList;
	}
	
	public static boolean hasHighlightedDocumentList(){
		return highlightedDocumentList != null;
	}
	
	public static boolean hasHighlightedFeatureTable(){
		return highlightedFeatureTable != null;
	}
	
	public static Recipe getHighlightedFeatureTableRecipe(){
		return highlightedFeatureTable;
	}

	public static void setUpdater(StatusUpdater up){
		update = up;
	}
	
	public static StatusUpdater getUpdater(){
		return update;
	}
	
	public static void setHighlightedDocumentListRecipe(Recipe highlight){
		highlightedDocumentList = highlight;
	}


	
	public static void setHighlightedFeatureTableRecipe(Recipe highlight){
		highlightedFeatureTable = highlight;
	}
}
