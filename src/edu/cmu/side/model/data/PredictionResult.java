package edu.cmu.side.model.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PredictionResult implements Serializable
{

	private String name;
	private List<? extends Comparable<?>> predictions;
	private Map<String, List<Double>> distributions;

	public String toString()
	{
		if (name != null) return name;
		return "predictions";
	}

	public String getName()
	{
		return name;
	}

	public void setName(String n)
	{
		name = n;
	}

	public PredictionResult(List<? extends Comparable<?>> pred)
	{
		predictions = pred;
	}

	public PredictionResult(List<String> pred, Map<String, List<Double>> dist)
	{
		this(pred);
		distributions = dist;
	}

	public List<Double> getScoresForLabel(String label)
	{
		return distributions.get(label);
	}

	public Map<String, List<Double>> getDistributions()
	{
		return distributions;
	}

	public List<? extends Comparable<?>> getPredictions()
	{
		return predictions;
	}
}
