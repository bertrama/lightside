package edu.cmu.side.plugin;

import java.io.Serializable;
import java.util.Map;

import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.FeatureTable;

/**
 * Filter plugins are used in the Modify Features panel. Given a feature table,
 * along with a list of options specified within a user interface, they return a
 * new set of FeatureHits that will be used in a new FeatureTable.
 * 
 * Remember that filter plugin UI components can't be static because we might have
 * multiples!
 * 
 */
public abstract class FilterPlugin extends SIDEPlugin implements Serializable{

	@Override
	public String getType() {
		return "filter_extractor";
	}
	
	public FeatureTable filter(FeatureTable original, Map<String, String> configuration, StatusUpdater progressIndicator){
		this.configureFromSettings(configuration);
		boolean[] allTrue = new boolean[original.getDocumentList().getSize()];
		for(int i = 0; i < allTrue.length; i++){ allTrue[i] = true; }
		return filterWithMaskForSubclass(original, allTrue, progressIndicator);
	}
	
	public FeatureTable filterWithCrossValidation(FeatureTable original, boolean[] mask, Map<String, String> configuration, StatusUpdater progressIndicator){
		this.configureFromSettings(configuration);
		return filterWithMaskForSubclass(original, mask, progressIndicator);
	}
	
	public FeatureTable filterTestSet(FeatureTable original, FeatureTable test, Map<String, String> configuration, StatusUpdater progressIndicator){
		this.configureFromSettings(configuration);
		return filterTestSetForSubclass(original, test, progressIndicator);
	}
	
	protected abstract FeatureTable filterWithMaskForSubclass(FeatureTable original, boolean[] mask, StatusUpdater progressIndicator);

	protected abstract FeatureTable filterTestSetForSubclass(FeatureTable original, FeatureTable test, StatusUpdater progressIndicator);
	
}
