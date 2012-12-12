package edu.cmu.side.simple.feature;

import java.io.Serializable;

/**
 * 
 * Represents a "hit" of a feature on a document instance. 
 * Subclasses specify the hit's location within and around this document, for visualization and analysis purposes.
 * 
 */
public class FeatureHit implements Comparable<FeatureHit>, Serializable
{
	private static final long serialVersionUID = 1423521357321268667L;

	/**
	 * which feature hit here?
	 */
	protected Feature feature;
	
	/**
	 * the value expressed for the feature that hit here.
	 */
	protected Object value;
	
	/**
	 * the document in which this feature hit.
	 */
	protected int documentIndex;

	public FeatureHit clone(){
		FeatureHit fh = new FeatureHit(feature, value, documentIndex);
		return fh;
	}
	
	public FeatureHit clone(String prefix){
		FeatureHit fh = new FeatureHit(feature.clone(prefix), value, documentIndex);
		return fh;
	}
	public FeatureHit(Feature feature, Object value, int documentIndex)
	{
		this.feature = feature;
		this.setValue(value);
		this.documentIndex = documentIndex;
	}

	public Feature getFeature()
	{
		return feature;
	}

	public void setFeature(Feature feature)
	{
		this.feature = feature;
	}

	public Object getValue()
	{
		return value;
	}

	/**
	 * Set the value for this feature hit. The type of the value must correspond to the Feature's type.
	 * @param value
	 */
	public void setValue(Object value)
	{
		if(   !feature.getFeatureType().getClassForType().isInstance(value) 
			|| feature.getFeatureType() == Feature.Type.NOMINAL && !feature.getNominalValues().contains(value))
			throw new IllegalArgumentException(value+" is not a legal value for the "+feature.getFeatureType()+" feature "+feature);

		this.value = value;
	}

	/**
	 * 
	 * @return an index for the matching document in the current document list.
	 */
	public int getDocumentIndex()
	{
		return documentIndex;
	}

	public void setDocumentIndex(int documentIndex)
	{
		this.documentIndex = documentIndex;
	}
	
	public String toString()
	{
		return feature+"@"+documentIndex+"("+value+")";
	}

	@Override
	public int compareTo(FeatureHit o) {
		if(feature.toString().equals(o.feature.toString())){
			if(documentIndex == o.documentIndex){
				return value.toString().compareTo(o.value.toString());
			}else{
				return documentIndex-o.documentIndex;
			}
		}else{
			return feature.toString().compareTo(o.feature.toString());
		}
	}
}