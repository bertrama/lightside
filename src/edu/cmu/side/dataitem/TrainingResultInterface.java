package edu.cmu.side.dataitem;

import org.w3c.dom.Element;


public interface TrainingResultInterface {

	public abstract String getSubtypeName();

	public abstract DocumentListInterface getDocumentList();

	public String getSummary();
}