package plugin.simple.filter;

import java.awt.Component;
import java.util.Set;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureTable;

public class DFFeatureSelecter extends feature_selection{
	
	protected Component getConfigurationUIForSubclass(){
		throw new UnsupportedOperationException();
	}
	
	public String getOutputName(){
		return "DF Feature Selecter";
	}
	
	protected Set<Feature> getRemoveFeatures(FeatureTable orgtable){
		throw new UnsupportedOperationException();
	}
	

}
