package edu.cmu.side.genesis.control;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.genesis.view.extract.ExtractLoadPanel;
import edu.cmu.side.genesis.view.extract.ExtractFileManagerPanel;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.FastListModel;
import edu.cmu.side.simple.newui.features.FeaturePluginPanel;

public class ExtractFeaturesControl extends GenesisControl{

	private static GenesisRecipe highlightedDocumentList;
	private static GenesisRecipe highlightedFeatureTable;
	private static int threshold;
	private static GenesisUpdater update;
	private static Map<FeaturePlugin, Boolean> featurePlugins;
	
	static{
		featurePlugins = new HashMap<FeaturePlugin, Boolean>();
		SIDEPlugin[] featureExtractors = PluginManager.getSIDEPluginArrayByType("feature_hit_extractor");
		for(SIDEPlugin fe : featureExtractors){
			featurePlugins.put((FeaturePlugin)fe, false);
		}
	}
	
	public static Map<FeaturePlugin, Boolean> getFeaturePlugins(){
		return featurePlugins;
	}
	
	public static class AddFilesListener implements ActionListener{
		private Component parentComponent;
		private FastListModel model;
		private JFileChooser chooser = new JFileChooser(GenesisWorkbench.csvFolder);

		public AddFilesListener(Component parentComponent){
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
			generateDocumentListRecipe(chooser.getSelectedFiles());
		}
	}
	
	public static class DeleteFilesListener implements ActionListener{
		private Component parentComponent;

		public DeleteFilesListener(Component parentComponent){
			this.parentComponent = parentComponent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ExtractFeaturesControl.deleteCurrentDocumentList();
		}
	}
	
	public static class PluginCheckboxListener implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent ie) {
			FeaturePlugin plug = (FeaturePlugin)((CheckBoxListEntry)ie.getSource()).getValue();
			System.out.println(plug + ", " + ie.getStateChange() + ", " + ie.SELECTED + " EFC88");		
			featurePlugins.put(plug, ie.getStateChange()==ie.SELECTED);
			GenesisWorkbench.update();
		}
		
	}
	
	public static void deleteCurrentDocumentList(){
		if(highlightedDocumentList != null){
			RecipeManager.removeRecipe(highlightedDocumentList);
			highlightedDocumentList = null;
			GenesisWorkbench.update();
		}
	}
	
	public static class AnnotationComboListener implements ActionListener{
		private ExtractLoadPanel parentComponent;

		public AnnotationComboListener(ExtractLoadPanel parentComponent){
			this.parentComponent = parentComponent;
		}
		
		public void actionPerformed(ActionEvent ae){
			if(ExtractFeaturesControl.hasHighlightedDocumentList() && parentComponent.getAnnotationField().getSelectedItem() != null && 
					ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList().allAnnotations().containsKey(
							parentComponent.getAnnotationField().getSelectedItem().toString())){
				SimpleDocumentList sdl = ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList();
				String annot = parentComponent.getAnnotationField().getSelectedItem().toString();
				if(sdl.getTextColumns().contains(annot)){
					sdl.setTextColumn(annot, false);					
				}
				sdl.setCurrentAnnotation(annot);
				Map<String, Boolean> columns = new TreeMap<String, Boolean>();
				for(String s : sdl.allAnnotations().keySet()){
					if(!sdl.getCurrentAnnotation().equals(s)) columns.put(s,  false);
				}
				for(String s : sdl.getTextColumns()){
					columns.put(s,  true);
				}
				parentComponent.reloadCheckBoxList(columns);
				GenesisWorkbench.update();				
			}
		}
	}
	
	
	public static class BuildTableListener implements ActionListener{
		
		private JProgressBar progress;
		
		public BuildTableListener(JProgressBar pr){
			progress = pr;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Collection<FeaturePlugin> plugins = new HashSet<FeaturePlugin>();
			for(FeaturePlugin plugin : ExtractFeaturesControl.getFeaturePlugins().keySet()){
				if(ExtractFeaturesControl.getFeaturePlugins().get(plugin)){
					plugins.add(plugin);
				}
			}
			GenesisRecipe newRecipe = GenesisRecipe.fetchRecipe(getHighlightedDocumentListRecipe(), plugins);
			ExtractFeaturesControl.BuildTableTask task = new ExtractFeaturesControl.BuildTableTask(progress, newRecipe);
			task.execute();
		}
		
	}
	

	private static class BuildTableTask extends OnPanelSwingTask{
		
		GenesisRecipe plan;
		
		public BuildTableTask(JProgressBar progressBar, GenesisRecipe newRecipe){
			this.addProgressBar(progressBar);
		}

		@Override
		protected Void doInBackground(){
			try{
				Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
				for(FeaturePlugin plug : plan.getExtractors().keySet()){
					hits.addAll(plug.extractFeatureHits(plan.getDocumentList(), plan.getExtractors().get(plug), update));
				}
				FeatureTable ft = new FeatureTable(plan.getDocumentList(), hits, threshold);
				plan.setFeatureTable(ft);
				RecipeManager.addRecipe(plan);
//				
//				halt.setEnabled(true);
//				int thresh = 0;
//				try{
//					thresh = Integer.parseInt(threshold.getText());
//				}catch(Exception ex){
//					AlertDialog.show("Error!", "Threshold is not an integer value.", null);
//					ex.printStackTrace();
//				}
//				FeatureTable table;
//				if(clickedPlugin.overridesFeatureTable()){
//					table = clickedPlugin.getCustomFeatureTable(corpus);
//				}else{
//					table = new FeatureTable(clickedPlugin, corpus, thresh);
//				}
//				if(table.getFeatureSet().size() > 0){
//					table.defaultEvaluation();
//					table.setTableName(tableName.getText());
//						SimpleWorkbench.addFeatureTable(table);					
//						List<FeatureTable> fts = SimpleWorkbench.getFeatureTables();
//						String name = "features";
//						boolean available = true;
//						for(FeatureTable ft : fts){
//							if(name.equals(ft.getTableName())) available = false;
//						}
//						if(!available){
//							int count = 0;
//							while(!available){
//								count++;
//								name = "features" + count;
//								available = true;
//								for(FeatureTable ft : fts){
//									if(name.equals(ft.getTableName())) available = false;
//								}	
//							}
//						}
//						tableName.setText(name);
//				}
//				if(halted){
//					halted = false;
//				}
//				progressLabel.setText("");
//				fireActionEvent();
//				halt.setEnabled(false);
			}catch(Exception e){
//				JTextArea text = new JTextArea();
//				text.setText(e.toString());
//				JOptionPane.showMessageDialog(FeaturePluginPanel.this, new JScrollPane(text), "Feature Extraction Failed", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			return null;				
		}
	}
	
	public static void generateDocumentListRecipe(File[] files){
		TreeSet<String> pass = new TreeSet<String>();
		for(File f : files){
			pass.add(f.getAbsolutePath());
		}
		generateDocumentListRecipe(pass);
	}
	
	public static void generateDocumentListRecipe(File file){
		TreeSet<String> pass = new TreeSet<String>();
		pass.add(file.getAbsolutePath());
		generateDocumentListRecipe(pass);
	}
	
	public static void generateDocumentListRecipe(Set<String> files){
		SimpleDocumentList sdl = new SimpleDocumentList(files);
		setHighlightedDocumentList(RecipeManager.fetchDocumentListRecipe(sdl));
	}

	public static int numDocumentLists(){
		return getDocumentLists().size();
	}

	public static int numFeatureTables(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.FEATURE_TABLE_RECIPES).size();
	}
	
	public static Collection<GenesisRecipe> getDocumentLists(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.DOCUMENT_LIST_RECIPES);
	}
	
	public static Collection<GenesisRecipe> getFeatureTables(){
		return GenesisWorkbench.getRecipesByPane(RecipeManager.FEATURE_TABLE_RECIPES);
	}

	public static GenesisRecipe getHighlightedDocumentListRecipe(){
		return highlightedDocumentList;
	}
	
	public static boolean hasHighlightedDocumentList(){
		return highlightedDocumentList != null;
	}
	
	public static boolean hasHighlightedFeatureTable(){
		return highlightedFeatureTable != null;
	}
	
	public static GenesisRecipe getHighlightedFeatureTableRecipe(){
		return highlightedFeatureTable;
	}
	
	public static void setThreshold(int n){
		threshold = n;
	}
	
	public static int getThreshold(){
		return threshold;
	}

	public static void setUpdater(GenesisUpdater up){
		update = up;
	}
	
	public static GenesisUpdater getUpdater(){
		return update;
	}
	
	public static void setHighlightedDocumentList(GenesisRecipe highlight){
		highlightedDocumentList = highlight;
		GenesisWorkbench.update();
	}
	
	public static void setHighlightedFeatureTable(GenesisRecipe highlight){
		highlightedFeatureTable = highlight;
		GenesisWorkbench.update();
	}
}
