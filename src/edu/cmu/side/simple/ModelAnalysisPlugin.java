package edu.cmu.side.simple;

import edu.cmu.side.plugin.SIDEPlugin;

public abstract class ModelAnalysisPlugin extends SIDEPlugin{

	@Override
	public String getType() {
		return "model_analysis";
	}

	
	/**
	 * @return A short prefix string for the plugin name.
	 */
	public abstract String getOutputName();

}
