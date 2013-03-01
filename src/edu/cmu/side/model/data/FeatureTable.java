package edu.cmu.side.model.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.model.feature.RegroupFeatureHit;

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
	
	private Map<String, double[]> numericConvertedClassValues = new HashMap<String, double[]>();
	private List<String> nominalConvertedClassValues = new ArrayList<String>();
	
	//For numeric class values to convert to quintiles
	ArrayList<Double> numericBreakpoints = new ArrayList<Double>();
	
	private Feature.Type type = null;
	private Integer threshold = 5;
	private String annotation;
	private String name = "no name set";

	/**
	 * Uses a sort of shoddy and roundabout catch-exception way of figuring out if the data type is nominal or numeric.
	 * @return
	 */
	public Feature.Type getClassValueType()
	{
		if(type == null)
			type = documents.getValueType(annotation);
		return type;
	}

	public String getAnnotation()
	{
		if(annotation == null)
			annotation = documents.getCurrentAnnotation();
		return annotation;
	}

	private FeatureTable(){
		this.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>(100000); //Rough guess at capacity requirement.
		this.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
	}

	public FeatureTable(DocumentList sdl, Collection<FeatureHit> hits, int thresh, String annotation, Feature.Type type)
	{
		this();
		this.annotation = annotation;
		this.type = type;
		
		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(100000);
		this.threshold = thresh;
		this.documents = sdl;
		System.out.println(sdl.getCurrentAnnotation() + " annotation FT63");
		
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
	
	public FeatureTable(DocumentList sdl, Collection<FeatureHit> hits, int thresh){
		this();
		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(100000);
		this.threshold = thresh;
		this.documents = sdl;
		annotation = sdl.getCurrentAnnotation();
		generateConvertedClassValues();
		
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
	
	
	public FeatureTable cloneTrainingFold(Map<Integer, Integer> foldMap, int fold, boolean train){
		List<Integer> indices = getFoldIndices(foldMap, fold, train);
		List<String> newFilenames = new ArrayList<String>();
		Map<String, List<String>> newText = new HashMap<String, List<String>>();
		Map<String, List<String>> newAnnots = new HashMap<String, List<String>>();
		DocumentList origDocs = getDocumentList();
		for(String lab : origDocs.allAnnotations().keySet()){
			newAnnots.put(lab, new ArrayList<String>());
			for(int index : indices){
				newAnnots.get(lab).add(origDocs.getAnnotationArray(lab).get(index));
			}
		}
		for(String lab : origDocs.getCoveredTextList().keySet()){
			newText.put(lab, new ArrayList<String>());
			for(int index : indices){
				newText.get(lab).add(origDocs.getCoveredTextList().get(lab).get(index));
			}
		}
		Map<Integer, Integer> newInstanceMap = new HashMap<Integer, Integer>();
		int i = 0;
		for(int index : indices){
			newInstanceMap.put(index, i++);
			newFilenames.add(origDocs.getFilename(index));
		}
		DocumentList docs = new DocumentList(newFilenames, newText, newAnnots, origDocs.getCurrentAnnotation());
		docs.setClassValueType(origDocs.type); //TODO: figure out why the second cloneTraining on the final pass of wrapperthingy is empty/numeric, and why it wasn't before
		Collection<FeatureHit> newHits = new HashSet<FeatureHit>();
		for(int index : indices){
			for(FeatureHit hit : getHitsForDocument(index)){
				if(hit instanceof RegroupFeatureHit){
					newHits.add(new RegroupFeatureHit(hit, newInstanceMap, ((RegroupFeatureHit)hit).getOriginalIndex()));
				}else{
					newHits.add(new FeatureHit(hit.getFeature(), hit.getValue(), newInstanceMap.get(index)));					
				}
			}
		}
		return new FeatureTable(docs, newHits, 1);
	}
	
	public void generateConvertedClassValues(){
		numericConvertedClassValues.clear();
		nominalConvertedClassValues.clear();
		DocumentList localDocuments = getDocumentList();
		switch(getClassValueType()){
		case NOMINAL:
		case BOOLEAN:
			for(String s : localDocuments.getLabelArray()){
				double[] convertedClassValues = new double[localDocuments.getSize()];
				for(int i = 0; i < localDocuments.getSize(); i++){
					convertedClassValues[i] = getNumericConvertedClassValue(i, s);
				}
				numericConvertedClassValues.put(s, convertedClassValues);
			}
			nominalConvertedClassValues = localDocuments.getAnnotationArray();
			break;
		case NUMERIC:
			String target = "numeric";
			double[] convertedClassValues = new double[localDocuments.getSize()];
			ArrayList<Double> toSort = new ArrayList<Double>();
			for(int i = 0; i < localDocuments.getSize(); i++){
				convertedClassValues[i] = getNumericConvertedClassValue(i, target);
				toSort.add(convertedClassValues[i]);
			}
			numericConvertedClassValues.put(target, convertedClassValues);

			ArrayList<Double> values = new ArrayList<Double>();
			for(int i = 0; i < convertedClassValues.length; i++){
				values.add(convertedClassValues[i]);
			}
			Collections.sort(values);
			for(double i = 1; i <= 4; i++){
				numericBreakpoints.add(values.get(((Double)(values.size()*(i/5.0))).intValue()-1));
			}
			ArrayList<String> nominalConvert = new ArrayList<String>();
			for(int i = 0; i < convertedClassValues.length; i++){
				Double actDbl = convertedClassValues[i];
				int Qact = -1; 
				int j = 0;
				while(j < 4 && actDbl > numericBreakpoints.get(j)) j++;
				Qact = j;
				nominalConvert.add("Q"+(Qact+1));
			}
			nominalConvertedClassValues = nominalConvert;
			break;
		}
	}
	
	public ArrayList<Double> getNumericBreakpoints(){
		return numericBreakpoints;
	}
	
	public double[] getNumericClassValues(String target){
		double[] out = null;
		switch(getClassValueType()){
		case NOMINAL:
		case BOOLEAN:
			out = numericConvertedClassValues.get(target);
			break;
		case NUMERIC:
			out = numericConvertedClassValues.get("numeric");
			break;
		}
		return out;	
	}
	
	public List<String> getNominalClassValues(){
		return nominalConvertedClassValues;
	}

	public int getSize(){
		return documents.getSize();
	}
	
	public void setName(String n){
		name = n;
	}

	public void setDocumentList(DocumentList sdl){
		documents = sdl;
		type = null;
		generateConvertedClassValues();
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

	public DocumentList getDocumentList()
	{
		if(documents != null && documents.allAnnotations.keySet().contains(annotation))
			documents.setCurrentAnnotation(annotation);
		return documents;
	}
	
	// Does not ensure that the class value has been updated - 
	// should only be used inside a loop and only when, as a precondition, 
	// getDocumentList() has been called at least once.
	public DocumentList getDocumentListQuickly(){
		return documents;
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
	    	Collection<FeatureHit> hitsPerF = hitsPerFeature.get(f);
			ft.hitsPerFeature.put(f, new TreeSet<FeatureHit>(hitsPerF));
	        for(FeatureHit fh : hitsPerF)
	        	ft.hitsPerDocument.get(fh.getDocumentIndex()).add(fh);
	    }
	}
	
	public FeatureTable clone()
	{
		FeatureTable ft = new FeatureTable();
	    ft.setName(getName()+" (clone)");

	    ft.documents = documents;
		ft.type = type;
		ft.threshold = threshold;
		ft.annotation = annotation;
	    
	    ft.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>(30000); //Rough guess at capacity requirement.
	    ft.threshold = threshold;
	    fillHitsPerDocument(ft);
	    
	    
	    return ft;
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
	
	public List<Integer> getFoldIndices(Map<Integer, Integer> foldMap, int fold, boolean train){
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for(int i = 0; i < getSize(); i++){
			if((train && foldMap.get(i) != fold) || (!train && foldMap.get(i) == fold)){
				indices.add(i);
			}
		}
		return indices;
	}
	
	public String[] getNominalLabelArray(){
		return getDocumentList().getLabelArray();
	}
	
	public String[] getLabelArray(){
		String[] result = null;
		switch(getClassValueType()){
		case NOMINAL:
		case BOOLEAN:
		case STRING:
			result = getNominalLabelArray();
			break;
		case NUMERIC:
			result = new String[]{"numeric"};
			break;
		}
		return result;
	}

	public void setHits(Collection<FeatureHit> hits)
	{
		//documents = sdl;
		//		annotation = sdl.getCurrentAnnotation();
		//		generateConvertedClassValues();
		

		this.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>(2000); //Rough guess at capacity requirement.
		this.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
		
		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(2000);
		
		hitsPerDocument.clear();
		hitsPerFeature.clear();
		
		for(int i = 0; i < documents.getSize(); i++){
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
	
	public void setHitsIgnoreThreshold(Collection<FeatureHit> hits)
	{
		this.hitsPerFeature.clear();
		this.hitsPerDocument.clear();

		for (int i = 0; i < documents.getSize(); i++)
		{
			hitsPerDocument.add(new TreeSet<FeatureHit>());
		}

		for (FeatureHit hit : hits)
		{
			hitsPerDocument.get(hit.getDocumentIndex()).add(hit);
			if (!hitsPerFeature.containsKey(hit.getFeature()))
			{
				hitsPerFeature.put(hit.getFeature(), new TreeSet<FeatureHit>());
			}
			hitsPerFeature.get(hit.getFeature()).add(hit);

		}
	}

	public void addFeatureHits(Collection<FeatureHit> hits)
	{
		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(10000);
		
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

	public void reconcile(FeatureTable train)
	{
		//TODO: decide if this removal step is neccessary.
//		for(Feature f : this.hitsPerFeature.keySet())
//		{
//			if(!train.hitsPerFeature.containsKey(f))
//			{
//				Collection<FeatureHit> hits = this.hitsPerFeature.remove(f);
//				for(FeatureHit h : hits)
//				{
//					hitsPerDocument.get(h.getDocumentIndex()).remove(h);
//				}
//			}
//		}
		
		for(Feature f : train.hitsPerFeature.keySet())
		{
			if(!this.hitsPerFeature.containsKey(f))
			{
				this.hitsPerFeature.put(f, new ArrayList<FeatureHit>());
			}
		}
	}
}