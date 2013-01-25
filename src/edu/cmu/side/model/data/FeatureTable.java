package edu.cmu.side.model.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.cmu.side.model.FreqMap;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.FeatureHit;

/**
 * A many-directional mapping of Features, FeatureHits and indexes into the DocumentList.
 *
 */
public class FeatureTable implements Serializable
{	
	private static final long serialVersionUID = 1048801132974685418L;
	private DocumentList documents;

	private Map<Feature, Collection<FeatureHit>> hitsPerFeature;
	private List<Collection<FeatureHit>> hitsPerDocument;
	private Feature.Type type = null;
	private Integer threshold = 5;
	private String name = "no name set";

	/**
	 * Uses a sort of shoddy and roundabout catch-exception way of figuring out if the data type is nominal or numeric.
	 * @return
	 */
	public Feature.Type getClassValueType(){
		return documents.getValueType(documents.getCurrentAnnotation());
	}


	private FeatureTable(){
		this.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>(100000); //Rough guess at capacity requirement.
		this.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
	}

	public FeatureTable(DocumentList sdl, Collection<FeatureHit> hits, int thresh){
		this();
		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(100000);
		this.threshold = thresh;
		this.documents = sdl;
		for(int i = 0; i < sdl.getSize(); i++){
			hitsPerDocument.add(new TreeSet<FeatureHit>());
		}
		for(FeatureHit hit : hits){
			Feature f = hit.getFeature();
			if(!localFeatures.containsKey(f)){
				localFeatures.put(f, new TreeSet<Integer>());
			}
			localFeatures.get(f).add(hit.getDocumentIndex());
		}
		for(FeatureHit hit : hits){
			if(localFeatures.get(hit.getFeature()).size() >= threshold){
				hitsPerDocument.get(hit.getDocumentIndex()).add(hit);
				if(!hitsPerFeature.containsKey(hit.getFeature())){
					hitsPerFeature.put(hit.getFeature(), new TreeSet<FeatureHit>());
				}
				hitsPerFeature.get(hit.getFeature()).add(hit);
			}
		}
	}
	

	public void setName(String n){
		name = n;
	}

	public void setDocumentList(DocumentList sdl){
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

	public DocumentList getDocumentList(){
		return documents;
	}
	
	public String getDomainName(int indx){
		return documents.getInstanceDomain(indx);
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
		return new TreeSet<Feature>(hitsPerFeature.keySet());
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
	        	ft.hitsPerDocument.get(fh.getDocumentIndex()).add(fh);
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
	
	public int numInstances(){
		return documents.getSize();
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
	
	public Double getNumericConvertedClassValue(int i, String target){
		if (getClassValueType() == Feature.Type.NUMERIC){
			return Double.parseDouble(documents.getAnnotationArray().get(i));			
		}else if (getClassValueType() == Feature.Type.BOOLEAN){
			return (documents.getAnnotationArray().get(i).equals(Boolean.TRUE.toString()))?1.0:0.0;
		}else {
			return (documents.getAnnotationArray().get(i).equals(target))?1.0:0.0;
		}
	}
	
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
}