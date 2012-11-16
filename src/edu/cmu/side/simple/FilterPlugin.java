package edu.cmu.side.simple;

import java.io.Serializable;

import java.util.Collection;
import java.util.Map;

import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

/**
 * Filter plugins are used in the Modify Features panel. Given a feature table,
 * along with a list of options specified within a user interface, they return a
 * new set of FeatureHits that will be used in a new FeatureTable.
 */
public abstract class FilterPlugin extends SIDEPlugin implements Serializable{

	@Override
	public String getType() {
		return "filter";
	}
	
	public FeatureTable filter(String name, FeatureTable original, Map<String, String> configuration, GenesisUpdater progressIndicator){
		this.configureFromSettings(configuration);
		return filterForSubclass(name, original, progressIndicator);
	}
	
	protected abstract FeatureTable filterForSubclass(String name, FeatureTable original, GenesisUpdater progressIndicator);
	
}
