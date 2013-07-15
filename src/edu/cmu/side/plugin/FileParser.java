package edu.cmu.side.plugin;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.cmu.side.model.data.DocumentList;

public abstract class FileParser extends SIDEPlugin {
	public static String type = "file_parser";
	public abstract DocumentList parseDocumentList(Set<String> filenames);
	public abstract boolean canHandle(String filename);
	public String getType(){
		return this.type;
	}
	public Map<String, String> generateConfigurationSettings() {
		return new HashMap<String,String>();
	}
	public void configureFromSettings(Map<String, String> settings) {
		//TODO:Is there anything meaningful to do here?
	}
	public String getOutputName() {
		//TODO: Does this even make sense?
		return "FileParser";
	}
	protected Component getConfigurationUIForSubclass() {
		// TODO Auto-generated method stub
		return null;
	}
}
