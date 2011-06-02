package edu.cmu.side.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.text.PorterStemmer;

import edu.cmu.side.SIDEToolkit;

/**
 * General-purpose utility methods that are potentially useful to a wide variety of feature extraction plugins.
 * @author elijah
 *
 */
public class FeatureExtractorToolkit {

	/**
	 * Converts an entire document from a CharSequence into an organized two-dimensional array of tokens.
	 * @param document The original input document.
	 * @param stopwords True if stopwords are to be removed.
	 * @param stems True if stemming is to be applied.
	 * @return A two dimensional array with each sentence in the first dimension and each token in that sentence in the second dimension.
	 */
	public static String[][] getTokenArrayFromCharSequence(CharSequence document, boolean stopwords, boolean stems)
	throws Exception{
		String[] sentenceArray = getSentenceArrayFromCharSequence(document);
		return getTokenArrayFromSentenceArray(sentenceArray, stopwords, stems);
	}

	public static String[] getSentenceArrayFromCharSequence(
			CharSequence document) {
		return SIDEToolkit.getSentenceDetector().sentDetect(document.toString());
	}

	public static <T> List<T> filterOutItems(T[] array, Collection<? extends T> removalCollection) {
		Set<T> removalSet = new TreeSet<T>(removalCollection);
		List<T> returnList = new ArrayList<T>();
		
		for(T t : array){
			if(removalSet.contains(t)){
				System.out.println("Removing " + t);
				continue; 
			}
			
			returnList.add(t);
		}
		return returnList;
	}
	
	public static String[][] getTokenArrayFromSentenceArray(String[] sentenceArray, boolean stopwords, boolean stems) throws Exception{
		String[][] tokenArray = new String[sentenceArray.length][];
		if(stems){
			for(int i=0; i<sentenceArray.length; i++){
				List<String> tokenList = new ArrayList<String>();
				tokenList.addAll(PorterStemmer.porterStem(sentenceArray[i]));
				tokenArray[i] = tokenList.toArray(new String[0]);
			}
		}else{
			for(int i=0; i<sentenceArray.length; i++){
				String sentence = sentenceArray[i];
				String[] tmpTokenArray = sentence.trim().split("[\\p{Space}]+");

				if(stopwords){
					List<String> lineList = FileToolkit.readAsStringList(SIDEToolkit.getDefaultStopwordsFile(Locale.ENGLISH), true, true);
					tokenArray[i] = filterOutItems(tmpTokenArray, lineList).toArray(new String[0]);
				}else{
					tokenArray[i] = tmpTokenArray;
				}
			}
		}
		return tokenArray;
	}
}
