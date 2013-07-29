package edu.cmu.side.plugin.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	private static boolean getValidPlugin(String fileName){
		boolean foundParser = false;
		for (SIDEPlugin parser : parsers) {
			if(((FileParser)parser).canHandle(fileName)) {
				fileChunks.get(parser).add(fileName);
				foundParser = true;
			}
		}
		return foundParser;
	}
	
	public static DocumentList makeDocumentList(TreeSet<String> fileNames) throws Exception, IOException, FileNotFoundException{
		if(fileNames.size()==0) return null;
		fileChunks = new HashMap<FileParser, HashSet<String>>();
		if(parsers.length==0){
			throw new Exception("There are no parsers");
		}
		for (SIDEPlugin parser : parsers) {
			fileChunks.put((FileParser)parser, new HashSet<String>());
		}
		for (String fileName : fileNames) {
			File file = new File(fileName);
			if(!file.exists()){
				throw new FileNotFoundException(fileName);
			}
			else if(!getValidPlugin(fileName)){
				throw new Exception("File: " + fileName.toString() + " could not be parsed.");
			}
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
