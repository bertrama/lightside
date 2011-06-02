package edu.cmu.side.plugin;

import java.awt.Component;
import java.io.File;

import edu.cmu.side.Workbench;

public abstract class SegmenterPlugin extends SIDEPlugin{

	public static String type = "segmenter";

	public SegmenterPlugin() {
		super();
	}

	public SegmenterPlugin(File rootFolder) {
		super(rootFolder);
	}

	public String getType() {
		return type;
	}
	
	/**
	 * get segmentation indexArray of text
	 * @param reader
	 * @return index of segmentation start point and the end point (0:inclusive, N:inclusive)
	 * @throws Exception
	 */
	public int[] getSegmentEndIndexArray(CharSequence cs) throws Exception{
		this.uiToMemory();
		
		return getSegmentEndIndexArrayForSubclass(cs);
	}
	
	protected abstract int[] getSegmentEndIndexArrayForSubclass(CharSequence cs) throws Exception;
	
	
	/**
	 * #### IMPORTANT ####
	 * option configuration for SegmenterPlugin is disabled right now.
	 * In order to enable it in the future,
	 * A way to give proper baseSubtypeName for all possible options should be made.
	 * And it should be human readable since baseSubtypeName is visible to end user
	 * when segmenting a new segment
	 * Also, baseSubtypeName should be distinct enough to reconstruct segmenter
	 * when summarizing...
	 */
	public final Component getConfigurationUIForSubclass(){
		throw new UnsupportedOperationException();
	}
	
	public static SegmenterPlugin create(String baseSubtypeName){
		PluginWrapper pluginWrapper = Workbench.current.pluginManager.getPluginWrapperByPluginClassName(baseSubtypeName);
		if(pluginWrapper==null){ return null; }
		
		SIDEPlugin sidePlugin = pluginWrapper.getSIDEPlugin();
		if(!(sidePlugin instanceof SegmenterPlugin)){ return null; }
		
		return (SegmenterPlugin)sidePlugin;
	}

	public static boolean isBaseSubtypeNameFromSegmenter(String baseSubtypeName) {
		return create(baseSubtypeName)==null;
	}
}
