package edu.cmu.side.recipe.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.thoughtworks.xstream.XStream;

import edu.cmu.side.model.Recipe;

public class ConverterControl {
	private ConverterControl(){
		
	}
	
	public static void writeToXML(String fileName, Recipe recipe){
		File file = createFile(fileName);
		writeToXML(file, recipe);
	}
	
	public static void writeToXML(File file, Recipe recipe){
		XStream stream = new XStream();
		stream.registerConverter(new FeatureTableConverter());
		try {
			FileWriter writer = new FileWriter(file);
			stream.toXML(recipe, writer);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Writing file complete");
	}
	
	public static String getXMLString(Recipe recipe)
	{
		XStream stream = new XStream();
		stream.registerConverter(new FeatureTableConverter());
		
		return stream.toXML(recipe);
	}

	public static Recipe getRecipeFromXMLString(String recipeXML)
	{
		XStream stream = new XStream();
		stream.registerConverter(new FeatureTableConverter());
		
		return (Recipe) stream.fromXML(recipeXML);
		
	}
	
	public static Recipe readFromXML(String fileName){
		File file = createFile(fileName);
		
		return readFromXML(file);
	}
	public static Recipe readFromXML(File file){
		XStream stream = new XStream();
		stream.registerConverter(new FeatureTableConverter());
		Recipe r =(Recipe) stream.fromXML(file);
		return r;
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
	//Let's hook these up
	private void zipUp(String fileName){
		long start = System.currentTimeMillis();
		byte[] buff = new byte[1000];
		FileOutputStream stream;
		try {
			stream = new FileOutputStream("testest.zip");
			ZipOutputStream zipper = new ZipOutputStream(stream);
			ZipEntry entr = new ZipEntry("testest.xml");
			zipper.putNextEntry(entr);
			FileInputStream in = new FileInputStream("testest.xml");
			int len;
			while((len=in.read(buff))>0){
				zipper.write(buff, 0, len);
			}
			in.close();
			zipper.closeEntry();
			zipper.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("ziptime : " + (end-start));
	}
	private void unZip() throws IOException{
		long start = System.currentTimeMillis();
		byte[] buffer = new byte[1000];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(new File("testest.zip")));
		ZipEntry entr = zis.getNextEntry();
		File newFile = new File(entr.getName());
		if(!newFile.exists()){
			newFile.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(newFile);             
		 
        int len;
        while ((len = zis.read(buffer)) > 0) {
   		fos.write(buffer, 0, len);
        }

        fos.close();   
        zis.closeEntry();
        zis.close();
        long end = System.currentTimeMillis();
		System.out.println("unziptime : " + (end-start));
	}

	
}
