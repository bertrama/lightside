package edu.cmu.side.simple;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.yerihyo.yeritools.csv.CSVReader;
import com.yerihyo.yeritools.swing.AlertDialog;

import edu.cmu.side.uima.DocumentListInterface;
import edu.cmu.side.uima.UIMAToolkit.Datatype;

public class SimpleDocumentList implements DocumentListInterface{

	ArrayList<String> text = new ArrayList<String>();
	HashMap<String, ArrayList<String>> allAnnotations = new HashMap<String, ArrayList<String>>();
	String currentAnnotation;
	ArrayList<String> filenameList = new ArrayList<String>();
	
	public SimpleDocumentList(Set<String> filenames, String currentAnnot, String textColumn){
		double time1 = System.currentTimeMillis();
		BufferedReader in;
		
		for(String filename : filenames){
			try{
				in = new BufferedReader(new FileReader(filename));
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
				while((instance = csvReader.readNextMeaningful()) != null){
					for(int i = 0; i < instance.length; i++){
						String value = instance[i].replaceAll("\"", "").trim();
						if(i==textColumnIndex){
							text.add(value);
						}else{
							try{
								allAnnotations.get(headers[i]).add(value);															
							}catch(Exception e){
								System.out.println(headers[i]);
								e.printStackTrace();
							}
						}
					}
					filenameList.add(filename);
				}
				csvReader.close();
			}catch(Exception e){
				AlertDialog.show("Error!", "Failed to load CSV into memory.", null);
				e.printStackTrace();
			}
		}
		currentAnnotation = currentAnnot;
		double time2 = System.currentTimeMillis();
		System.out.println("DocumentList created in " + (time2-time1) + " milliseconds.");
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
	public Datatype getInferredDatatype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getLabelArray() {
		ArrayList<String> labels = getAnnotationArray();
		Set<String> labelArray = new TreeSet<String>();
		for(String s : labels){
			labelArray.add(s);
		}
		return labelArray.toArray(new String[0]);
	}

	@Override
	public int getSize() {
		return getAnnotationArray().size();
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
		if(allAnnotations.containsKey(annot)){
			currentAnnotation = annot;
		}
	}
	
	public String getCurrentAnnotation(){
		return currentAnnotation;
	}
	
	/**
	 * Used for cross-validating by file.
	 * @param docIndex
	 * @return
	 */
	public String getFilename(int docIndex){
		return filenameList.get(docIndex);
	}
}
