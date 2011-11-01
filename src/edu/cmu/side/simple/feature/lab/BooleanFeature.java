package edu.cmu.side.simple.feature.lab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public class BooleanFeature extends LabFeature{

	Collection<Feature> features;
	String function;
	public BooleanFeature(String n, Collection<Feature> feats){
		features = feats;
		String name = "("+n+" ";
		function = n;
		for(Feature f : features){
			name += f.getFeatureName() + " ";
		}
		name = name.substring(0, name.length()-1);
		name += ")";

		this.featureName = name;
		this.extractorPrefix = "lab";
		this.featureType = Type.BOOLEAN;
	}
	
	@Override
	public void exportLabFeature(FeatureTable newTable){
		newTable.addAllHits(buildHits(newTable));
	}
	
	@Override
	public Collection<FeatureHit> buildHits(FeatureTable newTable){
		if(newTable == null){
			return new TreeSet<FeatureHit>();
		}
		Map<Feature, Collection<FeatureHit>> hits = new TreeMap<Feature, Collection<FeatureHit>>();
		for(Feature f : features){
			if(f instanceof LabFeature){
				hits.put(f, ((LabFeature)f).buildHits(newTable));
			}else{
				if(newTable.getFeatureSet().contains(f)){
					hits.put(f, newTable.getHitsForFeature(f));	
				}else{
					hits.put(f, new TreeSet<FeatureHit>());
				}
			}
		}
		Map<Integer, Collection<FeatureHit>> hitsByIndex = new TreeMap<Integer, Collection<FeatureHit>>();
		for(Feature f : hits.keySet()){
			Set<Integer> docs = new TreeSet<Integer>();
			for(FeatureHit hit : hits.get(f)){
				if(!docs.contains(hit.getDocumentIndex()) && !hitsByIndex.containsKey(hit.getDocumentIndex())){
					hitsByIndex.put(hit.getDocumentIndex(), new ArrayList<FeatureHit>());
					docs.add(hit.getDocumentIndex());
				}
				hitsByIndex.get(hit.getDocumentIndex()).add(hit);
			}
		}
		Collection<FeatureHit> out = new TreeSet<FeatureHit>();
		if(function.equals("NOT")){
			for(int i = 0; i < newTable.getDocumentList().getSize(); i++){
				if(!hitsByIndex.containsKey(i)){
					out.add(new FeatureHit(this, Boolean.TRUE, i));
				}
			}
		}else{
			for(Integer doc : hitsByIndex.keySet()){
				if(function.equals("OR")){
					out.add(new FeatureHit(this, Boolean.TRUE, doc));
				}else if(function.equals("XOR")){
					if(hitsByIndex.get(doc).size() == 1){
						out.add(new FeatureHit(this, Boolean.TRUE, doc));
					}
				}else if(function.equals("AND")){
					if(hitsByIndex.get(doc).size() == features.size()){
						out.add(new FeatureHit(this, Boolean.TRUE, doc));
					}
				}
			}			
		}
		return out;
	}
	
}
