package edu.cmu.side.simple;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.uima.DocumentListInterface;

public abstract class FeaturePlugin extends SIDEPlugin{

	/**
	 * Kept for legacy reasons, implemented in this class so the developer doesn't have to.
	 */
	@Override
	public boolean doValidation(StringBuffer msg) {
		return true;
	}

	@Override
	public void memoryToUI() {}

	/**
	 * Called before extracting features; implementation should ensure that 
	 * all settings in the UI are transferred to model settings in this method.
	 */
	@Override
	public void uiToMemory() {}

	public FeaturePlugin() {
		super();
	}
	
	public FeaturePlugin(File rootFolder) {
		super(rootFolder);
	}

	public static String type = "feature_hit_extractor";
	
	public String getType() {
		return type;
	}
		
	/**
	 * @return A short prefix string for the plugin name.
	 */
	public abstract String getOutputName();

	/**
	 * 
	 * @param documents in a corpus
	 * @return All features that this plugin should extract from each document in this corpus.
	 */
	public Collection<FeatureHit> extractFeatureHits(DocumentListInterface documents)
	{
		this.uiToMemory();
		// TODO make me abstract.
		// DUCT TAPE: convert featureMaps to FeatureHits
		
		ArrayList<FeatureHit> hits = new ArrayList<FeatureHit>();
		hits.addAll(extractFeatureHitsForSubclass(documents));
		return hits;
	}
		
	/**
	 * Implemented by the plugin to do feature extraction.
	 * @param documents
	 * @return Features for all documents.
	 */
	public abstract Collection<FeatureHit> extractFeatureHitsForSubclass(DocumentListInterface documents);
	
}
