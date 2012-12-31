package edu.cmu.side.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JProgressBar;

import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.GenesisWorkbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.FilterPlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.plugin.ModelMetricPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class BuildModelControl extends GenesisControl{

	private static Recipe highlightedFeatureTable;
	private static Recipe highlightedTrainedModel;

	private static Map<String, Object> validationSettings;
	private static Map<LearningPlugin, Boolean> learningPlugins;
	private static LearningPlugin highlightedLearningPlugin;
	
	private static Collection<ModelMetricPlugin> modelEvaluationPlugins;
	private static StatusUpdater update = new SwingUpdaterLabel();
	private static String newName = "model";

	static{
		validationSettings = new TreeMap<String, Object>();
		learningPlugins = new HashMap<LearningPlugin, Boolean>();
		SIDEPlugin[] learners = PluginManager.getSIDEPluginArrayByType("model_builder");
		for(SIDEPlugin le : learners){
			learningPlugins.put((LearningPlugin)le, true);
		}
		modelEvaluationPlugins = new ArrayList<ModelMetricPlugin>();
		SIDEPlugin[] tableEvaluations = PluginManager.getSIDEPluginArrayByType("model_evaluation");
		for(SIDEPlugin fe : tableEvaluations){
			modelEvaluationPlugins.add((ModelMetricPlugin)fe);
		}

	}
	
	public static Collection<ModelMetricPlugin> getModelEvaluationPlugins(){
		return modelEvaluationPlugins;
	}

	public static void setUpdater(StatusUpdater up){
		update = up;
	}

	public static void setNewName(String n){
		newName = n;
	}

	public static String getNewName(){
		return newName;
	}

	public static StatusUpdater getUpdater(){
		return update;
	}

	public static Map<String, Object> getValidationSettings(){
		return validationSettings;
	}

	public static void updateValidationSetting(String key, Object value){
		validationSettings.put(key, value);
	}

	public static class ValidationButtonListener implements ActionListener{

		String key;
		String value;
		public ValidationButtonListener(String k, String v){
			key = k;
			value = v;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			BuildModelControl.updateValidationSetting(key, value);
		}

		public void setValue(String v){
			value = v;
		}
	}

	public static Map<Integer, Integer> getFoldsMapRandom(DocumentList documents, int num){
		Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
		for(int i = 0; i < documents.getSize(); i++){
			foldsMap.put(i, i%num);
		}
		return foldsMap;
	}

	public static Map<Integer, Integer> getFoldsMapByAnnotation(DocumentList documents, String annotation, int num){
		Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
		return foldsMap;
	}


	public static Map<Integer, Integer> getFoldsMapByFile(DocumentList documents, int num){
		Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
		int foldNum = 0;
		Map<String, Integer> folds = new TreeMap<String, Integer>();
		for(int i = 0; i < documents.getSize(); i++){
			String filename = documents.getFilename(i);
			if(!folds.containsKey(filename)){
				folds.put(filename, foldNum++);
			}
			foldsMap.put(i, folds.get(filename));
		}
		return foldsMap;
	}

	public static class TrainModelListener implements ActionListener{

		JProgressBar progress;

		public TrainModelListener(JProgressBar p){
			progress = p;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(validationSettings.get("test").equals(Boolean.TRUE.toString())){
				if(validationSettings.get("type").equals("CV")){
					validationSettings.put("testSet", getHighlightedFeatureTableRecipe().getDocumentList());
				}
			}
			LearningPlugin learner = getHighlightedLearningPlugin();
			Map<String, String> settings = learner.generateConfigurationSettings();
			Recipe newRecipe = Recipe.addLearnerToRecipe(getHighlightedFeatureTableRecipe(), learner, settings);
			BuildModelControl.BuildModelTask task = new BuildModelControl.BuildModelTask(progress, newRecipe);
			task.execute();
		}

	}

	public static class BuildModelTask extends OnPanelSwingTask{
		Recipe plan;

		public BuildModelTask(JProgressBar progressBar, Recipe newRecipe){
			this.addProgressBar(progressBar);
			plan = newRecipe;
		}

		protected Void doInBackground(){
			try{
				FeatureTable current = plan.getTrainingTable();
				if(current != null){
					TrainingResult model = plan.getLearner().train(current, plan.getLearnerSettings(), validationSettings, BuildModelControl.getUpdater());
					model.generateDescriptionString(plan.getLearner(), plan.getLearnerSettings(), validationSettings);
					plan.setTrainingResult(model);
					model.setName(BuildModelControl.getNewName());
					BuildModelControl.setHighlightedTrainedModelRecipe(plan);
					RecipeManager.addRecipe(plan);
					GenesisWorkbench.update();
					update.reset();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
	}

	protected void prepareTestSet(Recipe train, DocumentList test){
		Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
		for(SIDEPlugin plug : train.getExtractors().keySet()){
			plug.configureFromSettings(train.getExtractors().get(plug));
			hits.addAll(((FeaturePlugin)plug).extractFeatureHits(test, train.getExtractors().get(plug), update));
		}
		FeatureTable ft = new FeatureTable(test, hits, train.getFeatureTable().getThreshold());
		for(SIDEPlugin plug : train.getFilters().keySet()){
			ft = ((FilterPlugin)plug).filterTestSet(train.getTrainingTable(), ft, train.getFilters().get(plug), update);
		}
	}

	public static Collection<LearningPlugin> getLearningPlugins(){
		return learningPlugins.keySet();
	}

	public static int numLearningPlugins(){
		return learningPlugins.size();
	}

	public static void setHighlightedLearningPlugin(LearningPlugin l){
		highlightedLearningPlugin = l;
	}

	public static LearningPlugin getHighlightedLearningPlugin(){
		return highlightedLearningPlugin;
	}

	public static boolean hasHighlightedFeatureTableRecipe(){
		return highlightedFeatureTable!=null;
	}

	public static boolean hasHighlightedTrainedModelRecipe(){
		return highlightedTrainedModel!=null;
	}

	public static Recipe getHighlightedFeatureTableRecipe(){
		return highlightedFeatureTable;
	}

	public static Recipe getHighlightedTrainedModelRecipe(){
		return highlightedTrainedModel;
	}

	public static void setHighlightedFeatureTableRecipe(Recipe highlight){
		highlightedFeatureTable = highlight;
		GenesisWorkbench.update();
	}

	public static void setHighlightedTrainedModelRecipe(Recipe highlight){
		highlightedTrainedModel = highlight;
		GenesisWorkbench.update();
	}
}
