package edu.cmu.side.uima;

import java.io.File;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class CSVFileReader extends CollectionReader_ImplBase {

	public static final String PARAM_INPUTFILE = "InputFILE";
	private File file;
	private boolean hasRead;
	
	public void initialize() throws ResourceInitializationException{
		hasRead = false;
		file = new File(((String) getConfigParameterValue(PARAM_INPUTFILE)).trim());
		File directory = file.getParentFile();
		
		// if input directory does not exist or is not a directory, throw exception
	    if (!directory.exists()) {
	    	directory.mkdirs();
	    }
	}

	
	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		UIMAToolkit.readCSVIntoCas(aCAS, file, "text", hasRead = true, true);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(hasNext()?0:1, 1, Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext(){
		return !hasRead;
	}

}
