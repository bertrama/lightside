package edu.cmu.side.simple;

import java.io.Serializable;

import java.util.Collection;

import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

/**
 * Filter plugins are used in the Modify Features panel. Given a feature table,
 * along with a list of options specified within a user interface, they return a
 * new set of FeatureHits that will be used in a new FeatureTable.
 */
public abstract class FilterPlugin extends SIDEPlugin implements Serializable{

	protected static boolean halt = false;

	@Override
	public String getType() {
		return "filter";
	}

	@Override
	public void memoryToUI() {}
	
	@Override
	public void uiToMemory() {}
	
	/**
	 * @return A short string for the plugin name.
	 */
	public abstract String getOutputName();
	
	public abstract FeatureTable filter(FeatureTable orgtable);
	
	@Override
	public boolean doValidation(StringBuffer msg) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public void stopWhenPossible(){
		halt = true;
	}

}
