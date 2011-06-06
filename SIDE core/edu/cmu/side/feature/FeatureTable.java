package edu.cmu.side.feature;

import java.util.*;

import edu.cmu.side.plugin.FEPlugin;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;

/**
 * 
 * A many-directional mapping of Features, FeatureHits and indexes into the DocumentList.
 *
 */
public class FeatureTable
{
	private Collection<FEPlugin> extractors;
	private DocumentList documents;
	private Map<Feature, Collection<FeatureHit>> hitsPerFeature;
	private List<Collection<FeatureHit>> hitsPerDocument;
	
	public FeatureTable(Collection<FEPlugin> extractors, DocumentList documents)
	{
		this.extractors = extractors;
		this.documents = documents;
		this.hitsPerFeature = new HashMap<Feature, Collection<FeatureHit>>();;
		this.hitsPerDocument  = new ArrayList<Collection<FeatureHit>>();
	}
	
	/**
	 * run the extractors on the documents and populate the feature hit tables.
	 */
	public void extractAll()
	{
		hitsPerDocument.clear();
		for(int i = 0; i < documents.getSize(); i++)
		{
			hitsPerDocument.add(new ArrayList<FeatureHit>());
		}
		
		for(FEPlugin extractor : extractors)
		{
			Collection<FeatureHit> hits = extractor.extractFeatureHits(documents);
			
			for(FeatureHit hit : hits)
			{
				hitsPerDocument.get(hit.documentIndex).add(hit);
				
				if(! hitsPerFeature.containsKey(hit.feature))
				{
					hitsPerFeature.put(hit.feature, new ArrayList<FeatureHit>());
				}
				hitsPerFeature.get(hit.feature).add(hit);
			}
		}
	}
	
	/**
	 * 
	 * @return the set of features extracted from the documents.
	 */
	public Set<Feature> getFeatureSet()
	{
		return hitsPerFeature.keySet();
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
	
	public DocumentList getDocumentList()
	{
		return documents;
	}
	
	public Collection<FEPlugin> getExtractors()
	{
		return extractors;
	}
	
	public void setExtractors(Collection<FEPlugin> extractors)
	{
		this.extractors = extractors;
	}
}
