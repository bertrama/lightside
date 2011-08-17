package edu.cmu.side.simple.feature;

import java.util.Collection;

public class Feature
{
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
	
	/**
	 * Construct a String, Boolean, or Numeric Feature.
	 * @param prefix the unique prefix for the extractor that produces this feature
	 * @param name a prefix-unique name for this feature
	 * @param type a hint for feature handling - Feature.Type.NUMERIC, BOOLEAN, or STRING
	 */
	public Feature(String prefix, String name, Feature.Type type)
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
	public Feature(String prefix, String name, Collection<String> nominals)
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
}
