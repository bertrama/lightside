package edu.cmu.side.simple;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.yerihyo.yeritools.csv.CSVReader;
import com.yerihyo.yeritools.swing.AlertDialog;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.dataitem.DocumentListInterface;

public class SimpleDocumentList implements DocumentListInterface, Serializable{
	private static final long serialVersionUID = -5433699826930815886L;
	
	ArrayList<String> filenameList = new ArrayList<String>();
	HashMap<String, ArrayList<String>> allAnnotations = new HashMap<String, ArrayList<String>>();
	String currentAnnotation; 
	String textColumn;
	
	public SimpleDocumentList(Set<String> filenames){
		double time1 = System.currentTimeMillis();
		BufferedReader in;
		textColumn = null;
		currentAnnotation = null;
		for(String filename : filenames){
			try{
				File f = new File(filename);
				if(!f.exists())
					f = new File(SimpleWorkbench.dataFolder.getAbsolutePath(), filename.substring(Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"))+1));
				in = new BufferedReader(new FileReader(f));
				String line;
				String[] headers = in.readLine().replaceAll("\"","").split(",");

				for(int i = 0; i < headers.length; i++)
					headers[i] = headers[i].trim();

				for(String annotation : headers)
					if(!allAnnotations.containsKey(annotation))
						allAnnotations.put(annotation, new ArrayList<String>());

				int lineID = 0;
				boolean showed = false;
				while((line = in.readLine()) != null){
					line = line.replaceAll("[^\r\n\\p{ASCII}]", "");
					lineID++;
					CSVReader csvReader = new CSVReader(new StringReader(line), 0);
					String[] instance = csvReader.readNextMeaningful();
					if (instance.length != headers.length){
						System.out.println(lineID + ", " + instance.length + ", " + headers.length);
						if(headers.length < instance.length && !showed){
							AlertDialog.show("Warning!", "At least one line (" + lineID + ") has more values than annotation columns. Data representation may be corrupted.", null);
						}
						if(headers.length > instance.length && !showed){
							AlertDialog.show("Warning!", "At least one line (" + lineID + ") has an empty annotation. Data representation may be corrupted.", null);
						}
						for(int i = 0; i < Math.max(instance.length, headers.length); i++){
							if(headers.length > i){
								System.out.print(headers[i]+": ");
							}else{
								System.out.print("null: ");
							}
							if(instance.length > i){
								System.out.println(instance[i]);
							}else{
								System.out.println("null");
							}
						}
						showed = true;
					}
					
					Set<String> inserted = new HashSet<String>();
					for(int i = 0; i < instance.length && i < headers.length; i++){
						if (inserted.contains(headers[i])) continue;
						inserted.add(headers[i]);
						String value = instance[i].replaceAll("\"", "").trim();
						allAnnotations.get(headers[i]).add(value);															
					}
					filenameList.add(filename);
				}
			}catch(Exception e){
				AlertDialog.show("Error!", "Failed to load CSV into memory.", null);
				e.printStackTrace();
			}
		}
		double time2 = System.currentTimeMillis();
	}

	public SimpleDocumentList(Set<String> filenames, String currentAnnot, String textCol){
		this(filenames);
		setTextColumn(textCol);
		setCurrentAnnotation(currentAnnot);		
	}

	public SimpleDocumentList(Set<String> filenames, String textCol){
		this(filenames);
		setTextColumn(textCol);		
	}


	@Override
	public HashMap<String, ArrayList<String>> allAnnotations() {
		return allAnnotations;
	}

	@Override
	public ArrayList<String> getAnnotationArray(String name) {
		return allAnnotations.get(name);
	}

	@Override
	public ArrayList<String> getAnnotationArray() {
		if (currentAnnotation == null) return null;
		return allAnnotations.get(currentAnnotation);
	}

	@Override
	public List<String> getCoveredTextList() {
		if (textColumn == null) return null;
		return allAnnotations.get(textColumn);
	}

	@Override
	public int getSize() {
		if (allAnnotations == null) return 0;
		for (String key : allAnnotations.keySet() )
			if (allAnnotations.get(key).size() > 0)
				return allAnnotations.get(key).size();
		return 0;
	}

	public void setCurrentAnnotation(String annot){
		if (!allAnnotations.containsKey(annot))
			throw new IllegalStateException("Can't find the label column named " + annot + " in provided file");
		currentAnnotation = annot;
	}

	public String getCurrentAnnotation(){
		return currentAnnotation;
	}

	public String getTextColumn(){
		return textColumn;
	}

	public void setTextColumn(String name){
		if(name.equals("[No Text]"))
			textColumn = null;
		else {
			if (!allAnnotations.containsKey(name))
				throw new IllegalStateException("Can't find the text column named " + name + " in provided file");
			textColumn = name;
		}
	}

	/**
	 * Used for cross-validating by file.
	 */
	public String getFilename(int docIndex){
		return filenameList.get(docIndex);
	}

	public Set<String> getFilenames(){
		Set<String> names = new HashSet<String>();
		for(String s : filenameList) names.add(s);
		return names;
	}

	public Map<Integer, Integer> getFoldsMapByNum(int num){
		Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
		for(int i = 0; i < getSize(); i++){
			foldsMap.put(i, i%num);
		}
		return foldsMap;
	}

	public void setFilenames(ArrayList<String> f){
		filenameList = f;
	}

	public Map<Integer, Integer> getFoldsMapByFile(){
		Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
		int foldNum = 0;
		Map<String, Integer> folds = new TreeMap<String, Integer>();
		for(int i = 0; i < getSize(); i++){
			String filename = getFilename(i);
			if(!folds.containsKey(filename)){
				folds.put(filename, foldNum++);
			}
			foldsMap.put(i, folds.get(filename));
		}
		return foldsMap;
	}

	/**
	 * Adds a new annotation. Primarily used by the prediction interface.
	 */
	public void addAnnotation(String name, ArrayList<String> annots){
		while (allAnnotations.containsKey(name))
			name = name + " (new one)";
		allAnnotations.put(name, annots);
	}

	@Override
	public int[] getAnnotationIndexArray(String[] targetAnnotationListArray) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getAnnotationIndexArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Object> fullIterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterator<Object> iterator(String subtype) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Object> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Iterator<?>> iterators() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	// wrap a list of unannotated plain-text instances as a DocumentList
	public SimpleDocumentList(List<String> instances)
	{
		text.addAll(instances);
	}
	public SimpleDocumentList(List<String> text, Map<String, ArrayList<String>> annotations){
		this(text);
		for(String ann : annotations.keySet()){
			addAnnotation(ann, annotations.get(ann));
		}
	}
	public SimpleDocumentList createFilteredDocumentList(SimpleDocumentList start, String annotation, String filterKeyword){
		SimpleDocumentList sdl = new SimpleDocumentList(new ArrayList<String>());
		sdl.filenameList = start.filenameList;
		sdl.labelArray = start.labelArray;
		sdl.currentAnnotation = start.currentAnnotation;
		sdl.textColumn = start.textColumn;
		for(String ann : start.allAnnotations.keySet()){
			sdl.allAnnotations.put(ann, new ArrayList<String>());
		}
		for(int i = 0; i < start.text.size(); i++){
			if(start.allAnnotations.get(annotation).get(i).equals(filterKeyword)){
				sdl.text.add(start.text.get(i));
				for(String ann : start.allAnnotations.keySet()){
					sdl.allAnnotations.get(ann).add(start.allAnnotations.get(ann).get(i));
				}
			}
		}
		return sdl;
	}
	// wrap a single unannotated plain-text instance as a DocumentList
	public SimpleDocumentList(String instance)
	{
		text.add(instance);
	}
	
	
	public String toCSVString(){
		StringBuilder header = new StringBuilder();
		for(String s : allAnnotations.keySet()){
			header.append(s+",");
		}
		if(!textColumn.equals("[No Text]")){
			header.append("text");
		}
		StringBuilder body = new StringBuilder();
		for(int i = 0 ; i < text.size(); i++){
			for(String s : allAnnotations.keySet()){
				body.append(allAnnotations.get(s).get(i)+",");
			}
			if(!textColumn.equals("[No Text]")){
				body.append(text.get(i));
			}
			body.append("\n");
		}
		return header.toString()+"\n"+body.toString();
	}
	*/

}
