package edu.cmu.side.recipe.converters;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.side.model.Recipe;

public class ConverterControlTest{
	Recipe recipe;
	@Before
	public void setUp(){
		File file = new File("bayes1.model.side");
		
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
	public void testSomethingForNow(){
		ConverterControl.writeToXML("Bananas.side", recipe);
	}
}