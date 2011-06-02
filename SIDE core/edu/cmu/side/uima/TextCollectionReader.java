package edu.cmu.side.uima;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class TextCollectionReader extends CollectionReader_ImplBase {

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
		JCas jcas;
	    try {
	      jcas = aCAS.getJCas();
	    } catch (CASException e) {
	      throw new CollectionException(e);
	    }

	    // open input stream to xmiFile
	    File file = (File) fileList.get(currentIndex++);
	    
	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    StringBuilder builder = new StringBuilder();
	    
	    char[] buffer = new char[1024];
	    for(int len=0; (len = reader.read(buffer))>0; ){
	    	
	    	builder.append(buffer,0,len);
	    }
	    reader.close();
	    
	    jcas.setDocumentText(builder.toString());
	    
	    // Also store location of source document in CAS. This information is critical
	    // if CAS Consumers will need to know where the original document contents are located.
	    // For example, the Semantic Search CAS Indexer writes this information into the
	    // search index that it creates, which allows applications that use the search index to
	    // locate the documents that satisfy their semantic queries.
	    SourceDocumentInformation srcDocInfo = new SourceDocumentInformation(jcas);
	    srcDocInfo.setUri(file.getAbsoluteFile().toURI().toURL().toString());
	    srcDocInfo.setOffsetInSource(0);
	    srcDocInfo.setDocumentSize((int) file.length());
	    srcDocInfo.setLastSegment(currentIndex == fileList.size());
	    srcDocInfo.addToIndexes();
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
