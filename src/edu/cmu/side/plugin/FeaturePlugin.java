package edu.cmu.side.plugin;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.feature.FeatureHit;

public abstract class FeaturePlugin extends SIDEPlugin implements Serializable{
	private static final long serialVersionUID = -2856017007104452008L;

	public static String type = "feature_hit_extractor";
	
	public String getType() {
		return type;
	}
	
	/**
	 * 
	 * @param documents in a corpus
	 * @return All features that this plugin should extract from each document in this corpus.
	 */
	public Collection<FeatureHit> extractFeatureHits(DocumentList documents, Map<String, String> configuration, StatusUpdater update)
	{
		halt = false;
		this.configureFromSettings(configuration);
		return extractFeatureHitsForSubclass(documents, update);
	}
	
	/**
	 * Implemented by the plugin to do feature extraction.
	 * @param documents
	 * @return Features for all documents.
	 */
	public abstract Collection<FeatureHit> extractFeatureHitsForSubclass(DocumentList documents, StatusUpdater update);
	
	public abstract void configureFromFile(String filename);

}
