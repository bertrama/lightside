package edu.cmu.side.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.view.util.Refreshable;
import edu.cmu.side.view.util.CheckBoxListEntry;
import edu.cmu.side.view.util.RecipeCellRenderer;
import edu.cmu.side.view.util.Refreshable;
import edu.cmu.side.view.util.ScrollablePanel;
import edu.cmu.side.view.util.ScrollablePanel.ScrollableSizeHint;

public abstract class GenesisControl {

	public static Map<Object, Collection<Refreshable>> listenerMap;
	public static Map<Object, Boolean> currentlyUpdatingMap;
	
	static{
		listenerMap = new HashMap<Object, Collection<Refreshable>>();
		currentlyUpdatingMap = new HashMap<Object, Boolean>();
	}
	
	public static void setCurrentlyUpdating(Object source, boolean val){
		currentlyUpdatingMap.put(source, val);
	}
	
	public static boolean isCurrentlyUpdating(Object source){
		if(!listenerMap.containsKey(source)){
			listenerMap.put(source, new ArrayList<Refreshable>());
			currentlyUpdatingMap.put(source, false);
		}
		return currentlyUpdatingMap.get(source);
	}

	public static void addListenerToMap(Object source, Refreshable child){
		if(!listenerMap.containsKey(source)){
			listenerMap.put(source, new ArrayList<Refreshable>());
			currentlyUpdatingMap.put(source, false);
		}
		listenerMap.get(source).add(child);
	}
	
	public static Collection<Refreshable> getListeners(Object source){
		if(!listenerMap.containsKey(source)){
			listenerMap.put(source, new ArrayList<Refreshable>());
			currentlyUpdatingMap.put(source, false);
		}
		return listenerMap.get(source);
	}
	
	public static class EvalCheckboxListener implements ItemListener{

		Map<? extends SIDEPlugin, Map<String, Boolean>>  plugins;
		StatusUpdater updater;
		Refreshable source;
		
		public EvalCheckboxListener(Refreshable s, Map<? extends SIDEPlugin, Map<String, Boolean>> p, StatusUpdater u){
			source = s;
			plugins = p;
			updater = u;
		}
		@Override
		public void itemStateChanged(ItemEvent ie) {
			String eval = ((CheckBoxListEntry)ie.getSource()).getValue().toString();
			for(SIDEPlugin plug : plugins.keySet()){
				if(plugins.get(plug).containsKey(eval)){
					boolean flip = !plugins.get(plug).get(eval);
					plugins.get(plug).put(eval, flip);
					Workbench.update(source);
				}
			}
		}
	}

	public static class PluginCheckboxListener<E> implements ItemListener{

		Refreshable source;
		Map<E, Boolean> plugins;

		public PluginCheckboxListener(Refreshable s, Map<E, Boolean> p){
			source = s;
			plugins = p;
		}

		@Override
		public void itemStateChanged(ItemEvent ie) {
			CheckBoxListEntry check = ((CheckBoxListEntry)ie.getSource());
			E plug = (E)check.getValue();
			plugins.put(plug, check.isSelected());
			Workbench.update(source);
		}
	}

	public static Collection<Recipe> getDocumentLists(){
		return Workbench.getRecipesByPane(RecipeManager.Stage.DOCUMENT_LIST);
	}

	public static Collection<Recipe> getFeatureTables(){
		return Workbench.getRecipesByPane(RecipeManager.Stage.FEATURE_TABLE);
	}

	public static Collection<Recipe> getFilterTables(){
		return Workbench.getRecipesByPane(RecipeManager.Stage.MODIFIED_TABLE);
	}

	public static Collection<Recipe> getTrainedModels(){
		return Workbench.getRecipesByPane(RecipeManager.Stage.TRAINED_MODEL);
	}

	public static int numFeatureTables(){
		return getFeatureTables().size();
	}

	public static int numFilterTables(){
		return getFilterTables().size();
	}

	public static int numDocumentLists(){
		return getDocumentLists().size();
	}

	public static int numTrainedModels(){
		return getTrainedModels().size();
	}

	protected void addDescriptionNode(SIDEPlugin plug){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(plug.toString());
		Map<String, String> settings = plug.generateConfigurationSettings();
		for(String key : settings.keySet()){
			DefaultMutableTreeNode sub = new DefaultMutableTreeNode(key);
			DefaultMutableTreeNode val = new DefaultMutableTreeNode(settings.get(key));
			sub.add(val);
			node.add(sub);
		}
	}

	public static Component getRecipeTree(Recipe r){
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(r.getStage());
		if(r.getDocumentList() != null){
			top.add(getDocumentsNode(r.getDocumentList()));
		}
		if(r.getExtractors().size()>0){
			top.add(getExtractorNodes(r));
		}
		if(r.getFeatureTable() != null){
			top.add(getTableNode(r.getFeatureTable(), "Feature Table:"));
		}
		if(r.getFilters().size()>0){
			top.add(getFilterNodes(r));
		}
		if(r.getFilteredTable() != null){
			top.add(getTableNode(r.getFilteredTable(), "Filtered Table:"));
		}
		if(r.getLearner() != null){
			top.add(getPluginNode("Learning Plugin: ", r.getLearner()));
		}
		if(r.getTrainingResult() != null){
			top.add(getModelNode(r.getTrainingResult()));		
		}
		if(r.getPredictionResult() != null){
			top.add(getPredictionNode(r.getPredictionResult()));		
		}

		JTree recipeComponent = new JTree(top);
		recipeComponent.setCellRenderer(new RecipeCellRenderer());
		
//		ScrollablePanel panel = new ScrollablePanel(new BorderLayout());
//		panel.add(recipeComponent, BorderLayout.CENTER);
//		panel.setScrollableWidth(ScrollableSizeHint.STRETCH);
//		panel.setScrollableHeight(ScrollableSizeHint.STRETCH);
		
		return recipeComponent;
	}

	public static MutableTreeNode getDocumentsNode(DocumentList docs){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Documents: " + docs.getName());

		DefaultMutableTreeNode size = new DefaultMutableTreeNode("Instances: " + docs.getSize());
		DefaultMutableTreeNode annot = new DefaultMutableTreeNode("Class: " + docs.getCurrentAnnotation());
		DefaultMutableTreeNode text = new DefaultMutableTreeNode("Text Columns:");
		for(String s : docs.getTextColumns()){
			DefaultMutableTreeNode textName = new DefaultMutableTreeNode(s);
			text.add(textName);
		}
		node.add(size);
		node.add(annot);
		node.add(text);
		return node;
	}

	public static MutableTreeNode getFilterNodes(Recipe r){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Filter Plugins:");
		for(SIDEPlugin plug : r.getFilters().keySet()){
			node.add(getPluginNode("",plug));
		}
		return node;
	}
	
	public static MutableTreeNode getExtractorNodes(Recipe r){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Feature Plugins:");
		for(SIDEPlugin plug : r.getExtractors().keySet()){
			node.add(getPluginNode("",plug));
		}
		return node;
	}

	public static MutableTreeNode getPluginNode(String label, SIDEPlugin plug){
		DefaultMutableTreeNode plugin = new DefaultMutableTreeNode(label + (label.length()>0?" ":"") + plug.toString());
		Map<String, String> keys = plug.generateConfigurationSettings();
		for(String s : keys.keySet()){
			DefaultMutableTreeNode set = new DefaultMutableTreeNode(s + ": " + keys.get(s));			
		}
		return plugin;
	}
	public static MutableTreeNode getTableNode(FeatureTable features, String key){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(key+" " + features.getName());
		DefaultMutableTreeNode size = new DefaultMutableTreeNode("# Features: " + features.getFeatureSet().size());
		node.add(size);
		return node;
	}

	public static MutableTreeNode getModelNode(TrainingResult model){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Trained Model: " + model.getName());
		return node;
	}

	public static MutableTreeNode getPredictionNode(PredictionResult model){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Prediction: " + model.getName());
		return node;
	}

}

