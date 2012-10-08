package edu.cmu.side.simple;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import javax.swing.JLabel;

import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.genesis.model.GenesisRecipe;

public abstract class LearningPlugin extends SIDEPlugin implements Serializable{
	private static final long serialVersionUID = -7928450759075851993L;

	protected static boolean halt = false;

	/**
	 * Kept for legacy reasons, implemented in this class so the developer doesn't have to.
	 */
	@Override
	public boolean doValidation(StringBuffer msg) {
		return true;
	}

/*	public void addEvaluationColumns(FeatureTable ft){
		
	}
*/	
	
	@Override
	public void memoryToUI() {}

	/**
	 * Called before extracting features; implementation should ensure that 
	 * all settings in the UI are transferred to model settings in this method.
	 */
	@Override
	public void uiToMemory() {}
	
	public LearningPlugin(){
		super();
	}
	
	public LearningPlugin(File root){
		super(root);
	}
	

	public static String type = "model_builder";
	
	public String getType() {
		return type;
	}
		
	/**
	 * @return A short prefix string for the plugin name.
	 */
	public abstract String getOutputName();
	
	public abstract void predict(String name, GenesisRecipe documents, String[] possibleLabels);
	
	public abstract TrainingResultInterface train(GenesisRecipe recipe, String desiredName, Map<String, String> evaluationSettings, Map<Integer, Integer> foldsMap, JLabel progressIndicator) throws Exception;

	public abstract void toFile(double uniqueID);
	
	public abstract void fromFile(double uniqueID);
	
	public void stopWhenPossible(){
		halt = true;
	}
}
