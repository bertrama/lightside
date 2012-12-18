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
import javax.swing.JTextField;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.extract.ExtractLoadPanel;
import edu.cmu.side.genesis.view.extract.ExtractFileManagerPanel;
import edu.cmu.side.genesis.view.extract.ExtractActionPanel;
import edu.cmu.side.genesis.view.generic.CheckBoxListEntry;
import edu.cmu.side.genesis.view.generic.SwingUpdaterLabel;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.TableEvaluationPlugin;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.FastListModel;
import edu.cmu.side.simple.newui.features.FeaturePluginPanel;

public class ExtractFeaturesControl extends GenesisControl{

	private static GenesisRecipe highlightedDocumentList;
	private static GenesisRecipe highlightedFeatureTable;
	private static GenesisUpdater update = new SwingUpdaterLabel();
	private static Map<FeaturePlugin, Boolean> featurePlugins;
	private static Map<TableEvaluationPlugin, Map<String, Boolean>> tableEvaluationPlugins;
	private static EvalCheckboxListener eval;
	private static String newName = "features";
	
	static{
		featurePlugins = new HashMap<FeaturePlugin, Boolean>();
		SIDEPlugin[] featureExtractors = PluginManager.getSIDEPluginArrayByType("feature_hit_extractor");
		for(SIDEPlugin fe : featureExtractors){
			featurePlugins.put((FeaturePlugin)fe, false);
		}
		tableEvaluationPlugins = new HashMap<TableEvaluationPlugin, Map<String, Boolean>>();
		SIDEPlugin[] tableEvaluations = PluginManager.getSIDEPluginArrayByType("table_evaluation");
		for(SIDEPlugin fe : tableEvaluations){
			tableEvaluationPlugins.put((TableEvaluationPlugin)fe, new TreeMap<String, Boolean>());
		}
		eval = new GenesisControl.EvalCheckboxListener(tableEvaluationPlugins);
	}
	
	public static void setNewName(String n){
		newName = n;
	}
	
	public static String getNewName(){
		return newName;
	}
	
	
	public static Map<FeaturePlugin, Boolean> getFeaturePlugins(){
		return featurePlugins;
	}
	
	public static EvalCheckboxListener getEvalCheckboxListener(){
		return eval;
	}
	
	public static Map<TableEvaluationPlugin, Map<String, Boolean>> getTableEvaluationPlugins(){
		return tableEvaluationPlugins;
	}
	
	public static void clearTableEvaluationPlugins(){
		for(TableEvaluationPlugin p : tableEvaluationPlugins.keySet()){
			tableEvaluationPlugins.put(p, new TreeMap<String, Boolean>());
		}
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
			featurePlugins.put(plug, !featurePlugins.get(plug));
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
		private JTextField threshold;
		public BuildTableListener(JProgressBar pr, JTextField thr){
			progress = pr;
			threshold = thr;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Collection<FeaturePlugin> plugins = new HashSet<FeaturePlugin>();
			for(FeaturePlugin plugin : ExtractFeaturesControl.getFeaturePlugins().keySet()){
				if(ExtractFeaturesControl.getFeaturePlugins().get(plugin)){
					plugins.add(plugin);
				}
			}
			int thresh = 1;
			try{
				thresh = Integer.parseInt(threshold.getText());		
			}catch(Exception e){
				JOptionPane.showMessageDialog(threshold, "Threshold value is not an integer!", "Warning", JOptionPane.WARNING_MESSAGE);
			}

			GenesisRecipe newRecipe = GenesisRecipe.addPluginsToRecipe(getHighlightedDocumentListRecipe(), plugins);
			ExtractFeaturesControl.BuildTableTask task = new ExtractFeaturesControl.BuildTableTask(progress, newRecipe, ExtractFeaturesControl.getNewName(), thresh);
			task.execute();
		}
		
	}

	private static class BuildTableTask extends OnPanelSwingTask{
		
		GenesisRecipe plan;
		String name;
		Integer threshold;
		
		public BuildTableTask(JProgressBar progressBar, GenesisRecipe newRecipe, String n, int t){
			this.addProgressBar(progressBar);
			plan = newRecipe;
			name = n;
			threshold = t;
		}

		@Override
		protected Void doInBackground(){
			try{
				Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
				for(SIDEPlugin plug : plan.getExtractors().keySet()){
					hits.addAll(((FeaturePlugin)plug).extractFeatureHits(plan.getDocumentList(), plan.getExtractors().get(plug), update));
				}
				double timeA = System.currentTimeMillis();
				FeatureTable ft = new FeatureTable(plan.getDocumentList(), hits, threshold);
				double timeB = System.currentTimeMillis();
				ft.setName(name);
				plan.setFeatureTable(ft);
				setHighlightedFeatureTableRecipe(plan);
				ModifyFeaturesControl.setHighlightedFeatureTableRecipe(plan);
				BuildModelControl.setHighlightedFeatureTableRecipe(plan);
				RecipeManager.addRecipe(plan);
				GenesisWorkbench.update();
				update.reset();
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
		setHighlightedDocumentListRecipe(RecipeManager.fetchDocumentListRecipe(sdl));
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

	public static void setUpdater(GenesisUpdater up){
		update = up;
	}
	
	public static GenesisUpdater getUpdater(){
		return update;
	}
	
	public static void setHighlightedDocumentListRecipe(GenesisRecipe highlight){
		highlightedDocumentList = highlight;
		SimpleDocumentList sdl = highlight.getDocumentList();
		if(sdl.getCurrentAnnotation() == null){
			for(String s : sdl.getAnnotationNames()){
				if(s.equalsIgnoreCase("class")){
					sdl.setCurrentAnnotation(s);
				}
			}
		}
		if(sdl.getTextColumns().size()==0){
			for(String s : sdl.getAnnotationNames()){
				if(s.equalsIgnoreCase("text")){
					sdl.setTextColumn(s, true);
				}
			}
		}
		GenesisWorkbench.update();
	}
	
	public static void setHighlightedFeatureTableRecipe(GenesisRecipe highlight){
		highlightedFeatureTable = highlight;
		GenesisWorkbench.update();
	}
}
