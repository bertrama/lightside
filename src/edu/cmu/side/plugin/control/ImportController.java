package edu.cmu.side.plugin.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.plugin.FileParser;
import edu.cmu.side.plugin.SIDEPlugin;

public class ImportController {
	private ImportController(){}
	static SIDEPlugin[] parsers = PluginManager.getSIDEPluginArrayByType("file_parser");
	static HashMap<FileParser, HashSet<String>> fileChunks;
	private static void getValidPlugin(String fileName){
		for (SIDEPlugin parser : parsers) {
			if(((FileParser)parser).canHandle(fileName)) fileChunks.get(parser).add(fileName);
		}
	}
	
	public static DocumentList makeDocumentList(TreeSet<String> fileNames){
		if(fileNames.size()==0) return null;
		fileChunks = new HashMap<FileParser, HashSet<String>>();
		if(parsers.length==0){
			//We need to throw an exception here
		}
		for (SIDEPlugin parser : parsers) {
			fileChunks.put((FileParser)parser, new HashSet<String>());
		}
		for (String file : fileNames) {
			getValidPlugin(file);
		}
		ArrayList<DocumentList> toAggregate = new ArrayList<DocumentList>();
		for(SIDEPlugin parser : parsers){
			toAggregate.add(((FileParser)parser).parseDocumentList(fileChunks.get(parser)));
		}
		DocumentList aggregatedDocumentList = toAggregate.remove(0);
		if(!toAggregate.isEmpty()){
			aggregatedDocumentList.combine(toAggregate);
		}
		return aggregatedDocumentList;
	}
}
