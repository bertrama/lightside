package edu.cmu.side.simple;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;

import edu.cmu.side.dataitem.DocumentListInterface;
import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public abstract class FeaturePlugin extends SIDEPlugin implements Serializable{
	private static final long serialVersionUID = -2856017007104452008L;

	protected static boolean halt = false;
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
	public List<FeatureHit> extractFeatureHits(DocumentListInterface documents, JLabel update)
	{
		this.uiToMemory();
		// TODO make me abstract.
		// DUCT TAPE: convert featureMaps to FeatureHits
		
		ArrayList<FeatureHit> hitsList = new ArrayList<FeatureHit>();
		Collection<FeatureHit> hits = extractFeatureHitsForSubclass(documents, update);
		if(hits != null){
			hitsList.addAll(hits);
		}
		return hitsList;
	}
	
	public boolean overridesFeatureTable(){
		return false;
	}
	
	public FeatureTable getCustomFeatureTable(DocumentListInterface documents){
		return null;
	}
	
	/**
	 * Implemented by the plugin to do feature extraction.
	 * @param documents
	 * @return Features for all documents.
	 */
	public abstract Collection<FeatureHit> extractFeatureHitsForSubclass(DocumentListInterface documents, JLabel update);

	public void stopWhenPossible(){
		halt = true;
	}
}
