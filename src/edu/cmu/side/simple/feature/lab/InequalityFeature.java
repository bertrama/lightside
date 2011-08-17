package edu.cmu.side.simple.feature.lab;

import edu.cmu.side.simple.feature.Feature;

public class InequalityFeature extends Feature{
	
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

}
