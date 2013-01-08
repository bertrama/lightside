package edu.cmu.side.model.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PredictionResult implements Serializable{

	private String name;
	private FeatureTable test; //what is my purpose?
	private boolean[] mask; //what is my purpose?
	private List<? extends Comparable<?>> predictions;
	private List<Map<String, Double>> distributions;

	public String toString(){
		if(name != null)
			return name;
		return "predictions on "+test.getName();
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
	
	public PredictionResult(FeatureTable te, boolean[] m, List<String> pred, List<Map<String, Double>> dist)
	{
		this(te, m, pred);
		distributions = dist;
	}

	public List<Map<String, Double>> getDistributions()
	{
		return distributions;
	}
	
	public List<? extends Comparable<?>> getPredictions(){
		return predictions;
	}
}
