package edu.cmu.side.plugin;

import java.io.File;

import java.util.List;
import java.util.Map;

import edu.cmu.side.uima.UIMAToolkit.DocumentList;

/**
 * In order to implement a new feature extractor, you must extend this class as well as 
 * @author elijah
 *
 */
public abstract class FEPlugin extends SIDEPlugin {
	

	@Override
	public boolean doValidation(StringBuffer msg) {
		return true;
	}

	@Override
	public void memoryToUI() {}

	@Override
	public void uiToMemory() {}

	public FEPlugin() {
		super();
	}
	
	public FEPlugin(File rootFolder) {
		super(rootFolder);
	}

	public static String type = "feature_extractor";
	public String getType() {
		return type;
	}
	
	public void setContext(DocumentList d){}
	
	public List<Map<String, Number>> extractFeatureMap(DocumentList documents) throws Exception{
		this.uiToMemory();
		return extractFeatureMapsForSubclass(documents); 
	}
	
	public abstract String getOutputName();
	
	public abstract List<Map<String, Number>> extractFeatureMapsForSubclass(DocumentList documents) throws Exception;

}
