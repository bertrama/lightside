package edu.cmu.side.dataitem;

import org.w3c.dom.Element;
import edu.cmu.side.uima.DocumentListInterface;

public interface TrainingResultInterface {

	public abstract String getSubtypeName();

	public abstract void fromXML(Element root) throws Exception;

	public abstract String toXML();

	public abstract DocumentListInterface getDocumentList();

}