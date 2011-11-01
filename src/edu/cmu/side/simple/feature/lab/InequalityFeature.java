package edu.cmu.side.simple.feature.lab;

import java.util.Collection;
import java.util.TreeSet;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public class InequalityFeature extends LabFeature{
	
	Feature base;
	String ineq;
	Double comparison;
	
	public InequalityFeature(Feature b, String i, Double c){
		base = b;
		ineq = i;
		comparison = c;
		this.featureName = base.getFeatureName()+ineq+comparison;
		this.extractorPrefix = "lab";
		this.featureType = Type.BOOLEAN;
	}

	@Override
	public void exportLabFeature(FeatureTable newTable){
		newTable.addAllHits(buildHits(newTable));
	}
	
	@Override
	public Collection<FeatureHit> buildHits(FeatureTable newTable){
		return new TreeSet<FeatureHit>();
	}
}
