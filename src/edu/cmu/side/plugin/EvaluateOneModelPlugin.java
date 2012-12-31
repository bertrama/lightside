package edu.cmu.side.plugin;


public abstract class EvaluateOneModelPlugin extends SIDEPlugin{

	@Override
	public String getType() {
		return "model_analysis";
	}

	
	/**
	 * @return A short prefix string for the plugin name.
	 */
	public abstract String getOutputName();

}
