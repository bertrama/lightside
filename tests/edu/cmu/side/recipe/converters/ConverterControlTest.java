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

public class ConverterControlTest
{
	
	Recipe predictRecipe;
	Recipe wholeRecipe;
	String delim = File.pathSeparator;
	File xmlPredictFile = new File("testData"+delim+"test.predict.xml");
	File xmlWholeModelFile = new File("testData"+delim+"test.model.side.xml");
	
	@Before
	public void setUp(){
		
		File serializedWholeModelFile = new File("testData"+delim+"test.model.side.ser");
		File serializedPredictFile = new File("testData"+delim+"test.predict.ser");
		
		
		try {
			
			
			FileInputStream in = new FileInputStream(serializedWholeModelFile);
			ObjectInputStream stream = new ObjectInputStream(in);
			
			wholeRecipe = (Recipe)stream.readObject();
			stream.close();
			
			in = new FileInputStream(serializedPredictFile);
			stream = new ObjectInputStream(in);
			predictRecipe = (Recipe)stream.readObject();
			stream.close();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//This is an abnormally long test. We can speed it up by switching it to writing to a file.
	
	
	@Test
	public void testPredictionRecipeXML()
	{
		ConverterControl.writeToXML("test_prediction_foobarblah.xml", predictRecipe);
		Recipe r = ConverterControl.readFromXML("test_prediction_foobarblah.xml");
		assertTrue(r.equals(predictRecipe));
	}
	@Test
	public void testWholeRecipeXML()
	{
		ConverterControl.writeToXML("test_whole_foobarblah.xml", wholeRecipe);
		Recipe r = ConverterControl.readFromXML("test_whole_foobarblah.xml");
		assertTrue(r.equals(wholeRecipe));
	}
	
}