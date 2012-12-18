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


import edu.cmu.side.dataitem.FreqMap;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.feature.Feature.Type;

/**
 * The result of the training process; doesn't actually contain the learning itself,
 * only the set of training tables, test tables, predictions, and confusion matrix.
 * @author emayfiel
 *
 */
public class SimpleTrainingResult{

	private String name;
	private FeatureTable train;
	private FeatureTable test;
	private Map<String, String> evaluation = null;
	private List<? extends Comparable> predictions;
	private Map<String, Map<String, ArrayList<Integer>>> confusionMatrix = new TreeMap<String, Map<String, ArrayList<Integer>>>();

	public String toString(){
		return name;
	}

	public String getName(){
		return name;
	}

	public void setName(String n){
		name = n;
	}
	
	public String getDescriptionString(){
		return "Training Result!";
	}

	public String getSummary(){
		return evaluation.get("summary");
	}

	public Map<String, Map<String, ArrayList<Integer>>> getConfusionMatrix(){
		return confusionMatrix;
	}
	
	public Map<String, String> getEvaluations(){
		return evaluation;
	}
	
	public SimpleTrainingResult(FeatureTable tr, List<? extends Comparable> pred){
		train = tr;
		predictions = pred;
		evaluation = new TreeMap<String, String>();
		generateConfusionMatrix(tr.getClassValueType(), tr.getDocumentList().getAnnotationArray(), pred);
	}

	/**
	 * Use with supplied test set.
	 */
	public SimpleTrainingResult(FeatureTable tr, FeatureTable te, List<? extends Comparable> pred){
		train = tr;
		test = te;
		predictions = pred;
		evaluation = new TreeMap<String, String>();
		generateConfusionMatrix(te.getClassValueType(), te.getDocumentList().getAnnotationArray(), pred);
	}

	private void generateConfusionMatrix(Feature.Type type, List<String> actual, List<? extends Comparable> predicted){
		switch(type){
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


	//        private GenesisRecipe trainrecipe;
	//        private GenesisRecipe testrecipe;
	//        private double uniqueID = -1;
	//        /** first label is predicted label, second label is actual label. */
	//
	//        public SimpleTrainingResult(String modelname, GenesisRecipe gr){
		//                name = modelname;
		//                trainrecipe = gr;
		//                uniqueID = Math.random();
		//                gr.getLearningPlugin().toFile(uniqueID);
		//        }
	//        
	//        public void serialize(File f){
		//                try{
	//                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));                               
	//                        out.writeObject(name);
	//                        trainrecipe.writeSerializedTable(out);
	//                        out.writeObject(predictions);
	//                        out.writeObject(evaluation);
	//                        out.writeObject(confusionMatrix);
	//                        out.close();
	//                }catch(Exception e){
	//                        e.printStackTrace();
	//                }
	//        }
	//        
	//        public SimpleTrainingResult(File f) throws Exception{
	//                ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
	//                name = (String)in.readObject();
	//                trainrecipe = new GenesisRecipe(in);            
	//                predictions = (List<Comparable>)in.readObject();
	//                evaluation = (Map<String, String>)in.readObject();
	//                confusionMatrix = (Map<String, Map<String, ArrayList<Integer>>>)in.readObject();
	//                in.close();
	//        }
	//        
	//        public GenesisRecipe predictLabels(String newName, SimpleDocumentList newData)
	//        {
	//                GenesisRecipe topredict = new GenesisRecipe(trainrecipe, newData, true);
	//                topredict.modify(trainrecipe.getOriginalTable());
	//                trainrecipe.getLearningPlugin().predict(newName, topredict, trainrecipe.getModifiedTable().getPossibleLabels());
	//                return topredict;
	//        }
	//
	//        public GenesisRecipe getEvaluationRecipe(){
	//                return (testrecipe == null) ? trainrecipe : testrecipe;
	//        }
	//
	//        public void setEvaluationRecipe(GenesisRecipe gr){
	//                testrecipe = gr;
	//        }
	//
	//        public String getSubtypeName() {
	//                return null;
	//        }
	//
	//
	//        public SimpleDocumentList getDocumentList() {
	//                return trainrecipe.getDocumentList();
	//        }
	//
	//        public FeatureTable getorgFeatureTable(){
	//                return trainrecipe.getOriginalTable();
	//        }
	//        
	//        public FeatureTable getmodifyFeatureTable(){
	//                return trainrecipe.getModifiedTable();
	//        }
	//
	//        
	//        
	//        
	//        
	//        
	//        public Map<String, String> getEvaluation(){
	//                return evaluation;
	//        }
	//
	//        public void addEvaluation(Map<String, String> ev){
	//                if(ev == null) return;
	//
	//                try{
	//                        for(String eval : ev.keySet()){
	//                                if(eval.equals("cv-fold-predictions")){
	//                                        String[] labels = ev.get(eval).split("\n");
	//                                        switch(trainrecipe.getModifiedTable().getClassValueType()){
	//                                        case NUMERIC:
	//                                                for(int i = 0; i < labels.length; i++)
	//                                                        if(labels[i].length() > 0)
	//                                                                predictions.add(Double.parseDouble(labels[i]));                                                         
	//                                                break;
	//                                        case BOOLEAN:
	//                                        case STRING:
	//                                        case NOMINAL:
	//                                                for(int i = 0; i < labels.length; i++)
	//                                                        predictions.add(labels[i]);
	//                                                break;
	//                                        }
	//                                        if(testrecipe != null)
	//                                                generateConfusionMatrix(testrecipe.getModifiedTable().getClassValueType(), testrecipe.getDocumentList().getAnnotationArray(), predictions);                                             
	//                                        else
	//                                                generateConfusionMatrix(trainrecipe.getModifiedTable().getClassValueType(), trainrecipe.getDocumentList().getAnnotationArray(), predictions);   
	//                                }
	//                        }
	//                        if(evaluation == null){
	//                                evaluation = ev;                                
	//                        }else{
	//                                evaluation.putAll(ev);
	//                        }
	//                }catch(Exception e){
	//                        e.printStackTrace();
	//                }
	//        }
	//
	//        private void calculateQuadraticKappa(Feature.Type type, List<String> actual, List<Comparable> predicted) {
	//                boolean quad = false;
	//                if(type==Feature.Type.NUMERIC){
	//                        quad = true;
	//                }
	//                List<Integer> realActuals = new ArrayList<Integer>();
	//                List<Integer> realPredicted = new ArrayList<Integer>();
	//                if(type==Feature.Type.NOMINAL){
	//                        boolean reallyNumeric = true;
	//                        try{
	//                                for(String s : actual){
	//                                        Integer i = Integer.parseInt(s.substring(1));
	//                                        realActuals.add(i);
	//                                }
	//                                for(Comparable s : predicted){
	//                                        Integer i = Integer.parseInt(s.toString().substring(1));
	//                                        realPredicted.add(i);
	//                                }
	//                        }catch(Exception e){
	//                                reallyNumeric = false;
	//                        }
	//                        if(reallyNumeric){
	//                                quad = true;
	//                        }
	//                }else if(type == Feature.Type.NUMERIC){
	//                        for(String s : actual){
	//                                realActuals.add(Integer.parseInt(s));
	//                        }
	//                        for(Comparable s : predicted){
	//                                realPredicted.add((new Double(Math.ceil(Double.parseDouble(s.toString())))).intValue());
	//                        }
	//                }
	//                quad = quad&&trainrecipe.getDocumentList().allAnnotations().containsKey("score1")&&trainrecipe.getDocumentList().allAnnotations().containsKey("score2");
	//                if(quad){
	//                        System.out.println("quad");
	//                        String weightedEvaluation = getWeightedKappa(realActuals, realPredicted);
	//                        List<Integer> scorer1 = new ArrayList<Integer>();
	//                        List<Integer> scorer2 = new ArrayList<Integer>();
	//                        for(int i = 0; i < trainrecipe.getDocumentList().getSize(); i++){
	////                              scorer1.add(Integer.parseInt(table.getDocumentList().allAnnotations().get("score1").get(i)));
	////                              scorer2.add(Integer.parseInt(table.getDocumentList().allAnnotations().get("score2").get(i)));
	//                                scorer1.add(Integer.parseInt(trainrecipe.getDocumentList().allAnnotations().get("score1").get(i).substring(1)));
	//                                scorer2.add(Integer.parseInt(trainrecipe.getDocumentList().allAnnotations().get("score2").get(i).substring(1)));
	//                        }
	//                        weightedEvaluation += "Gold Standard:\n" + getWeightedKappa(scorer1, scorer2);
	//                        if(evaluation == null){
	//                                evaluation = new TreeMap<String, String>();
	//                        }
	//                        evaluation.put("Weighted Kappa", weightedEvaluation);
	//                }
	//        }
	//        
	//        public String getWeightedKappa(List<Integer> realActuals, List<Integer> realPredicted){
	//                Map<Integer, FreqMap<Integer>> histO = new TreeMap<Integer, FreqMap<Integer>>();
	//                FreqMap<Integer> e1 = new FreqMap<Integer>();
	//                FreqMap<Integer> e2 = new FreqMap<Integer>();
	//                Integer N = 0;
	//                for(int i = 0; i < trainrecipe.getDocumentList().getSize(); i++){
	//                        e1.count(realActuals.get(i));
	//                        e2.count(realPredicted.get(i));
	//                        if(!histO.containsKey(realActuals.get(i))){
	//                                histO.put(realActuals.get(i), new FreqMap<Integer>());
	//                        }
	//                        N = Math.max(N,realActuals.get(i));
	//                        histO.get(realActuals.get(i)).count(realPredicted.get(i));
	//                }
	//                String histString = "";
	//                Map<Integer, Map<Integer, Double>> probE = new TreeMap<Integer, Map<Integer, Double>>();
	//                for(Integer x : e1.keySet()){
	//                        probE.put(x, new TreeMap<Integer, Double>());
	//                        for(Integer y : e2.keySet()){
	//                                histString += "E,"+x+","+y+": "+e1.get(x) + "," + e2.get(y) + "\n";
	//                                double p1 = (0.0+e1.get(x))/trainrecipe.getDocumentList().getSize();
	//                                double p2 = (0.0+e2.get(y))/trainrecipe.getDocumentList().getSize();
	//                                probE.get(x).put(y, p1*p2);
	//                        }
	//                }
	//                Map<Integer, Map<Integer, Double>> probO = new TreeMap<Integer, Map<Integer, Double>>();
	//                for(Integer x : histO.keySet()){
	//                        probO.put(x, new TreeMap<Integer, Double>());
	//                        for(Integer y : histO.get(x).keySet()){
	//                                histString += "O,"+x+","+y+": "+histO.get(x).get(y) + "\n";
	//                                probO.get(x).put(y, (0.0+histO.get(x).get(y))/(0.0+trainrecipe.getDocumentList().getSize()));
	//                        }
	//                }
	//                double num = 0.0;
	//                double denom = 0.0;
	//                String probString = "";
	//                for(Integer x : probE.keySet()){
	//                        for(Integer y : probE.get(x).keySet()){
	//                                double w = Math.pow((0.0+(x-y)), 2.0)/(0.0+Math.pow(0.0+(N-1), 2.0));
	//                                denom += w*probE.get(x).get(y);
	//                                probString += "E,"+x+","+y+": " + w + " * " + probE.get(x).get(y) + "\n";
	//                        }
	//                }
	//                for(Integer x : probO.keySet()){
	//                        for(Integer y : probO.get(x).keySet()){
	//                                double w = Math.pow((0.0+(x-y)), 2.0)/(0.0+Math.pow(0.0+(N-1), 2.0));
	//                                num += w*probO.get(x).get(y);
	//                                probString += "O,"+x+","+y+": " + w + " * " + probO.get(x).get(y) + "\n";
	//                        }
	//                }
	//                double k = 1.0-(num/denom);
	//                double Z = 0.5*Math.log((1.0+k)/(1.0-k));
	//                String weightedEvaluation = "Z: " + Z + "\nk: " + k + "\nNumerator: " + num + "\nDenominator: " + denom + "\n" + probString + "\n" + histString;
	//                return weightedEvaluation;
	//        }
	//
	//        public List<Integer> getConfusionMatrixCell(String pred, String act){
	//                if(confusionMatrix.containsKey(pred)){
	//                        if(confusionMatrix.get(pred).containsKey(act)){
	//                                return confusionMatrix.get(pred).get(act);
	//                        }
	//                }
	//                return new ArrayList<Integer>();
	//        }
	//        
	//        public double getKappa(){
	//                Map<String, Double> predProb = new TreeMap<String, Double>();
	//                Map<String, Double> actProb = new TreeMap<String, Double>();
	//                double correctCount = 0.0;
	//                FeatureTable evaluationList = getEvaluationRecipe().getModifiedTable();
	//                for(String pred : evaluationList.getPossibleLabels()){
	//                        for(String act : evaluationList.getPossibleLabels()){
	//                                List<Integer> cell = getConfusionMatrixCell(pred, act);
	//                                if(!predProb.containsKey(pred)){
	//                                        predProb.put(pred, 0.0);
	//                                }
	//                                predProb.put(pred, predProb.get(pred)+cell.size());
	//                                if(!actProb.containsKey(act)){
	//                                        actProb.put(act, 0.0);
	//                                }
	//                                actProb.put(act, actProb.get(act)+cell.size());
	//                                if(act.equals(pred)){
	//                                        correctCount += cell.size();
	//                                }
	//                        }
	//                }
	//                double chance = 0.0;
	//                for(String lab : evaluationList.getPossibleLabels()){
	//                        predProb.put(lab, predProb.get(lab)/(0.0+evaluationList.getInstanceSize()));
	//                        actProb.put(lab, actProb.get(lab)/(0.0+evaluationList.getInstanceSize()));
	//                        chance += (predProb.get(lab)*actProb.get(lab));
	//                }
	//                correctCount /= (0.0+evaluationList.getInstanceSize());
	//                double kappa = (correctCount-chance)/(1-chance);
	//                return kappa;
	//        }
	//        
	//        public String getTextConfusionMatrix(){
	//                return getTextConfusionMatrix(trainrecipe.getModifiedTable().getPossibleLabels(), confusionMatrix);
	//        }
	//        
	//        public static String getTextConfusionMatrix(String[] labelArray, Map<String, Map<String, ArrayList<Integer>>> confusion){
	//                StringBuilder sb = new StringBuilder();
	//                int max = 4;
	//                for(String p : labelArray){
	//                        for(String a : labelArray){
	//                                max = Math.max(max, Math.max(p.length(), a.length()));
	//                                int numDigits = 1;
	//                                int numHits = confusion.containsKey(p)?(confusion.get(p).containsKey(a)?confusion.get(p).get(a).size():0):0;
	//                                while(numHits>=10){
	//                                        numHits /= 10;
	//                                        numDigits++;
	//                                }
	//                                max =  Math.max(max, numDigits);
	//                        }
	//                }
	//                for(int i = 0; i < max; i++){
	//                        sb.append(" ");
	//                }
	//                String format = "%"+max+"s";
	//                for(String p : labelArray){
	//                        sb.append(String.format(format,p));
	//                }
	//                sb.append("\n");
	//                for(String a : labelArray){
	//                        sb.append(String.format(format,a));
	//                        for(String p : labelArray){
	//                                int numHits = confusion.containsKey(p)?(confusion.get(p).containsKey(a)?confusion.get(p).get(a).size():0):0;
	//                                sb.append(String.format(format,(""+numHits)));                  
	//                        }
	//                        sb.append("\n");
	//                }
	//                return sb.toString();
	//        }
	//
	//        public Double getAverageValue(List<Integer> docIndices, Feature f){
	//                Double accumulator = 0.0;
	//                if(docIndices.size()==0) return 0.0;
	//                for(Integer doc : docIndices){
	//                        Collection<FeatureHit> hits = trainrecipe.getModifiedTable().getHitsForFeature(f);
	//                        if(hits == null) continue;
	//                        switch(f.getFeatureType()){
	//                        case NUMERIC:
	//                                for(FeatureHit hit : hits){
	//                                        if(hit.getDocumentIndex()==doc) accumulator += ((Number)hit.getValue()).doubleValue();
	//                                }
	//                                break;
	//                        case BOOLEAN:
	//                                for(FeatureHit hit : hits){
	//                                        if(hit.getDocumentIndex()==doc && Boolean.TRUE.equals(hit.getValue())) accumulator++;
	//                                }
	//                                break;
	//                        case NOMINAL:
	//                                break;
	//                        case STRING:
	//                                break;
	//                        }                       
	//                }
	//                Double value = accumulator/docIndices.size();
	//                return value;
	//        }
	//
	//        public List<Comparable> getPredictions(){
	//                return predictions;
	//        }
	//
}
