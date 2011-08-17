package edu.cmu.side.simple.feature;

import java.util.Collection;

/**
 * represents a feature hit that utilizes information about other documents in a sequential document list.
 *
 */
public class MultiDocumentFeatureHit extends FeatureHit
{
	/**
	 * the relevant context-documents informing this hit.
	 */
	private Collection<Integer> otherDocumentIndexes;

	public MultiDocumentFeatureHit(Feature feature, Object value, int documentIndex, Collection<Integer> collateralDocuments)
	{
		super(feature, value, documentIndex);
		otherDocumentIndexes = collateralDocuments;
	}
	/**
	 * @return the indexes of the relevant context-documents informing this hit.
	 */
	public Collection<Integer> getOtherDocumentIndexes()
	{
		return otherDocumentIndexes;
	}

}
