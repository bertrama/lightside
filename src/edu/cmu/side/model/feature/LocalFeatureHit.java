package edu.cmu.side.model.feature;

import java.util.ArrayList;
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

	private int[] singleHit;
	
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
	
	public LocalFeatureHit(Feature feature, Object value, int documentIndex, int start, int end)
	{
		super(feature, value, documentIndex);
		hits = new ArrayList<int[]>();
		addHit(start, end);
	}
	
//	public LocalFeatureHit(Feature feature, Object value, int documentIndex, int[] h)
//	{
//		super(feature, value, documentIndex);
//		this.singleHit = h;
//		hits = new ArrayList<int[]>();
//		addHit(h[0], h[1]);
//	}
	
	public Collection<int[]> getHits()
	{
		return hits;
	}
	
	public String toString()
	{
		String x = feature+"@"+documentIndex+"("+value+"):";
		if(hits != null)
		{
			for(int[] h : hits)
				x+="("+h[0]+","+h[1]+") ";
		}
		return x;
	}

	public void addHit(int start, int end)
	{
		hits.add(new int[]{start, end});
	}
}
