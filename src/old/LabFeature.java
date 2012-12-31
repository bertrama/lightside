package old;

import java.util.Collection;
import java.util.TreeSet;

import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.FeatureHit;

public class LabFeature extends Feature{

	public void exportLabFeature(FeatureTable newTable){}
	
	public Collection<FeatureHit> buildHits(FeatureTable newTable){
		return new TreeSet<FeatureHit>();
	}
}
