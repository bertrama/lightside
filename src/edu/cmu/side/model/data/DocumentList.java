package edu.cmu.side.model.data;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.yerihyo.yeritools.csv.CSVReader;
import com.yerihyo.yeritools.swing.AlertDialog;

import edu.cmu.side.Workbench;

public class DocumentList implements Serializable{
	private static final long serialVersionUID = -5433699826930815886L;

	ArrayList<String> filenameList = new ArrayList<String>();
	Map<String, List<String>> allAnnotations = new HashMap<String, List<String>>();
	Map<String, List<String>> textColumns = new HashMap<String, List<String>>();
	String currentAnnotation; 
	String currentDomain;
	String name = "Default documents";
	
	String[] annotationNames = null;
	String[] labelArray = null;

	// wrap a list of unannotated plain-text instances as a DocumentList
	public DocumentList(List<String> instances)
	{
		addAnnotation("text", instances);
		setTextColumn("text", true);
	}
	
	public void setName(String n){
		name = n;
	}
	
	public String getName(){
		return name;
	}

	public DocumentList(List<String> text, Map<String, ArrayList<String>> annotations){
		this(text);
		for(String ann : annotations.keySet()){
			addAnnotation(ann, annotations.get(ann));
		}
	}
	// wrap a single unannotated plain-text instance as a DocumentList
	public DocumentList(String instance)
	{
		List<String> instances = new ArrayList<String>();
		instances.add(instance);
		addAnnotation("text", instances);
		setTextColumn("text", true);
	}

	public DocumentList(Set<String> filenames, String currentAnnot, String textCol){
		this(filenames);
		setTextColumn(textCol, true);
		setCurrentAnnotation(currentAnnot);
		getLabelArray();
	}

	public DocumentList(Set<String> filenames, String textCol){
		this(filenames);
		setTextColumn(textCol, true);         
	}

	public DocumentList(Set<String> filenames){
		CSVReader in;
		currentAnnotation = null;
		int totalLines = 0;
		String localName = "";
		for(String filename : filenames){
			int ending = filename.lastIndexOf(".csv");
			localName += filename.substring(filename.lastIndexOf("/")+1, ending==-1?filename.length():ending) + " ";
			ArrayList<Integer> blanks = new ArrayList<Integer>();
			ArrayList<Integer> extras = new ArrayList<Integer>();
			int lineID = 0;

			try{
				File f = new File(filename);
				if(!f.exists())
					f = new File(Workbench.dataFolder.getAbsolutePath(), filename.substring(Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"))+1));
				in = new CSVReader(new FileReader(f));
				String[] headers = in.readNextMeaningful();
				List<Integer> annotationColumns = new ArrayList<Integer>();
				for(int i = 0; i < headers.length; i++){
					headers[i] = headers[i].trim();
					if(headers[i].length()>0){
						annotationColumns.add(i);
					}
				}

				for(String annotation : headers){
					if(annotation.length() > 0 && !allAnnotations.containsKey(annotation)){
						allAnnotations.put(annotation, new ArrayList<String>());						
					}
				}

				String[] line;

				while((line = in.readNextMeaningful()) != null){
					String[] instance = new String[line.length];
					for(int i = 0; i < line.length; i++){
						instance[i] = line[i].replaceAll("[^\r\n\\p{ASCII}]", "");
					}
					for(int i = 0; i < instance.length; i++){
						String value = instance[i];
						if(annotationColumns.contains(i)){
							if(value.length()>0){
								allAnnotations.get(headers[i]).add(value);
							}else{
								allAnnotations.get(headers[i]).add("?");
								blanks.add(lineID);
							}
						}else{
							extras.add(lineID);
						}
					}
					filenameList.add(filename);
					lineID++;
				}
			}catch(Exception e){
				AlertDialog.show("Error!", "Failed to load CSV into memory.", null);
				e.printStackTrace();
			}

			totalLines += lineID;
		}
		localName.trim();
		setName(localName);
	}

	public Map<String, List<String>> allAnnotations() {
		return allAnnotations;
	}

	/**
	 * Adds a new annotation. Primarily used by the prediction interface.
	 */
	public void addAnnotation(String name, List<String> annots){
		while (allAnnotations.containsKey(name))
			name = name + " (new one)";
		allAnnotations.put(name, annots);
	}

	public List<String> getAnnotationArray(String name) {
		return allAnnotations.get(name);
	}

	public List<String> getAnnotationArray() {
		if (currentAnnotation == null) return null;
		return allAnnotations.get(currentAnnotation);
	}

	public Map<String, List<String>> getCoveredTextList() {
		return textColumns;
	}

	public String getCurrentAnnotation(){
		return currentAnnotation;
	}

	public Set<String> getTextColumns(){
		return textColumns.keySet();
	}
	
	public void setDomainColumn(String s){
		currentDomain = s;
	}

	public String getInstanceDomain(int indx){
		return allAnnotations.get(currentDomain).get(indx);
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

	public String[] getLabelArray() {
		if(labelArray == null){
			List<String> labels = getAnnotationArray();
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
	 * For predictions on unlabled data, it's neccessary to know what's possible
	 */
	public void setLabelArray(String[] labels)
	{
		labelArray = labels;
	}
	
	public Set<String> getPossibleAnn(String name) {
		List<String> labels = getAnnotationArray(name);
		Set<String> labelSet = new TreeSet<String>();
		if(labels != null) 
			for(String s : labels)
				labelSet.add(s);
		return labelSet;
	}
	
	public String[] getAnnotationNames(){
		return allAnnotations.keySet().toArray(new String[0]);
	}

	public int getSize() {
		if(textColumns.keySet().size() > 0){
			for(String key : textColumns.keySet()){
				if(textColumns.get(key).size() > 0){
					return textColumns.get(key).size();
				}
			}
		}
		if(allAnnotations != null){
			for (String key : allAnnotations.keySet() )
				if (allAnnotations.get(key).size() > 0)
					return allAnnotations.get(key).size();
		}
		return 0;
	}

	public void setCurrentAnnotation(String annot){
		if (!allAnnotations.containsKey(annot))
			throw new IllegalStateException("Can't find the label column named " + annot + " in provided file");
		labelArray = null;
		currentAnnotation = annot;
		getLabelArray();
	}

	public void setTextColumn(String name, boolean isText){
		if(isText){
			if (!allAnnotations.containsKey(name)){
				throw new IllegalStateException("Can't find the text column named " + name + " in provided file");
			}else{
				textColumns.put(name, allAnnotations.get(name));
				allAnnotations.remove(name);
			}			
		}else{
			if(textColumns.containsKey(name)){
				allAnnotations.put(name, textColumns.get(name));
				textColumns.remove(name);
			}			
		}
	}

	public void setFilenames(ArrayList<String> f){
		filenameList = f;
	}
	
	//	        public String toCSVString(){
	//	                StringBuilder header = new StringBuilder();
	//	                for(String s : allAnnotations.keySet()){
	//	                        header.append(s+",");
	//	                }
	//	                if(!textColumn.equals("[No Text]")){
	//	                        header.append("text");
	//	                }
	//	                StringBuilder body = new StringBuilder();
	//	                for(int i = 0 ; i < text.size(); i++){
	//	                        for(String s : allAnnotations.keySet()){
	//	                                body.append(allAnnotations.get(s).get(i)+",");
	//	                        }
	//	                        if(!textColumn.equals("[No Text]")){
	//	                                body.append(text.get(i));
	//	                        }
	//	                        body.append("\n");
	//	                }
	//	                return header.toString()+"\n"+body.toString();
	//	        }
	//        */

}