package edu.cmu.side.recipe.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.thoughtworks.xstream.XStream;

import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.DocumentList;

public class ConverterControl {
	public static DocumentList docList;
	//whatever else should get repeated here
	private ConverterControl(){
		
	}
	
	public static void writeToXML(String fileName, Recipe recipe){
		clearVariables();
		File file = createFile(fileName);
		XStream stream = new XStream();
		try {
			FileOutputStream out = new FileOutputStream(file);
			PrintStream printStream = new PrintStream(out);
			printStream.print(stream.toXML(recipe));
			printStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Recipe readFromXML(String fileName){
		clearVariables();
		File file = new File(fileName);
		if(!file.exists()) return null;
		XStream stream = new XStream();
		return (Recipe) stream.fromXML(file);
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
	
	private static void clearVariables(){
		docList = null;
		
	}
}
