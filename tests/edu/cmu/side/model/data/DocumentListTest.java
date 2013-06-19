package edu.cmu.side.model.data;

import org.junit.Test;

import edu.cmu.side.model.feature.Feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.TestCase;

public class DocumentListTest extends TestCase{
	public void setUp(){
		//Do we need?
	}
	/*
	 * Testing for constructor #1:
	 * Arguments for constructor #1:
	 * List<Map<String, String>> rows, Collection<String> columns
	 * This essentially creates the table
	 * 
	 */
	@Test
	public void testRowColumnConstructor(){
		List<Map<String,String>> rows = new ArrayList<Map<String,String>>();
		Collection<String> columns = new ArrayList<String>();
		columns.add("text");
		columns.add("value");
		Map<String,String> first = new HashMap<String,String>();
		Map<String,String> second = new HashMap<String,String>();
		Map<String,String> third = new HashMap<String,String>();
		first.put("text", "First");
		first.put("value", "FirstVal");
		second.put("text", "Second");
		third.put("value", "SecondVal");
		rows.add(first);
		rows.add(second);
		rows.add(third);
		DocumentList dList = new DocumentList(rows, columns);
		assertEquals(dList.allAnnotations.size(), 2);
		assertEquals(dList.getTextColumns().size(), 0);
		assertEquals(dList.filenameList.size(), 3);
	}

	@Test
	public void testRowColumnEmpty(){
		List<Map<String,String>> rows = new ArrayList<Map<String,String>>();
		Collection<String> columns = new ArrayList<String>();
		DocumentList dList = new DocumentList(rows, columns);
		assertEquals(dList.allAnnotations.size(),0);
		assertEquals(dList.getTextColumns().size(),0);
		assertEquals(dList.filenameList.size(),0);
	}
	/*
	 * Testing for constructor #2:
	 * Arguments for constructor #2:
	 * List<String> instances
	 * Add text columns
	 * Side-effects to test:
	 * annotation, textcolumn, fileNameList
	 */

	@Test
	public void testInstancesConstructor(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("i");
		}
		DocumentList dList = new DocumentList(instances);
		assertEquals(dList.allAnnotations.size(),0);
		assertEquals(dList.getTextColumns().size(),1);
		assertEquals(dList.getFilenameList().size(),5);
	}
	@Test
	public void testInstancesEmpty(){
		List<String> instances = new ArrayList<String>();
		DocumentList dList = new DocumentList(instances);
		assertEquals(dList.allAnnotations.size(),0);
		assertEquals(dList.getTextColumns().size(),1);
		assertEquals(dList.getFilenameList().size(),0);
	}
	/*
	 * Testing for constructor #3:
	 * Arguments for constructor #3:
	 * List<String> text, Map<String, List<String>> annotations
	 * Add text columns and annotated data
	 * Side-effects to test:
	 * 
	 */

	@Test
	public void testInstancesAndAnnotationsConstruction(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("i");
		}
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		annMap.put("otherValue", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		assertEquals(dList.allAnnotations.size(),2);
		assertEquals(dList.getTextColumns().size(),1);
		assertEquals(dList.getFilenameList().size(),5);
	}
	/*
	 * Testing for constructor #4:
	 * Arguments for constructor #4:
	 * List<String> filenames, Map<String, List<STring>> texts, Map<String, List<String>> Annotations,
	 * |-> String currentAnnot
	 * This is a simple assignment constructor that assigns the parameters to their respective fields
	 * |-> Without doing anything else.
	 * Side-effects to test:
	 * 
	 */

	@Test
	public void testAssignmentConstructor(){
		String[] files = {"Doc","Doc","Doc"};
		List<String> fileNames = new ArrayList<String>(Arrays.asList(files));
		Map<String, List<String>> texts = new HashMap<String, List<String>>();
		String[] values = {"one","two","three"};
		ArrayList<String> firstVals = new ArrayList<String>(Arrays.asList(values));
		texts.put("Value", firstVals);
		String currentAnnot = "Value";
		DocumentList dList = new DocumentList(fileNames, texts,texts,currentAnnot);
		assertEquals(dList.filenameList, fileNames);
		assertTrue(dList.getTextColumns().contains("Value"));
		assertEquals(dList.currentAnnotation,currentAnnot);
	}
	/*
	 * Testing for constructor #5:
	 * Arguments for constructor #5:
	 * Set<String> filenames
	 * Parse and create whole DocumentList out of file list
	 * Side-effects to test: annotationList. Size. AssureNull on currentAnnotation and size 0 of textColumns.
	 * 
	 */
	@Test
	public void testSingleFileName(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		DocumentList docList = new DocumentList(fileNames);
		assertNotNull(docList);
		assertEquals(docList.getSize(),300);
		String[] annNames = docList.getAnnotationNames();
		assertEquals(annNames.length,2);
		assertEquals(docList.getAnnotationNames()[0], "class");
		assertEquals(docList.getAnnotationNames()[1], "text");
		assertNull(docList.currentAnnotation);
		assertEquals(docList.getTextColumns().size(),0);
	}
	@Test
	public void testSingleFileNameNoSuffix(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("train2");
		DocumentList docList = new DocumentList(fileNames);
		assertNotNull(docList);
		assertEquals(docList.getSize(),1278);
		String[] annNames = docList.getAnnotationNames();
		assertEquals(annNames.length,4);
		assertEquals(docList.getAnnotationNames()[0], "score1");
		assertEquals(docList.getAnnotationNames()[1], "score2");
		assertNull(docList.currentAnnotation);
		assertEquals(docList.getTextColumns().size(),0);
	}
	@Test
	public void testMultipleFileNameDifferentHeaders(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		fileNames.add("train2.csv");
		DocumentList docList = new DocumentList(fileNames);
		int size = 0;
		for(List<String> arString: docList.allAnnotations.values()){
			if(size==0){
				size=arString.size();
			} else {
				assertEquals(arString.size(),size);
			}
		}
		assertEquals(docList.getSize(), 1578);
		assertEquals(docList.getAnnotationNames().length, 5);

	}

	@Test
	public void testMultipleFileNameSameHeaders(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("sentiment_documents.csv");
		fileNames.add("sentiment_sentences.csv");
		DocumentList docList = new DocumentList(fileNames);
		int size = 0;
		for(List<String> arString: docList.allAnnotations.values()){
			if(size==0){
				size=arString.size();
			} else {
				assertEquals(arString.size(),size);
			}
		}
		assertEquals(docList.getSize(), 12662);
		assertEquals(docList.getAnnotationNames().length, 2);
	}
	@Test
	public void testThreeFileNameDifferentHeaders(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("sentiment_documents.csv");
		fileNames.add("TutorialData.csv");
		fileNames.add("train2.csv");
		DocumentList docList = new DocumentList(fileNames);
		int size = 0;
		for(List<String> arString: docList.allAnnotations.values()){
			if(size==0){
				size=arString.size();
			} else {
				assertEquals(arString.size(),size);
			}
		}
		assertEquals(docList.getSize(), 4393);
		assertEquals(docList.getAnnotationNames().length, 17);
	}

	/*
	 * Testing for constructor #6:
	 * Arguments for constructor #6:
	 * Set<String> filenames, String textCol
	 * Parse and create whole DocumentList out of filelist and assign textColumn
	 * Side-effects to test:
	 * 
	 */
	@Test
	public void testSingleFileNameWithText(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		DocumentList docList = new DocumentList(fileNames, "text");
		assertNotNull(docList);
		assertEquals(docList.getSize(),300);
		String[] annNames = docList.getAnnotationNames();
		assertEquals(annNames.length,1);
		assertEquals(docList.getAnnotationNames()[0],"class");
		assertNull(docList.currentAnnotation);
		assertEquals(docList.getTextColumns().size(),1);
		assertTrue(docList.getTextColumns().contains("text"));
	}
	@Test
	public void testSingleFileNameWithInvalidText(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		try{
			DocumentList docList = new DocumentList(fileNames, "invalid");
			fail("No illegal state exception caught");
		} catch (IllegalStateException e){
			assertEquals(e.getMessage(), "Can't find the text column named invalid in provided file");
		}
	}
	/*
	 * Testing for constructor #7:
	 * Arguments for constructor #7:
	 * Set<STring> Filenames, String currentAnnot, String textCol
	 * Parse and create whole DocumentList out of filelist and assign textColumn as well as the current annotation
	 * Side-effects to test:
	 * 
	 */
	@Test
	public void testFilesAndTextAndCurrentAnnot(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		DocumentList docList = new DocumentList(fileNames, "class","text");
		assertNotNull(docList);
		assertEquals(docList.getSize(),300);
		String[] annNames = docList.getAnnotationNames();
		assertEquals(annNames.length,1);
		assertEquals(docList.currentAnnotation,"class");
		assertEquals(docList.getAnnotationNames()[0],"class");
		assertEquals(docList.getTextColumns().size(),1);
		assertTrue(docList.getTextColumns().contains("text"));
	}
	@Test
	public void testInvalidCurrentAnnot(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		try{
			DocumentList docList = new DocumentList(fileNames, "INVALID!","text");
			fail("should've caught IllegalStateException but didn't");
		} catch (IllegalStateException e){
			assertEquals(e.getMessage(), "Can't find the label column named INVALID! in provided file");
		}
	}

	/*
	 * Testing for constructor #8:
	 * Arguments for constructor #8:
	 * String Instance
	 * Add text columns after wrapping instance into List.
	 */

	@Test
	public void testTextColumnConstructor(){
		String instance = "instance";
		DocumentList dList = new DocumentList(instance);
		assertEquals(dList.allAnnotations.size(),0);
		assertEquals(dList.getTextColumns().size(),1);
		assertEquals(dList.getFilenameList().size(),1);
	}
	/*---------------------------------------------------------------------------------------------------*/
	//Various Method Tests Begins
	
	@Test
	public void testGetPossibleAnn(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("i");
		}
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		annMap.put("otherValue", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		assertEquals(dList.getPossibleAnn("value"),new HashSet<String>(instances));
	}
	@Test
	public void testGetPossibleAnnNull(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("i");
		}
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		annMap.put("otherValue", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		assertEquals(dList.getPossibleAnn("shouldBeNull"), new HashSet<String>());
	}
	@Test
	public void testGetValueTypeNumeric(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("1");
		}
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		assertEquals(dList.getValueType("value"),Feature.Type.NUMERIC);
	}
	@Test 
	public void testGetValueTypeNominal(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("i");
		}
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		assertEquals(dList.getValueType("value"),Feature.Type.NOMINAL);
	}
	@Test
	public void testGetValueTypeNull(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("1");
		}
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		assertEquals(dList.getValueType(null),Feature.Type.NOMINAL);
	}
	@Test
	public void testSetNameAndGetName(){
		List<String> instances = new ArrayList<String>();
		instances.add("i");
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		dList.setName("testName");
		assertEquals("testName", dList.getName());
	}
	@Test
	public void testAllAnnotations(){
		List<String> instances = new ArrayList<String>();
		instances.add("i");
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		dList.setName("testName");
		assertEquals(dList.allAnnotations, dList.allAnnotations());
	}
	@Test
	public void testGuessTextColumnsAndAnnots(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		DocumentList docList = new DocumentList(fileNames);
		assertNull(docList.currentAnnotation);
		assertEquals(docList.getTextColumns().size(),0);
		docList.guessTextAndAnnotationColumns();
		assertEquals(docList.currentAnnotation, "class");
		assertTrue(docList.getTextColumns().contains("text"));
	}
	@Test
	public void testGuessTextAndAnnotsNoClass(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("test1.csv");
		DocumentList docList = new DocumentList(fileNames);
		assertNull(docList.currentAnnotation);
		assertEquals(docList.getTextColumns().size(),0);
		docList.guessTextAndAnnotationColumns();
	}
	@Test
	public void testAlreadyHaveTextAndAnnots(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		DocumentList docList = new DocumentList(fileNames);
		assertNull(docList.currentAnnotation);
		assertEquals(docList.getTextColumns().size(),0);
		docList.guessTextAndAnnotationColumns();
		assertEquals(docList.currentAnnotation, "class");
		assertTrue(docList.getTextColumns().contains("text"));
		docList.guessTextAndAnnotationColumns();
		assertEquals(docList.currentAnnotation, "class");
		assertTrue(docList.getTextColumns().contains("text"));
	}
	@Test
	public void testNotInClassList(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("heuristicTest.csv");
		DocumentList dList = new DocumentList(fileNames);
		assertNull(dList.currentAnnotation);
		assertEquals(dList.getTextColumns().size(),0);
		dList.guessTextAndAnnotationColumns();
		assertEquals(dList.currentAnnotation, "sbv");
		assertTrue(dList.getTextColumns().contains("sbt"));
	}
	@Test
	public void testGuessText(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("heuristicTest.csv");
		DocumentList dList = new DocumentList(fileNames);
		dList.setCurrentAnnotation("sbv");
		assertEquals(dList.currentAnnotation, "sbv");
		assertEquals(dList.getTextColumns().size(),0);
		dList.guessTextAndAnnotationColumns();
		assertEquals(dList.currentAnnotation, "sbv");
		assertTrue(dList.getTextColumns().contains("sbt"));
	}
	@Test
	public void testCurrAnnot(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("heuristicTest.csv");
		DocumentList dList = new DocumentList(fileNames);
		dList.setTextColumn("sbt", true);
		assertNull(dList.currentAnnotation);
		assertEquals(dList.getTextColumns().size(),1);
		dList.guessTextAndAnnotationColumns();
		assertEquals(dList.currentAnnotation, "sbv");
		assertTrue(dList.getTextColumns().contains("sbt"));
	}
	@Test
	public void testAddAnnotationsUpdateExisting(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.addAnnotation("exists", new ArrayList<String>(), false);
		docList.addAnnotation("exists", new ArrayList<String>(), false);
		docList.addAnnotation("exists", new ArrayList<String>(), false);
		assertTrue(docList.allAnnotations().containsKey("exists"));
		assertTrue(docList.allAnnotations().containsKey("exists (new)"));
		assertTrue(docList.allAnnotations().containsKey("exists (new) (new)"));
	}
	@Test
	public void testAddAnnotationNotUpdateExisting(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.addAnnotation("exists", new ArrayList<String>(), true);
		docList.addAnnotation("exists", new ArrayList<String>(), true);
		docList.addAnnotation("exists", new ArrayList<String>(), true);
		assertTrue(docList.allAnnotations().containsKey("exists"));
		assertFalse(docList.allAnnotations().containsKey("exists (new)"));
		assertFalse(docList.allAnnotations().containsKey("exists (new) (new)"));
	}
	@Test
	public void testGetCoveredTextList(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("MovieReviews.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setTextColumn("text", true);
		List<String> textList = docList.textColumns.get("text");
		assertEquals(textList, docList.getCoveredTextList().get("text"));
	}
	@Test
	public void testGetPrintableTextAtSingleColumn(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setTextColumn("text", true);
		String expected = docList.getCoveredTextList().get("text").get(3);
		assertEquals(docList.getPrintableTextAt(3), expected);
	}
	@Test
	public void testGetPrintableTextAtMultipleColumns(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setTextColumn("text", true);
		docList.setTextColumn("Session", true);
		String expected = "Session:\n" + docList.getCoveredTextList().get("Session").get(3) + "\n";
		expected += "text:\n" + docList.getCoveredTextList().get("text").get(3) + "\n";
		assertEquals(docList.getPrintableTextAt(3), expected);
	}
	
	@Test
	public void testTextColumnsAreDifferentiatedSetterAndGetters(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		boolean areDiff = docList.textColumnsAreDifferentiated();
		docList.setDifferentiateTextColumns(!areDiff);
		assertEquals(!areDiff, docList.textColumnsAreDifferentiated());
	}
	
	@Test
	public void testGetTextFeatureNameDifferentiated(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setDifferentiateTextColumns(true);
		String expected = "column:basename";
		assertEquals(expected, docList.getTextFeatureName("basename","column"));
	}
	@Test
	public void testGetTextFeatureNameNonDifferentiated(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setDifferentiateTextColumns(false);
		String expected = "basename";
		assertEquals(expected, docList.getTextFeatureName("basename","column"));
	}
	@Test
	public void testGetFileName(){
		String[] files = {"Doc","Doc2","Doc"};
		List<String> fileNames = new ArrayList<String>(Arrays.asList(files));
		Map<String, List<String>> texts = new HashMap<String, List<String>>();
		String[] values = {"one","two","three"};
		ArrayList<String> firstVals = new ArrayList<String>(Arrays.asList(values));
		texts.put("Value", firstVals);
		String currentAnnot = "Value";
		DocumentList dList = new DocumentList(fileNames, texts,texts,currentAnnot);
		assertEquals(dList.getFilename(1), "Doc2");
	}
	@Test
	public void testGetFileList(){
		String[] files = {"Doc","Doc2","Doc"};
		List<String> fileNames = new ArrayList<String>(Arrays.asList(files));
		Map<String, List<String>> texts = new HashMap<String, List<String>>();
		String[] values = {"one","two","three"};
		ArrayList<String> firstVals = new ArrayList<String>(Arrays.asList(values));
		texts.put("Value", firstVals);
		String currentAnnot = "Value";
		DocumentList dList = new DocumentList(fileNames, texts,texts,currentAnnot);
		assertEquals(dList.getFilenameList(), Arrays.asList(files));
	}
	
	@Test
	public void testGetFileNames(){
		String[] files = {"Doc","Doc2","Doc"};
		List<String> fileNames = new ArrayList<String>(Arrays.asList(files));
		Map<String, List<String>> texts = new HashMap<String, List<String>>();
		String[] values = {"one","two","three"};
		ArrayList<String> firstVals = new ArrayList<String>(Arrays.asList(values));
		texts.put("Value", firstVals);
		String currentAnnot = "Value";
		DocumentList dList = new DocumentList(fileNames, texts,texts,currentAnnot);
		Set<String> fileList = dList.getFilenames();
		for(String str: files){
			assertTrue(fileList.contains(str));
		}
	}
	
	@Test
	public void testGetLabelArrayNominal(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		String[] expected = {"A","B","C","D"};
		assertTrue(Arrays.equals(docList.getLabelArray("Class", Feature.Type.NOMINAL), expected));
	}
	//TODO: THIS TEST MEANS LITTLE->NOTHING. Consult bosses
	@Test
	public void testGetLabelArrayBoolean(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		String[] expected = {"A","B","C","D"};
		assertTrue(Arrays.equals(docList.getLabelArray("Class", Feature.Type.BOOLEAN), expected));
	}
	@Test
	public void testGetLabelArrayNumeric(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		String[] expected = {"Q1","Q2","Q3","Q4","Q5"};
		assertTrue(Arrays.equals(docList.getLabelArray("Class", Feature.Type.NUMERIC), expected));
	}
	@Test
	public void testGetLabelArrayNull(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		String[] expected = {};
		assertTrue(Arrays.equals(docList.getLabelArray("Fail", Feature.Type.BOOLEAN), expected));
	}
	@Test
	public void testSetLabelArray(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		String[] newLabelArray = {"first", "second"};
		docList.setCurrentAnnotation("Class");
		docList.setLabelArray(newLabelArray);
		assertTrue(Arrays.equals(newLabelArray, docList.getLabelArray("Class", Feature.Type.NOMINAL)));
	}
	@Test
	public void testGetSizeFromText(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("i");
		}
		DocumentList dList = new DocumentList(instances);
		assertEquals(dList.getSize(), 5);
	}
	@Test
	public void testGetSizeEmptyText(){
		List<String> instances = new ArrayList<String>();
		DocumentList dList = new DocumentList(instances);
		assertEquals(dList.getSize(),0);
	}
	@Test
	public void testGetSizeByAnnotations(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		assertEquals(docList.getSize(),1115);
	}
	@Test
	public void testGetSizeByAnnotationsNoAnnotation(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		Map<String, List<String>> newAnnotList = new TreeMap<String, List<String>>();
		newAnnotList.put("test", new ArrayList<String>());
		docList.allAnnotations = newAnnotList;
		assertEquals(docList.getSize(),0);
	}
	@Test
	public void testGetSizeEmptyDocumentList(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.allAnnotations = null;
		assertEquals(docList.getSize(),0);
	}
	@Test
	public void testSetCurrentAnnotation(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setCurrentAnnotation("Class");
		assertNotNull(docList.labelArray);
		assertEquals("Class", docList.currentAnnotation);
	}
	@Test
	public void testSetCurrentAnnotationAlreadySet(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setCurrentAnnotation("Class");
		String[] currentLabelArray = docList.labelArray;
		assertNotNull(docList.labelArray);
		assertEquals("Class", docList.currentAnnotation);
		docList.setCurrentAnnotation("Class");
		assertEquals(currentLabelArray, docList.labelArray);
		assertEquals("Class", docList.currentAnnotation);
	}
	@Test
	public void testSetCurrAnnotationInvalid(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		try{
			docList.setCurrentAnnotation("Invalid");
			fail("Failed to catch exception");
		} catch(IllegalStateException e){
			assertEquals(e.getMessage(),"Can't find the label column named Invalid in provided file");
		}
	}
	@Test
	public void testSetCurrAnnotationDifferentAnn(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setCurrentAnnotation("Class");
		String[] currentLabelArray = docList.labelArray;
		assertNotNull(docList.labelArray);
		assertEquals("Class", docList.currentAnnotation);
		docList.setCurrentAnnotation("Session");
		assertNotSame(currentLabelArray, docList.labelArray);
		assertEquals("Session", docList.currentAnnotation);
	}
	@Test
	public void testSetCurrAnnotationWithType(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setCurrentAnnotation("Class", Feature.Type.NOMINAL);
		assertNotNull(docList.labelArray);
		assertEquals("Class", docList.currentAnnotation);
	}
	@Test
	public void testSetCurrentAnnotationWithTypeAlreadySet(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setCurrentAnnotation("Class", Feature.Type.NOMINAL);
		String[] currentLabelArray = docList.labelArray;
		assertNotNull(docList.labelArray);
		assertEquals("Class", docList.currentAnnotation);
		docList.setCurrentAnnotation("Class", Feature.Type.NOMINAL);
		assertEquals(currentLabelArray, docList.labelArray);
		assertEquals("Class", docList.currentAnnotation);
	}
	@Test
	public void testSetCurrAnnotationWithTypeInvalid(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		try{
			docList.setCurrentAnnotation("Invalid", Feature.Type.NOMINAL);
			fail("Failed to catch exception");
		} catch(IllegalStateException e){
			assertEquals(e.getMessage(),"Can't find the label column named Invalid in provided file");
		}
	}
	@Test
	public void testSetCurrAnnotationWithTypeDifferentAnn(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setCurrentAnnotation("Class", Feature.Type.NOMINAL);
		String[] currentLabelArray = docList.labelArray;
		assertNotNull(docList.labelArray);
		assertEquals("Class", docList.currentAnnotation);
		docList.setCurrentAnnotation("Session", Feature.Type.NOMINAL);
		assertNotSame(currentLabelArray, docList.labelArray);
		assertEquals("Session", docList.currentAnnotation);
	}
	@Test
	public void testSetTextColumns(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		Set<String> texts = new HashSet<String>();
		texts.add("text");
		docList.setTextColumns(texts);
		assertNotNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
	}
	@Test
	public void testOverWriteTextColumns(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		Set<String> texts = new HashSet<String>();
		texts.add("text");
		docList.setTextColumns(texts);
		assertNotNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
		texts.remove("text");
		texts.add("Role");
		docList.setTextColumns(texts);
		assertNotNull(docList.textColumns.get("Role"));
		assertNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
	}
	@Test
	public void testSetTextColumnIsText(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setTextColumn("text", true);
		assertNotNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
	}
	@Test
	public void testSetTextColumnAlreadyThere(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setTextColumn("text", true);
		assertNotNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
		docList.setTextColumn("text", true);
		assertNotNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
	}
	@Test
	public void testSetTextColumnInvalidText(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		try{
			docList.setTextColumn("invalid", true);
			fail("Failed to catch exception");
		} catch(IllegalStateException e){
			assertEquals(e.getMessage(), "Can't find the text column named invalid in provided file");
		}
	}
	@Test
	public void testRemoveTextColumn(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setTextColumn("text", true);
		assertNotNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
		docList.setTextColumn("text", false);
		assertNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 0);
	}
	@Test
	public void testRemoveTextColumnNotThere(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setTextColumn("text", true);
		assertNotNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
		docList.setTextColumn("notThere", false);
		assertNotNull(docList.textColumns.get("text"));
		assertEquals(docList.textColumns.size(), 1);
	}
	@Test
	public void testSetFileNames(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		fileNames.add("test1.csv");
		ArrayList<String> newFileList = new ArrayList<String>(fileNames);
		docList.setFilenames(newFileList);
		assertEquals(docList.getFilenameList().size(), 2);
		assertEquals(docList.getFilenameList(), newFileList);
		docList.setFilenames(null);
		assertNull(docList.getFilenameList());
	}
	@Test
	public void testEmptyAddInstances(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		int currentSize = docList.getSize();
		docList.addInstances(new ArrayList<Map<String,String>>(), new ArrayList<String>());
		assertEquals(currentSize, docList.getSize());
	}
	@Test
	public void testAddInstancesMultipleCases(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		ArrayList<Map<String,String>> rows = new ArrayList<Map<String,String>>();
		Map<String,String> firstAddition = new HashMap<String,String>();
		firstAddition.put("add1", "firstValue");
		firstAddition.put("add2", "SecondValue");
		Map<String,String> secondAddition = new HashMap<String,String>();
		secondAddition.put("add1", "firstValue");
		secondAddition.put("add2", "SecondValue");
		rows.add(firstAddition);
		rows.add(secondAddition);
		String[] newColumns = {"add1", "add2", "text"};
		List<String> columns = Arrays.asList(newColumns);
		int currentSize = docList.getSize();
		docList.addInstances(rows, columns);
		assertEquals(currentSize+rows.size(), docList.getSize());
	}
	@Test
	public void testAddInstancesTextColumns(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		ArrayList<Map<String,String>> rows = new ArrayList<Map<String,String>>();
		Map<String,String> firstAddition = new HashMap<String,String>();
		Map<String,String> secondAddition = new HashMap<String,String>();
		firstAddition.put("text", "sampletext1");
		firstAddition.put("text", "sampletext2");
		secondAddition.put("text", "sampletext1");
		secondAddition.put("text", "sampletext2");
		rows.add(firstAddition);
		rows.add(secondAddition);
		docList.setTextColumn("text", true);
		docList.setTextColumn("Cond", true);
		String[] newColumns = {"add1", "add2", "text"};
		List<String> columns = Arrays.asList(newColumns);
		int currentSize = docList.getSize();
		docList.addInstances(rows, columns);
		assertEquals(currentSize + rows.size(), docList.getSize());
	}
	@Test
	public void testGetAndSetEmptAnnotStr(){
		Set<String> fileNames = new TreeSet<String>();
		fileNames.add("TutorialData.csv");
		DocumentList docList = new DocumentList(fileNames);
		docList.setEmptyAnnotationString("test");
		assertEquals(docList.getEmptyAnnotationString(), "test");
	}
	/****************************************************/
	//Useless Tests Go Here
	@Test
	public void testGetAnnotationArrayNull(){
		DocumentList dList = new DocumentList("test");
		assertNull(dList.getAnnotationArray(null));
	}
	@Test
	public void testGetAnnotationArray(){
		List<String> instances = new ArrayList<String>();
		for(int i = 0; i < 5; i++){
			instances.add("i");
		}
		Map<String, List<String>> annMap = new TreeMap<String, List<String>>();
		annMap.put("value", instances);
		annMap.put("otherValue", instances);
		DocumentList dList = new DocumentList(instances, annMap);
		assertEquals(instances, dList.getAnnotationArray("value"));
	}
	
}