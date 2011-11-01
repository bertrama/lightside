package edu.cmu.side.simple.feature.lab;

import java.util.Collection;
import java.util.TreeSet;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public class LabFeature extends Feature{

	public void exportLabFeature(FeatureTable newTable){}
	
	public Collection<FeatureHit> buildHits(FeatureTable newTable){
		return new TreeSet<FeatureHit>();
	}
}
