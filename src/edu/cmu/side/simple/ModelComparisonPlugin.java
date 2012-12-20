package edu.cmu.side.simple;

import java.awt.Component;
import java.util.Collection;
import java.util.Map;

import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureTable;

public abstract class ModelComparisonPlugin extends SIDEPlugin{

	@Override
	public String getType() {
		return "model_comparison";
	}

	
	/**
	 * @return A short prefix string for the plugin name.
	 */
	public abstract String getOutputName();

}
