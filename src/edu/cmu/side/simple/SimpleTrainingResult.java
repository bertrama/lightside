package edu.cmu.side.simple;

import java.util.ArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;


import edu.cmu.side.dataitem.DocumentListInterface;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public class SimpleTrainingResult implements TrainingResultInterface{

	private String name;
	private FeatureTable table;
	private FeatureTable testSet;
	private List<Comparable> predictions = new ArrayList<Comparable>();
	private Map<String, String> evaluation = null;

	/** first label is predicted label, second label is actual label. */
	private Map<String, Map<String, ArrayList<Integer>>> confusionMatrix = new TreeMap<String, Map<String, ArrayList<Integer>>>();

	public SimpleTrainingResult(String n, FeatureTable f){
		name = n;
		table = f;
	}

	@Override
	public void fromXML(Element root) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSubtypeName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(){
		return name;
	}

	@Override
	public DocumentListInterface getDocumentList() {
		return table.getDocumentList();
	}

	public FeatureTable getFeatureTable(){
		return table;
	}
	
	public Map<String, String> getEvaluation(){
		return evaluation;
	}

	public void addEvaluation(Map<String, String> ev){
		try{
			for(String eval : ev.keySet()){
				if(eval.equals("cv-fold-predictions")){
					String[] labels = ev.get(eval).split("\n");
					switch(table.getClassValueType()){
					case NUMERIC:
						for(int i = 0; i < labels.length; i++){
							if(labels[i].length() > 0){
								predictions.add(Double.parseDouble(labels[i]));								
							}
						}
						break;
					case BOOLEAN:
					case STRING:
					case NOMINAL:
						for(int i = 0; i < labels.length; i++){
							predictions.add(labels[i]);
						}
						generateConfusionMatrix(table.getDocumentList().getAnnotationArray(), predictions);
						break;
					}
				}
			}
			evaluation = ev;
		}catch(Exception e){
			e.printStackTrace();

		}
	}

	public void generateConfusionMatrix(List<String> actual, List<Comparable> predicted){
		System.out.println(actual.size() + ", " + predicted.size());
		if(actual.size() != predicted.size()){
			System.out.println(predicted.get(0).toString());
		}
		for(int i = 0; i < actual.size(); i++){
			String pred = predicted.get(i).toString();
			String act = actual.get(i);
			if(!confusionMatrix.containsKey(pred)){
				confusionMatrix.put(pred, new TreeMap<String, ArrayList<Integer>>());
			}
			if(!confusionMatrix.get(pred).containsKey(act)){
				confusionMatrix.get(pred).put(act, new ArrayList<Integer>());
			}
			confusionMatrix.get(pred).get(act).add(i);
		}
		System.out.print("    ");
		for(String label : table.getDocumentList().getLabelArray()){
			System.out.printf("%4s", label);
		}
		System.out.println();
		for(String act : table.getDocumentList().getLabelArray()){
			System.out.printf("%4s", act);
			for(String pred : table.getDocumentList().getLabelArray()){
				System.out.printf("%4d", getConfusionMatrixCell(pred, act).size());
			}
			System.out.println();
		}
	}

	public List<Integer> getConfusionMatrixCell(String pred, String act){
		if(confusionMatrix.containsKey(pred)){
			if(confusionMatrix.get(pred).containsKey(act)){
				return confusionMatrix.get(pred).get(act);
			}
		}
		return new ArrayList<Integer>();
	}

	public Double getAverageValue(List<Integer> docIndices, Feature f){
		Double accumulator = 0.0;
		for(Integer doc : docIndices){
			Collection<FeatureHit> hits = table.getHitsForFeature(f);
			if(hits == null) continue;
			switch(f.getFeatureType()){
			case NUMERIC:
				for(FeatureHit hit : hits){
					if(hit.getDocumentIndex()==doc) accumulator += ((Number)hit.getValue()).doubleValue();
				}
				break;
			case BOOLEAN:
				for(FeatureHit hit : hits){
					if(hit.getDocumentIndex()==doc && Boolean.TRUE.equals(hit.getValue())) accumulator++;
				}
				break;
			case NOMINAL:
				break;
			case STRING:
				break;
			}			
		}
		Double value = accumulator/docIndices.size();
		return value;
	}
	
	public List<Comparable> getPredictions(){
		return predictions;
	}
}
