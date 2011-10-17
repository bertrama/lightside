package edu.cmu.side.simple.feature;

import java.util.Collection;

/**
 *  A feature hit with a particular location within a document
 */
public class SingleLocalFeatureHit extends FeatureHit
{
	/**
	 * {start, end} pairs indicating where this feature is expressed in this document.
	 * end - start = length of hit.
	 */
	private int start, end;

	/**
	 * 
	 * @param feature the feature which has hit the document in these spots.
	 * @param value the value expressed by the feature for this document. Its type should agree with feature.getFeatureType()
	 * @param documentIndex the index of the document in the current document set.
	 * @param hits {start, end} pairs indicating exactly where this feature hits in this document.
	 */
	public SingleLocalFeatureHit(Feature feature, Object value, int documentIndex, int start, int end)
	{
		super(feature, value, documentIndex);
		this.start = start;
		this.end = end;
	}

	public int getStart()
	{
		return start;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public String toString()
	{
		return "hit=doc#"+this.getDocumentIndex()+":("+start+","+end+")";
	}
}
