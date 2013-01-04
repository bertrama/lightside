package edu.cmu.side.model.data;

import java.util.List;

public class PredictionResult {

	private String name;
	private FeatureTable test;
	private boolean[] mask;
	private List<? extends Comparable<?>> predictions;

	public String toString(){
		return name;
	}

	public String getName(){
		return name;
	}

	public void setName(String n){
		name = n;
	}
	
	public PredictionResult(FeatureTable te, boolean[] m, List<? extends Comparable<?>> pred){
		test = te; mask = m; predictions = pred;
	}
	
	public List<? extends Comparable<?>> getPredictions(){
		return predictions;
	}
}
