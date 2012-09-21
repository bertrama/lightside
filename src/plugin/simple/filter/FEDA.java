package plugin.simple.filter;

import java.awt.Component;

import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.FilterPlugin;

public class FEDA extends FilterPlugin {

	protected Component getConfigurationUIForSubclass(){
		throw new UnsupportedOperationException();
	}
	
	public String getOutputName(){
		return "FEDA";
	}
	
	public FeatureTable filter(FeatureTable orgtable){
		throw new UnsupportedOperationException();
	}	
}
