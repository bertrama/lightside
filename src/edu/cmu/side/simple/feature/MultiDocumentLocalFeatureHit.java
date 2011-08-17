package edu.cmu.side.simple.feature;

import java.util.Collection;
import java.util.Map;

/**
 * represents a feature hit that utilizes information about other documents in a sequential document list.
 *
 */
public class MultiDocumentLocalFeatureHit extends FeatureHit
{
	/**
	 * the relevant context-documents informing this hit.
	 */
	private Map<Integer, Collection<int[]>> documentHits;

	public MultiDocumentLocalFeatureHit(Feature feature, Object value, int documentIndex, Map<Integer, Collection<int[]>> documentHitMap)
	{
		super(feature, value, documentIndex);
		documentHits = documentHitMap;
	}
	
	/**
	 * 
	 * @return the indexes of documents hit by this feature.
	 */
	public Collection<Integer> getOtherDocumentIndexes()
	{
		return documentHits.keySet();
	}
	
	public Collection<int[]> getHitIndexesForDocument(int index)
	{
		return documentHits.get(index);
	}
	
	/**
	 * @return the hit patterns across all the relevant documents.
	 */
	public Map<Integer, Collection<int[]>> getOtherDocumentHits()
	{
		return documentHits;
	}

}
