package edu.cmu.side.simple.feature;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


import java.util.*;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.Evaluation;

import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.feature.Feature.Type;
import edu.cmu.side.simple.newui.features.FeaturePluginPanel;

/**
 * A many-directional mapping of Features, FeatureHits and indexes into the DocumentList.
 *
 */
public class FeatureTable implements Serializable
{	
	private static final long serialVersionUID = 1048801132974685418L;
	private SimpleDocumentList documents;

	private Map<Feature, Collection<FeatureHit>> hitsPerFeature;
	private List<Collection<FeatureHit>> hitsPerDocument;
	private Feature.Type type = null;
	private Integer threshold = 5;
	private String name = "no name set";
	public String getDescriptionString(){
		return hitsPerFeature.keySet().size() + " Features";	
	}


	/**
	 * Uses a sort of shoddy and roundabout catch-exception way of figuring out if the data type is nominal or numeric.
	 * @return
	 */
	public Feature.Type getClassValueType(){
		if(type == null){
			for(String s : documents.getLabelArray()){
				try{
					Double num = Double.parseDouble(s);
				}catch(Exception e){
					type = Feature.Type.NOMINAL;
					return type;
				}
			}
			type = Feature.Type.NUMERIC;
		}
		return type;
	}


	private FeatureTable(){
		this.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>(100000); //Rough guess at capacity requirement.
		this.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
	}

	public FeatureTable(SimpleDocumentList sdl, Collection<FeatureHit> hits, int thresh){
		this();
		double timeA = System.currentTimeMillis();
		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(100000);
		this.threshold = thresh;
		this.documents = sdl;
		for(int i = 0; i < sdl.getSize(); i++){
			hitsPerDocument.add(new TreeSet<FeatureHit>());
		}
		double timeB = System.currentTimeMillis();

		for(FeatureHit hit : hits){
			Feature f = hit.getFeature();
			if(!localFeatures.containsKey(f)){
				localFeatures.put(f, new TreeSet<Integer>());
			}
			localFeatures.get(f).add(hit.getDocumentIndex());
		}
		double timeC = System.currentTimeMillis();
		for(FeatureHit hit : hits){
			if(localFeatures.get(hit.getFeature()).size() >= threshold){
				hitsPerDocument.get(hit.getDocumentIndex()).add(hit);
				if(!hitsPerFeature.containsKey(hit.getFeature())){
					hitsPerFeature.put(hit.getFeature(), new TreeSet<FeatureHit>());
				}
				hitsPerFeature.get(hit.getFeature()).add(hit);
			}
		}
		double timeD = System.currentTimeMillis();

		System.out.println((timeB-timeA) + ", " + (timeC-timeB) + ", " + (timeD-timeC) + " ms FT81*********************************");
		System.out.println("FT82 finished table with " + hitsPerDocument.size() + " documents " + hitsPerFeature.keySet().size() + " features");
	}

	public void setName(String n){
		name = n;
	}

	public void setDocumentList(SimpleDocumentList sdl){
		documents = sdl;
	}

	public void setThreshold(int n){
		threshold = n;
	}
	
	public int getThreshold(){
		return threshold;
	}
	public String getName(){
		return name;
	}

	public SimpleDocumentList getDocumentList(){
		return documents;
	}
	
	public String getDomainName(int indx){
		return documents.getInstanceDomain(indx);
	}

	public int getThreshold(){
		return threshold;
	}
	
	/**
	 * @return the set of features extracted from the documents.
	 */
	public Set<Feature> getFeatureSet() {
		return hitsPerFeature.keySet();
	}
	/**
	 * @return the set of features extracted from the documents.
	 */
	public Collection<Feature> getSortedFeatures() {        
		return new TreeSet(hitsPerFeature.keySet());
	}

	public Collection<FeatureHit> getHitsForFeature(Feature feature) {
		return hitsPerFeature.get(feature);
	}

	public Collection<FeatureHit> getHitsForDocument(int index) {
		return hitsPerDocument.get(index);
	}
	

	/**
	 * When creating feature hits, they're done on a per-feature basis. This fills the data structure
     * that maps those hits per document instead.
    */
	private void fillHitsPerDocument(FeatureTable ft) {
		ft.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
	    for(int i = 0; i < hitsPerDocument.size(); i++)
	    	ft.hitsPerDocument.add(new ArrayList<FeatureHit>());
	    
	    for(Feature f : hitsPerFeature.keySet()){
	    	ft.hitsPerFeature.put(f, hitsPerFeature.get(f));
	        for(FeatureHit fh : ft.hitsPerFeature.get(f))
	        	ft.hitsPerDocument.get(fh.documentIndex).add(fh);
	    }
	}
	
	public FeatureTable clone(){
		FeatureTable ft = new FeatureTable();
	    ft.setName(getName()+" (clone)");
	    ft.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>(30000); //Rough guess at capacity requirement.
	    ft.threshold = threshold;
	    fillHitsPerDocument(ft);
	    return ft;
	}
	
	public int getInstanceNumber(){
		return hitsPerDocument.size();
	}
	
	
    public void deleteFeatureSet(Set<Feature> f){
    	for(int i = 0; i < hitsPerDocument.size(); i++){
    		Collection<FeatureHit> tmphits = new ArrayList<FeatureHit>();
	        for(FeatureHit hit : hitsPerDocument.get(i))
	        	if(!f.contains(hit.getFeature())) tmphits.add(hit);
	        hitsPerDocument.set(i, tmphits);
	     }
	     for (Feature fe : f)
	    	 hitsPerFeature.remove(fe);
    }
	
	
	//	    	                
	//	    	                for(int i = 0; i < Length; i++){
	//	    	                        hitsPerDocument.add(new TreeSet<FeatureHit>());
	//	    	                }
	//	    	                for(FeatureHit hit : hits){
	//	    	                        if(!hitsPerFeature.containsKey(hit.getFeature())){
	//	    	                                hitsPerFeature.put(hit.getFeature(), new TreeSet<FeatureHit>());
	//	    	                                activatedFeatures.put(hit.getFeature(), Boolean.TRUE);
	//	    	                        }
	//	    	                        hitsPerFeature.get(hit.getFeature()).add(hit);
	//	    	                        hitsPerDocument.get(hit.getDocumentIndex()).add(hit);
	//	    	                }
	//	    	        }

	//        public final int NUM_BASELINE_EVALUATIONS = 7;
	//        private String[] constantEvaluations = {"predictor of","kappa","precision","recall","f-score","accuracy","hits"};
	//        /** These show up as columns in the FeatureTablePanel */
	//        private Map<String, Map<Feature, Comparable>> evaluations;
	//
	//        private String tableName = "default";
	//        private Map<Feature, Boolean> activatedFeatures;
	//        /** Stores the type of the class value */
	//        
	//        private String[] labels;
	//        private String[] possibleLabels;
	//        
	//        /** These variables are for weka. Filled when needed only. Stored 
	//         * in the feature table so that it's cleaner to populate. */
	//        private FastVector fastVector = null;
	//        private Instances instances = null;
	//        private Map<Feature, Integer> attributeMap = new HashMap<Feature, Integer>();
	//
	//        
	//        
	//        public void serialize(File f){
	//                try{
	//                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));                               
	//                        writeSerializedTable(out);
	//                        out.close();                    
	//                }catch(Exception e){
	//                        e.printStackTrace();
	//                }
	//        }
	//
	//        public void writeSerializedTable(ObjectOutputStream out) throws Exception{
	//                out.writeObject(hitsPerFeature);
	//                out.writeObject(labels);
	//                out.writeObject(activatedFeatures);
	//                out.writeObject(tableName);
	//                out.writeObject(type);
	//                out.writeObject((Integer)threshold);
	//        }
	//
	//        public FeatureTable(ObjectInputStream in){
	//                try{
	//                        hitsPerFeature = (Map<Feature, Collection<FeatureHit>>)in.readObject();
	//                        labels = (String[])in.readObject();
	//                        activatedFeatures = (Map<Feature, Boolean>)in.readObject();
	//                        tableName = (String)in.readObject();
	//                        type = (Feature.Type)in.readObject();
	//                        threshold = (Integer)in.readObject();
	//                        fillHitsPerDocument(this);
	//                        this.evaluations = new TreeMap<String, Map<Feature, Comparable>>();
	//                        this.possibleLabels = null;
	//                        defaultEvaluation();
	//                }catch(Exception e){
	//                        e.printStackTrace();
	//                }
	//        }
	//

	//        
	//        public FeatureTable(Collection<FeatureHit> hits, String[] l, int thres, int doclen){
	//                init(hits, l, thres, doclen);
	//        }
	//        
	//        public FeatureTable(Collection<FeatureHit> hits, String[] l, int doclen){
	//                init(hits, l, 0, doclen);
	//        }
	//        
	//        private FeatureTable(){}
	//        
	//        /**
	//         * Functionality for the "Freeze" button in the GUI. Removes deactivated features.
	//         */
	//        public FeatureTable subsetClone(){
	//                FeatureTable ft = new FeatureTable();
	//                ft.setTableName(getTableName()+" (subset)");
	//                ft.evaluations = new TreeMap<String, Map<Feature, Comparable>>();
	//                for(String eval : evaluations.keySet()){
	//                        ft.evaluations.put(eval, evaluations.get(eval));
	//                }
	//                ft.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>(30000); //Rough guess at capacity requirement.
	//                ft.activatedFeatures = new HashMap<Feature, Boolean>();
	//                ft.threshold = threshold;
	//                fillHitsPerDocument(ft);
	//                return ft;
	//        }
	//        

	//        
	//        /**
	//         * Called by external classes (notably the FeaturePluginPanel and FeatureLabPanel) to edit 
	//         * an existing feature table by adding new features. Generally followed by calling activationsChanged()
	//         * on the FeatureTablePanel in the GUI.
	//         */
	//        public void addAllHits(Collection<FeatureHit> hits){
	//                Map<Feature, Collection<FeatureHit>> localMap = new HashMap<Feature, Collection<FeatureHit>>(100000);
	//                for(FeatureHit fh : hits){
	//                        if(!localMap.containsKey(fh.getFeature())){
	//                                localMap.put(fh.getFeature(), new ArrayList<FeatureHit>());
	//                        }
	//                        localMap.get(fh.getFeature()).add(fh);
	//                }
	//                for(Feature f : localMap.keySet()){
	//                        if(localMap.get(f).size() >= threshold){
	//                                for(FeatureHit fh : localMap.get(f)){
	//                                        if(!hitsPerDocument.get(fh.getDocumentIndex()).contains(fh)){
	//                                                hitsPerDocument.get(fh.getDocumentIndex()).add(fh);                                             
	//                                        }
	//                                        if(!hitsPerFeature.containsKey(fh.getFeature())){
	//                                                hitsPerFeature.put(fh.getFeature(), new HashSet<FeatureHit>());
	//                                                activatedFeatures.put(fh.getFeature(), true);
	//                                        }
	//                                        if(!hitsPerFeature.get(fh.getFeature()).contains(fh)){
	//                                                hitsPerFeature.get(fh.getFeature()).add(fh);                    
	//                                        }
	//                                }
	//                        }
	//                }
	//                fastVector = null;
	//                instances = null;
	//                defaultEvaluation();
	//        }
	//
	//        
	//        
	//
	//        /**
	//         * Generates subsets of data from this feature table, used for cross validation. Makes a shallow copy of features
	//         * from the overall Instances object.
	//         * 
	//         * @param foldMap Set of documents to use in this subset.
	//         * @param fold Number of the fold to use for CV-by-fold radio button.
	//         * @param train Whether this is the training or test set.
	//         * @return
	//         */
	//        public Instances getInstances(Map<Integer, Integer> foldMap, int fold, boolean train){
	//                if(instances == null) getInstances();
	//                
	//                Instances format = new Instances(getTableName(), fastVector, 0);
	//                for(int i = 0; i < instances.numInstances(); i++){
	//                        if((train && foldMap.get(i) != fold) || (!train && foldMap.get(i) == fold)){
	//                                format.add((Instance)instances.instance(i).copy());
	//                        }
	//                        format.setClass(format.attribute("CLASS"));
	//                }
	//                return format;
	//        }
	//
	//        /**
	//         * Since we're doing cross-validation in a more intelligent way than SIDE originally did it (taking every nth instance
	//         * for n folds, instead of taking the first 100/n% of the data for each fold), we need to keep a map of which keys in the 
	//         * subset from getInstances() correspond to which instances in the whole data set. In this case, keys are the subset's 
	//         * document index and values are the original document index.
	//         */
	//        public static Map<Integer, Integer> foldIndexToIndex(Map<Integer, Integer> foldMap, int fold, int l){
	//                Map<Integer, Integer> foldIndexToIndex = new TreeMap<Integer, Integer>();
	//                int index = 0;
	//                for(int i = 0; i < l; i++)
	//                        if(foldMap.get(i)==fold)
	//                                foldIndexToIndex.put(index++, i);
	//                return foldIndexToIndex;
	//        }
	//        
	//        public static boolean[] getMask(int l){
	//                boolean[] ans = new boolean[l];
	//                for(int i = 0; i < l; i++) ans[i]=true; 
	//                return ans;
	//        }
	//        
	//        public static boolean[] getMask(Map<Integer, Integer> foldMap, int fold){
	//                boolean[] ans = new boolean[foldMap.size()];
	//                for(int i = 0; i < foldMap.size(); i++)
	//                        if(foldMap.get(i) != fold) ans[i]=true; else ans[i]=false;
	//                return ans;
	//        }
	//        /**
	//         * run the extractors on the documents and populate the feature hit tables.
	//         */
	//        public void correval(){
	//                Map<Feature, Comparable> corr = new HashMap<Feature, Comparable>();
	//                Map<Feature, Comparable> predictorOf = new HashMap<Feature, Comparable>();
	//                
	//                Instances alldata = getInstances();
	//                FastVector nowattrs = new FastVector();
	//                nowattrs.addElement(new Attribute("f"));
	//                nowattrs.addElement(new Attribute("c"));
	//                
	//                for (Feature f: hitsPerFeature.keySet()){
	//                        int i=getFastVectorIndex(f);
	//                        Instances data = new Instances("getcorr", nowattrs, 0);
	//                        double[] values = new double[2];
	//                        for (int j=0; j<alldata.numInstances(); j++){
	//                                values[0] = alldata.instance(j).value(i);
	//                                values[1] = alldata.instance(j).classValue();
	//                                data.add(new SparseInstance(1,values));
	//                        }
	//                        data.setClassIndex(1);
	//                        try{
	//                                LinearRegression LR = new LinearRegression();
	//                                Evaluation eval = new Evaluation(data);
	//                                LR.buildClassifier(data);
	//                                eval.evaluateModel(LR, data);
	//                                double nowcorr = eval.correlationCoefficient();
	//                                predictorOf.put(f, nowcorr>0?"POS":"NEG");
	//                                corr.put(f, nowcorr);
	//                        } catch (Exception x){
	//                                System.out.println(x);
	//                        }
	//                }
	//                addEvaluation("sign", predictorOf);
	//                addEvaluation("correlation", corr);
	//        }
	//
	//        /**
	//         * Evaluates feature table for precision, recall, f-score, and kappa at creation time.
	//         */
	//        public void defaultEvaluation(){
	//                        addEvaluation("predictor of", bestMap);
	//                        addEvaluation("kappa", kappaMap);
	//                        addEvaluation("precision", precisionMap);
	//                        addEvaluation("recall", recallMap);
	//                        addEvaluation("f-score", fScoreMap);
	//                        addEvaluation("accuracy", accuracyMap);
	//                        addEvaluation("hits", hitsMap);
	//                        if(possiblelabels != null){
	//                                String[] hitLabels = new String[possiblelabels.length];
	//                                for(int i = 0; i < hitLabels.length; i++){
	//                                        hitLabels[i] = "hits_" + possiblelabels[i];
	//                                        if(!hitsByLabelMap.containsKey(possiblelabels[i])){
	//                                                hitsByLabelMap.put(possiblelabels[i], new HashMap<Feature, Comparable>());
	//                                        }
	//                                        addEvaluation(hitLabels[i], hitsByLabelMap.get(possiblelabels[i]));                                             
	//                                }
	//                                if(constantEvaluations.length==7){
	//                                        String[] newConstants = new String[constantEvaluations.length+hitLabels.length];
	//                                        System.arraycopy(constantEvaluations, 0, newConstants, 0, constantEvaluations.length);
	//                                        System.arraycopy(hitLabels, 0, newConstants, constantEvaluations.length, hitLabels.length);
	//                                        constantEvaluations = newConstants;                                     
	//                                }
	//                        }
	//                }
	//                double time2 = System.currentTimeMillis();
	//        }
	//

	//        
	//        public void addEvaluation(String evaluationName, Map<Feature, Comparable> eval){
	//                if (eval.keySet().size()==0) return;
	//                if (evaluations.containsKey(evaluationName)){
	//                        for(Feature f : eval.keySet()){
	//                                if(!evaluations.get(evaluationName).containsKey(f)){
	//                                        evaluations.get(evaluationName).put(f, eval.get(f));
	//                                }
	//                        }
	//                }else{
	//                        evaluations.put(evaluationName, eval);                  
	//                }
	//        }
	//        
	//        public String[] getConstantEvaluations(){
	////              return evaluations.keySet().toArray(new String[0]);
	////              return constantEvaluations;
	//                List<String> mergedEval = new ArrayList<String>();
	//                for (int i=0; i<constantEvaluations.length; i++)
	//                        if (evaluations.containsKey(constantEvaluations[i]))
	//                                mergedEval.add(constantEvaluations[i]);
	///*              for (String evalkey : evaluations.keySet())
	//                        if (!mergedEval.contains(evalkey)) mergedEval.add(evalkey);
	//*/                      
	//                return mergedEval.toArray(new String[0]);       
	//                //return getClassValueType().equals(Feature.Type.NUMERIC)?new String[]{/*"sign","correlation"*/}:constantEvaluations;
	//        }
	//
	//        public Map<String, Map<Feature, Comparable>> getEvaluations(){
	//                return evaluations;
	//        }
	//        
	//        public String toString(){
	//                return getTableName();
	//        }
	
	//
	//        public Integer getThreshold(){
	//                return threshold;
	//        }
	//        
	//        }
	//
	//
	//        /**
	//         * Removes a feature and all of its hits from a feature table.
	//         * @param f
	//         */
	//        public void deleteFeature(Feature f){
	//                for(int i = 0; i < getInstanceSize(); i++){
	//                        FeatureHit[] docHits = hitsPerDocument.get(i).toArray(new FeatureHit[0]);
	//                        for(FeatureHit hit : docHits){
	//                                if(hit.getFeature().equals(f)){
	//                                        hitsPerDocument.get(i).remove(hit);
	//                                }
	//                        }
	//                }
	//                hitsPerFeature.remove(f);
	//                activatedFeatures.remove(f);
	//        }
	//
	//        public void deleteFeatureSet(Set<Feature> f){
	//                for(int i = 0; i < getInstanceSize(); i++){
	//                        Collection<FeatureHit> tmphits = new ArrayList<FeatureHit>();
	//                        for(FeatureHit hit : hitsPerDocument.get(i))
	//                                if(!f.contains(hit.getFeature()))
	//                                        tmphits.add(hit);
	//                        hitsPerDocument.set(i, tmphits);
	//                }
	//                for (Feature fe : f){
	//                        hitsPerFeature.remove(fe);
	//                        activatedFeatures.remove(fe);
	//                }
	//        }
	//        
	//        
	//
	//        public void addEmptyFeature(Feature f){
	//                hitsPerFeature.put(f, new HashSet<FeatureHit>());
	//        }
	//        
	//        
	//        /**
	//         * Used for unannotated data when predicting new labels.
	//         */
	//        public void setPossibleLabels(String[] ls){
	//                possibleLabels = ls;
	//        }
	//        public void setClassValueType(Feature.Type type){
	//                this.type = type;
	//        }
	//        
	//        /**
	//         * Given two feature tables, alter the feature space of the second table to match the feature
	//         * space in the first table. Returns that second table post-alteration.
	//         */
	//        public static FeatureTable reconcileFeatures(FeatureTable oldFeatureTable, FeatureTable newFeatureTable){
	//                Set<Feature> oldTableFeatures = oldFeatureTable.getFeatureSet();
	//                Set<Feature> newTableFeatures = newFeatureTable.getFeatureSet();
	//
	//                double time1 = System.currentTimeMillis();
	//                Set<Feature> remove = new HashSet<Feature>();
	//                for(Feature f: newTableFeatures) {
	//                        boolean found = oldTableFeatures.contains(f);
	//                        if(!found) remove.add(f);
	//                }
	//                        
	//                double time1a = System.currentTimeMillis();
	//                newFeatureTable.deleteFeatureSet(remove);
	//
	//                double time2 = System.currentTimeMillis();
	//                oldTableFeatures = oldFeatureTable.getFeatureSet();
	//                newTableFeatures = newFeatureTable.getFeatureSet();
	//
	//                for(Feature f : oldTableFeatures) {
	//                        boolean found = newTableFeatures.contains(f);
	//                        if(!found) newFeatureTable.addEmptyFeature(f);
	//                }
	//                double time3 = System.currentTimeMillis();
	//                System.out.println("Reconcile: Find remove: " + (time1a-time1) + ",Remove: " + (time2-time1a) + ",Add new: " + (time3-time2));
	//                
	//
	//                //Set nominal features with same nominal values;
	//                Feature[] oldf = oldFeatureTable.getSortedFeatures().toArray(new Feature[0]);
	//                Feature[] newf = newFeatureTable.getSortedFeatures().toArray(new Feature[0]);
	//                for (int i=0; i<oldf.length; i++)
	//                        newf[i] = Feature.reconcile(oldf[i], newf[i]);
	//                
	//                //Set class value to the same
	//                if (newFeatureTable.getClassValueType() == null){     
	//                        //if it is in prediction panel and we do not have label infomation, we just set as training data
	//                        newFeatureTable.setClassValueType(oldFeatureTable.getClassValueType());
	//                        newFeatureTable.setPossibleLabels(oldFeatureTable.getPossibleLabels());
	//                } else {
	//                        //if it is in model evaluation part, we have to merge the nominal classes
	//                        if (!newFeatureTable.getClassValueType().equals(oldFeatureTable.getClassValueType()))
	//                                throw new IllegalStateException("Two feature table has different class type");
	//                        if (newFeatureTable.getClassValueType() == Feature.Type.NOMINAL){
	//                                Set<String> mergeclass = new HashSet<String>();
	//                                for (String label: oldFeatureTable.getPossibleLabels())
	//                                        mergeclass.add(label);
	//                                int sizen = mergeclass.size();
	//                                for (String label: newFeatureTable.getPossibleLabels())
	//                                        if (!mergeclass.contains(label)) sizen++;
	//                                
	//                                String[] newpossible = new String[sizen];
	//                                int i=0;
	//                                for (i=0; i<oldFeatureTable.getPossibleLabels().length; i++)
	//                                        newpossible[i] = oldFeatureTable.getPossibleLabels()[i];
	//                                for (String label: newFeatureTable.getPossibleLabels())
	//                                        if (!mergeclass.contains(label))
	//                                                newpossible[i++] = label;
	//                                mergeclass.clear();
	//                                newFeatureTable.setPossibleLabels(newpossible);
	//                        }
	//                }       
	//                return newFeatureTable;
	//        }
}