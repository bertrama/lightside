package edu.cmu.side.simple.feature;

import java.util.Collection;

/**
 *  A feature hit with a particular location within a document
 */
public class LocalFeatureHit extends FeatureHit
{
	/**
	 * {start, end} pairs indicating where this feature is expressed in this document.
	 * end - start = length of hit.
	 */
	private Collection<int[]> hits;

	/**
	 * 
	 * @param feature the feature which has hit the document in these spots.
	 * @param value the value expressed by the feature for this document. Its type should agree with feature.getFeatureType()
	 * @param documentIndex the index of the document in the current document set.
	 * @param hits {start, end} pairs indicating exactly where this feature hits in this document.
	 */
	public LocalFeatureHit(Feature feature, Object value, int documentIndex, Collection<int[]> hits)
	{
		super(feature, value, documentIndex);
		this.hits = hits;
	}

	public Collection<int[]> getHits()
	{
		return hits;
	}
	
	public String toString()
	{
		String x = getDocumentIndex()+"";
		for(int[] h : hits)
			x+="("+h[0]+","+h[1]+") ";
		return x;
	}
}
