package plugin.simple.filter;

import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.feature.Feature;
import java.util.Set;


public abstract class feature_selection extends FilterPlugin{
	
	public FeatureTable filter(FeatureTable orgtable){
		FeatureTable newtable = orgtable.subsetClone();
		newtable.setTableName(orgtable.getTableName() + " (selected)");
		newtable.deleteFeatureSet( getRemoveFeatures(orgtable) );
		return newtable;
	}
	
	protected abstract Set<Feature> getRemoveFeatures(FeatureTable orgtable);
}
