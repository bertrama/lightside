package edu.cmu.side.simple.feature;

import java.io.File;


import java.util.*;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.SimpleDocumentList;

/**
 * 
 * A many-directional mapping of Features, FeatureHits and indexes into the DocumentList.
 *
 */
public class FeatureTable
{
	public static String[] constantEvaluations = {"predictor of","kappa","precision","recall","f-score","accuracy","hits"};
	private Collection<FeaturePlugin> extractors;
	private SimpleDocumentList documents;
	private Map<Feature, Collection<FeatureHit>> hitsPerFeature;
	private List<Collection<FeatureHit>> hitsPerDocument;
	private String tableName;
	/** Stores the type of the class value */
	private Feature.Type type = null;
	/** These show up as columns in the FeatureTablePanel */
	private Map<String, Map<Feature, Comparable>> evaluations;

	/** These variables are for weka. Filled when needed only. Stored 
	 * in the feature table so that it's cleaner to populate. */
	private FastVector fastVector = null;
	private double[] empty = null;	
	private Instances instances = null;
	private Map<String, Integer> attributeMap = new HashMap<String, Integer>();

	private void init(Collection<FeaturePlugin> extractors, SimpleDocumentList documents, int threshold){
		this.extractors = extractors;
		this.documents = documents;
		this.evaluations = new TreeMap<String, Map<Feature, Comparable>>();
		this.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>(30000); //Rough guess at capacity requirement.
		this.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
		extractAll(threshold);
	}

	public FeatureTable(FeaturePlugin extractor, SimpleDocumentList documents, int threshold)
	{
		Set<FeaturePlugin> extractors = new TreeSet<FeaturePlugin>();
		extractors.add(extractor);
		init(extractors, documents, threshold);
	}

	public FeatureTable(Collection<FeaturePlugin> extractors, SimpleDocumentList documents, int threshold)
	{
		init(extractors, documents, threshold);
	}

	/**
	 * Builds a set of features for Weka's internal data structures.
	 * Doesn't convert the instances yet (use getInstances() for that).
	 */
	public void generateFastVector(){
		double time1 = System.currentTimeMillis();
		if(fastVector == null){
			FastVector attributes = new FastVector();
			int index = 0;
			Collection<Feature> featureSet = getFeatureSet();
			empty = new double[featureSet.size()+1];
			for(Feature f : featureSet){
				Attribute att = null;
				FastVector fv = new FastVector();
				switch(f.getFeatureType()){
				case BOOLEAN:
					fv.addElement(Boolean.FALSE.toString());
					fv.addElement(Boolean.TRUE.toString());
					att = new Attribute(f.getFeatureName(), fv);
					break;
				case NOMINAL:
					for(String s : f.getNominalValues()) fv.addElement(s);
					att = new Attribute(f.getFeatureName(), fv);
					break;
				case NUMERIC:
					att = new Attribute(f.getFeatureName());
					break;
				case STRING:
					att = new Attribute(f.getFeatureName(), (FastVector)null);
					break;
				}
				if(att != null){
					attributes.addElement(att);		
					String id = f.getExtractorPrefix()+":"+f.getFeatureName();
					attributeMap.put(id, index++);
				}
			}
			switch(getClassValueType()){
			case NOMINAL:
				FastVector fv = new FastVector();
				for(String s : getDocumentList().getLabelArray()){ 
					fv.addElement(s);
				}
				attributes.addElement(new Attribute("CLASS", fv));
				break;
			case NUMERIC:
				attributes.addElement(new Attribute("CLASS"));
				break;
			}
			fastVector = attributes;
		}
		double time2 = System.currentTimeMillis();
		System.out.println((time2-time1) + " milliseconds to generate fast vector.");
	}

	/**
	 * Generates the set of instances used for the final model (not for cross-validation)
	 * @return
	 */
	public Instances getInstances(){
		if(instances == null){
			Instances format = new Instances(getTableName(), fastVector, 0);
			double runningTotal = 0.0;
			for(int i = 0; i < documents.getSize(); i++){
				double time1 = System.currentTimeMillis();
				Instance inst = new Instance(format.numAttributes());
				format.add(inst);					
				fillInstance(format, i);
				double time2 = System.currentTimeMillis();
				runningTotal += (time2-time1);
//				System.out.println(i + " documents, " + runningTotal + " ms, " + getHitsTime + ", " + setValueTime + " (" + aTime + ", " + bTime + ", " + cTime + ", " + dTime + ", " + eTime + "), " + classValueTime + ", " + cleanupTime);
			}
			format.setClass(format.attribute("CLASS"));
			instances = format;			
		}
		return instances;
	}
	static double getHitsTime = 0.0;
	static double setValueTime = 0.0;
	static double classValueTime = 0.0;
	static double cleanupTime = 0.0;


	/**
	 * Generates subsets of data from this feature table, used for cross validation. Makes a shallow copy of features
	 * from the overall Instances object.
	 * 
	 * @param foldMap Set of documents to use in this subset.
	 * @param fold Number of the fold to use for CV-by-fold radio button.
	 * @param train Whether this is the training or test set.
	 * @return
	 */
	public Instances getInstances(Map<Integer, Integer> foldMap, int fold, boolean train){
		if(instances == null){
			getInstances();
		}
		Instances format = new Instances(getTableName(), fastVector, 0);
		for(int i = 0; i < instances.numInstances(); i++){
			if((train && foldMap.get(i) != fold) || (!train && foldMap.get(i) == fold)){
				format.add((Instance)instances.instance(i).copy());
			}
			format.setClass(format.attribute("CLASS"));
		}
		return format;
	}

	/**
	 * Since we're doing cross-validation in a more intelligent way than SIDE originally did it (taking every nth instance
	 * for n folds, instead of taking the first 100/n% of the data for each fold), we need to keep a map of which keys in the 
	 * subset from getInstances() correspond to which instances in the whole data set. In this case, keys are the subset's 
	 * document index and values are the original document index.
	 * @param foldMap
	 * @param fold
	 * @return
	 */
	public Map<Integer, Integer> foldIndexToIndex(Map<Integer, Integer> foldMap, int fold){
		Map<Integer, Integer> foldIndexToIndex = new TreeMap<Integer, Integer>();
		int index = 0;
		for(int i = 0; i < getDocumentList().getSize(); i++){
			if(foldMap.get(i)==fold){
				foldIndexToIndex.put(index++, i);
			}
		}
		return foldIndexToIndex;
	}

	static double aTime = 0.0;
	static double bTime = 0.0;
	static double cTime = 0.0;
	static double dTime = 0.0;
	static double eTime = 0.0;
	/**
	 * Generates Instance objects (weka format) for a document in the corpus. Actually,
	 * these objects already exist, we're just filling the value.
	 * 
	 * @param format The Instances object to put this generated Instance in.
	 * @param i The document to fill. 
	 */
	private void fillInstance(Instances format, int i) {
		int j = 0;
		double time1 = System.currentTimeMillis();
		Collection<FeatureHit> hits = getHitsForDocument(i);
		double time2 = System.currentTimeMillis();
		Instance instance = format.instance(i);
		for(FeatureHit hit : hits){
			double time2a = System.currentTimeMillis();
			Feature f = hit.getFeature();
			double time2b = System.currentTimeMillis();
			String id = f.getExtractorPrefix()+":"+f.getFeatureName();
			double time2c = System.currentTimeMillis();
			Integer att = attributeMap.get(id);
			double time2d = System.currentTimeMillis();
			Feature.Type type = f.getFeatureType();
			double time2e = System.currentTimeMillis();
			try{
				switch(type){
				case NUMERIC:
					instance.setValue(att, (Double)hit.getValue());
					break;
				case STRING:
				case NOMINAL:
				case BOOLEAN:
					instance.setValue(att, hit.getValue().toString());
					break;
				}				
			}catch(Exception e){
				e.printStackTrace();
			}
			double time2f = System.currentTimeMillis();
			aTime += (time2b-time2a);
			bTime += (time2c-time2b);
			cTime += (time2d-time2c);
			dTime += (time2e-time2d);
			eTime += (time2f-time2e);

		}
		double time3 = System.currentTimeMillis();
		Attribute classAtt = format.attribute("CLASS");
		switch(getClassValueType()){
		case NUMERIC:
			instance.setValue(classAtt, Double.parseDouble(documents.getAnnotationArray().get(i)));
			break;
		case BOOLEAN:
		case STRING:
		case NOMINAL:
			instance.setValue(classAtt, documents.getAnnotationArray().get(i));
			break;
		}
		double time4 = System.currentTimeMillis();
		instance.replaceMissingValues(empty);
		double time5 = System.currentTimeMillis();
		getHitsTime += (time2-time1);
		setValueTime += (time3-time2);
		classValueTime += (time4-time3);
		cleanupTime += (time5-time4);
	}

	/**
	 * run the extractors on the documents and populate the feature hit tables.
	 */
	public void extractAll(int threshold)
	{
		hitsPerDocument.clear();
		for(int i = 0; i < documents.getSize(); i++)
		{
			hitsPerDocument.add(new ArrayList<FeatureHit>());
		}
		Map<Feature, Collection<FeatureHit>> localMap = new HashMap<Feature, Collection<FeatureHit>>(100000);

		for(FeaturePlugin extractor : extractors)
		{
			Collection<FeatureHit> hits = extractor.extractFeatureHits(documents);

			System.out.println(hits.size() + " feature hits from this extractor.");
			for(FeatureHit hit : hits)
			{
				if(! localMap.containsKey(hit.feature))
				{
					localMap.put(hit.feature, new ArrayList<FeatureHit>());
				}
				localMap.get(hit.feature).add(hit);
			}
		}
		Feature[] features = localMap.keySet().toArray(new Feature[0]);
		for(Feature f : features){
			if(localMap.get(f).size() < threshold){
				localMap.remove(f);
			}
		}
		for(Feature f : localMap.keySet()){
			if(!hitsPerFeature.containsKey(f)){
				hitsPerFeature.put(f, new ArrayList<FeatureHit>());
			}
			for(FeatureHit hit : localMap.get(f)){
				hitsPerDocument.get(hit.documentIndex).add(hit);
				hitsPerFeature.get(f).add(hit);
			}
		}
		localMap.clear();
	}
	
	/**
	 * Evaluates feature table for precision, recall, f-score, and kappa at creation time.
	 */
	public void defaultEvaluation(){
		double time1 = System.currentTimeMillis();
		
		Map<Feature, Comparable> precisionMap = new HashMap<Feature, Comparable>();
		Map<Feature, Comparable> recallMap = new HashMap<Feature, Comparable>();
		Map<Feature, Comparable> fScoreMap = new HashMap<Feature, Comparable>();
		Map<Feature, Comparable> accuracyMap = new HashMap<Feature, Comparable>();
		Map<Feature, Comparable> kappaMap = new HashMap<Feature, Comparable>();
		Map<Feature, Comparable> bestMap = new HashMap<Feature, Comparable>();
		Map<Feature, Comparable> hitsMap = new HashMap<Feature, Comparable>();
		
		ArrayList<String> trueAnnot = documents.getAnnotationArray();
		double timeA = 0.0;
		double timeA2 = 0.0;
		double timeB = 0.0;
		double timeC = 0.0;
		double timeD = 0.0;
		double lostTime = System.currentTimeMillis();
		for(Feature f : hitsPerFeature.keySet()){
			double f1 = System.currentTimeMillis();
			Collection<FeatureHit> hits = hitsPerFeature.get(f);
			double maxPrec = Double.NEGATIVE_INFINITY;
			double maxRec = Double.NEGATIVE_INFINITY;
			double maxF = Double.NEGATIVE_INFINITY;
			double maxKappa = Double.NEGATIVE_INFINITY;
			double maxAcc = Double.NEGATIVE_INFINITY;
			String bestLabel = "[useless]";
			String[] labels = documents.getLabelArray();
			double f2 = System.currentTimeMillis();
			for(String label : labels){
				double f3 = System.currentTimeMillis();
				double[][] kappaMatrix = new double[2][2];
				for(int i = 0; i < 2; i++){for(int j = 0; j < 2; j++){ kappaMatrix[i][j]=0;}}
				boolean[] hit = new boolean[documents.getSize()];
				for(FeatureHit fh : hits){
					if(checkHitMatch(f, fh.getValue())) hit[fh.getDocumentIndex()] = true;
				}
				double f3a = System.currentTimeMillis();
				for(int i = 0; i < documents.getSize(); i++){
					kappaMatrix[trueAnnot.get(i).equals(label)?0:1][hit[i]?1:0]++;
				}
				double f4 = System.currentTimeMillis();
				double rightHits = kappaMatrix[0][0];
				double wrongHits = kappaMatrix[1][0];
				double all = documents.getSize();
				double featHits = kappaMatrix[0][0] + kappaMatrix[1][0];
				double actHits = kappaMatrix[0][0] + kappaMatrix[0][1];
				double accuracy = (kappaMatrix[0][0] + kappaMatrix[1][1])/all;
				double pChance = ((featHits/all)*(actHits/all))+(((all-featHits)/all)*((all-actHits)/all));

				double prec = rightHits/(rightHits+wrongHits);
				double rec = rightHits/actHits;
				double fmeasure = (2*prec*rec)/(prec+rec);
				double kappa = (accuracy - pChance)/(1 - pChance);

				if(Double.NaN == rec) rec = 0.0;
				if(Double.NaN == fmeasure) fmeasure = 0.0;
				if(kappa > maxKappa){
					maxPrec = prec;
					maxRec = rec;
					maxF = fmeasure;
					maxAcc = accuracy;
					maxKappa = kappa;
					bestLabel = label;
				}
				double f5 = System.currentTimeMillis();
				timeA2 += (f3a-f3);
				timeB += (f4-f3a);
				timeC += (f5-f4);
			}
			double f6 = System.currentTimeMillis();
			precisionMap.put(f, maxPrec);
			recallMap.put(f, maxRec);
			fScoreMap.put(f, maxF);
			accuracyMap.put(f, maxAcc);
			kappaMap.put(f, maxKappa);
			bestMap.put(f, bestLabel);
			hitsMap.put(f, hits.size());
			double f7 = System.currentTimeMillis();
			timeA += (f2-f1);
			timeD += (f7-f6);
			System.out.println(timeA+","+timeA2+","+timeB+","+timeC+","+timeD);
		}
		System.out.println((System.currentTimeMillis()-lostTime-timeA-timeB-timeC-timeD) + " ms lost.");
		addEvaluation("predictor of", bestMap);
		addEvaluation("kappa", kappaMap);
		addEvaluation("precision", precisionMap);
		addEvaluation("recall", recallMap);
		addEvaluation("f-score", fScoreMap);
		addEvaluation("accuracy", accuracyMap);
		addEvaluation("hits", hitsMap);
		double time2 = System.currentTimeMillis();
		System.out.println("Evaluation done in " + (time2-time1) + " milliseconds.");
	}

	/**
	 * Checks whether this feature "hit" a document, for the purpose of converting all these different
	 * feature types into a boolean check for basic evaluations.
	 */
	public boolean checkHitMatch(Feature f, Object value){
		switch(f.getFeatureType()){
		case BOOLEAN:
			return Boolean.TRUE.equals(value);
		case NOMINAL:
			return false;
		case NUMERIC:
			return ((Number)value).doubleValue()>0;
		case STRING:
			return value.toString().length()>0;
		}
		return false;
	}

	/**
	 * 
	 * @return the set of features extracted from the documents.
	 */
	public Set<Feature> getFeatureSet()
	{
		Set<Feature> set = hitsPerFeature.keySet();
		return set;
	}

	/**
	 * 
	 * @param feature
	 * @return all hits for the given feature.
	 */
	public Collection<FeatureHit> getHitsForFeature(Feature feature)
	{
		return hitsPerFeature.get(feature);
	}

	/**
	 * 
	 * @param index
	 * @return all hits on the given document index.
	 */
	public Collection<FeatureHit> getHitsForDocument(int index)
	{
		return hitsPerDocument.get(index);
	}

	public SimpleDocumentList getDocumentList()
	{
		return documents;
	}

	public Collection<FeaturePlugin> getExtractors()
	{
		return extractors;
	}

	public void setExtractors(Collection<FeaturePlugin> extractors)
	{
		this.extractors = extractors;
	}

	public String getTableName(){
		return tableName;
	}

	public void setTableName(String name){
		tableName = name;
	}

	public Map<String, Map<Feature, Comparable>> getEvaluations(){
		return evaluations;
	}

	public void addEvaluation(String evaluationName, Map<Feature, Comparable> eval){
		evaluations.put(evaluationName, eval);
	}

	/**
	 * TODO: Implement file import/export of feature tables.
	 */
	public static FeatureTable createFromXML(File f){
		return null;
	}

	public String toString(){
		return getTableName();
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
	
	public static String[] getConstantEvaluations(){
		return constantEvaluations;
	}
}
