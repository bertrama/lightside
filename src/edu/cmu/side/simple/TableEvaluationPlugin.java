package edu.cmu.side.simple;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.SimpleTrainingResult;

public abstract class TableEvaluationPlugin<E extends Comparable<E>> extends SIDEPlugin{

	ArrayList<String> evaluationNames = new ArrayList<String>();
	
	public TableEvaluationPlugin() {
		super();
	}
	
	public static String type = "table_evaluation";
	
	public String getType() {
		return type;	
	}

	@Override
	protected Component getConfigurationUIForSubclass() {
		return null;
	}
		
	/**
	 * @return A short prefix string for the plugin name.
	 */
	public abstract String getOutputName();

	public abstract Collection<String> getAvailableEvaluations(FeatureTable table);
	
	/**
	 * 
	 */
	public abstract Map<Feature, E> evaluateTableFeatures(FeatureTable model, String evaluation);
}
