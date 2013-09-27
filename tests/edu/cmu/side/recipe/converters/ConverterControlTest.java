package edu.cmu.side.recipe.converters;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import com.thoughtworks.xstream.XStream;

import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;

public class ConverterControlTest{
	Recipe recipe;
	@Before
	public void setUp(){
		String delim = System.getProperty("file.separator");
		File file = new File("testData"+delim+"test.model.side");

		try {
			FileInputStream in = new FileInputStream(file);
			ObjectInputStream stream = new ObjectInputStream(in);
			recipe = (Recipe)stream.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testConverterControlWriter(){
		ConverterControl.writeToXML("Blahbla.xml", recipe);
		Recipe r = ConverterControl.readFromXML("Blahbla.xml");
		assertTrue(r.getFeatureTable().equals(recipe.getFeatureTable()));
		assertTrue(r.equals(recipe));
	}
	
	
	private static File createFile(String name){
		File file = new File(name);
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return file;
	}
	
}