package edu.cmu.side.plugin;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cmu.side.feature.*;
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

	public Collection<FeatureHit> extractFeatureHits(DocumentList documents)
	{
		// TODO make me abstract.
		// DUCT TAPE: convert featureMaps to FeatureHits
		
		ArrayList<FeatureHit> hits = new ArrayList<FeatureHit>();
		
		try
		{
			HashMap<String, Feature> myFeatures = new HashMap<String, Feature>();
			
			List<Map<String, Number>> featureMaps = extractFeatureMapsForSubclass(documents);
			for(int i = 0; i < documents.getSize(); i++)
			{
				for(Entry<String, Number> feature : featureMaps.get(i).entrySet())
				{
					String featureName = feature.getKey();
					if(!myFeatures.containsKey(featureName))
						myFeatures.put(featureName, new Feature(getOutputName(), featureName, Feature.Type.NUMERIC));
					
					hits.add(new FeatureHit(myFeatures.get(featureName), feature.getValue(), i));
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hits;
	}

}
