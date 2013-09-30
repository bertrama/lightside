package edu.cmu.side.recipe.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sun.tools.hat.internal.parser.Reader;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.Xpp3DomDriver;
import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;

import edu.cmu.side.model.Recipe;

public class ConverterControl
{
	private static XStream xStream;
	
	private ConverterControl()
	{

	}


	public static Recipe readFromXML(String fileName) throws IOException
	{
		File file = createFile(fileName);

		return readFromXML(file);
	}

	public static Recipe readFromXML(File file)
	{
		XStream stream = getXStream();
		Recipe r =(Recipe) stream.fromXML(file);
        
		return r;
	}
	
	public static void writeToXML(String fileName, Recipe recipe) throws IOException
	{
		File file = createFile(fileName);
		writeToXML(file, recipe);
	}

	public static void writeToXML(File file, Recipe recipe) throws IOException
	{
		XStream stream = getXStream();
		FileWriter writer = new FileWriter(file);
		stream.toXML(recipe, writer);
		writer.close();
		System.out.println("Wrote XML recipe for "+recipe.getRecipeName()+" to "+file.getPath());
	}

	public static String getXMLString(Recipe recipe)
	{
		XStream stream = getXStream();

		return stream.toXML(recipe);
	}

	public static Recipe getRecipeFromXMLString(String recipeXML)
	{
		XStream stream = getXStream();

		return (Recipe) stream.fromXML(recipeXML);

	}

	public static Recipe getRecipeFromZippedXMLString(String zippedRecipeXML) throws IOException
	{
		InputStream stringIn = new ByteArrayInputStream(zippedRecipeXML.getBytes());
		
		Recipe recipe = streamInZippedXML(stringIn);
		
		stringIn.close();
		return recipe;
	}

	public static Recipe readFromZippedXML(File file) throws IOException
	{
		InputStream fileIn = new FileInputStream(file);
		
		Recipe recipe = streamInZippedXML(fileIn);
		
		fileIn.close();
		return recipe;
	}	
	
	public static void writeToZippedXML(File file, Recipe recipe) throws IOException
	{
		FileOutputStream fileOut = new FileOutputStream(file);
		
		streamOutZippedXML(recipe, fileOut);
		
		fileOut.close();
		System.out.println("Wrote zipped XML recipe for "+recipe.getRecipeName()+" to "+file.getPath());
	}

	public static String getZippedXMLString(Recipe recipe) throws IOException
	{
		ByteArrayOutputStream stringOut = new ByteArrayOutputStream();
		
		streamOutZippedXML(recipe, stringOut);
		String zippedString = stringOut.toString();
		
		stringOut.close();
		return zippedString;
	}


	private static File createFile(String name) throws IOException
	{
		File file = new File(name);
		if (!file.exists())
		{
			file.createNewFile();
		}
		return file;
	}
	
	protected static void streamOutZippedXML(Recipe r, OutputStream out) throws IOException
	{
		ZipOutputStream zipper = new ZipOutputStream(out);
		zipper.putNextEntry(new ZipEntry(r.getRecipeName()));
		XStream stream = getXStream();
		stream.toXML(r, zipper);
		zipper.closeEntry();
		zipper.close();
	}
	
	protected static Recipe streamInZippedXML(InputStream in) throws IOException
	{
		ZipInputStream unzipper = new ZipInputStream(in);
		ZipEntry entry = unzipper.getNextEntry();
		System.out.println("Getting Zipped "+entry.getName());
		
		XStream stream = getXStream();
		Recipe r = (Recipe) stream.fromXML(unzipper);
		unzipper.close();
		
		return r;
	}


	protected static XStream getXStream()
	{
		if(xStream == null)
		{
			xStream = new XStream();
			xStream.registerConverter(new FeatureTableConverter());
		}
		return xStream;
	}


	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		long start, end;
//		start = System.currentTimeMillis();
//		Recipe rs = (Recipe) new ObjectInputStream(new FileInputStream(new File("saved/essay_source.ser"))).readObject();
//		
//		end = System.currentTimeMillis();
//		System.out.println("loaded super large serialized recipe in "+(end - start)+"ms.");
		
		start = System.currentTimeMillis();
		Recipe rx = ConverterControl.readFromXML(new File("saved/movies.side.xml"));
		
		end = System.currentTimeMillis();
		System.out.println("loaded medium XML recipe in "+(end - start)+"ms.");
//		
//		start = System.currentTimeMillis();
//		Recipe rx = ConverterControl.readFromXML(new File("saved/essay_source.side.xml"));
//		
//		end = System.currentTimeMillis();
//		System.out.println("loaded super large plain XML in "+(end - start)+"ms.");
//		
//		start = System.currentTimeMillis();
//		Recipe rz = ConverterControl.readFromZippedXML(new File("saved/essay_source.side.xml.zip"));
//		
//		end = System.currentTimeMillis();
//		System.out.println("loaded super large zipped XML in "+(end - start)+"ms.");
//		System.out.println("recipes match? "+(rx.equals(rz)));
	}
}
