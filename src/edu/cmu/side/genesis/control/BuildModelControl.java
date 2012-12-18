package edu.cmu.side.genesis.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JProgressBar;

import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.GenesisWorkbenchPanel;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.generic.SwingUpdaterLabel;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public class BuildModelControl extends GenesisControl{

	private static GenesisRecipe highlightedFeatureTable;
	private static GenesisRecipe highlightedTrainedModel;
	
	private static Map<String, Object> validationSettings;
	private static Map<LearningPlugin, Boolean> learningPlugins;
	private static LearningPlugin highlightedLearningPlugin;
	private static GenesisUpdater update = new SwingUpdaterLabel();
	private static String newName = "model";
	
	static{
		validationSettings = new TreeMap<String, Object>();
		learningPlugins = new HashMap<LearningPlugin, Boolean>();
		SIDEPlugin[] learners = PluginManager.getSIDEPluginArrayByType("model_builder");
		for(SIDEPlugin le : learners){
			learningPlugins.put((LearningPlugin)le, true);
		}
	}

	public static void setUpdater(GenesisUpdater up){
		update = up;
	}

	public static void setNewName(String n){
		newName = n;
	}
	
	public static String getNewName(){
		return newName;
	}
	
	public static GenesisUpdater getUpdater(){
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
	
    public static Map<Integer, Integer> getFoldsMapRandom(SimpleDocumentList documents, int num){
        Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
        for(int i = 0; i < documents.getSize(); i++){
                foldsMap.put(i, i%num);
        }
        return foldsMap;
    }

    public static Map<Integer, Integer> getFoldsMapByAnnotation(SimpleDocumentList documents, String annotation, int num){
        Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
        return foldsMap;
    }


    public static Map<Integer, Integer> getFoldsMapByFile(SimpleDocumentList documents, int num){
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
			GenesisRecipe newRecipe = GenesisRecipe.addLearnerToRecipe(getHighlightedFeatureTableRecipe(), learner, settings);
			BuildModelControl.BuildModelTask task = new BuildModelControl.BuildModelTask(progress, newRecipe);
			task.execute();
		}
    	
    }
    
    public static class BuildModelTask extends OnPanelSwingTask{
    	GenesisRecipe plan;
    	
    	public BuildModelTask(JProgressBar progressBar, GenesisRecipe newRecipe){
    		this.addProgressBar(progressBar);
    		plan = newRecipe;
    	}
    	
    	protected Void doInBackground(){
    		try{
    			FeatureTable current = plan.getTrainingTable();
    			if(current != null){
    				System.out.println(GenesisWorkbenchPanel.refreshCount +" BMC151");
    				SimpleTrainingResult model = plan.getLearner().train(current, plan.getLearnerSettings(), validationSettings, BuildModelControl.getUpdater());
    				System.out.println(GenesisWorkbenchPanel.refreshCount +" BMC153");
    				plan.setTrainingResult(model);
    				System.out.println(GenesisWorkbenchPanel.refreshCount +" BMC155");
    				model.setName(BuildModelControl.getNewName());
    				System.out.println(GenesisWorkbenchPanel.refreshCount +" BMC157");
    				BuildModelControl.setHighlightedTrainedModelRecipe(plan);
    				System.out.println(GenesisWorkbenchPanel.refreshCount +" BMC159");
    				RecipeManager.addRecipe(plan);
    				System.out.println(GenesisWorkbenchPanel.refreshCount +" BMC161");
    				GenesisWorkbench.update();
    				System.out.println(GenesisWorkbenchPanel.refreshCount +" BMC163");
    				update.reset();
    			}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		return null;
    	}
    }
    

	protected void prepareTestSet(GenesisRecipe train, SimpleDocumentList test){
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
    
    public static GenesisRecipe getHighlightedFeatureTableRecipe(){
    	return highlightedFeatureTable;
    }
    
    public static GenesisRecipe getHighlightedTrainedModelRecipe(){
    	return highlightedTrainedModel;
    }
    
    public static void setHighlightedFeatureTableRecipe(GenesisRecipe highlight){
    	highlightedFeatureTable = highlight;
    	GenesisWorkbench.update();
    }
    
    public static void setHighlightedTrainedModelRecipe(GenesisRecipe highlight){
    	highlightedTrainedModel = highlight;
    	GenesisWorkbench.update();
    }
}
