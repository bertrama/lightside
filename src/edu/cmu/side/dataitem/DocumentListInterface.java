package edu.cmu.side.dataitem;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface DocumentListInterface {

	public abstract int getSize();

	public abstract HashMap<String, ArrayList<String>> allAnnotations();

	public abstract ArrayList<String> getAnnotationArray(String name);

	public abstract Iterator<Object> fullIterator();

	public abstract HashMap<String, Iterator<?>> iterators();

	public abstract Iterator<Object> iterator(String subtype);

	public abstract Iterator<Object> iterator();

	public abstract int[] getAnnotationIndexArray(
			String[] targetAnnotationListArray);

	public abstract int[] getAnnotationIndexArray();

	public abstract ArrayList<String> getAnnotationArray();

	public abstract List<String> getCoveredTextList();

	public abstract void setCurrentAnnotation(String annot);

	public abstract String getCurrentAnnotation();
	
	public abstract Set<String> getFilenames();
	
	public abstract String getTextColumn();
}