package edu.cmu.side.uima;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class CSVCollectionReader extends CollectionReader_ImplBase {

	public static final String PARAM_INPUTDIR = "InputDirectory";
	private ArrayList<File> fileList;
	private int currentIndex = 0;
	
	public void initialize() throws ResourceInitializationException{
		File directory = new File(((String) getConfigParameterValue(PARAM_INPUTDIR)).trim());
		
		// if input directory does not exist or is not a directory, throw exception
	    if (!directory.exists() || !directory.isDirectory()) {
	      throw new ResourceInitializationException(ResourceConfigurationException.DIRECTORY_NOT_FOUND,
	              new Object[] { PARAM_INPUTDIR, this.getMetaData().getName(), directory.getPath() });
	    }

	    // get list of files (not subdirectories) in the specified directory
	    fileList = new ArrayList<File>();
	    File[] files = directory.listFiles();
	    for (int i = 0; i < files.length; i++) {
	      if (!files[i].isDirectory()) {
	    	  fileList.add(files[i]);
	      }
	    }
	}

	
	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
	    // open input stream to xmiFile
	    File file = (File) fileList.get(currentIndex++);
	    UIMAToolkit.readCSVIntoCas(aCAS, file, "text", currentIndex < fileList.size(), true);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, fileList.size(), Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return currentIndex < fileList.size();
	}

}
