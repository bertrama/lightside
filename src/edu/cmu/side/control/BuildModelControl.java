package edu.cmu.side.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.OrderedPluginMap;
import edu.cmu.side.model.Recipe;
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
import edu.cmu.side.plugin.WrapperPlugin;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.generic.ActionBar;
import edu.cmu.side.view.generic.ActionBarTask;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class BuildModelControl extends GenesisControl{

	private static Recipe highlightedFeatureTable;
	private static Recipe highlightedTrainedModel;

	private static Map<String, Serializable> validationSettings;
	private static Map<LearningPlugin, Boolean> learningPlugins;
	private static Map<WrapperPlugin, Boolean> wrapperPlugins;
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
		
		wrapperPlugins = new HashMap<WrapperPlugin, Boolean>();
		SIDEPlugin[] wrappers = PluginManager.getSIDEPluginArrayByType("learning_wrapper");
		for(SIDEPlugin wr : wrappers){
			wrapperPlugins.put((WrapperPlugin)wr, false);
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

		System.out.println("BMC 131: fold by annotation using up to "+num+" folds");
		
		Map<String, Integer> foldsByLabel = new TreeMap<String, Integer>();
		List<String> annotationValues = documents.getAnnotationArray(annotation);
		for (int i = 0; i < documents.getSize(); i++)
		{
			String annotationValue = annotationValues.get(i);
			if (!foldsByLabel.containsKey(annotationValue))
			{
				foldsByLabel.put(annotationValue, foldNum++);
				System.out.println("BMC 141: "+annotationValue+" is assigned to fold #"+foldsByLabel.get(annotationValue) % num);
			}
			foldsMap.put(i, foldsByLabel.get(annotationValue) % num);
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

		ActionBar action;
		JTextField name;

		public TrainModelListener(ActionBar action, JTextField n){
			this.action = action;
			name = n;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			Workbench.update(action);

			try
			{
				if (Boolean.TRUE.toString().equals(validationSettings.get("test")))
				{
					if (validationSettings.get("type").equals("CV"))
					{
						validationSettings.put("testSet", getHighlightedFeatureTableRecipe().getDocumentList());
					}
//					if (validationSettings.get("type").equals("SUPPLY"))
//					{
//						DocumentList test = (DocumentList) validationSettings.get("testSet");
//						FeatureTable extractTestFeatures = prepareTestFeatureTable(getHighlightedFeatureTableRecipe(), test, update);
//						validationSettings.put("testFeatureTable", extractTestFeatures);
//					}
				}
				
				//System.out.println("BMC 198: wrappers="+wrapperPlugins);
				
				Recipe newRecipe = getHighlightedFeatureTableRecipe();
				
				
				LearningPlugin learner = getHighlightedLearningPlugin();
				Map<String, String> settings = learner.generateConfigurationSettings();
				newRecipe = Recipe.addLearnerToRecipe(newRecipe, learner, settings);
				newRecipe.setValidationSettings(new TreeMap<String, Serializable>(validationSettings));
				
				for(WrapperPlugin wrap : wrapperPlugins.keySet())
				{
					if(wrapperPlugins.get(wrap))
					{
						newRecipe.addWrapper(wrap, wrap.generateConfigurationSettings());						
					}
				}
				
//				System.out.println(settings);
				
//				JFileChooser chooser = new JFileChooser(new File("saved"));
//				int result = chooser.showDialog(null, "Save Model Chef Recipe");
//				if(result == JFileChooser.APPROVE_OPTION)
//				{
//					File target = chooser.getSelectedFile();//new File("saved/self-model.side");
//					saveBuildModelRecipe(newRecipe, target);
//				}
				
				BuildModelControl.BuildModelTask task = new BuildModelControl.BuildModelTask(action, newRecipe, name.getText());
				task.execute();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "Build Model", JOptionPane.ERROR_MESSAGE);
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
		FeatureTable originalTable = recipe.getTrainingTable();
		FeatureTable ft = new FeatureTable(test, hits, 0, originalTable.getAnnotation(), originalTable.getClassValueType());
		for (SIDEPlugin plug : recipe.getFilters().keySet())
		{
			ft = ((RestructurePlugin) plug).filterTestSet(originalTable, ft, recipe.getFilters().get(plug), updater);
		}
		
		ft.reconcileFeatures(originalTable);
		
		return ft;

	}

	public static class BuildModelTask extends ActionBarTask
	{
		Recipe plan;
		String name;


		public BuildModelTask(ActionBar action, Recipe newRecipe, String n)
		{
			super(action);
			plan = newRecipe;
			name = n;

		}

		@Override
		protected void doTask(){
			try
			{
				FeatureTable current = plan.getTrainingTable();
				if (current != null)
				{

					if (validationSettings.get("type").equals("SUPPLY"))
					{
						DocumentList test = (DocumentList) validationSettings.get("testSet");
						FeatureTable extractTestFeatures = prepareTestFeatureTable(plan, test, update);
						validationSettings.put("testFeatureTable", extractTestFeatures);
					}
					
					TrainingResult model = plan.getLearner().train(current, plan.getLearnerSettings(), validationSettings, plan.getWrappers(), BuildModelControl.getUpdater());
					if(model != null)
					{
						plan.setTrainingResult(model);
						model.setName(name);

						plan.setLearnerSettings(plan.getLearner().generateConfigurationSettings());
						plan.setValidationSettings(new TreeMap<String, Serializable>(validationSettings));
						BuildModelControl.setHighlightedTrainedModelRecipe(plan);
						Workbench.getRecipeManager().addRecipe(plan);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finishTask();
		}

		@Override
		public void requestCancel()
		{
			plan.getLearner().stopWhenPossible();
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

	public static Map<WrapperPlugin, Boolean> getWrapperPlugins(){
		return wrapperPlugins;
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
	}

	public static void setHighlightedTrainedModelRecipe(Recipe highlight){
		highlightedTrainedModel = highlight;
	}

	public static void prepareDocuments(DocumentList test) throws IllegalStateException
	{
		Recipe recipe = getHighlightedFeatureTableRecipe();
		DocumentList train = recipe.getDocumentList();

		try
		{
			test.setCurrentAnnotation(recipe.getTrainingTable().getAnnotation(), recipe.getTrainingTable().getClassValueType());
			test.setTextColumns(new HashSet<String>(train.getTextColumns()));


			Collection<String> trainColumns = train.allAnnotations().keySet();
			Collection<String> testColumns = test.allAnnotations().keySet();
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
			throw new java.lang.IllegalStateException("Test set annotations do not match training set.\nMissing ["+recipe.getTrainingTable().getAnnotation()+"] or "+train.getTextColumns()+" columns.");
		}


	}

	/**
	 * @param newRecipe
	 * @param target
	 */
	protected static void saveBuildModelRecipe(Recipe newRecipe, File target)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream(target);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(newRecipe);
			oos.close();

		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error while saving:\n" + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}
