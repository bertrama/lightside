package edu.cmu.side.model.data;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.yerihyo.yeritools.csv.CSVReader;
import com.yerihyo.yeritools.swing.AlertDialog;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.Feature.Type;

public class DocumentList implements Serializable
{
	private static final long serialVersionUID = -5433699826930815886L;

	List<String> filenameList = new ArrayList<String>();
	Map<String, List<String>> allAnnotations = new TreeMap<String, List<String>>();
	Map<String, List<String>> textColumns = new TreeMap<String, List<String>>();
	boolean differentiateTextColumns = false;
	String currentAnnotation; 
//	Feature.Type type;
	String name = "Documents";
	
	String emptyAnnotationString = "";
	
	String[] annotationNames = null;
	String[] labelArray = null;

	// wrap a list of unannotated plain-text instances as a DocumentList
	public DocumentList(List<String> instances)
	{
		addAnnotation("text", instances);
		setTextColumn("text", true);
	}
	

//	public void setClassValueType(Feature.Type t)
//	{
////		System.out.println("DL 48: setting class value type for "+currentAnnotation+" to "+t);
//		if(t != type)
//		{
//			new Exception("tracing setClassValueType").printStackTrace(System.out);
//			type = t;
//			labelArray = null;
//			getLabelArray();
//		}
//	}
	
	public Feature.Type getValueType(String label)
	{
		if(label == null)
		{
			return Type.NOMINAL;
		}
		
		Feature.Type localType = guessValueType(label);
		return localType;
	}
	
	/*
	public Feature.Type getValueType(String label)
	{
		if(label == null)
		{
			return Type.NOMINAL;
		}
		
		if(type != null && label.equals(currentAnnotation))
		{
			return type;
		}
		else
		{
			Feature.Type localType = guessValueType(label);
			if(label.equals(currentAnnotation))
			{
				setClassValueType(localType);
			}
			return localType;
		}
	}*/

	/**
	 * Uses a sort of shoddy and roundabout catch-exception way of figuring out
	 * if the data type is nominal or numeric.
	 * 
	 * @return
	 */
	public Feature.Type guessValueType(String label)
	{
//		System.out.println("DL 87: guessing type for "+label);
		
		Feature.Type localType;
		for (String s : getPossibleAnn(label))
		{
			try
			{
				Double.parseDouble(s);
			}
			catch (Exception e)
			{
				localType = Feature.Type.NOMINAL;
//				if (label.equals(currentAnnotation))
//				{
//					setClassValueType(localType);
//				}
				return localType;
			}
		}
		localType = Feature.Type.NUMERIC;
//		if (label.equals(currentAnnotation))
//		{
//			setClassValueType(localType);
//		}
		return localType;

	}
	
	public void setName(String n){
		name = n;
	}
	
	public String getName(){
		return name;
	}
	
	public DocumentList(List<String> filenames, Map<String, List<String>> texts, Map<String, List<String>> annotations, String currentAnnot){
		filenameList = filenames;
		textColumns = texts;
		allAnnotations = annotations;
		currentAnnotation = currentAnnot;
	}

	public DocumentList(List<String> text, Map<String, List<String>> annotations){
		this(text);
		for(String ann : annotations.keySet()){
			addAnnotation(ann, annotations.get(ann));
			filenameList.add("Document");
		}
	}
	// wrap a single unannotated plain-text instance as a DocumentList
	public DocumentList(String instance)
	{
		List<String> instances = new ArrayList<String>();
		instances.add(instance);
		addAnnotation("text", instances);
		setTextColumn("text", true);
		filenameList.add("Document");
	}

	public DocumentList(Set<String> filenames, String currentAnnot, String textCol){
		this(filenames);
		setTextColumn(textCol, true);
		setCurrentAnnotation(currentAnnot);
		getLabelArray(currentAnnot, guessValueType(currentAnnot));
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
								allAnnotations.get(headers[i]).add(emptyAnnotationString);
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

	private static String[] classGuesses = {"class", "label", "value", "annotation", "score"};
	private static String[] textGuesses = {"text", "sentence", "turn", "posting", "instance", "essay"};

	public String guessTextAndAnnotationColumns()
	{
		if(currentAnnotation == null)
		{
			String className = guessAnnotation(classGuesses);
			if(className != null)
				setCurrentAnnotation(className);
		}
		
		if(textColumns.isEmpty())
		{
			String textName = guessAnnotation(textGuesses);
			if(textName != null)
				setTextColumn(textName, true);
		}
		
		if(currentAnnotation == null || textColumns.isEmpty())
			for (String s : this.getAnnotationNames())
			{
				Set<String> values = new TreeSet<String>();
				double length = 0;
				for (String t : this.getAnnotationArray(s))
				{
					values.add(t);
					length += t.length();
				}
				length = length/getSize();
				
				if(currentAnnotation == null && values.size() < (this.getSize() / 10.0))
				{
					this.setCurrentAnnotation(s);
				}
				
				if (getTextColumns().isEmpty() && length > 10 && values.size() >= (this.getSize() / 2.0))
				{
					this.setTextColumn(s, true);
				}
			}
		System.out.println("guessing "+currentAnnotation);
		return currentAnnotation;
	}


	/**
	 * @param guesses
	 */
	protected String guessAnnotation(String[] guesses)
	{
		for (String guess : guesses)
			for (String a : allAnnotations.keySet())
				if (a.equalsIgnoreCase(guess)) 
					return a;

		return null;
	}

	/**
	 * Adds a new annotation. Primarily used by the prediction interface.
	 */
	public void addAnnotation(String name, List<String> annots){
		while (allAnnotations.containsKey(name))
			name = name + " (new)";
		allAnnotations.put(name, annots);
	}

	public List<String> getAnnotationArray(String name) 
	{
		if(name != null)
			return allAnnotations.get(name);
		return null;
	}

//	public List<String> getAnnotationArray() {
//		if (currentAnnotation == null) return null;
//		return allAnnotations.get(currentAnnotation);
//	}

	public Map<String, List<String>> getCoveredTextList() {
		return textColumns;
	}
	
	public String getPrintableTextAt(int index){
		boolean labels = getTextColumns().size()>1;
		StringBuilder sb = new StringBuilder();
		for(String key : getTextColumns()){
			if(labels){
				sb.append(key + ":\n");
			}
			sb.append(getCoveredTextList().get(key).get(index));
			if(labels){
				sb.append("\n");
			}
		}
		return sb.toString();
	}

//use FeatureTable.getAnnotation or ExtractFeatureControl.getTargetAnnotation instead
//	public String getCurrentAnnotation()
//	{
//		return currentAnnotation;
//	}

	public Set<String> getTextColumns()
	{
		return textColumns.keySet();
	}
	
	
	
	public boolean textColumnsAreDifferentiated()
	{
		return differentiateTextColumns;
	}


	public void setDifferentiateTextColumns(boolean textColumnsShouldBeDifferentiated)
	{
		this.differentiateTextColumns = textColumnsShouldBeDifferentiated;
	}


	public String getTextFeatureName(String baseName, String column)
	{
		if(differentiateTextColumns)
		{
			return column+":"+baseName;
		}
		else return baseName;
	}
	
	/**
	 * Used for cross-validating by file.
	 */
	public String getFilename(int docIndex){
		return filenameList.get(docIndex);
	}

	public List<String> getFilenameList(){
		return filenameList;
	}
	
	public Set<String> getFilenames(){
		Set<String> names = new HashSet<String>();
		for(String s : filenameList) names.add(s);
		return names;
	}

	
	public String[] getLabelArray(String column, Type t)
	{

		if (!column.equals(currentAnnotation) || labelArray == null)
		{
			Set<String> labelSet = new TreeSet<String>();
			switch (t)
			{
				case NOMINAL:
				case BOOLEAN:
					List<String> labels = getAnnotationArray(column);
					if (labels != null)
					{
						for (String s : labels)
						{
							labelSet.add(s);
						}
					}
					break;
				case NUMERIC:
					for (int i = 0; i < 5; i++)
					{
						labelSet.add("Q" + (i + 1));
					}
					break;
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

	public void setCurrentAnnotation(String annot)
	{
		if(annot == currentAnnotation)  //TODO: make sure this shortcut doesn't break anything expecting labelArray to be populated
			return;
		
		if (!allAnnotations.containsKey(annot))
			throw new IllegalStateException("Can't find the label column named " + annot + " in provided file");
		
		if(currentAnnotation == null || !currentAnnotation.equals(annot) )//|| type != t)
		{
			labelArray = null;
			currentAnnotation = annot;
			getLabelArray(annot, guessValueType(annot));
		}
	}
	
	public void setCurrentAnnotation(String annot, Type t)
	{
		if(annot == currentAnnotation)  //TODO: make sure this shortcut doesn't break anything expecting labelArray to be populated
			return;
		
		if (!allAnnotations.containsKey(annot))
			throw new IllegalStateException("Can't find the label column named " + annot + " in provided file");
		
		if(currentAnnotation == null || !currentAnnotation.equals(annot) )//|| type != t)
		{
			labelArray = null;
//			type = t;
			currentAnnotation = annot;
			getLabelArray(annot, t);
		}
	}

	//TODO: use this in BuildModel and Chef and anywhere else we're translating a recipe to a new document list
	public void setTextColumns(Set<String> columns)
	{
		for(String s : getTextColumns())
		{
			setTextColumn(s, false);
		}
		
		for(String s : columns)
		{
			setTextColumn(s, true);
		}
	}
	
	public void setTextColumn(String name, boolean isText){
		if (isText)
		{
			if (textColumns.containsKey(name))
			{
				return;
			}
			else if (!allAnnotations.containsKey(name))
			{
				throw new IllegalStateException("Can't find the text column named " + name + " in provided file");
			}
			else
			{
				textColumns.put(name, allAnnotations.get(name));
				allAnnotations.remove(name);
			}
		}
		else
		{
			if (textColumns.containsKey(name))
			{
				allAnnotations.put(name, textColumns.get(name));
				textColumns.remove(name);
			}
		}
	}

	public void setFilenames(List<String> f){
		filenameList = f;
	
	}
}