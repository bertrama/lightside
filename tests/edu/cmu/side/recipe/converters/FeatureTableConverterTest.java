package edu.cmu.side.recipe.converters;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;

public class FeatureTableConverterTest {
	Recipe recipe;
	String delim = System.getProperty("file.separator");
	File predictFile = new File("testData"+delim+"testPrediction.xml");
	@Before
	public void setUp(){
		
		File file = new File("testData"+delim+"test.model.side");
		
		
		try {
			FileInputStream in = new FileInputStream(file);
			ObjectInputStream stream = new ObjectInputStream(in);
			
			recipe = (Recipe)stream.readObject();
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
	public void testConvertExistant(){
		FeatureTable ftBefore = recipe.getFeatureTable();
		XStream streamer = new XStream();
		streamer.registerConverter(new FeatureTableConverter());
		String xml = streamer.toXML(ftBefore);
		Object afterObj = streamer.fromXML(xml);
		FeatureTable ftAfter = (FeatureTable) afterObj;
		assertTrue(ftBefore.equals(ftAfter));
	}
	@Test
	public void testConvertNull(){
		FeatureTable ftBefore = recipe.getFilteredTable();
		XStream streamer = new XStream();
		streamer.registerConverter(new FeatureTableConverter());
		String XML = streamer.toXML(ftBefore);
		Object afterObj = streamer.fromXML(XML);
		assertNull(afterObj);
		assertNull(ftBefore);
	}
	@Test
	public void testPredictOnlyParse(){
		XStream streamer = new XStream();
		streamer.registerConverter(new FeatureTableConverter());
		streamer.fromXML(predictFile);
	}
}
