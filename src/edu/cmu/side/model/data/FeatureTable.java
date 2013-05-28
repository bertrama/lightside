package edu.cmu.side.model.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.Feature.Type;
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
	
	private String[] labelArray;

	private FeatureTable(){
		this.hitsPerFeature = new TreeMap<Feature, Collection<FeatureHit>>(); //Rough guess at capacity requirement.
		this.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
	}

	public FeatureTable(DocumentList sdl, Collection<FeatureHit> hits, int thresh, String annotation, Feature.Type type)
	{
		this();
		setAnnotation(annotation);
		this.type = type;
		
		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(100000);
		this.threshold = thresh;
		this.documents = sdl;

		generateConvertedClassValues();

//		System.out.println("FT 74: " + hits.size() + "total incoming hits");

		for (int i = 0; i < sdl.getSize(); i++)
		{
			hitsPerDocument.add(new TreeSet<FeatureHit>());
		}
		for (FeatureHit hit : hits)
		{
			Feature f = hit.getFeature();
			if (!localFeatures.containsKey(f))
			{
				localFeatures.put(f, new TreeSet<Integer>());
			}
			localFeatures.get(f).add(hit.getDocumentIndex());
		}

		for (FeatureHit hit : hits)
		{
			if (localFeatures.get(hit.getFeature()).size() >= threshold)
			{
				hitsPerDocument.get(hit.getDocumentIndex()).add(hit);
				if (!hitsPerFeature.containsKey(hit.getFeature()))
				{
					hitsPerFeature.put(hit.getFeature(), new TreeSet<FeatureHit>());
				}
				hitsPerFeature.get(hit.getFeature()).add(hit);
			}
		}

//		System.out.println("FT 74: "+hitsPerDocument.get(0).size()+" thresholded hits for doc 0");
	}

//	@Deprecated
//	public FeatureTable(DocumentList sdl, Collection<FeatureHit> hits, int thresh){
//		this();
//		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(100000);
//		this.threshold = thresh;
//		this.documents = sdl;
//		this.annotation = sdl.getCurrentAnnotation();
//		this.type = sdl.getValueType(annotation);
//		generateConvertedClassValues();
//		
//		for(int i = 0; i < sdl.getSize(); i++){
//			hitsPerDocument.add(new TreeSet<FeatureHit>());
//		}
//		for(FeatureHit hit : hits){
//			Feature f = hit.getFeature();
//			if(!localFeatures.containsKey(f)){
//				localFeatures.put(f, new TreeSet<Integer>());
//			}
//			localFeatures.get(f).add(hit.getDocumentIndex());
//		}
//
//		for(FeatureHit hit : hits){
//			if(localFeatures.get(hit.getFeature()).size() >= threshold){
//				hitsPerDocument.get(hit.getDocumentIndex()).add(hit);
//				if(!hitsPerFeature.containsKey(hit.getFeature())){
//					hitsPerFeature.put(hit.getFeature(), new TreeSet<FeatureHit>());
//				}
//				hitsPerFeature.get(hit.getFeature()).add(hit);
//			}
//		}
//	}
	
	
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
		DocumentList docs = new DocumentList(newFilenames, newText, newAnnots, annotation);
//		docs.setClassValueType(origDocs.type);
		//TODO: figure out why the second cloneTraining on the final pass of wrapperthingy is empty/numeric, and why it wasn't before
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
		return new FeatureTable(docs, newHits, 1, this.getAnnotation(), this.getClassValueType());
	}
	
	public void generateConvertedClassValues()
	{
		if(annotation == null || type == null)
			return;
		
		numericConvertedClassValues.clear();
		nominalConvertedClassValues.clear();
		DocumentList localDocuments = getDocumentList();
		type = getClassValueType();
		switch(type){
		case NOMINAL:
		case BOOLEAN:
			for(String s : localDocuments.getLabelArray(annotation, type)){
				double[] convertedClassValues = new double[localDocuments.getSize()];
				for(int i = 0; i < localDocuments.getSize(); i++){
					convertedClassValues[i] = getNumericConvertedClassValue(i, s);
				}
				numericConvertedClassValues.put(s, convertedClassValues);
			}
			nominalConvertedClassValues = localDocuments.getAnnotationArray(annotation);
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

	public int getSize()
	{
		if(documents != null)
			return documents.getSize();
		else return hitsPerDocument.size();
	}
	
	public void setName(String n){
		name = n;
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
		if(documents != null && annotation != null && documents.allAnnotations.keySet().contains(annotation))
			documents.setCurrentAnnotation(annotation, type);
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
	    
	    ft.hitsPerFeature = new TreeMap<Feature, Collection<FeatureHit>>();
	    ft.threshold = threshold;
	    ft.numericConvertedClassValues = new HashMap<String, double[]>(numericConvertedClassValues);
	    ft.nominalConvertedClassValues = new ArrayList<String>(nominalConvertedClassValues);
	    
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
	
	public Double getNumericConvertedClassValue(int i, String target)
	{
		if (getClassValueType() == Feature.Type.NUMERIC)
		{
			return Double.parseDouble(documents.getAnnotationArray(annotation).get(i));
		}
		else if (getClassValueType() == Feature.Type.BOOLEAN)
		{
			return (documents.getAnnotationArray(annotation).get(i).equals(Boolean.TRUE.toString())) ? 1.0 : 0.0;
		}
		else
		{
			return (documents.getAnnotationArray(annotation).get(i).equals(target)) ? 1.0 : 0.0;
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
	
	public String[] getNominalLabelArray()
	{
		return getLabelArray();//documents.getLabelArray(annotation, type);
	}
	
	public String[] getLabelArray()
	{

		if (labelArray == null)
		{
			Set<String> labelSet = new TreeSet<String>();
			switch (type)
			{
				case NOMINAL:
				case BOOLEAN:
					List<String> labels = documents.getAnnotationArray(annotation);
					if (labels != null)
					{
						for (String s : labels)
						{
							labelSet.add(s);
						}
					}
					break;
				case NUMERIC:
					for (int i = 0; i < 5; i++)
					{
						labelSet.add("Q" + (i + 1));
					}
					break;
			}
			labelArray = labelSet.toArray(new String[0]);
		}
		return labelArray;
	}
	
	public String[] getFeatureTableLabelArray(){
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
		

		this.hitsPerFeature = new TreeMap<Feature, Collection<FeatureHit>>(); //Rough guess at capacity requirement.
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

	public void reconcileFeatures(FeatureTable train)
	{
		//TODO: decide if this removal step is neccessary - it may be un-needful, but could save space.
		Collection<Feature> toRemove = new ArrayList<Feature>();

//		System.out.println("FT 480: Unreconciled feature table has "+this.getFeatureSet().size() + " features, vs. "+train.getFeatureSet().size()+" in target");
		
		for(Feature f : this.hitsPerFeature.keySet())
		{
			if(!train.hitsPerFeature.containsKey(f))
			{
				Collection<FeatureHit> hits = this.hitsPerFeature.get(f);
				toRemove.add(f);
				
				for(FeatureHit h : hits)
				{
					hitsPerDocument.get(h.getDocumentIndex()).remove(h);
				}
			}
		}
		for(Feature f : toRemove)
		{
			this.hitsPerFeature.remove(f);
		}
//		System.out.println("FT 487: removed "+toRemove.size() + " features. "+this.getFeatureSet().size() + " features remain.");
		
		
		//add empty feature map entries so all training features are accounted for in this new feature table.
		for(Feature f : train.hitsPerFeature.keySet())
		{
			if(!this.hitsPerFeature.containsKey(f))
			{
				this.hitsPerFeature.put(f, new ArrayList<FeatureHit>());
			}
		}
		
//		System.out.println("FT 511: Reconciled table has "+this.getFeatureSet().size() + " features");
		
	}

	public List<String> getAnnotations()
	{
		return documents.getAnnotationArray(annotation);
	}
	
	public void setAnnotation(String annotation)
	{
		if(annotation != null && !annotation.equals(this.annotation))
			labelArray = null;
		this.annotation = annotation;
	}
	
	/**
	 * Uses a sort of shoddy and roundabout catch-exception way of figuring out if the data type is nominal or numeric.
	 * @return
	 */
	public Feature.Type getClassValueType()
	{
		return type;
	}

	public String getAnnotation()
	{
		return annotation;
	}

	public void setClassValueType(Feature.Type type)
	{
		if(this.type != type)
			labelArray = null;
		
		this.type = type;
	}

	public FeatureTable predictionClone()
	{
		FeatureTable ft = new FeatureTable();
	    ft.setName(getName()+" (prediction clone)");

//	    ft.documents = documents;
		ft.type = type;
		ft.threshold = threshold;
		ft.annotation = annotation;
		ft.labelArray = labelArray;
	    
	    ft.hitsPerFeature = new TreeMap<Feature, Collection<FeatureHit>>();
	    ft.threshold = threshold;
	    ft.numericConvertedClassValues = new HashMap<String, double[]>(numericConvertedClassValues);
	    ft.nominalConvertedClassValues = new ArrayList<String>(nominalConvertedClassValues);
	    
	    Collection<FeatureHit> emptyHits = new ArrayList<FeatureHit>(0);
	    
	    for(Feature f : hitsPerFeature.keySet())
	    {
	    	ft.hitsPerFeature.put(f, emptyHits);
	    }
	    
	    for(int i = 0; i < getSize(); i++)
	    {
	    	ft.hitsPerDocument.add(emptyHits);
	    }
	    
	    
	    return ft;
	}
	
}