package edu.cmu.side.uima;

import java.awt.Color;

import java.awt.Component;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CasConsumer;
import org.apache.uima.collection.CasConsumerDescription;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceCreationSpecifier;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.apache.uima.resource.metadata.MetaDataObject;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.collections.CollectionsToolkit.BooleanOperator;
import com.yerihyo.yeritools.collections.CollectionsToolkit.CompoundIterator;
import com.yerihyo.yeritools.collections.CollectionsToolkit.Filter;
import com.yerihyo.yeritools.collections.CollectionsToolkit.FilteredIterator;
import com.yerihyo.yeritools.collections.MapToolkit.ListValueMap;
import com.yerihyo.yeritools.csv.CSVReader;
import com.yerihyo.yeritools.csv.CSVWriter;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.math.StatisticsToolkit;
import com.yerihyo.yeritools.swing.ColorLabelConfigPanel.ColorLabel;
import com.yerihyo.yeritools.swing.SwingToolkit.MultipleSelectionOption;
import com.yerihyo.yeritools.swing.TimelineBarToolkit.Indicator;
import com.yerihyo.yeritools.text.SegmentationMetricToolkit;
import com.yerihyo.yeritools.text.StringToolkit;
import com.yerihyo.yeritools.xml.XMLToolkit;
import com.yerihyo.yeritools.xml.XMLable;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTableKey;
import edu.cmu.side.ml.PredictionToolkit.NominalPredictionResult;
import edu.cmu.side.ml.PredictionToolkit.PredictionResult;
import edu.cmu.side.plugin.SegmenterPlugin;
import edu.cmu.side.uima.type.SIDEAnnotation;
import edu.cmu.side.uima.type.SIDEAnnotationSetting;
import edu.cmu.side.uima.type.SIDEDocumentSetting;
import edu.cmu.side.uima.type.SIDEPredictionAnnotation;
import edu.cmu.side.uima.type.SIDESegment;

public class UIMAToolkit {

	public static String VAR_uimaAnnotationType = "uimaAnnotationType";
	public static String VAR_annotationSubtypeName = "annotationSubtypeName";

	public static enum Datatype{
		AUTO, NOMINAL, NUMERIC;
		
		public static final String xmlTag = "Datatype";
		
		public String toString(){ return this.name(); }
		public static Datatype fromString(CharSequence cs){
			return Datatype.valueOf(cs.toString());
		}
		
		public static Datatype merge(Datatype datatype1, Datatype datatype2) {
			if(datatype1==AUTO){ return datatype2; }
			if(datatype2==AUTO){ return datatype1; }
			
			if(datatype1==NOMINAL || datatype2==NOMINAL){ return NOMINAL; }
			else if(datatype1==NUMERIC && datatype2==NUMERIC){ return NUMERIC; }
			throw new UnsupportedOperationException();
		}
		public static Datatype create(SIDEAnnotationSetting setting) {
			if(setting==null){ return Datatype.AUTO; }
			return Datatype.fromString(setting.getDatatypeString());
		}
		public String getName() {
			return this.name();
		}
		public static Datatype extract(String[] labelArray) {
			for(String s : labelArray){
				if(s==null){
					continue;
				}
				try{
					Double.parseDouble(s);
				}catch(NumberFormatException ex){
					return Datatype.NOMINAL;
				}
			}
			return Datatype.NUMERIC;
		}
	}
	
	public static File sideTypeSystemDescriptionFile = new File(SIDEToolkit.rootFolder,
			"descriptors/SIDETypeSystem.xml");

	public static CAS[] getSIDETestCasArray() throws InvalidXMLException,
			ResourceInitializationException, CollectionException, IOException {
		File[] fileArray = SIDEToolkit.xmiFolder.listFiles();
		
		CAS[] returnValue = new CAS[fileArray.length];
		for(int i=0; i<fileArray.length; i++){
			File file = fileArray[i];
			CAS cas = UIMAToolkit.createSIDECAS(file);
			returnValue[i] = cas;
		}
		return returnValue;
	}
	
	public static class AnnotationIndicator implements Indicator{

		private SIDEAnnotation sideAnnotation;
		public AnnotationIndicator(SIDEAnnotation sideAnnotation){
			this.sideAnnotation = sideAnnotation;
		}
		
		@Override
		public Color getColor() {
			try {
				return UIMAToolkit.getLabelColor(sideAnnotation);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getId() {
			return sideAnnotation.getLabelString();
		}

		@Override
		public String getLabel() {
			return sideAnnotation.getLabelString();
		}
	}
//	public static Conversation parseXmiFileToConversation(File xmiFile, String subtypeName){
//		
//		JCas jCas;
//		try {
//			jCas = UIMAToolkit.createSIDEJCas(xmiFile);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		
//		List<SIDESegment> sideSegmentList = UIMAToolkit.getSIDESegmentList(jCas, SIDESegment.type, subtypeName);
//		
//		List<Move> moveList = new ArrayList<Move>();
//		for(SIDESegment sideSegment : sideSegmentList){
//			String text = sideSegment.getCoveredText();
//			SIDEAnnotation sideAnnotation = (SIDEAnnotation)sideSegment;
//			Move move = new Move( new AnnotationIndicator(sideAnnotation), text);
//			moveList.add(move);
//		}
//		
//		Conversation conversation = new Conversation(moveList.toArray(new Move[0]));
//		return conversation;	
//	}
	
//	public static boolean hasAnnotation(DocumentList documentList, String subtypeName){
//		
//		SortedSet<String> subtypeNameSet = UIMAToolkit.getAnnotationSubtypeSet(documentList.getJCasList().get(0));
//		return subtypeNameSet.contains(subtypeName);
//	}
	
	public static PredictionResult addSelfPredictionAnnotation(TrainingResultInterface trainingResult, String desiredSubtypeName){
		DocumentList documentList = ((TrainingResult)trainingResult).getDocumentList();
//		String desiredSubtypeName =
//			UIMAToolkit.getRecommendedPredictionAnnotationSubtypeName(trainingResult, documentList.getBaseSubtypeName());
		PredictionResult selfPredictionResult = ((TrainingResult)trainingResult).getSelfPredictionResult();
		
		addPredictionAnnotation(documentList, desiredSubtypeName, selfPredictionResult, true);
		return selfPredictionResult;
	}

	public static void addPredictionAnnotation(TrainingResultInterface trainingResult, DocumentList documentList, String desiredSubtypeName, boolean override){
		
		PredictionResult predictionResult;
		try {
			predictionResult = ((TrainingResult)trainingResult).predict(documentList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		addPredictionAnnotation(documentList, desiredSubtypeName, predictionResult, override);
//		return predictionResult;
	}
	
	protected static void addPredictionAnnotation(DocumentList inputDocumentList, String desiredSubtypeName, PredictionResult predictionResult, boolean override){
		if(override){
			addPredictionAnnotationWithOverride(inputDocumentList, desiredSubtypeName, predictionResult);
		}else{
			addPredictionAnnotationWithoutOverride(inputDocumentList, predictionResult);
		}
	}
	
	private static void addPredictionAnnotationWithoutOverride(DocumentList inputDocumentList, PredictionResult predictionResult){
		Iterator<Object> iterator = inputDocumentList.iterator();
		
		Set<SIDEAnnotationSetting> settingSet = new HashSet<SIDEAnnotationSetting>();
		
		for(int i=0; iterator.hasNext(); i++){
			SIDESegment sideSegment = (SIDESegment)iterator.next();
			YeriDebug.ASSERT(sideSegment instanceof SIDEAnnotation);
			
			SIDEAnnotation sideAnnotation = (SIDEAnnotation)sideSegment;
			if(sideAnnotation.getLabelString()!=null){ continue; }
			String label = predictionResult.getMostLikelyLabel(i);
			sideAnnotation.setLabelString(label);

			SIDEAnnotationSetting sideAnnotationSetting = sideAnnotation.getSetting();
			if(settingSet.contains(sideAnnotationSetting)){ continue; }
			
			ColorLabel[] defaultColorLabelArray = predictionResult.createColorLabelArray();
			setColorLabelString(sideAnnotationSetting, defaultColorLabelArray, true);
		}
		try {
			inputDocumentList.saveEach();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void addPredictionAnnotationWithOverride(DocumentList inputDocumentList, String desiredSubtypeName, PredictionResult predictionResult){
		DocumentList baseDocumentList = new DocumentList(inputDocumentList.getBaseSubtypeName());
		baseDocumentList.getJCasList().addAll(inputDocumentList.getJCasList());
		
		JCas[] jCasArray = baseDocumentList.getJCasList().toArray(new JCas[0]);
		UIMAToolkit.removeSIDEAnnotationList(jCasArray, SIDEAnnotation.type, desiredSubtypeName);
		
		
		Map<JCas,SIDEAnnotationSetting> settingMap = new HashMap<JCas, SIDEAnnotationSetting>();
		List<SIDEPredictionAnnotation> predictionAnnotationList = new ArrayList<SIDEPredictionAnnotation>();
		Iterator<Object> iterator = baseDocumentList.iterator();
		for(int i=0; iterator.hasNext(); i++){
			SIDESegment sideSegment = (SIDESegment)iterator.next();
			JCas jCas = UIMAToolkit.getJCas(sideSegment.getCAS());
			
			SIDEAnnotationSetting sideAnnotationSetting = settingMap.get(jCas);
			if(sideAnnotationSetting==null){
				sideAnnotationSetting = UIMAToolkit.createSIDEAnnotationSetting(jCas);
				ColorLabel[] defaultColorLabelArray = predictionResult.createColorLabelArray();
				setColorLabelString(sideAnnotationSetting, defaultColorLabelArray, false);
				sideAnnotationSetting.addToIndexes();
				
				settingMap.put(jCas, sideAnnotationSetting);
			}
			
			SIDESegment sourceSegmentation = getBaseSIDESegment(sideSegment);
			
			SIDEPredictionAnnotation predictionAnnotation = new SIDEPredictionAnnotation(jCas);
			predictionAnnotation.setSourceSegmentation(sourceSegmentation);
			predictionAnnotation.setLabelString(predictionResult.getMostLikelyLabel(i));
			
			predictionAnnotation.setSetting(sideAnnotationSetting);
			
			
			predictionAnnotation.setSubtypeName(desiredSubtypeName);
			predictionAnnotation.setBegin(sourceSegmentation.getBegin());
			predictionAnnotation.setEnd(sourceSegmentation.getEnd());
			
			if(predictionResult instanceof NominalPredictionResult){
				NominalPredictionResult npr = (NominalPredictionResult)predictionResult;
				if(npr.isPredictionProbabilityValid()){
					double[] predictionProbabilityArray = npr.getPredictionProbabilityArray(i);
					DoubleArray doubleArray = new DoubleArray(jCas, predictionProbabilityArray.length);
					doubleArray.copyFromArray(predictionProbabilityArray, 0, 0, predictionProbabilityArray.length);
					predictionAnnotation.setPredictionArray(doubleArray);
				}
			}
			
			
			predictionAnnotationList.add(predictionAnnotation);
		}
		
		for(SIDEPredictionAnnotation predictionAnnotation: predictionAnnotationList){
			predictionAnnotation.addToIndexes();
		}
		
		try {
			baseDocumentList.saveEach();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static SIDESegment getBaseSIDESegment(SIDESegment sideSegment) {
		while(sideSegment instanceof SIDEAnnotation){
			SIDEAnnotation sideAnnotation = (SIDEAnnotation)sideSegment;
			sideSegment = sideAnnotation.getSourceSegmentation();
		}
		return sideSegment;
	}

	public static JCas getJCas(CAS cas) {
		JCas jCas = null;
		try {
			jCas = cas.getJCas();
		} catch (CASException e) {
			throw new RuntimeException(e);
		}
		return jCas;
	}

	public static boolean hasEqualTextAndAnnotation(SIDEAnnotation sideAnnotation1, SIDEAnnotation sideAnnotation2){
		if(!sideAnnotation1.getLabelString().equals(sideAnnotation2.getLabelString())){
			return false;
		}
		return sideAnnotation1.getCoveredText().equals(sideAnnotation2.getCoveredText());
	}
	
	private static void textCollectionToXmi(File inputFolder, File destFolder)
			throws InvalidXMLException, ResourceInitializationException,
			IOException, CollectionException {

		CollectionReader reader = createCollectionReader(new XMLInputSource(
				new File(SIDEToolkit.getDescriptorsFolder(),
						"TextCollectionReader.xml")),
				new String[][] { new String[] {
						TextCollectionReader.PARAM_INPUTDIR,
						inputFolder.getAbsolutePath() } });

		collectionReaderToXmi(reader, destFolder);
		reader.destroy();
	}

	private static void csvCollectionToXmi(File inputFolder, File destFolder)
			throws IOException, InvalidXMLException,
			ResourceInitializationException, CollectionException {

		CollectionReader reader = createCollectionReader(new XMLInputSource(
				"descriptors/CSVCollectionReaderDescriptor.xml"),
				new String[][] { new String[] {
						CSVCollectionReader.PARAM_INPUTDIR,
						inputFolder.getAbsolutePath() } });

		collectionReaderToXmi(reader, destFolder);
		reader.destroy();
	}

	private static void collectionReaderToXmi(CollectionReader reader,
			File destFolder) throws InvalidXMLException,
			ResourceInitializationException, IOException, CollectionException {
		CasConsumer consumer = createCasConsumer(new XMLInputSource(new File(
				SIDEToolkit.getDescriptorsFolder(), "XmiWriterCasConsumer.xml")),
				new String[][] { new String[] {
						XmiWriterCasConsumer.PARAM_OUTPUTDIR,
						destFolder.getAbsolutePath() } });

		CAS cas = createSIDECAS();

		try {
			while (reader.hasNext()) {
				try {
					reader.getNext(cas);
					consumer.processCas(cas);
					cas.reset();
				} catch (CollectionException ex) {
					YeriDebug.die(ex);
				} catch (AnalysisEngineProcessException ex) {
					YeriDebug.die(ex);
				} catch (ResourceProcessException ex) {
					YeriDebug.die(ex);
				}
			}
			consumer.destroy();
		} catch (CollectionException ex) {
			System.exit(-1);
		}
	}

	public static String getContent(File xmiFile) throws InvalidXMLException,
			ResourceInitializationException, CollectionException, IOException {
		CAS cas = createSIDECAS(xmiFile);
		String text = cas.getDocumentText();
		cas.reset();
		return text;
	}

	// public static CAS readSingleXmiIntoCAS(File xmiFile, TypeSystem
	// typeSystem)
	// throws ResourceInitializationException, SAXException, IOException{
	// // create a new CAS
	// CAS cas = CasCreationUtils.createCas(Collections.EMPTY_LIST, typeSystem,
	// UIMAFramework
	// .getDefaultPerformanceTuningProperties());
	// // deserialize XCAS into CAS
	// FileInputStream xcasInStream = null;
	// try {
	// xcasInStream = new FileInputStream(xmiFile);
	// XmlCasDeserializer.deserialize(xcasInStream, cas, true);
	// } finally {
	// if (xcasInStream != null)
	// xcasInStream.close();
	// }
	// return cas;
	// }

	public static void readSingleXmiIntoCAS(File xmiFile, CAS cas)
			throws InvalidXMLException, ResourceInitializationException,
			IOException, CollectionException {
		CollectionReader reader = UIMAToolkit.createCollectionReader(
				new XMLInputSource(new File(SIDEToolkit.rootFolder, "descriptors/XmiFileReader.xml")),
				new String[][] { new String[] { XmiFileReader.PARAM_INPUTFILE,
						xmiFile.getAbsolutePath() } });

		// default AE with
		if (reader.hasNext()) {
			reader.getNext(cas);
		}
		reader.destroy();
	}

	/**
	 * CAS should not be created directly from SIDE by using this method.
	 * For caching SIDE to be working properly, createSIDECAS(File xmiFile)
	 * should be used.
	 *  
	 * @return
	 * @throws InvalidXMLException
	 * @throws IOException
	 * @throws ResourceInitializationException
	 */
	public static CAS createSIDECAS() throws InvalidXMLException, IOException,
			ResourceInitializationException {
		
		String casConsumerDescriptorFilePath = "descriptors/XmiWriterCasConsumer.xml"; 
		File casConsumerDescriptorFile = new File(SIDEToolkit.rootFolder, casConsumerDescriptorFilePath);
		CasConsumerDescription ccDesc = UIMAFramework.getXMLParser()
				.parseCasConsumerDescription(
						new XMLInputSource(casConsumerDescriptorFile));

		File descriptorFile = UIMAToolkit.sideTypeSystemDescriptionFile;

		// parse descriptor. Could be either AE or TypeSystem descriptor
		TypeSystemDescription tsDesc = UIMAFramework.getXMLParser()
				.parseTypeSystemDescription(new XMLInputSource(descriptorFile));
		// instantiate CAS to get type system. Also build style map file if
		// there is none.
		tsDesc.resolveImports();

		List<MetaDataObject> descriptions = new ArrayList<MetaDataObject>();
		descriptions.add(ccDesc);
		descriptions.add(tsDesc);

		CAS cas = CasCreationUtils.createCas(descriptions);
		return cas;
	}
	
	
	public static JCas createSIDEJCas(File xmiFile) throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException{
		return getJCas(createSIDECAS(xmiFile));
	}
	public static CAS createSIDECAS(File xmiFile) throws InvalidXMLException, IOException,
	ResourceInitializationException, CollectionException {
		CAS cas =null; 
		if(cas==null){
			cas = createSIDECAS();
			readSingleXmiIntoCAS(xmiFile, cas);
		}
		return cas;
	}
	
	public static void cacheCAS(File xmiFile, CAS cas){
//		casCache.put(xmiFile, cas);
	}

	public static ListValueMap<String, SIDESegment> getSIDESegmentListedValueMap(
			File xmiFile, int uimaAnnotationType,
			String annotationSubtypeNameRegex) throws InvalidXMLException,
			ResourceInitializationException, IOException, CollectionException,
			CASException {
		CAS cas = createSIDECAS(xmiFile);
		ListValueMap<String, SIDESegment> map = UIMAToolkit
				.getSIDESegmentListedValueMap(cas.getJCas(),
						uimaAnnotationType, annotationSubtypeNameRegex);
		cas.reset();
		return map;
	}

	private static ListValueMap<String, SIDESegment> getSIDESegmentListedValueMap(
			JCas jCas, int uimaAnnotationType, String annotationSubtypeNameRegex) {
		AnnotationIndex annotationIndex = jCas
				.getAnnotationIndex(uimaAnnotationType);
		Iterator<?> iterator = annotationIndex.iterator();

		ListValueMap<String, SIDESegment> listedValueMap = new ListValueMap<String, SIDESegment>();
		Pattern pattern = Pattern.compile(annotationSubtypeNameRegex);

		while (iterator.hasNext()) {
			SIDESegment sideSegmentation = (SIDESegment) iterator.next();
			String annotationSubtypeName = sideSegmentation.getSubtypeName();
			if (annotationSubtypeNameRegex == null
					|| pattern.matcher(annotationSubtypeName).matches()) {
				listedValueMap.add(annotationSubtypeName, sideSegmentation);
			}
		}
		return listedValueMap;
	}
	
	public static class SIDESegmentSubtypeNameFilter implements Filter<Object>{
		private String subtypeName;
		
		private SIDESegmentSubtypeNameFilter(String subtypeName){
			this.subtypeName = subtypeName;
		}

		@Override
		public boolean accept(Object o) {
			if(!(o instanceof SIDESegment)){ return false; }
			SIDESegment sideSegment = (SIDESegment)o;
			String thisSubtypeName = sideSegment.getSubtypeName();
			return thisSubtypeName==null?false:thisSubtypeName.equals(subtypeName);
		}
		
		public static SIDESegmentSubtypeNameFilter create(String subtypeName){
			return new SIDESegmentSubtypeNameFilter(subtypeName);
		}
		
	}
	
//	public static class Document{
//		private JCas jCas;
//		private String subtypeName;
//		public Document(JCas jCas, String subtypeName){
//			this.jCas = jCas;
//			this.subtypeName = subtypeName;
//		}
//		public JCas getJCas() {
//			return jCas;
//		}
//		public String getSubtypeName() {
//			return subtypeName;
//		}
//	}
	
	public static boolean isSubtypeNameContainedByAny(JCas[] jCasArray, int type, String desiredSubtypeName) {
		for(JCas jCas : jCasArray){
			Set<String> subtypeNameSet = UIMAToolkit.getSubtypeNameSet(jCas, type);
			if(subtypeNameSet==null){ continue; }
			if(subtypeNameSet.contains(desiredSubtypeName)){
				return true;
			}
		}
		return false;
	}
	
	public static class DocumentList implements XMLable, DocumentListInterface{
		private List<JCas> jCasList = new ArrayList<JCas>();
		private String subtypeName;
		private int type = SIDESegment.type;
		public static final String tagName = "documentList";
		public Map<String, ArrayList<String>> cachedAnnotations = new TreeMap<String, ArrayList<String>>();
		private DocumentList(){}
		
		public DocumentList(String subtypeName){
			this.subtypeName = subtypeName;
		}
		
		public static DocumentListInterface create(Collection<? extends JCas> jCasCollection, String subtypeName){
			DocumentList documentList = new DocumentList(subtypeName);
			documentList.getJCasList().addAll(jCasCollection);
			return documentList;
		}
		public static DocumentList create(JCas[] jCasArray, String subtypeName){
			DocumentList documentList = new DocumentList(subtypeName);
			CollectionsToolkit.addAll(documentList.getJCasList(), jCasArray);
			return documentList;
		}
		
		public static final String tempSummaryBaseSubtypeName = "summary_tmp";
		
		public DocumentList createSummaryDocumentList(Map<String, int[]> segmentLastIndexArrayMap){
			
			DocumentList documentList = new DocumentList();
			
			documentList.jCasList.addAll(this.getJCasList());
			documentList.subtypeName = tempSummaryBaseSubtypeName;
			
			for(JCas jCas : this.getJCasList()){
				UIMAToolkit.removeSIDEBaseAnnotationList(jCas, SIDESegment.type, tempSummaryBaseSubtypeName);
				
				int[] endIndexArray = segmentLastIndexArrayMap.get(UIMAToolkit.getXmiURIString(jCas));
				
				int startIndex = 0;
				for(int endIndex : endIndexArray){
					SIDESegment sideSegment = new SIDESegment(jCas, startIndex, endIndex);
					sideSegment.setSubtypeName(tempSummaryBaseSubtypeName);
					sideSegment.addToIndexes();
					startIndex = endIndex;
				}
				try {
					UIMAToolkit.saveJCasToSource(jCas);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return documentList;
		}
		public static DocumentList create(Element root){
			DocumentList documentList = new DocumentList();
			documentList.fromXML(root);
			return documentList;
		}
		
		public String toXML(){
			StringBuilder builder = new StringBuilder();
			for(String uriString : UIMAToolkit.getXmiURIStringArray(this.getJCasList().toArray(new JCas[0]))){
				builder.append(XMLToolkit.wrapContentWithTag(uriString, "doc"));
			}
			builder.append(XMLToolkit.wrapContentWithTag(this.getSubtypeName(), "subtypename"));
			return XMLToolkit.wrapContentWithTag(builder, tagName).toString();
		}
		
		private static Map<String, File> fileMap = new HashMap<String,File>();
		private static File getFileFromURI(String uriString){
			URI uri;
			try { uri = new URI(uriString); }
			catch (URISyntaxException e) { throw new RuntimeException(e); }
			
			File file = new File(uri);
			if(file.exists()){ return file; }
			
			// see if the file is in default xmi folder
			file = new File(SIDEToolkit.xmiFolder, file.getName());
			if(file.exists()){ return file; }
			
			file = fileMap.get(uriString);
			if(file!=null){ return file; }
				
			return askFileFromUser(uriString);
		}
		
		private static File askFileFromUser(String uriString){
			String[] messageArray = new String[]{
					"link broken:",
					uriString,
			};
			JOptionPane.showMessageDialog(null, StringToolkit.toString(messageArray, StringToolkit.newLine()), "error", JOptionPane.ERROR_MESSAGE);
			
			JFileChooser fileChooser = new JFileChooser();
			
			File originalFile = null;
			try { originalFile = new File(new URI(uriString));}
			catch (URISyntaxException e) { throw new RuntimeException(e); }
			fileChooser.setSelectedFile(originalFile);
			
			int result = fileChooser.showOpenDialog(null);
			if(result!=JFileChooser.APPROVE_OPTION){ return null; }
			
			File selectedFile = fileChooser.getSelectedFile();
			if(selectedFile!=null){ fileMap.put(uriString, selectedFile); }
			
			return selectedFile;
		}
		
		public void fromXML(Element root){
			for(Element element : XMLToolkit.getChildElements(root)){
				String tagName = element.getTagName();
				if(tagName.equalsIgnoreCase("doc")){
					JCas jCas;
					try {
						File file = getFileFromURI(XMLToolkit.getTextContent(element).toString());
						if(file==null){ continue; }
						
						jCas = UIMAToolkit.createSIDEJCas(file);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					jCasList.add(jCas);
				}else if(tagName.equalsIgnoreCase("subtypename")){
					this.subtypeName = XMLToolkit.getTextContent(element).toString();
				}else{
					throw new UnsupportedOperationException();
				}
			}
		
		}
		public void saveEach() throws InvalidXMLException, ResourceInitializationException, ResourceProcessException, IOException, URISyntaxException {
			for(JCas jCas : jCasList){
				URI uri = new URI(UIMAToolkit.getXmiURIString(jCas));
				UIMAToolkit.saveCas(jCas.getCas(), new File(uri));
			}
		}

		public List<JCas> getJCasList() {
			return jCasList;
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#getSize()
		 */
		public int getSize(){
			
			int size=0;
			for(Iterator<Object> iterator = iterator(); iterator.hasNext(); size++){
				iterator.next();
			}
			return size;
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#allAnnotations()
		 */
		public HashMap<String, ArrayList<String>> allAnnotations(){
			HashMap<String, ArrayList<String>> annotations = new HashMap<String, ArrayList<String>>();
			Set<String> cols = new HashSet<String>();
			for(JCas cas : jCasList){
				Set<String> names = getSubtypeNameSet(cas, SIDEAnnotation.type);
				cols.addAll(names);
			}
			for(String name : cols){
				annotations.put(name, getAnnotationArray(name));
			}
			return annotations;
		}
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#getAnnotationArray(java.lang.String)
		 */
		public ArrayList<String> getAnnotationArray(String name) {
			if(cachedAnnotations.containsKey(name)) return cachedAnnotations.get(name);
			ArrayList<String> annotationList = new ArrayList<String>();
			for(Iterator<Object> documentIterator = this.iterator(name); documentIterator.hasNext();){
				Object object = documentIterator.next();
				if(object instanceof SIDEAnnotation){
//				YeriDebug.ASSERT(object instanceof SIDEAnnotation);
				SIDEAnnotation sideAnnotation = (SIDEAnnotation)object;
				annotationList.add(sideAnnotation.getLabelString());
				}
			}
			cachedAnnotations.put(name, annotationList);
			return annotationList;
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#fullIterator()
		 */
		public Iterator<Object> fullIterator(){
			Iterator<?>[] iteratorArray = new Iterator<?>[jCasList.size()];
			for(int i=0; i<jCasList.size(); i++){
				iteratorArray[i] = jCasList.get(i).getAnnotationIndex(type).iterator();
			}
			return CompoundIterator.create(iteratorArray);
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#iterators()
		 */
		public HashMap<String, Iterator<?>> iterators(){
			HashMap<String, Iterator<?>> its = new HashMap<String, Iterator<?>>();
			for(int i=0; i < jCasList.size(); i++){
				its.put(i + ":", jCasList.get(i).getAnnotationIndex(type).iterator());
			}
			return its;
		}
		
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#iterator(java.lang.String)
		 */
		public Iterator<Object> iterator(String subtype){
			Iterator<Object> compoundIterator = fullIterator();
			Iterator<Object> filteredIterator = FilteredIterator.create(compoundIterator, SIDESegmentSubtypeNameFilter.create(subtype));
			return filteredIterator;
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#iterator()
		 */
		public Iterator<Object> iterator(){
			return iterator(subtypeName);
		}
		
		public boolean hasDocument(){
			return jCasList.size()>0;
		}
		public String getSubtypeName() {
			return subtypeName;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getDescription() {
			StringBuilder builder = new StringBuilder();
			builder.append("source:").append(StringToolkit.newLine());
			for(JCas jCas : this.getJCasList()){
				builder.append('\t'+UIMAToolkit.getSIDEDocumentSetting(jCas).getSourceURI()).append(StringToolkit.newLine());
			}
			builder.append("subtype: ").append(this.getSubtypeName());
			return builder.toString();
		}

		public double getPKValue(SegmenterPlugin selectedSegmenterPlugin) {
			
//			CharSequence builder = FileToolkit.readFrom( new File("c:/temp/test01.txt"));
//			int[] referenceSegmentEndIndexArray = StringToolkit.getTokenEndIndexArray(builder, StringToolkit.newlinePattern);
			
			List<Integer> referenceSegmentLengthList = new ArrayList<Integer>();
			
			StringBuilder builder = new StringBuilder();
			Iterator<Object> iterator = this.iterator();
			while(iterator.hasNext()){
				SIDESegment sideSegment = (SIDESegment)iterator.next();
				String text = sideSegment.getCoveredText();
				
				int length = UIMAToolkit.getLength(sideSegment);
				referenceSegmentLengthList.add(length);
				
				builder.append(text);
			}
			
			int[] referenceSegmentEndIndexArray = StatisticsToolkit.lengthArrayToEndIndexArray(CollectionsToolkit.toIntArray(referenceSegmentLengthList));
			int[] hypothesisSegmentEndIndexArray;
			try {
				hypothesisSegmentEndIndexArray = selectedSegmenterPlugin.getSegmentEndIndexArray(builder);
			} catch (Exception e) {
				throw new RuntimeException();
			}
			int[] tokenEndIndexArray = StringToolkit.getTokenEndIndexArray(builder, StringToolkit.whiteSpacePattern);
					
			return SegmentationMetricToolkit.getPKMetricByEndIndexArray(tokenEndIndexArray, hypothesisSegmentEndIndexArray, referenceSegmentEndIndexArray);
		}
		
		public class IndexedSIDESegment implements Comparable<IndexedSIDESegment>{
			private int index;
			private SIDESegment sideSegment;
			
			public IndexedSIDESegment(int index, SIDESegment sideSegment){
				this.index = index;
				this.sideSegment = sideSegment;
			}
			
			public DocumentListInterface getDocumentList(){ return DocumentList.this; }

			public int getIndex() {
				return index;
			}

			public SIDESegment getSideSegment() {
				return sideSegment;
			}

			@Override
			public int compareTo(IndexedSIDESegment o) {
				return this.getIndex()-o.getIndex();
			}
		}
		
		public List<SIDESegment> getSIDESegmentList() {
			List<SIDESegment> sideSegmentList = new ArrayList<SIDESegment>();
			
			for(Iterator<?> iterator = this.iterator(); iterator.hasNext(); ){
				SIDESegment sideSegment = (SIDESegment)iterator.next();
				sideSegmentList.add(sideSegment);
			}
			return sideSegmentList;
		}
		public String getBaseSubtypeName() {
			Iterator<Object> iterator = this.iterator();
			if(!iterator.hasNext()){ return null; }
			
			SIDESegment sideSegment = (SIDESegment)this.iterator().next();
			if(sideSegment instanceof SIDEAnnotation){
				sideSegment = ((SIDEAnnotation)sideSegment).getSourceSegmentation();
			}
			return sideSegment.getSubtypeName();
		}
		public DocumentListInterface createDocumentListWithNewSubtype(String subtypeName) {
			DocumentList documentList = new DocumentList(subtypeName);
			documentList.getJCasList().addAll(this.getJCasList());
			return documentList;
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#getLabelArray()
		 */
		public String[] getLabelArray(){
			return getLabelPaintMap().keySet().toArray(new String[0]);
		}
		public Paint[] getPaintArray(){
			return getLabelPaintMap().values().toArray(new Paint[0]);
		}
		
		public Map<String,Paint> getLabelPaintMap(){
			Map<String,Paint> labelPaintMap = new HashMap<String,Paint>();
			Set<SIDEAnnotationSetting> sideAnnotationSettingSet = new HashSet<SIDEAnnotationSetting>();
			
			String subtypeName = null;
			for(Iterator<Object> iterator = this.iterator(); iterator.hasNext();){
				Object object = iterator.next();
				if(!(object instanceof SIDEAnnotation)){ continue; }
				
				SIDEAnnotation sideAnnotation = (SIDEAnnotation)object;
				String currentSubtypeName = sideAnnotation.getSubtypeName();
				
				if(subtypeName==null){
					subtypeName = currentSubtypeName;
				}else{
					YeriDebug.ASSERT(subtypeName.equals(currentSubtypeName));
				}
				
				SIDEAnnotationSetting sideAnnotationSetting = sideAnnotation.getSetting();
				if(!sideAnnotationSettingSet.contains(sideAnnotationSetting)){
					SortedMap<String,Color> labelColorMap = UIMAToolkit.getLabelColorMap(sideAnnotationSetting);
					for(String key : labelColorMap.keySet()){
//						if(!nullIncluded && key==null){ continue; }
						if(labelPaintMap.containsKey(key)){ continue; }
						labelPaintMap.put(key, labelColorMap.get(key));
					}
					sideAnnotationSettingSet.add(sideAnnotationSetting);
				}
				
				String annotationString = sideAnnotation.getLabelString();
				if(!labelPaintMap.containsKey(annotationString)
//						&& (annotationString!=null && !nullIncluded)
						){
					labelPaintMap.put(annotationString, Color.darkGray);
				}
			}
			return labelPaintMap;
		}

		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#getAnnotationIndexArray(java.lang.String[])
		 */
		public int[] getAnnotationIndexArray(String[] targetAnnotationListArray){
			ArrayList<String> annotationArray = this.getAnnotationArray();
			YeriDebug.ASSERT_compareInteger(targetAnnotationListArray.length, annotationArray.size());
			return CollectionsToolkit.getIndexArrayOfValueArray(this.getLabelArray(), targetAnnotationListArray);
		}
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#getAnnotationIndexArray()
		 */
		public int[] getAnnotationIndexArray(){
			String[] labelArray = this.getLabelArray();
			String[] annotationArray = this.getAnnotationArray().toArray(new String[0]);
			return CollectionsToolkit.getIndexArrayOfValueArray(labelArray, annotationArray);
		}
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#getAnnotationArray()
		 */
		public ArrayList<String> getAnnotationArray() {
			return getAnnotationArray(subtypeName);
		}
		public void setSubtypeName(String subtypeName) {
			this.subtypeName = subtypeName;
		}
		
		public Map<String, int[]> getSegmentEndIndexArrayMap(Set<String> baseSubtypeNameSet) {
			
			Map<String, int[]> segmentEndIndexArrayMap = new HashMap<String,int[]>();
			
			for(JCas jCas : this.getJCasList()){
				int[] endIndexArray = null;
				for(String baseSubtypeName : baseSubtypeNameSet){
					int[] currentEndIndexArray = UIMAToolkit.toLastIndexArray(UIMAToolkit.getSIDESegmentList(jCas, SIDESegment.type, baseSubtypeName));
					if(endIndexArray==null){
						endIndexArray = currentEndIndexArray;
					}else{
						endIndexArray = CollectionsToolkit.union(endIndexArray, currentEndIndexArray);
					}
				}
				YeriDebug.ASSERT(endIndexArray!=null && endIndexArray.length>0);
				segmentEndIndexArrayMap.put(UIMAToolkit.getXmiURIString(jCas), endIndexArray);
			}
			return segmentEndIndexArrayMap;
		}
		public SIDEAnnotationSetting[] getSIDEAnnotationSettingArray() {
			List<JCas> jCasList = this.getJCasList();
			if(jCasList==null || jCasList.size()==0){ return null; }
			
			SIDEAnnotationSetting[] settingArray = new SIDEAnnotationSetting[jCasList.size()];
			for(int i=0; i<jCasList.size(); i++){
				JCas jCas = jCasList.get(i);
				settingArray[i] = UIMAToolkit.getSIDEAnnotationSetting(jCas, this.getSubtypeName());
			}
			return settingArray;
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#getCoveredTextList()
		 */
		public List<String> getCoveredTextList() {
			List<String> coveredTextList = new ArrayList<String>();
			
			for(Iterator<Object> iterator = this.iterator(); iterator.hasNext();){
				SIDESegment sideSegment = (SIDESegment)iterator.next();
				coveredTextList.add(sideSegment.getCoveredText());
			}
			return coveredTextList;
		}
		public int getJCasIndex(JCas jCas) {
			String xmiURIString = UIMAToolkit.getXmiURIString(jCas);
			if(xmiURIString==null){ throw new UnsupportedOperationException(); }
			
			List<JCas> jCasList = this.getJCasList();
			for(int i=0; i<jCasList.size(); i++){
				JCas thisJCas = jCasList.get(i);
				if(xmiURIString.equals(UIMAToolkit.getXmiURIString(thisJCas))){
					return i;
				}
			}
			return -1;
		}
		
		/* (non-Javadoc)
		 * @see edu.cmu.side.uima.DocumentListInterface#getInferredDatatype()
		 */
		public Datatype getInferredDatatype() {
			SIDEAnnotationSetting[] settingArray = this.getSIDEAnnotationSettingArray();
			Datatype totalDatatype = UIMAToolkit.Datatype.AUTO;
			for(int i=0; i<settingArray.length; i++){
				Datatype datatype = Datatype.fromString(settingArray[i].getDatatypeString());
				totalDatatype = UIMAToolkit.Datatype.merge(totalDatatype, datatype);
			}
			if(totalDatatype==Datatype.NOMINAL){ return totalDatatype; }
			
			for(Iterator<Object> iterator = this.iterator(); iterator.hasNext(); ){
				SIDEAnnotation sideAnnotation = (SIDEAnnotation)iterator.next();
				String annotationLabelString = sideAnnotation.getLabelString();
				if(!StringToolkit.isNumber(annotationLabelString)){
					return Datatype.NOMINAL;
				}
			}
			return Datatype.NUMERIC;
		}

		@Override
		public String getCurrentAnnotation() {
			return subtypeName;
		}

		@Override
		public void setCurrentAnnotation(String annot) {
			if(allAnnotations().containsKey(annot)){
				subtypeName = annot;
			}
		}
	}
	
	public static List<SIDESegment> getSIDESegmentList(JCas jCas,
			int uimaAnnotationType, String subtypeName) {
		if(jCas==null){ return null; }
		Iterator<?> iterator = jCas.getAnnotationIndex(uimaAnnotationType)
				.iterator();

		List<SIDESegment> returnList = new ArrayList<SIDESegment>();
		while (iterator.hasNext()) {
			SIDESegment sideSegmentation = (SIDESegment) iterator.next();
			String thisSubtypeName = sideSegmentation.getSubtypeName();
			if (subtypeName == null
					|| thisSubtypeName.equals(subtypeName)) {
				returnList.add(sideSegmentation);
			}
		}
		return returnList;
	}
	
	public static int[] toLastIndexArray(List<? extends Annotation> annotationList) {
		List<Integer> list = new ArrayList<Integer>();
		for(Annotation annotation : annotationList){
			list.add(annotation.getEnd());
		}
		return CollectionsToolkit.toIntArray(list);
	}

	public static int getLength(SIDESegment sideSegment) {
		return sideSegment.getEnd()-sideSegment.getBegin();
	}


	public static SortedSet<String> getAnnotationSubtypeSet(JCas jCas){
		SortedSet<String> annotationNameSet = new TreeSet<String>();
		for(SIDESegment sideSegment : UIMAToolkit.getSIDESegmentList(jCas, SIDEAnnotation.type, null)){
			annotationNameSet.add(sideSegment.getSubtypeName());
		}
		return annotationNameSet;
	}
	
	public static SortedSet<String> getAnnotationBaseSubtypeSet(JCas jCas){
		SortedSet<String> annotationNameSet = new TreeSet<String>();
		for(SIDESegment sideSegment : UIMAToolkit.getSIDESegmentList(jCas, SIDESegment.type, null)){
			if(sideSegment instanceof SIDEAnnotation){
				continue; 
			}
			annotationNameSet.add( sideSegment.getSubtypeName());
		}
		return annotationNameSet;
	}

	public static void removeSIDEAnnotationList(JCas[] jCasArray, int uimaAnnotationType, String annotationSubtypeName) {
		for(JCas jCas : jCasArray){
			removeSIDEAnnotationList(jCas, uimaAnnotationType, annotationSubtypeName);
		}
		
	}
	public static void removeSIDEAnnotationList(JCas jCas,
			int uimaAnnotationType, String annotationSubtypeName) {

		AnnotationIndex annotationIndex = jCas
				.getAnnotationIndex(uimaAnnotationType);
		Iterator<?> iterator = annotationIndex.iterator();

		List<SIDESegment> removeList = new ArrayList<SIDESegment>();
		while (iterator.hasNext()) {
			SIDESegment next = (SIDESegment) iterator.next();
			if (next.getSubtypeName().equals(annotationSubtypeName)) {
				removeList.add(next);
			}
		}
		for (SIDESegment sideSegmentation : removeList) {
			sideSegmentation.removeFromIndexes(jCas);
		}
	}
	
	public static void removeSIDEBaseAnnotationList(JCas jCas,
			int uimaAnnotationType, String baseAnnotationSubtypeName) {

		AnnotationIndex annotationIndex = jCas
				.getAnnotationIndex(uimaAnnotationType);
		Iterator<?> iterator = annotationIndex.iterator();

		List<SIDESegment> removeList = new ArrayList<SIDESegment>();
		while (iterator.hasNext()) {
			SIDESegment next = (SIDESegment) iterator.next();
			if (next.getSubtypeName().equals(baseAnnotationSubtypeName)) {
				removeList.add(next);
			}
			
			if(!(next instanceof SIDEAnnotation)){ continue; }
			
			SIDEAnnotation sideAnnotation = (SIDEAnnotation)next;
			if(sideAnnotation.getSourceSegmentation().getSubtypeName().equals(baseAnnotationSubtypeName)){
				removeList.add(next);
			}
		}
		for (SIDESegment sideSegmentation : removeList) {
			sideSegmentation.removeFromIndexes(jCas);
		}
	}
	
	public static void saveJCasToSource(JCas jCas) throws URISyntaxException, InvalidXMLException, ResourceInitializationException, ResourceProcessException, IOException{
		URI uri = new URI(UIMAToolkit.getXmiURIString(jCas));
		UIMAToolkit.saveCas(jCas.getCas(), new File(uri));	
	}
	

	public static void saveCas(CAS cas, File ofile) throws InvalidXMLException,
			ResourceInitializationException, IOException,
			ResourceProcessException {
		CasConsumer writer = createCasConsumer(new XMLInputSource(
				"descriptors/XmiFileWriterCasConsumer.xml"),
				new String[][] { new String[] { "OutputFile",
						ofile.getAbsolutePath() } });
		writer.processCas(cas);
		writer.destroy();
	}

	public static Map<Long, ColorLabel> getColorLabelMap(
			SIDEAnnotationSetting sideAnnotationSetting) {
		if(sideAnnotationSetting==null){ return null; }
		
		String labelColorMapString = sideAnnotationSetting
				.getLabelColorMapString();
		if (labelColorMapString == null || labelColorMapString.length() == 0) {
			sideAnnotationSetting.setLabelColorMapString("");
			return new HashMap<Long, ColorLabel>();
		}

		Map<Long, ColorLabel> returnValue = new HashMap<Long, ColorLabel>();
		String[] pairArray = labelColorMapString.split(labelColorAndString);
		for (String pair : pairArray) {
			ColorLabel colorLabel = ColorLabel.createFromCode(pair);
			returnValue.put(colorLabel.getId(), colorLabel);
		}

		return returnValue;
	}

	public static SortedMap<String, Color> getLabelColorMap(
			SIDEAnnotationSetting sideAnnotationSetting) {
		if(sideAnnotationSetting==null){ return null; }
		String labelColorMapString = sideAnnotationSetting
				.getLabelColorMapString();
		if (labelColorMapString == null || labelColorMapString.length() == 0) {
			sideAnnotationSetting.setLabelColorMapString("");
			return new TreeMap<String, Color>();
		}

		String[] pairArray = labelColorMapString.split(labelColorAndString);

		SortedMap<String, Color> map = new TreeMap<String, Color>();

		for (int i = 0; i < pairArray.length; i++) {
			String eachPair = pairArray[i];
			String[] labelColor = eachPair.split(labelColorEqualsString);
			YeriDebug.ASSERT_compareInteger(labelColor.length, 3);
			Color color = new Color(Integer.parseInt(labelColor[2]));
			map.put(labelColor[1], color);
		}
		return map;
	}

	public static String makeCSVAnnotationTypeID(String columnName) { return "csv_" + columnName; }
	public static String makeCSVAnnotationTypeName(String columnName) { return columnName; }
	public static String makeCSVSegmentationTypeID() { return "csv"; }
	public static String makeCSVSegmentationTypeName() { return "native"; }

	public static ResourceSpecifier getResourceSpecifier(File infile)
			throws IOException, InvalidXMLException {
		XMLInputSource in = new XMLInputSource(infile);
		return UIMAFramework.getXMLParser().parseResourceSpecifier(in);
	}
	
	private static void setupParameters(ResourceCreationSpecifier spec,
			String[][] parameterMap) throws InvalidXMLException {
		ConfigurationParameterSettings configurationParameterSettings = spec
				.getMetaData().getConfigurationParameterSettings();
		if (parameterMap != null) {
			for (String[] entry : parameterMap) {
				YeriDebug.ASSERT(entry.length == 2);
				configurationParameterSettings.setParameterValue(entry[0],
						entry[1]);
			}
		}
	}

	public static CollectionReader createCollectionReader(
			XMLInputSource xmlInputSource, String[][] parameterMap)
			throws IOException, InvalidXMLException,
			ResourceInitializationException {
		CollectionReaderDescription collectionReaderDescription = UIMAFramework
				.getXMLParser()
				.parseCollectionReaderDescription(xmlInputSource);
		setupParameters(collectionReaderDescription, parameterMap);
		CollectionReader reader = UIMAFramework
				.produceCollectionReader(collectionReaderDescription);
		return reader;
	}

	public static AnalysisEngine createAnalysisEngine(
			XMLInputSource xmlInputSource, String[][] parameterMap)
			throws IOException, InvalidXMLException,
			ResourceInitializationException {
		AnalysisEngineDescription analysisEngineDescription = UIMAFramework
				.getXMLParser().parseAnalysisEngineDescription(xmlInputSource);
		setupParameters(analysisEngineDescription, parameterMap);
		AnalysisEngine ae = UIMAFramework
				.produceAnalysisEngine(analysisEngineDescription);
		return ae;
	}

	public static CasConsumer createCasConsumer(XMLInputSource xmlInputSource,
			String[][] parameterMap) throws IOException, InvalidXMLException,
			ResourceInitializationException {
		CasConsumerDescription casConsumerDescription = UIMAFramework
				.getXMLParser().parseCasConsumerDescription(xmlInputSource);
		setupParameters(casConsumerDescription, parameterMap);
		CasConsumer writer = UIMAFramework
				.produceCasConsumer(casConsumerDescription);
		return writer;
	}

	public static void readCSVFile(File csvFile) throws IOException,
			InvalidXMLException, ResourceInitializationException,
			ResourceProcessException {

		CollectionReader reader = createCollectionReader(
				new XMLInputSource(
						"descriptors/CSVCollectionReaderDescriptor.xml"),
				new String[][] { new String[] {
						CSVCollectionReader.PARAM_INPUTDIR,
						"C:/yeri/projects/summarization/SIDE_environment/SIDE_src/SIDE/data/articles/csv/" } });

		AnalysisEngine ae = createAnalysisEngine(new XMLInputSource(
				"descriptors/SIDESegmenterWrapper.xml"), null);

		CasConsumer writer = createCasConsumer(
				new XMLInputSource("descriptors/XmiWriterCasConsumer.xml"),
				new String[][] { new String[] {
						XmiWriterCasConsumer.PARAM_OUTPUTDIR,
						"C:/yeri/projects/summarization/SIDE_environment/result" } });

		List<ProcessingResourceMetaData> list = new ArrayList<ProcessingResourceMetaData>();
		list.add(ae.getAnalysisEngineMetaData());
		list.add(writer.getProcessingResourceMetaData());
		CAS cas = CasCreationUtils.createCas(list);

		try {
			while (reader.hasNext()) {
				try {
					reader.getNext(cas);
					ae.process(cas);
					writer.processCas(cas);
					cas.reset();
				} catch (CollectionException ex) {
					YeriDebug.die(ex);
				} catch (AnalysisEngineProcessException ex) {
					YeriDebug.die(ex);
				}
			}
			writer.destroy();
			ae.destroy();
			reader.destroy();
		} catch (CollectionException ex) {
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) throws Exception{
		readDexMLIntoCas(createSIDECAS(), new File("/home/elijah/Foulser.PC.3-11-04.xml"));
	}
	
	public static void readDexMLIntoCas(CAS cas, File file){
		JCas jCas = getJCas(cas);
		DOMParser parser = new DOMParser();
		int currentLength = 0;
		StringBuilder sb = new StringBuilder();
		try{
			parser.parse(file.getAbsolutePath());
		}catch(Exception e){e.printStackTrace();}
		Document doc = parser.getDocument();
		NodeList document = doc.getElementsByTagName("TEI.2").item(0).getChildNodes();
		Node header = getChildByType(document, "teiHeader"); 
		NodeList text = getChildByType(getChildByType(document, "text").getChildNodes(), "body").getChildNodes();
		SIDEAnnotationSetting whoSetting = UIMAToolkit.createSIDEAnnotationSetting(jCas);
		Map<String, Color> labelColorMap = UIMAToolkit.getLabelColorMap(whoSetting);
		SIDEAnnotationSetting segSetting = UIMAToolkit.createSIDEAnnotationSetting(jCas);
		Map<String, Color> segColorMap = UIMAToolkit.getLabelColorMap(segSetting);

		for(int i = 0; i < text.getLength(); i++){
			if(text.item(i).getNodeName().equals("u")){
				currentLength = collectSpeechAct(sb, currentLength, jCas, whoSetting, labelColorMap, text.item(i));
			}
		}
		addAnnotationLayer(jCas, UIMAToolkit.makeCSVSegmentationTypeName(), "who");
		jCas.setDocumentText(sb.toString());
		try{
			addSourceDocumentInformation(jCas, file.getAbsoluteFile().toURI(), (int)file.length());			
		}catch(Exception e){e.printStackTrace();}
	}
	
	public static int collectSpeechAct(StringBuilder sb, int current, JCas j, SIDEAnnotationSetting whoSetting, Map<String, Color> colors, Node u){
		NodeList segs = u.getChildNodes();
		String who = u.getAttributes().getNamedItem("who").getTextContent();
		for(int i = 0; i < segs.getLength(); i++) if(!nameMatch(segs.item(i), "#text")){
			if(nameMatch(segs.item(i), "seg")){
				String text = segs.item(i).getTextContent();
				sb.append(text);
				SIDEAnnotation segmentation = new SIDEAnnotation(j);
				segmentation.setBegin(current);
				segmentation.setEnd(current+text.length());
				segmentation.setSubtypeName(UIMAToolkit.makeCSVSegmentationTypeName());
				String id = segs.item(i).getAttributes().getNamedItem("id").getTextContent();
				segmentation.setLabelString(id);
//				segmentation.addToIndexes();
				
				
				SIDEAnnotation annotation = new SIDEAnnotation(j);
				annotation.setBegin(current);
				annotation.setEnd(current+text.length());
				annotation.setSubtypeName("who");
				annotation.setLabelString(who);
				annotation.setSourceSegmentation(segmentation);

				Color color = colors.get(who);
				if (color == null) {
					ColorLabel colorLabel = ColorLabel
							.createFromLabel(who);
					Map<Long, ColorLabel> colorLabelMap = UIMAToolkit
							.getColorLabelMap(whoSetting);
					colorLabelMap.put(colorLabel.getId(), colorLabel);
					UIMAToolkit.setColorLabelString(whoSetting, colorLabelMap
							.values().toArray(new ColorLabel[0]), false);
				}
				annotation.setSetting(whoSetting);
				annotation.addToIndexes();

				current += text.length();
			}
		}
		return current;
	}
	
	public static Node getChildByType(NodeList parent, String name){
		for(int i = 0; i < parent.getLength(); i++) if(nameMatch(parent.item(i), name)) return parent.item(i);
		return null;
	}
	
	public static boolean nameMatch(Node n, String s){ return n.getNodeName().equals(s); }
	
	public static void readDexMLCodeFileIntoCas(CAS cas, File f){
		JCas jc = getJCas(cas);
		
		String annotationName = f.getName().substring(0, f.getName().indexOf(".xml"));
		DOMParser parser = new DOMParser();
		System.out.println(f.getAbsolutePath());
		try{
			parser.parse(f.getAbsolutePath());
		}catch(Exception e){e.printStackTrace();}
		Document doc = parser.getDocument();
		NodeList types = getChildByType(getChildByType(doc.getChildNodes(), "dexter_code_set").getChildNodes(), "data").getChildNodes();
		SIDEAnnotationSetting codeSetting = UIMAToolkit.createSIDEAnnotationSetting(jc);
		List<SIDESegment> sideSegmentList = UIMAToolkit.getSIDESegmentList(jc, SIDESegment.type, null);
		for(int i = 0; i < types.getLength(); i++){
			String typeName = types.item(i).getNodeName();
			if(!typeName.startsWith("#")){
				NodeList tokens = types.item(i).getChildNodes();
				for(int j = 0; j < tokens.getLength(); j++){
					Node token = tokens.item(j);
					if(!token.getNodeName().startsWith("#")){
						NodeList atts = token.getChildNodes();
						String start = getChildByType(atts, "start_string").getTextContent();
						String end = getChildByType(atts, "end_string").getTextContent();
						System.out.println(start + ", " + end);
						for(SIDESegment seg : sideSegmentList){
							if(seg instanceof SIDEAnnotation){
								System.out.println(((SIDEAnnotation)seg).getCoveredText());
							}
						}
					}
				}
			}
		}

	}
	
	public static void readCSVIntoCas(CAS cas, File file, String textColumnName, boolean isLast, boolean headerExist)
			throws CollectionException, IOException {
		JCas jCas = getJCas(cas);

		// open input stream to xmiFile
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = reader.readLine()) != null){
			sb.append(line + "\n");
		}
		String out = sb.toString().replaceAll("[^\r\n\\p{ASCII}]", "");
		CSVReader csvReader = new CSVReader(new StringReader(out), 0);
		String[] nextRow = null;
		StringBuilder builder = new StringBuilder();

		int prevTextLength = 0;
		int textColumnIndex = 0;
		boolean noText = false;
		if("XX:-Text".equals(textColumnName)){
			noText = true;
			textColumnIndex = Integer.MIN_VALUE;
		}

		boolean isFirst = true;
		String[] titleRow = null;
		SIDEAnnotationSetting[] settingArray = null;
		while ((nextRow = csvReader.readNextMeaningful()) != null) {
			String currentText;
			if(noText){
				currentText = prevTextLength + " ";	
			}else{
				nextRow[textColumnIndex] = nextRow[textColumnIndex]+ " ";
				currentText = nextRow[textColumnIndex];
			}
			
			if (isFirst) {
				if(noText){
					titleRow = new String[nextRow.length+1];
					titleRow[nextRow.length] = "Text";
					System.arraycopy(nextRow, 0, titleRow, 0, nextRow.length);
				}else{
					titleRow = nextRow;	
				}
				settingArray = new SIDEAnnotationSetting[titleRow.length];
				for (int i = 0; i < settingArray.length; i++) {
					if(headerExist){
						if(textColumnName!=null && titleRow[i].equalsIgnoreCase(textColumnName)){
								textColumnIndex = i;								
						}
					}
					settingArray[i] = UIMAToolkit.createSIDEAnnotationSetting(jCas);
				}
				isFirst = false;
				if(headerExist){ continue; }
			}
			if(noText){
				String[] noTextRow = new String[nextRow.length+1];
				noTextRow[nextRow.length] = currentText;
				System.arraycopy(nextRow, 0, noTextRow, 0, nextRow.length);
				addCSVAnnotation(jCas, prevTextLength, noTextRow, titleRow,
						settingArray, noTextRow.length-1);
			}else{
				addCSVAnnotation(jCas, prevTextLength, nextRow, titleRow,
						settingArray, textColumnIndex);
			}
			
			builder.append(currentText);
			prevTextLength += currentText.length();
		}
		csvReader.close();

		// String text = FileUtils.file2String(xmiFile, null);
		// put document in CAS
		jCas.setDocumentText(builder.toString());
		// Also store location of source document in CAS. This information is
		// critical
		// if CAS Consumers will need to know where the original document
		// contents are located.
		// For example, the Semantic Search CAS Indexer writes this information
		// into the
		// search index that it creates, which allows applications that use the
		// search index to
		// locate the documents that satisfy their semantic queries.
		addSourceDocumentInformation(jCas, file.getAbsoluteFile().toURI(), (int)file.length());
	}
	
	public static void addSourceDocumentInformation(JCas jCas, URI uri, int documentSize) throws MalformedURLException{
		SourceDocumentInformation srcDocInfo = new SourceDocumentInformation(
				jCas);
		srcDocInfo.setUri(uri.toURL().toString());
		srcDocInfo.setOffsetInSource(0);
		srcDocInfo.setDocumentSize(documentSize);
		srcDocInfo.setLastSegment(true);
		srcDocInfo.addToIndexes();
	}

	private static void addCSVAnnotation(JCas jcas, int prevTextLength,
			String[] currentRow, String[] titleRow,
			SIDEAnnotationSetting[] settingArray, int textColumnIndex) {
		String currentText = currentRow[textColumnIndex];
		int currentTextLength = currentText.length();

		jcas.getDocumentText();
		SIDESegment segmentation = new SIDESegment(jcas);
		segmentation.setBegin(prevTextLength);
		segmentation.setEnd(prevTextLength + currentTextLength);
		segmentation.setSubtypeName(UIMAToolkit.makeCSVSegmentationTypeName());
		segmentation.addToIndexes();

		for (int j = 0; j < currentRow.length; j++) {
			if(j==textColumnIndex){ continue; }
			
			Map<String, Color> labelColorMap = UIMAToolkit
					.getLabelColorMap(settingArray[j]);

			String columnName = titleRow[j];
			String currentAnnotation = SIDEToolkit.getLegalLabelString(currentRow[j]);

			SIDEAnnotation annotation = new SIDEAnnotation(jcas);
			int tot = prevTextLength + currentTextLength;
			annotation.setBegin(prevTextLength);
			annotation.setEnd(prevTextLength + currentTextLength);
			annotation.setSubtypeName(UIMAToolkit
					.makeCSVAnnotationTypeName(columnName));
			annotation.setLabelString(currentAnnotation);
			annotation.setSourceSegmentation(segmentation);

			Color color = labelColorMap.get(currentAnnotation);
			if (color == null) {
				ColorLabel colorLabel = ColorLabel
						.createFromLabel(currentAnnotation);
				Map<Long, ColorLabel> colorLabelMap = UIMAToolkit
						.getColorLabelMap(settingArray[j]);
				colorLabelMap.put(colorLabel.getId(), colorLabel);
				UIMAToolkit.setColorLabelString(settingArray[j], colorLabelMap
						.values().toArray(new ColorLabel[0]), false);
			}
			annotation.setSetting(settingArray[j]);
			annotation.addToIndexes();
			
		}
		
	}

	public static Color getLabelColor(SIDEAnnotation sideAnnotation){
		Map<Long, ColorLabel> colorLabelMap = getColorLabelMap(sideAnnotation
				.getSetting());
		Color defaultColor = new Color(0xdddddd);

		if (colorLabelMap == null) {
			return defaultColor;
		}
		String annotationLabel = sideAnnotation.getLabelString();
		for (ColorLabel colorLabel : colorLabelMap.values()) {
			String labelOfColor = colorLabel.getLabel();
			
			if (labelOfColor.equals(annotationLabel)) {
				Color color = colorLabel.getColor();
				return color;
			}
		}
		return defaultColor;
	}

	public static String labelColorAndString = "&";
	public static String labelColorEqualsString = "=";

	public static void setColorLabelString(
			SIDEAnnotationSetting sideAnnotationSetting,
			ColorLabel[] c, boolean append) {
		StringBuilder code = new StringBuilder();
		
		
		if(append){
			code.append(sideAnnotationSetting.getLabelColorMapString());
		}
		
		CharSequence newCode = toLabelColorString(c, append?UIMAToolkit.getLabelColorMap(sideAnnotationSetting):null);
		if(code.length()!=0){ code.append(labelColorAndString); }
		code.append(newCode);
		
		sideAnnotationSetting.setLabelColorMapString(code.toString());
	}
	
	public static CharSequence toLabelColorString(ColorLabel[] result) {
		return toLabelColorString(result, null);
	}
	
	public static CharSequence toLabelColorString(
			ColorLabel[] result, Map<String,Color> labelColorMap) {
		StringBuilder resultString = new StringBuilder();
		boolean isFirst = true;
		for (ColorLabel colorLabel : result) {
			String label = colorLabel.getLabel();
			if(labelColorMap!=null && labelColorMap.containsKey(label)){ continue; }
			
			if (isFirst) {
				isFirst = false;
			} else {
				resultString.append(labelColorAndString);
			}
			resultString.append(colorLabel.toCode());
		}
		return resultString;
	}
	
	public static SIDEAnnotationSetting getSIDEAnnotationSetting(JCas jCas,
			String subtypeName) {
		List<SIDESegment> list = getSIDESegmentList(jCas, SIDEAnnotation.type,
				subtypeName);
		if (list == null || list.size() == 0) {
			return null;
		}

		SIDEAnnotation sideAnnotation = (SIDEAnnotation) list.get(0);
		return sideAnnotation.getSetting();
	}
	
	public static SIDEDocumentSetting getSIDEDocumentSetting(JCas jCas) {
		
		Iterator<?> iterator = jCas.getAnnotationIndex(SIDEDocumentSetting.type).iterator();
		
		if(!iterator.hasNext()){ return null; }
		else{ return (SIDEDocumentSetting)iterator.next(); }
	}
	
	public static List<String[]> getColorLabelChangeList(
			Collection<? extends ColorLabel> originalList,
			Collection<? extends ColorLabel> changedList) {
		List<String[]> changeList = new ArrayList<String[]>();

		for (ColorLabel original : originalList) {
			long originalID = original.getId();
			String originalLabel = original.getLabel();

			for (ColorLabel changed : changedList) {
				long changedID = changed.getId();
				String changedLabel = changed.getLabel();

				if (changedID == originalID) {
					if (!changedLabel.equals(originalLabel)) {
						changeList.add(new String[] { originalLabel,
								changedLabel });
					}
					break;
				}
			}
		}
		return changeList;
	}

	public static void addSIDEOptions(CAS cas, File xmiFile) {
		JCas jCas = null;
		try {
			jCas = cas.getJCas();
		} catch (CASException e) {
			throw new RuntimeException(e);
		}
		Iterator<?> iterator = jCas
				.getAnnotationIndex(SIDEDocumentSetting.type).iterator();
		if (iterator.hasNext()) {
			return;
		}

		SIDEDocumentSetting docSetting = new SIDEDocumentSetting(jCas);
		docSetting.setSourceURI(xmiFile.toURI().toString());
		docSetting.addToIndexes();
	}
//	public static String[] getLabelArray(List<? extends Annotation> sideSegmentList){
//		
//	}


	public static List<String> getFeatureNameList(
			List<FeatureTableKey> constraintFeatureTableKeyList) {
		List<String> featureNameList = new ArrayList<String>();
		for(FeatureTableKey featureTableKey : constraintFeatureTableKeyList){
			featureNameList.add(featureTableKey.getFeatureName());
		}
		return featureNameList;
	}

	public static String getXmiURIString(JCas jCas) {
		SIDEDocumentSetting sideDocumentSetting = UIMAToolkit.getSIDEDocumentSetting(jCas);
		String sourceURI = sideDocumentSetting.getSourceURI();
		return sourceURI;
	}
	public static String[] getXmiURIStringArray(JCas[] jCasArray) {
		Set<String> sourceURISet = new HashSet<String>();
		
		for(JCas jCas : jCasArray){
			String sourceURI = getXmiURIString(jCas);
			sourceURISet.add(sourceURI);
		}
		
		return sourceURISet.toArray(new String[0]);
	}


	public static Set<String> getSubtypeNameSet(DocumentList documentList) {
		JCas jCas = documentList.getJCasList().get(0);
		int type = documentList.getType();
		return getSubtypeNameSet(jCas, type);
	}

	public static Set<String> getBaseSubtypeNameSet(JCas jCas) {
		List<SIDESegment> sideSegmentList = UIMAToolkit.getSIDESegmentList(jCas, SIDESegment.type, null);

		Set<String> subtypeSet = new HashSet<String>();
		for (SIDESegment sideSegment : sideSegmentList) {
			if(sideSegment instanceof SIDEAnnotation){ continue; }
			
			String subtypeName = sideSegment.getSubtypeName();
			subtypeSet.add(subtypeName);
		}
		return subtypeSet;
	}
	
	public static Set<String> getSubtypeNameSet(JCas jCas, int type) {
		return getSubtypeNameSet(jCas, type, null);
	}
	
	public static Set<String> getSubtypeNameSet(JCas jCas, int type, String baseSubtypeName) {
		List<SIDESegment> sideSegmentList = UIMAToolkit.getSIDESegmentList(jCas, type, null);

		Set<String> subtypeSet = new HashSet<String>();
		for (SIDESegment sideSegment : sideSegmentList) {
//			if(sideSegment.getBegin()>0){ continue; }

			if(baseSubtypeName!=null && !baseSubtypeName.equals(UIMAToolkit.getBaseSIDESegment(sideSegment).getSubtypeName())){
				continue;
			}
			
			String subtypeName = sideSegment.getSubtypeName();
			subtypeSet.add(subtypeName);
		}
		return subtypeSet;
	}

	public static boolean overrideAndContinueSubtype(JCas[] jCasArray, int type, String subtypeName, Component parentComponent) {
		if(!isSubtypeNameContainedByAny(jCasArray, type, subtypeName)){ return true; }
		
		int result = JOptionPane.showConfirmDialog(parentComponent,
				"subtype '"+subtypeName+"' already exists. override?", "warning (boolean)", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		return result==JOptionPane.YES_OPTION;
	}

	public static MultipleSelectionOption overrideAndContinueSubtype(JCas jCas, int type, String subtypeName,
			Component parentComponent, MultipleSelectionOption option) {
		Set<String> subtypeNameSet = UIMAToolkit.getSubtypeNameSet(jCas, type);
		if(subtypeNameSet.contains(subtypeName)){
			if(option==null){
				int result = JOptionPane.showConfirmDialog(parentComponent,
						"subtype '"+subtypeName+"' already exists. override?", "warning (multiple selection)", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				option = (result==JOptionPane.YES_OPTION)?MultipleSelectionOption.YES_ALL_OPTION:MultipleSelectionOption.NO_ALL_OPTION;
			}
			if(option==MultipleSelectionOption.YES_ALL_OPTION){
				UIMAToolkit.removeSIDEAnnotationList(jCas, type, subtypeName);
			}
			return option;
		}
		else{
			return MultipleSelectionOption.DEFAULT_OPTION;
		}
	}


	
	public static String[] toStringArray(List<SIDESegment> sideSegmentList) {
		String[] textArray = new String[sideSegmentList.size()];
		for(int i=0; i<sideSegmentList.size(); i++){
			textArray[i] = sideSegmentList.get(i).getCoveredText();
		}
		return textArray;
	}

	public static String getHiddenPredictionAnnotationSubtypeName(TrainingResult trainingResult, String targetBaseSubtypeName) {
		return "predict_"+trainingResult.getSubtypeName()+"_with_base_"+targetBaseSubtypeName+"_at_"+trainingResult.getTimestamp();
//		return trainingResult.getSubtypeName();
	}

	public static String getRecommendedPredictionAnnotationSubtypeName(TrainingResultInterface trainingResult, String targetBaseSubtypeName) {
//		return "predict_"+trainingResult.getSubtypeName()+"_with_base_"+targetBaseSubtypeName+"_at_"+trainingResult.getTimestamp();
		return trainingResult.getSubtypeName();
	}
	public static String segmentDocumentList(DocumentList documentList, SegmenterPlugin segmenterPlugin) throws Exception {
		String subtypeName = null;
		for(JCas jCas : documentList.getJCasList()){
			String tmpSubtypeName = segmentJCas(jCas, segmenterPlugin);
			if(subtypeName==null){
				subtypeName = tmpSubtypeName;
			}else{
				YeriDebug.ASSERT(subtypeName.equals(tmpSubtypeName));
			}
		}
		return subtypeName;
	}
	
	public static String getSegmenterBaseSubtypeName(SegmenterPlugin segmenterPlugin){
		return segmenterPlugin.getClass().getName();
	}
	public static String segmentJCas(JCas jCas, SegmenterPlugin segmenterPlugin) throws Exception {
		
		String segmentBaseSubtypeName = getSegmenterBaseSubtypeName(segmenterPlugin);
		
		// check whether there already exists segmentation
		if(UIMAToolkit.getSIDESegmentList(jCas, SIDESegment.type, segmentBaseSubtypeName).size()>0){
			return segmentBaseSubtypeName;
		}
		
		String documentText = jCas.getDocumentText();
		int contentLength = documentText.length();
		int[] indices = segmenterPlugin.getSegmentEndIndexArray(documentText);
		
		// when last index is not included
		if(contentLength>0 && (indices.length==0 || indices[indices.length-1]!=contentLength)){
			indices = Arrays.copyOf(indices, indices.length+1);
			indices[indices.length-1]=contentLength;
		}
		
		int prevIndex=-1, index=0;
		for(int i=0; i<indices.length; i++){
			prevIndex = index;
			index = indices[i];
			
			SIDESegment segmentation = new SIDESegment(jCas);
			segmentation.setBegin(prevIndex);
			segmentation.setEnd(index);
			segmentation.setSubtypeName(segmentBaseSubtypeName);
			segmentation.addToIndexes();
		}
		UIMAToolkit.saveJCasToSource(jCas);
		return segmentBaseSubtypeName;
	}

	public static void addAnnotationLayer(JCas jCas, String segmentationSubtypeName, String annotationSubtypeName) {
		
		SIDEAnnotationSetting sideAnnotationSetting = UIMAToolkit.createSIDEAnnotationSetting(jCas);
		List<SIDESegment> sideSegmentList = UIMAToolkit.getSIDESegmentList(jCas, SIDESegment.type, segmentationSubtypeName);
		
		for(SIDESegment sideSegment : sideSegmentList){
			
			SIDEAnnotation annotation = new SIDEAnnotation(jCas);
			annotation.setBegin(sideSegment.getBegin());
			annotation.setEnd(sideSegment.getEnd());
			annotation.setSubtypeName(annotationSubtypeName);
			annotation.setSourceSegmentation(sideSegment);
			
			annotation.setSetting(sideAnnotationSetting);
			annotation.addToIndexes();
		}
	}

	public static SIDEAnnotationSetting createSIDEAnnotationSetting(JCas jCas) {
		SIDEAnnotationSetting sideAnnotationSetting = new SIDEAnnotationSetting(jCas);
		sideAnnotationSetting.setLabelColorMapString("");
		sideAnnotationSetting.setDatatypeString(Datatype.AUTO.toString());
		return sideAnnotationSetting;
	}

	public static List<SIDESegment> getSummaryAnnotationList(
			DocumentListInterface documentList, boolean[] suitabilityArray) {
		Iterator<Object> documentIterator = documentList.iterator();
		List<SIDESegment> sideSegmentList = new ArrayList<SIDESegment>();
		
		int prevEnd = -1;
		for(int i=0; documentIterator.hasNext(); i++){
			SIDESegment sideSegment = (SIDESegment)documentIterator.next();
			
			if(!suitabilityArray[i]){
				continue;
			}
			sideSegmentList.add(sideSegment);
		}
		return sideSegmentList;
	}

	protected static boolean doOverlap(SIDESegment a, SIDESegment b){
		return (a.getEnd()-b.getBegin())*(a.getBegin()-b.getEnd())<0;
	}
	
	public static List<SIDESegment> mergeSIDESegmentList(
			List<SIDESegment> aList,
			List<SIDESegment> bList,
			BooleanOperator booleanOperator) {
		int i=0; int j=0;
		while(true){
			SIDESegment a = aList.get(i);
			SIDESegment b = bList.get(j);
			doOverlap(a,b);
			
		}
	}

	public static String getEnclosingAnnotationLabelName(SIDESegment targetSegment, String predictionAnnotationSubtypeName) {
		JCas jCas = UIMAToolkit.getJCas(targetSegment.getCAS());
		List<SIDESegment> sideSegmentList = UIMAToolkit.getSIDESegmentList(jCas, SIDEAnnotation.type, predictionAnnotationSubtypeName);
		int begin = targetSegment.getBegin();
		int end = targetSegment.getEnd();
		
		for(SIDESegment sideSegment: sideSegmentList){
			SIDEAnnotation sideAnnotation  = (SIDEAnnotation)sideSegment;
			if(sideAnnotation.getBegin()<=begin && sideAnnotation.getEnd()>=end){
				return sideAnnotation.getLabelString();
			}
		}
		return null;
	}

	public static Set<String> getCommonSubtypeNameSet(
			Collection<? extends JCas> casCollection, int type) {
		return getCommonSubtypeNameSet(casCollection.toArray(new JCas[0]), type);
	}
	
	public static Set<String> getCommonSubtypeNameSet(
			JCas[] casArray, int type) {
		Set<String> returnSet = null;
		
		for(JCas jCas : casArray){
			Set<String> thisSubtypeNameSet = getSubtypeNameSet(jCas, type);
			if(returnSet==null){ returnSet = thisSubtypeNameSet; }
			else{ CollectionsToolkit.intersection(thisSubtypeNameSet, returnSet, new StringToolkit.StringComparator()); }
		}
		return returnSet;
	}

	public static boolean isBaseSubtypeNameFromSegmenter(String baseSubtypeName) {
		return baseSubtypeName.equals("native");
	}

	public static void exportToCSV(JCas jCas, String subtypeName, File csvFile) throws IOException {
		String baseSubtypeName = getBaseSubtypeName(jCas, subtypeName);
		
		Set<String> subtypeNameSet = UIMAToolkit.getSubtypeNameSet(jCas, SIDEAnnotation.type, baseSubtypeName);
		DocumentList[] dlArray = getDocumentList(jCas, subtypeNameSet);
		Iterator[] iteratorArray = getIteratorArray(dlArray);
		
		CSVWriter csvWriter = new CSVWriter(new FileWriter(csvFile));
		
		csvWriter.writeNext(getTitleStringArray(dlArray));
		
		while(iteratorArray[0].hasNext()){
			String[] content = new String[iteratorArray.length+1];
			SIDEAnnotation sideAnnotation = null;
			for(int j=0; j<iteratorArray.length; j++){
				sideAnnotation = (SIDEAnnotation)iteratorArray[j].next(); 
				content[j+1] = sideAnnotation.getLabelString();
			}
			content[0] = sideAnnotation.getCoveredText();
			csvWriter.writeNext(content);
		}
		csvWriter.close();
	}
	
	private static String[] getTitleStringArray(DocumentList[] dlArray){
		String[] returnValue = new String[dlArray.length+1];
		returnValue[0] = "text";
		for(int i=0; i<dlArray.length; i++){
			returnValue[i+1] = dlArray[i].getSubtypeName();
		}
		return returnValue;
	}
	
	private static DocumentList[] getDocumentList(JCas jCas, Collection<? extends String> subtypeNameCollection){
		DocumentList[] dlArray = new DocumentList[subtypeNameCollection.size()];
		Iterator<? extends String> subtypeNameSetIterator = subtypeNameCollection.iterator();
		for(int i=0; subtypeNameSetIterator.hasNext(); i++){
			DocumentList documentList = new DocumentList(subtypeNameSetIterator.next());
			documentList.getJCasList().add(jCas);
			dlArray[i] = documentList; 
		}
		return dlArray;
	}
	
	private static Iterator[] getIteratorArray(DocumentListInterface[] dlArray){
		Iterator[] iteratorArray = new Iterator[dlArray.length];
		for(int i=0; i<dlArray.length; i++){
			iteratorArray[i] = dlArray[i].iterator(); 
		}
		return iteratorArray;
	}

	public static String getBaseSubtypeName(JCas jCas, String subtypeName){
		DocumentList documentList = new DocumentList(subtypeName);
		documentList.getJCasList().add(jCas);
		return documentList.getBaseSubtypeName();
	}

}
