package edu.cmu.side.simple;

import java.io.File;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

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
	public Collection<FeatureHit> extractFeatureHits(SimpleDocumentList documents, Map<String, String> configuration, GenesisUpdater update)
	{
		this.configureFromSettings(configuration);
		return extractFeatureHitsForSubclass(documents, update);
	}
	
	/**
	 * Implemented by the plugin to do feature extraction.
	 * @param documents
	 * @return Features for all documents.
	 */
	public abstract Collection<FeatureHit> extractFeatureHitsForSubclass(SimpleDocumentList documents, GenesisUpdater update);

}
