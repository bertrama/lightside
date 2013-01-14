package edu.cmu.side.control;

import java.awt.Component;
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
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.plugin.FeatureMetricPlugin;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.TableFeatureMetricPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.extract.ExtractCombinedLoadPanel;
import edu.cmu.side.view.util.FastListModel;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class ExtractFeaturesControl extends GenesisControl{

	private static Recipe highlightedDocumentList;
	private static Recipe highlightedFeatureTable;
	private static StatusUpdater update = new SwingUpdaterLabel();
	private static Map<FeaturePlugin, Boolean> featurePlugins;
	private static Map<TableFeatureMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins;
	private static EvalCheckboxListener eval;
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
		eval = new GenesisControl.EvalCheckboxListener(tableEvaluationPlugins);
	}

	public static class AddFilesListener implements ActionListener{
		private Component parentComponent;
		private FastListModel model;
		private JFileChooser chooser = new JFileChooser(Workbench.csvFolder);

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
			Recipe plan = generateDocumentListRecipe(chooser.getSelectedFiles());
			setHighlightedDocumentListRecipe(plan);
			Workbench.update();
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
		Recipe plan = RecipeManager.fetchDocumentListRecipe(sdl);
		return plan;
	}
	
	public static Map<FeaturePlugin, Boolean> getFeaturePlugins(){
		return featurePlugins;
	}
	
	public static EvalCheckboxListener getEvalCheckboxListener(){
		return eval;
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
	
	public static void deleteCurrentDocumentList(){
		if(highlightedDocumentList != null){
			RecipeManager.removeRecipe(highlightedDocumentList);
			highlightedDocumentList = null;
			Workbench.update();
		}
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
				sdl.setCurrentAnnotation(annot);
				Map<String, Boolean> columns = new TreeMap<String, Boolean>();
				for(String s : sdl.allAnnotations().keySet()){
					if(!sdl.getCurrentAnnotation().equals(s)) columns.put(s,  false);
				}
				for(String s : sdl.getTextColumns()){
					columns.put(s,  true);
				}
				parentComponent.reloadCheckBoxList(columns);
				Workbench.update();				
			}
		}
	}
	
	
	public static class BuildTableListener implements ActionListener{
		
		private JProgressBar progress;
		private JTextField threshold;
		private JTextField name;
		public BuildTableListener(JProgressBar pr, JTextField thr, JTextField n){
			progress = pr;
			threshold = thr;
			name = n;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			progress.setVisible(true);
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

			Recipe newRecipe = Recipe.addPluginsToRecipe(getHighlightedDocumentListRecipe(), plugins);
			ExtractFeaturesControl.BuildTableTask task = new ExtractFeaturesControl.BuildTableTask(progress, newRecipe, name.getText(), thresh);
			task.execute();
		}
		
	}

	private static class BuildTableTask extends OnPanelSwingTask{
		
		Recipe plan;
		String name;
		Integer threshold;
		JProgressBar visible;
		
		public BuildTableTask(JProgressBar progressBar, Recipe newRecipe, String n, int t){
			this.addProgressBar(progressBar);
			plan = newRecipe;
			name = n;
			threshold = t;
			visible = progressBar;
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
				RestructureTablesControl.setHighlightedFeatureTableRecipe(plan);
				BuildModelControl.setHighlightedFeatureTableRecipe(plan);
				RecipeManager.addRecipe(plan);
				Workbench.update();
				update.reset();
				visible.setVisible(false);
			}catch(Exception e){
//				JTextArea text = new JTextArea();
//				text.setText(e.toString());
//				JOptionPane.showMessageDialog(FeaturePluginPanel.this, new JScrollPane(text), "Feature Extraction Failed", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			return null;				
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
		if(highlight != null){
			DocumentList sdl = highlight.getDocumentList();
			boolean noText = (sdl.getTextColumns().size()==0);		
			if(sdl.getCurrentAnnotation() == null){
				for(String s : sdl.getAnnotationNames()){
					Set<String> values = new TreeSet<String>();
					for(String t : sdl.getAnnotationArray(s)){
						values.add(t);
					}
					if(values.size() < (sdl.getSize()/10.0)){
						sdl.setCurrentAnnotation(s);
					}
					if(noText && values.size() >= (sdl.getSize()/2.0)){
						sdl.setTextColumn(s, true);
					}
				}
			}
		}
		Workbench.update();
	}
	
	public static void setHighlightedFeatureTableRecipe(Recipe highlight){
		highlightedFeatureTable = highlight;
		Workbench.update();
	}
}
