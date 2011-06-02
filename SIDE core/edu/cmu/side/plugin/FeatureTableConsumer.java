package edu.cmu.side.plugin;

import java.awt.Component;
import java.io.File;

import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;

public abstract class FeatureTableConsumer extends SIDEPlugin {
	public FeatureTableConsumer () {super();}
	
	public static final String type = "feature_table_consumer";
	public String getType () {return type;}
	
	
	public FeatureTableConsumer (File rootFolder)
	{
		super (rootFolder);
	}
	
	public abstract void comsumeFeatureTable(FeatureTable featureTable) throws Exception;
	
	@Override
	public Component getConfigurationUIForSubclass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void memoryToUI() {
		throw new UnsupportedOperationException();		
	}

	@Override
	public void uiToMemory() {
		throw new UnsupportedOperationException();		
	}
}
