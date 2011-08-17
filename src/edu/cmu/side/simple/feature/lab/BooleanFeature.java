package edu.cmu.side.simple.feature.lab;

import java.util.Collection;

import edu.cmu.side.simple.feature.Feature;

public class BooleanFeature extends Feature{

	public BooleanFeature(String n, Collection<Feature> features){
		String name = "("+n+" ";
		for(Feature f : features){
			name += f.getFeatureName() + " ";
		}
		name = name.substring(0, name.length()-1);
		name += ")";

		this.featureName = name;
		this.extractorPrefix = "lab";
		this.featureType = Type.BOOLEAN;
	}
}
