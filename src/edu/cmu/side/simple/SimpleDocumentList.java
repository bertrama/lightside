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

	ArrayList<String> text = new ArrayList<String>();
	HashMap<String, ArrayList<String>> allAnnotations = new HashMap<String, ArrayList<String>>();
	String currentAnnotation; String textColumn;
	ArrayList<String> filenameList = new ArrayList<String>();
	String[] labelArray;
	
	/**
	 * wrap a list of unannotated plain-text instances as a DocumentList
	 */
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

	/**
	 * wrap a single unannotated plain-text instance as a DocumentList
	 */
	public SimpleDocumentList(String instance)
	{
		text.add(instance);
	}
	
	public SimpleDocumentList(Set<String> filenames, String textCol){
		double time1 = System.currentTimeMillis();
		BufferedReader in;
		textColumn = textCol;
		currentAnnotation = null;
		for(String filename : filenames){
			try{
				File f = new File(filename);
				if(!f.exists()){
					f = new File(SimpleWorkbench.dataFolder.getAbsolutePath(), filename.substring(Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"))+1));
				}
				in = new BufferedReader(new FileReader(f));
				StringBuilder sb = new StringBuilder();
				String line;
				while((line = in.readLine()) != null){
					sb.append(line + "\n");
				}
				String out = sb.toString().replaceAll("[^\r\n\\p{ASCII}]", "");
				CSVReader csvReader = new CSVReader(new StringReader(out), 0);
				int textColumnIndex = -1;
				String[] headers = csvReader.readNextMeaningful();
				for(int i = 0; i < headers.length; i++){
					String clean = headers[i].replaceAll("\"", "").trim();
					headers[i] = clean;
					if(clean.equals(textColumn)){
						textColumnIndex = i;
					}
				}
				for(String annotation : headers){
					if(!annotation.equals(textColumn) && !allAnnotations.containsKey(annotation)){
						allAnnotations.put(annotation, new ArrayList<String>());
					}
				}
				String[] instance;
				int count = 0;
				int lineID = 0;
				while((instance = csvReader.readNextMeaningful()) != null){
					for(int i = 0; i < instance.length && i < headers.length; i++){
						String value = instance[i].replaceAll("\"", "").trim();
						if(i==textColumnIndex){
							text.add(value);
						}else{
							try{
								allAnnotations.get(headers[i]).add(value);															
							}catch(Exception e){
								System.out.println(i + ", " + lineID);
								e.printStackTrace();
							}
						}
					}
					filenameList.add(filename);
					lineID++;
				}
				csvReader.close();
			}catch(Exception e){
				AlertDialog.show("Error!", "Failed to load CSV into memory.", null);
				e.printStackTrace();
			}
		}
		double time2 = System.currentTimeMillis();
	}
	
	public SimpleDocumentList(Set<String> filenames, String currentAnnot, String textCol){
		this(filenames, textCol);
		setCurrentAnnotation(currentAnnot);		
	}
	
	@Override
	public HashMap<String, ArrayList<String>> allAnnotations() {
		return allAnnotations;
	}

	@Override
	public Iterator<Object> fullIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getAnnotationArray(String name) {
		return allAnnotations.get(name);
	}

	@Override
	public ArrayList<String> getAnnotationArray() {
//		System.out.println(currentAnnotation + ", " + allAnnotations.containsKey(currentAnnotation) + " SDL156");
		return allAnnotations.get(currentAnnotation);
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
	public List<String> getCoveredTextList() {
		return text;
	}

	@Override
	public String[] getLabelArray() {
		if(labelArray == null){
			ArrayList<String> labels = getAnnotationArray();
			Set<String> labelSet = new TreeSet<String>();
			if(labels != null)
			{
				for(String s : labels)
				{
					labelSet.add(s);
				}
			}
			labelArray = labelSet.toArray(new String[0]);			
		}
		return labelArray;
	}
	
	/**
	 * Used for predicting labels on unannotated data.
	 * @param labels
	 */
	public void setExternalLabelArray(String[] labels){
		labelArray = labels;
	}

	@Override
	public int getSize() {
		if(getAnnotationArray() != null){
			return getAnnotationArray().size();			
		}else if(text != null){
			return text.size();
		}else return 0;
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

	public void setCurrentAnnotation(String annot){
		if(annot != currentAnnotation && allAnnotations.containsKey(annot)){
			labelArray = null;
			currentAnnotation = annot;
		}
	}
	
	public String getCurrentAnnotation(){
		return currentAnnotation;
	}
	
	public String getTextColumn(){
		return textColumn;
	}
	
	public void setTextColumn(String name){
		allAnnotations.put(textColumn, text);
		if(name.equals("[No Text]")){
			textColumn = null;
			text = null;
		}else{
			textColumn = name;
			text = allAnnotations.get(name);
		}
	}
	
	/**
	 * Used for cross-validating by file.
	 * @param docIndex
	 * @return
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
		allAnnotations.put(name, annots);
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
}
