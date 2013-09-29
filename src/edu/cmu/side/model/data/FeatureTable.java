package edu.cmu.side.model.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.side.model.Recipe;
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

	private String[] labelArray;

	private FeatureTable(){
		this.hitsPerFeature = new TreeMap<Feature, Collection<FeatureHit>>(); //Rough guess at capacity requirement.
		this.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
	}

	/**
	 * Appropriate when reconciling with an exisiting feature table, or with unlabeled data.
	 * @param sdl
	 * @param hits
	 * @param thresh
	 * @param annotation
	 * @param type
	 * @param labels the label array to use - the new doclist may not have all the possible labels in it.
	 */
	public FeatureTable(DocumentList sdl, Collection<FeatureHit> hits, int thresh, String annotation, Feature.Type type, String[] labels)
	{
		this(sdl, hits, thresh, annotation, type);
		labelArray = labels;
	}

	
	/**
	 * build an empty feature table from its components without any calculation. Used when assembling a prediction-only table. DocumentList might be null.
	 * @param name the name of this feature table
	 * @param thresh rare feature extraction threshold
	 * @param currentAnnotation the  name for the predicted class
	 * @param type the class' type - nominal or numeric
	 * @param labelArray the labels the associated model is trained to predict
	 * @param featureList the set of features expected by the associated trained model
	 * @param docs
	 */
	public FeatureTable(String name, int thresh, String currentAnnotation, Feature.Type type, String[] labelArray,
			ArrayList<Feature> featureList, DocumentList docs){
		this.name=name;
		this.threshold = thresh;
		this.annotation=currentAnnotation;
		this.type=type;
		this.labelArray = labelArray;
		this.hitsPerFeature = new TreeMap<Feature, Collection<FeatureHit>>();
		this.hitsPerDocument = new ArrayList<Collection<FeatureHit>>(0);
		Collection<FeatureHit> emptyHits = new ArrayList<FeatureHit>(0);
		for(Feature f : featureList)
		{
			this.hitsPerFeature.put(f, emptyHits);
		}

		documents = docs;
		
		if(documents != null)
		for(int i = 0; i < docs.getSize(); i++)
		{
			this.hitsPerDocument.add(emptyHits);
		}
	}

	//TODO: review this constructor. 
	public FeatureTable(DocumentList sdl, Collection<FeatureHit> hits, int thresh, String annotation, Feature.Type type)
	{
		this();
		Boolean docExists = true;
		if(sdl==null) docExists=false;
		setAnnotation(annotation);
		this.type = type;
		Map<Feature, Set<Integer>> localFeatures = new HashMap<Feature, Set<Integer>>(100000);
		this.threshold = thresh;
		this.documents = sdl;
		long hitCount = 0;

		generateConvertedClassValues();

//		System.out.println("FT 65: " + hits.size() + " total incoming hits, "+sdl.getSize()+ " instances");

		for (int i = 0; i < sdl.getSize(); i++)
		{
			hitsPerDocument.add(new TreeSet<FeatureHit>());
		}
		Iterator<FeatureHit> hiterator = hits.iterator();
		while(hiterator.hasNext())
		{
			FeatureHit hit = hiterator.next();
			Feature f = hit.getFeature();
			if (!hitsPerFeature.containsKey(f))
			{
				hitsPerFeature.put(f, new TreeSet<FeatureHit>());
			}
			hitsPerFeature.get(f).add(hit);
			hiterator.remove(); //TODO: does emptying the hitlist while populating the table actually make a practical memory difference?
		}
//		System.out.println("All features added to table. Thresholding...");

		Iterator<Entry<Feature, Collection<FeatureHit>>> fiterator = hitsPerFeature.entrySet().iterator();
		
		while(fiterator.hasNext())
		{
			Entry<Feature, Collection<FeatureHit>> entry = fiterator.next();
			
			int numHitsForThisFeature = hitsPerFeature.get(entry.getKey()).size();
			if(numHitsForThisFeature >= threshold)
			{
				hitCount += numHitsForThisFeature;
				for(FeatureHit hit : entry.getValue())
				{
					hitsPerDocument.get(hit.getDocumentIndex()).add(hit);
				}
			}
			else
			{
				fiterator.remove();
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

	public Map<String, double[]> getNumericClassValues(){
		return this.numericConvertedClassValues;
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

	@Override
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
	
	public void reconcileFeatures(Set<Feature> guaranteedFeatures)
	{
		//TODO: decide if this removal step is neccessary - it may be un-needful, but could save space.
		Collection<Feature> toRemove = new ArrayList<Feature>();

		//		System.out.println("FT 480: Unreconciled feature table has "+this.getFeatureSet().size() + " features, vs. "+train.getFeatureSet().size()+" in target");

		for(Feature f : this.hitsPerFeature.keySet())
		{
			if(!guaranteedFeatures.contains(f))
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
		for(Feature f : guaranteedFeatures)
		{
			if(!this.hitsPerFeature.containsKey(f))
			{
				this.hitsPerFeature.put(f, new ArrayList<FeatureHit>());
			}
		}

//				System.out.println("FT 511: Reconciled table has "+this.getFeatureSet().size() + " features");

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

	/**
	 * Produces a feature-table suitable only for prediction, not analysis. 
	 * @param dummyDocs might be null. If not null, pass in a proxy for the original DocumentList.
	 * @return
	 */
	public FeatureTable predictionClone(DocumentList dummyDocs)
	{
		FeatureTable ft = new FeatureTable();

		String cloneName = getName();
		if(!cloneName.endsWith(Recipe.PREDICTION_SUFFIX))
			cloneName+=Recipe.PREDICTION_SUFFIX;

		ft.setName(cloneName);

	    ft.documents = dummyDocs;
		ft.type = type;
		ft.threshold = threshold;
		ft.annotation = annotation;
		ft.labelArray = labelArray;

		ft.hitsPerFeature = new TreeMap<Feature, Collection<FeatureHit>>();
		ft.numericConvertedClassValues = new HashMap<String, double[]>(numericConvertedClassValues);
		ft.nominalConvertedClassValues = new ArrayList<String>(nominalConvertedClassValues);

		Collection<FeatureHit> emptyHits = new ArrayList<FeatureHit>(0);

		for(Feature f : hitsPerFeature.keySet())
		{
			ft.hitsPerFeature.put(f, emptyHits);
		}

		for(int i = 0; i < dummyDocs.getSize(); i++)
		{
			ft.hitsPerDocument.add(emptyHits);
		}

		return ft;
	}

	public boolean isWeighted()
	{
		Pattern p = Pattern.compile("\\D*(\\d+)\\D*");
		for(String s : getLabelArray())
		{
			Matcher m = p.matcher(s);
			if(!m.matches())
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean equals(FeatureTable other)
	{
		boolean returnStatement = true;
		if (this.documents != null)
		{
			returnStatement = this.documents.equals(other.getDocumentList()) ? returnStatement : false;
		}
		else
		{
			returnStatement = other.getDocumentList() == null ? returnStatement : false;
		}

		returnStatement = this.threshold.equals(other.threshold) ? returnStatement : false;
		returnStatement = this.getAnnotation().equals(other.getAnnotation()) ? returnStatement : false;
		returnStatement = this.getClassValueType().equals(other.getClassValueType()) ? returnStatement : false;
		if (this.getFeatureSet().equals(other.getFeatureSet()))
		{
			for (Feature feat : this.getFeatureSet())
			{
				returnStatement = this.getHitsForFeature(feat).equals(other.getHitsForFeature(feat)) ? returnStatement : false;
			}
		}
		else
		{
			returnStatement = false;
		}
		if (this.getDocumentList().equals(other.getDocumentList()))
		{
			for (int i = 0; i < this.hitsPerDocument.size(); i++)
			{
				returnStatement = this.getHitsForDocument(i).equals(other.getHitsForDocument(i)) ? returnStatement : false;
			}
		}
		else
		{
			returnStatement = false;
		}
		return returnStatement;
	}
}