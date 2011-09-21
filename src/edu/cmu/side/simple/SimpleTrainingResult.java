package edu.cmu.side.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private List<Comparable> predictions = new ArrayList<Comparable>();
	private Map<String, String> evaluation = null;
	private String annot;
	private double uniqueID = -1;
	LearningPlugin plugin = null;
	/** first label is predicted label, second label is actual label. */
	private Map<String, Map<String, ArrayList<Integer>>> confusionMatrix = new TreeMap<String, Map<String, ArrayList<Integer>>>();

	public SimpleTrainingResult(LearningPlugin p, String n, FeatureTable f){
		name = n;
		table = f;
		plugin = p;
		annot = ""+f.getDocumentList().getCurrentAnnotation();
		uniqueID = Math.random();
		p.toFile(uniqueID);
	}
	
	public SimpleTrainingResult(File f){
		try{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
			name = (String)in.readObject();
			table = new FeatureTable(in);
			predictions = (List<Comparable>)in.readObject();
			evaluation = (Map<String, String>)in.readObject();
			confusionMatrix = (Map<String, Map<String, ArrayList<Integer>>>)in.readObject();
			plugin = (LearningPlugin)in.readObject();
			annot = (String)in.readObject();
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void serialize(File f){
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));				
			out.writeObject(name);
			table.writeSerializedTable(out);
			out.writeObject(predictions);
			out.writeObject(evaluation);
			out.writeObject(confusionMatrix);
			out.writeObject(plugin);
			out.writeObject(annot);
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public String getSubtypeName() {
		// TODO Auto-generated method stub
		return null;
	}


	public String toString(){
		return name;
	}

	@Override
	public DocumentListInterface getDocumentList() {
		table.getDocumentList().setCurrentAnnotation(annot);
		return table.getDocumentList();
	}

	public FeatureTable getFeatureTable(){
		return table;
	}

	public Map<String, String> getEvaluation(){
		return evaluation;
	}

	public void addEvaluation(Map<String, String> ev){
		if(ev == null) return;
		table.getDocumentList().setCurrentAnnotation(annot);
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
						break;
					}
					generateConfusionMatrix(table.getDocumentList().getAnnotationArray(), predictions);
				}
			}
			evaluation = ev;
		}catch(Exception e){
			e.printStackTrace();

		}
	}

	public void generateConfusionMatrix(List<String> actual, List<Comparable> predicted){
		table.getDocumentList().setCurrentAnnotation(annot);
		switch(table.getClassValueType()){
		case NOMINAL:
		case BOOLEAN:
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
			break;
		case NUMERIC:
			ArrayList<Double> values = new ArrayList<Double>();
			for(int i = 0; i < actual.size(); i++){
				values.add(Double.parseDouble(actual.get(i)));
			}
			Collections.sort(values);
			double[] quarts = new double[4];
			for(double i = 1; i <= 4; i++){
				quarts[((Double)i).intValue()-1] = values.get(((Double)(values.size()*(i/5.0))).intValue()-1);
			}
			for(int i = 0; i < actual.size(); i++){
				Double predDbl = Double.parseDouble(predicted.get(i).toString());
				Double actDbl = Double.parseDouble(actual.get(i).toString());
				int Qact = -1; int Qpred = -1;
				int j = 0;
				while(j < 4 && predDbl > quarts[j]) j++;
				Qpred = j;
				j = 0;
				while(j < 4 && actDbl > quarts[j]) j++;
				Qact = j;
				String pred = "Q"+(Qpred+1);
				String act = "Q"+(Qact+1);
				if(!confusionMatrix.containsKey(pred)){
					confusionMatrix.put(pred, new TreeMap<String, ArrayList<Integer>>());
				}
				if(!confusionMatrix.get(pred).containsKey(act)){
					confusionMatrix.get(pred).put(act, new ArrayList<Integer>());
				}
				confusionMatrix.get(pred).get(act).add(i);
			}
			break;
		}
	}

	public List<Integer> getConfusionMatrixCell(String pred, String act){
		table.getDocumentList().setCurrentAnnotation(annot);
		if(confusionMatrix.containsKey(pred)){
			if(confusionMatrix.get(pred).containsKey(act)){
				return confusionMatrix.get(pred).get(act);
			}
		}
		return new ArrayList<Integer>();
	}

	public Double getAverageValue(List<Integer> docIndices, Feature f){
		table.getDocumentList().setCurrentAnnotation(annot);
		Double accumulator = 0.0;
		if(docIndices.size()==0) return 0.0;
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
	
	public String getSummary(){
		return evaluation.get("summary");
	}
	
	public FeatureTable predictLabels(String newName, SimpleDocumentList newData)
	{
		Collection<FeaturePlugin> featureExtractors = table.getExtractors();
		FeatureTable newFeatureTable = new FeatureTable(featureExtractors, newData, 0);
		return predictLabels(newName, annot, newFeatureTable);
		
	}
	
	public FeatureTable predictLabels(String newName, String oldName, FeatureTable newFeatureTable)
	{
		//the old document list knows all...
		table.getDocumentList().setCurrentAnnotation(oldName);
		newFeatureTable.setExternalClassValueType(table.getClassValueType());
		newFeatureTable.getDocumentList().setExternalLabelArray(table.getDocumentList().getLabelArray());
		
		reconcileFeatures(newFeatureTable);

		Set<Feature> oldTableFeatures = table.getFeatureSet();
		Set<Feature> newTableFeatures= newFeatureTable.getFeatureSet();

		//plugin.fromFile(uniqueID); //WHY?
		
		System.out.println("plugin: "+plugin);
		
		if(oldTableFeatures.size() == newTableFeatures.size())
		{
			plugin.predict(newName, newFeatureTable);
		}
		else
			System.err.println("features do not match:\nold: "+oldTableFeatures.size()+"\nnew: "+newTableFeatures.size());
		
		return newFeatureTable;
	}

	private void reconcileFeatures(FeatureTable newFeatureTable)
	{
		Set<Feature> oldTableFeatures = table.getFeatureSet();
		Set<Feature> newTableFeatures = newFeatureTable.getFeatureSet();
		
		//weka does lots of things by index, instead of key... which is why the feature tables have to match exactly.
		int count = 0;
		System.out.println("old features: "+oldTableFeatures.size());
		System.out.println("new features: "+newTableFeatures.size());
		
		if(oldTableFeatures.size() != newTableFeatures.size())
		{
			Set<Feature> remove = new HashSet<Feature>();
			for(Feature f: newTableFeatures)
			{
				boolean found = oldTableFeatures.contains(f);
//				boolean found = false;
//				for(Feature oldFeat : oldTableFeatures)
//				{
//					if(oldFeat.getExtractorPrefix().equals(f.getExtractorPrefix()) && oldFeat.getFeatureName().equals(f.getFeatureName()))
//					{
//						found = true;
//						break;
//					}
//				}
				if(!found)
				{
					remove.add(f);
					count++;
				}
			}
			for(Feature f : remove)
			{
				newFeatureTable.deleteFeature(f);
			}
			System.out.println(count+" novel features removed");

			oldTableFeatures = table.getFeatureSet();
			newTableFeatures = newFeatureTable.getFeatureSet();
			
			count = 0;
			for(Feature f : oldTableFeatures)
			{
				boolean found = newTableFeatures.contains(f);
				//boolean found = false;
//				for(Feature newFeat : newTableFeatures)
//				{
//					if(newFeat.getExtractorPrefix().equals(f.getExtractorPrefix()) &&newFeat.getFeatureName().equals(f.getFeatureName()))
//					{
//						found = true;
//						break;
//					}
//				}
				if(!found)
				{
					count++;
					newFeatureTable.addEmptyFeature(f);
				}	
				
			}
			System.out.println(count+" empty features added");
		}
	}
}
