package edu.cmu.side.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.plugin.ModelMetricPlugin;
import edu.cmu.side.plugin.RestructurePlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class BuildModelControl extends GenesisControl{

	private static Recipe highlightedFeatureTable;
	private static Recipe highlightedTrainedModel;

	private static Map<String, Serializable> validationSettings;
	private static Map<LearningPlugin, Boolean> learningPlugins;
	private static LearningPlugin highlightedLearningPlugin;
	
	private static Collection<ModelMetricPlugin> modelEvaluationPlugins;
	private static StatusUpdater update = new SwingUpdaterLabel();
	private static String newName = "model";

	static{
		validationSettings = new TreeMap<String, Serializable>();
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

	public static Map<String, Serializable> getValidationSettings(){
		return validationSettings;
	}

	public static void updateValidationSetting(String key, Serializable value){
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
		
		int foldNum = 0;
		Map<String, Integer> folds = new TreeMap<String, Integer>();
		List<String> annotationValues = documents.getAnnotationArray(annotation);
		for (int i = 0; i < documents.getSize(); i++)
		{
			String annotationValue = annotationValues.get(i);
			if (!folds.containsKey(annotationValue))
			{
				folds.put(annotationValue, foldNum++);
			}
			foldsMap.put(i, folds.get(annotationValue) % num);
		}
		
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
			foldsMap.put(i, folds.get(filename)%num);
		}
		return foldsMap;
	}

	public static class TrainModelListener implements ActionListener{

		JProgressBar progress;
		JTextField name;
		JButton haltButton;
		
		public TrainModelListener(JProgressBar p, JTextField n, JButton h){
			progress = p;
			name = n;
			haltButton = h;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			progress.setVisible(true);
			Workbench.update();
			
			try
			{
				if (validationSettings.get("test").equals(Boolean.TRUE.toString()))
				{
					if (validationSettings.get("type").equals("CV"))
					{
						validationSettings.put("testSet", getHighlightedFeatureTableRecipe().getDocumentList());
					}
					if (validationSettings.get("type").equals("SUPPLY"))
					{
						DocumentList test = (DocumentList) validationSettings.get("testSet");
						FeatureTable extractTestFeatures = prepareTestFeatureTable(getHighlightedFeatureTableRecipe(), test, update);
						validationSettings.put("testFeatureTable", extractTestFeatures);
					}
				}
				LearningPlugin learner = getHighlightedLearningPlugin();
				Map<String, String> settings = learner.generateConfigurationSettings();
				Recipe newRecipe = Recipe.addLearnerToRecipe(getHighlightedFeatureTableRecipe(), learner, settings);
				BuildModelControl.BuildModelTask task = new BuildModelControl.BuildModelTask(progress, newRecipe, name.getText(), haltButton);
				task.execute();
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage(), "Build Model", JOptionPane.ERROR_MESSAGE);
				haltButton.setEnabled(false);
			}
		}

	}
	
	protected static FeatureTable prepareTestFeatureTable(Recipe recipe, DocumentList test, StatusUpdater updater)
	{
		prepareDocuments(test); //assigns classes, annotations.

		Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
		OrderedPluginMap extractors = recipe.getExtractors();
		for (SIDEPlugin plug : extractors.keySet())
		{
			Collection<FeatureHit> extractorHits = ((FeaturePlugin) plug).extractFeatureHits(test, extractors.get(plug), updater);
			hits.addAll(extractorHits);
		}
		FeatureTable ft = new FeatureTable(test, hits, 0);
		for (SIDEPlugin plug : recipe.getFilters().keySet())
		{
			ft = ((RestructurePlugin) plug).filterTestSet(recipe.getTrainingTable(), ft, recipe.getFilters().get(plug), updater);
		}
		return ft;
		
	}


	public static class BuildModelTask extends OnPanelSwingTask{
		Recipe plan;
		String name;

		JProgressBar progress;
		JButton haltButton;
		ActionListener stopListener ;
		
		public BuildModelTask(JProgressBar progressBar, Recipe newRecipe, String n, JButton h){
			this.addProgressBar(progressBar);
			plan = newRecipe;
			name = n;
			progress = progressBar;
			haltButton = h;
			stopListener = new ActionListener(){
		
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						if(!plan.getLearner().isStopped())
							plan.getLearner().stopWhenPossible();
						else //we tried to be nice
						{
							BuildModelTask.this.cancel(true);
							resetStatusIndicators();
						}
					}
					
				};
			haltButton.addActionListener(stopListener);
		}
		protected Void doInBackground(){
			try
			{
				haltButton.setEnabled(true);
				FeatureTable current = plan.getTrainingTable();
				if (current != null)
				{
					TrainingResult model = plan.getLearner().train(current, plan.getLearnerSettings(), validationSettings, BuildModelControl.getUpdater());
					
					if(model != null)
					{
						plan.setTrainingResult(model);
						model.setName(name);
	
						plan.setLearnerSettings(plan.getLearner().generateConfigurationSettings());
						plan.setValidationSettings(new TreeMap<String, Serializable>(validationSettings));
						RecipeManager.addRecipe(plan);
	
						BuildModelControl.setHighlightedTrainedModelRecipe(plan);
						Workbench.update();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			resetStatusIndicators();
			return null;
		}
		/**
		 * 
		 */
		protected void resetStatusIndicators()
		{
			update.reset();
			progress.setVisible(false);
			haltButton.setEnabled(false);
			haltButton.removeActionListener(stopListener);
		}
	}

	//TODO: learn from the changes made in chef.Predictor
//	protected void prepareTestSet(Recipe train, DocumentList test){
//		Collection<FeatureHit> hits = new TreeSet<FeatureHit>();
//		for(SIDEPlugin plug : train.getExtractors().keySet()){
//			plug.configureFromSettings(train.getExtractors().get(plug));
//			hits.addAll(((FeaturePlugin)plug).extractFeatureHits(test, train.getExtractors().get(plug), update));
//		}
//		FeatureTable ft = new FeatureTable(test, hits, train.getFeatureTable().getThreshold());
//		for(SIDEPlugin plug : train.getFilters().keySet()){
//			ft = ((RestructurePlugin)plug).filterTestSet(train.getTrainingTable(), ft, train.getFilters().get(plug), update);
//		}
//	}

	public static Map<LearningPlugin, Boolean> getLearningPlugins(){
		return learningPlugins;
	}

	public static int numLearningPlugins(){
		return learningPlugins.size();
	}

	public static void setHighlightedLearningPlugin(LearningPlugin l){
		highlightedLearningPlugin = l;
		for(LearningPlugin plug : learningPlugins.keySet()){
			learningPlugins.put(plug, plug==l);
		}
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
		Workbench.update();
	}

	public static void setHighlightedTrainedModelRecipe(Recipe highlight){
		highlightedTrainedModel = highlight;
		Workbench.update();
	}

	public static void prepareDocuments(DocumentList test) throws IllegalStateException
	{
		Recipe recipe = getHighlightedFeatureTableRecipe();
		DocumentList train = recipe.getDocumentList();
		
		try
		{
			test.setCurrentAnnotation(train.getCurrentAnnotation());
			for(String column : train.getTextColumns())
			{
				test.setTextColumn(column, true);
			}

			
			List<String> trainColumns = train.getAnnotationArray();
			List<String> testColumns = test.getAnnotationArray();
			if(!testColumns.containsAll(trainColumns))
			{
				ArrayList<String> missing = new ArrayList<String>(trainColumns);
				missing.removeAll(testColumns);
				throw new java.lang.IllegalStateException("Test set annotations do not match training set.\nMissing columns: "+missing);
			}

			validationSettings.put("testSet",test);
		}
		catch(Exception e)
		{
			throw new java.lang.IllegalStateException("Test set annotations do not match training set.\nMissing ["+train.getCurrentAnnotation()+"] or "+train.getTextColumns()+" columns.");
		}
		
		
	}
}
