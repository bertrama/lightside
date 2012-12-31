package edu.cmu.side.model.feature;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Feature implements Serializable, Comparable<Feature>
{
	private static final long serialVersionUID = -7567947807818964630L;

	/**
	 * 
	 * FeatureHits for a given Feature must be assigned values corresponding to these types.
	 * NOMINAL Features may only be assigned the values enumerated in the Feature's nominalValues field.
	 *
	 */
	public enum Type 
	{
		NUMERIC(Number.class), BOOLEAN(Boolean.class), STRING(String.class), NOMINAL(String.class);
		
		private Class<?> classForType;
		Type(Class<?> c){classForType = c;}
		
		public Class<?> getClassForType()
		{return classForType;}
	};
	
	protected String featureName;
	protected String extractorPrefix;
	protected Feature.Type featureType;
	protected Collection<String> nominalValues;
	
	private static Map<String, Map<String, Feature>> featureCache = new TreeMap<String, Map<String, Feature>>();
	
	public static Feature fetchFeature(String prefix, String name, Feature.Type type){
		if(!featureCache.containsKey(prefix)){
			featureCache.put(prefix, new HashMap<String, Feature>(100000));
		}
		if(!featureCache.get(prefix).containsKey(name+"_"+type.toString())){
			Feature newFeat = new Feature(prefix, name, type);
			featureCache.get(prefix).put(name+"_"+type.toString(), newFeat);
			return newFeat;			
		}else{
			return featureCache.get(prefix).get(name+"_"+type.toString());
		}
	}
	
	public static Feature fetchFeature(String prefix, String name, Collection<String> nominals){
		Feature f = fetchFeature(prefix, name, Feature.Type.NOMINAL);
		if(f.nominalValues == null){
			f.setNominalValues(nominals);
		}
		return f;
	}
	
	public void setNominalValues(Collection<String> nom){
		nominalValues = nom;
	}
	
	/**
	 * Construct a String, Boolean, or Numeric Feature.
	 * @param prefix the unique prefix for the extractor that produces this feature
	 * @param name a prefix-unique name for this feature
	 * @param type a hint for feature handling - Feature.Type.NUMERIC, BOOLEAN, or STRING
	 */
	private Feature(String prefix, String name, Feature.Type type)
	{
		this.featureName = name;
		this.extractorPrefix = prefix;
		this.featureType = type;
	}
	
	protected Feature(){
		this("none","none",Type.BOOLEAN);
	}
	/**
	 * Construct a Nominal (enumerated type) Feature.
	 * @param prefix the unique prefix for the the extractor that produces this feature
	 * @param name a prefix-unique name for this feature
	 * @param nominals the possible values this Feature can express
	 */
	private Feature(String prefix, String name, Collection<String> nominals)
	{
		this.featureName = name;
		this.extractorPrefix = prefix;
		this.featureType = Feature.Type.NOMINAL;
		this.nominalValues = nominals;
	}
	
	public String toString()
	{
		return featureName;
	}
	
	public Feature clone(String prefix){
		if (featureType == Feature.Type.NOMINAL)
			return new Feature(extractorPrefix, prefix+featureName, nominalValues);
		return new Feature(extractorPrefix, prefix+featureName, featureType);
	}
	
	/**
	 * @return the prefix-unique name for this feature.
	 */
	public String getFeatureName()
	{
		return featureName;
	}
	
	/**
	 * 
	 * @return the extractor prefix - indicates which extractor plugin instantiated this feature.
	 */
	public String getExtractorPrefix()
	{
		return extractorPrefix;
	}

	/**
	 * 
	 * @return Number, Boolean, String, or Nominal
	 */
	public Feature.Type getFeatureType()
	{
		return featureType;
	}
	
	/**
	 * 
	 * @return the possible values of this nominal feature.
	 */
	public Collection<String> getNominalValues()
	{
		if(featureType != Feature.Type.NOMINAL)
			throw new IllegalStateException(this+" is not a nominal feature.");
		return nominalValues;
	}

	@Override
	public int compareTo(Feature o) {
		if(extractorPrefix.equals(o.extractorPrefix)){
			return featureName.compareTo(o.featureName);
		}else return extractorPrefix.compareTo(o.extractorPrefix);
	}
	
	@Override
	public int hashCode()
	{
		return (this.extractorPrefix+this.featureName).hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		return (o instanceof Feature)&&(this.compareTo((Feature)o)==0)&&this.featureType.equals(((Feature)o).featureType);
	}
	
	//For prediction, newly extracted features may not have some nominalvalues as original one
	public static Feature reconcile(Feature a, Feature b){
		if (!a.equals(b))
			throw new IllegalStateException(a + " is different from " + b);
		if (b.getFeatureType() == Feature.Type.NOMINAL) 
			b.setNominalValues(a.getNominalValues());
		return b;
	}
}
