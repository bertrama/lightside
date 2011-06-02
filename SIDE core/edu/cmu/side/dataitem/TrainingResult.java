package edu.cmu.side.dataitem;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import oracle.xml.parser.v2.XMLDocument;

import org.apache.uima.jcas.JCas;
import org.w3c.dom.Element;

import com.mysterion.xml.XMLBoss;
import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.text.StringToolkit;
import com.yerihyo.yeritools.tree.TreeNodeToolkit;
import com.yerihyo.yeritools.xml.XMLToolkit;
import com.yerihyo.yeritools.xml.XMLable;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.SIDEToolkit.FileType;
import edu.cmu.side.dataitem.DataItem.SIDEFilterOperatorSetting.SIDEFilterOperator;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTableKey;
import edu.cmu.side.ml.PredictionToolkit.NominalPredictionResult;
import edu.cmu.side.ml.PredictionToolkit.PredictionResult;
import edu.cmu.side.plugin.MLAPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.SegmenterPlugin;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;
import edu.cmu.side.uima.type.SIDEAnnotation;

public class TrainingResult extends DataItem implements XMLable
{
	private static final long serialVersionUID = 1L;
	
	private FeatureTable featureTable = null;
	private TreeMap<String,String> evaluationResult = null;
	private MLAPlugin mlaPlugin = null;
	private int fold;
	
	
	public static final String pluginKey = "plugin";
	public static final String labelArrayKey = "labelArray";
	public static final String labelDeliminator = "\n";
	
	
	public static final File folder = SIDEToolkit.modelFolder;
	
	
	public static enum LabelType{
		TRUE, PREDICTION;

		public String getLabel(PredictionResult predictionResult,
				SIDEAnnotation sideAnnotation, int docIndex) {
			if(this==TRUE){ return sideAnnotation.getLabelString(); }
			else if(this==PREDICTION){ return predictionResult.getMostLikelyLabel(docIndex); }
			else{ throw new IllegalArgumentException(); }
		}
		
		public String getName(){
			return this.name().toLowerCase();
		}
	}
	
	private TrainingResult(){
		yeriInit();
	}
	
	public TrainingResult(FeatureTable featureTable, SortedMap<String,String> evaluationResult, MLAPlugin mlaPlugin) {
		yeriInit();
		this.featureTable = featureTable;
		this.evaluationResult = new TreeMap<String,String>(evaluationResult);
		this.mlaPlugin = mlaPlugin;
	}
	private void yeriInit() {
		timestamp = System.currentTimeMillis();
	}
	
	public String getSubtypeName(){
		DocumentList documentList = this.featureTable.getDocumentList();
		if(documentList==null){ return null; }
		
		return documentList.getSubtypeName();
	}

	private transient SortedMap<String,String> settingDescription = null;
	public SortedMap<String,String> getTrainSettingDescription(){
		if(settingDescription!=null){ return settingDescription; }
		
		DocumentList documentList = featureTable.getDocumentList();
		String[] sourceURIStringArray = UIMAToolkit.getXmiURIStringArray(documentList.getJCasList().toArray(new JCas[0]));
		String[] labelArray = documentList.getLabelArray();
		
		settingDescription = new TreeMap<String,String>();
		settingDescription.put("source documents", StringToolkit.toString(sourceURIStringArray, StringToolkit.newLine()).toString());
		settingDescription.put(labelArrayKey, StringToolkit.toString(labelArray, labelDeliminator).toString());
		
		StringBuilder builder = new StringBuilder();
		for(FeatureTableKey featureTableKey : featureTable.getAllFeatureTableKeyList()){
			if(!featureTable.getUseMetafeatures() && featureTableKey.getFeatureIdentifyingString().substring(0, 4).equals("meta")){
				continue;
			}
			builder.append(featureTableKey.getFeatureIdentifyingString()).append(StringToolkit.newLine());
		}
		settingDescription.put("global feature collection", builder.toString());
		settingDescription.put("model", mlaPlugin.toString());
		settingDescription.putAll(evaluationResult);
		
		return settingDescription;
	}
	
	public String[] getLabelArray(){
		return this.getTrainSettingDescription().get(labelArrayKey).trim().split(labelDeliminator);
	}
	
	public static TrainingResult create(File xmlFile){
		XMLDocument doc;
		try {
			doc = XMLBoss.XMLFromFile(xmlFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Element root = doc.getDocumentElement();
		return create(root);
	}	
	public static TrainingResult create(Element root){
		TrainingResult trainingResult = new TrainingResult();
		try {
			trainingResult.fromXML(root);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return trainingResult;
	}
	@Override
	public void fromXML(Element root) throws Exception {
		for(Element element : XMLToolkit.getChildElements(root)){
			String tagName = element.getTagName();
			if(tagName.equalsIgnoreCase("featuretable")){
//				String featureTableFilePath = XMLToolkit.getTextContent(element).toString();
				featureTable = FeatureTable.createFromXML(element);
			}else if(tagName.equalsIgnoreCase("evaluationResult")){
				evaluationResult = new TreeMap<String,String>(XMLToolkit.xmlAllContentFormToMap(element, "evaluationResult", "option", "key", "value"));
			}
			else if(tagName.equalsIgnoreCase(MLAPlugin.type)){
				this.mlaPlugin = (MLAPlugin)SIDEPlugin.createFromXML(element);
			}
			else if(tagName.equalsIgnoreCase(SegmenterPlugin.type)){
				segmenterPlugin = (SegmenterPlugin)SIDEPlugin.createFromXML(element);
			}
			else if(tagName.equalsIgnoreCase(foldTagName)){
				fold = Integer.parseInt(XMLToolkit.getTextContent(element).toString());
			}
			else{
				this.itemsFromXML(element);
			}
		}
	}
	
	public static final String xmlTagName = "trainingresult";
	public static final String foldTagName = "fold";
	@Override
	public String toXML() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.itemsToXML());
		builder.append(mlaPlugin.toXML());
		builder.append(featureTable.toXML());
		builder.append(segmenterPlugin.toXML());
		builder.append(XMLToolkit.wrapContentWithTag(Integer.toString(fold), foldTagName));
		builder.append(XMLToolkit.mapToXMLAllContentForm(evaluationResult, "evaluationResult", "option", "key", "value", true));
		
		return XMLToolkit.wrapContentWithTag(builder, xmlTagName).toString();
	}
	public List<FeatureTableKey> getFeatureTableKeyList() {
		return this.featureTable.getAllFeatureTableKeyList();
	}

	public void save(File selectedFile) throws IOException {
		FileToolkit.writeTo(selectedFile, this.toXML());
	}
	
	public PredictionResult predict(DocumentList documentList) throws Exception{
		MLAPlugin mlaPlugin = this.getMLAPlugin();
		return mlaPlugin.predict(this, documentList);
	}

	protected MLAPlugin getMLAPlugin() {
		return mlaPlugin;
	}

	public DocumentList getDocumentList() {
		return featureTable.getDocumentList();
	}

	@Override
	public FileType getFileType() {
		return FileType.trainingResult;
	}

	public FeatureTable getFeatureTable() {
		return featureTable;
	}

//	private transient PredictionResult selfPredictionResult = null;
	public PredictionResult getSelfPredictionResult(){
		return this.mlaPlugin.getSelfPredictionResult(this);
		
//		if(selfPredictionResult==null){
//			System.out.println("generating self prediction...");
//			DocumentList documentList = this.getDocumentList();
//			try {
//				selfPredictionResult = this.predict(documentList);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//		return selfPredictionResult;
	}
	
	public int[] getSelfAnnotationIncorrectIndexArray(){
		boolean[] annotationCorrectnessArray = this.getSelfAnnotationCorrectnessArray();
		
		List<Integer> wrongAnnotationIndexList = new ArrayList<Integer>();
		for(int i=0; i<annotationCorrectnessArray.length; i++){
			if(!annotationCorrectnessArray[i]){
				wrongAnnotationIndexList.add(i);
			}
		}
		return CollectionsToolkit.toIntArray(wrongAnnotationIndexList);
	}
	
	public int[] getSelfAnnotationLabelIndexArray() {
		DocumentList documentList = this.getDocumentList();
		PredictionResult predictionResult = getSelfPredictionResult();
		String[] predictionAnnotationArray = predictionResult.getMostLikelyLabelStrings();
		return documentList.getAnnotationIndexArray(predictionAnnotationArray);
	}
	
	public boolean[] getSelfAnnotationCorrectnessArray() {
		String[] trueAnnotationArray = this.getDocumentList().getAnnotationArray();
		PredictionResult predictionResult = getSelfPredictionResult();
		String[] predictionAnnotationArray = predictionResult.getMostLikelyLabelStrings();
		return CollectionsToolkit.equalsEach(trueAnnotationArray, predictionAnnotationArray);
	}

//	public void setSelfPredictionResult(PredictionResult selfPredictionResult) {
//		this.selfPredictionResult = selfPredictionResult;
//	}

	public MLAPlugin getMlaPlugin() {
		return mlaPlugin;
	}

	/**
	 * This is not necessary for model training but exist for summarization
	 */
	private SegmenterPlugin segmenterPlugin;
	public void setSegmenterPlugin(SegmenterPlugin segmenterPlugin) {
		this.segmenterPlugin = segmenterPlugin;
	}

	public SegmenterPlugin getSegmenterPlugin() {
		return segmenterPlugin;
	}

	public static TrainingResult[] extract(
			DefaultMutableTreeNode booleanExpressionRoot) {
		Set<Object> userObjectSet = TreeNodeToolkit.getUserObjectSet(booleanExpressionRoot);
		List<TrainingResult> trainingResultList = new ArrayList<TrainingResult>();
		
		for(Object o : userObjectSet){
			SIDEFilterOperator sideFilterOperator = (SIDEFilterOperator)o;
			TrainingResult trainingResult = sideFilterOperator.getTrainingResult();
			if(trainingResult==null){ continue; }
			trainingResultList.add(trainingResult);
		}
		
		return trainingResultList.toArray(new TrainingResult[0]);
	}
	
	public String[] getPredictionSubtypeNameArray(DocumentList documentList){
		String baseSubtypeName;
		String predictionSubtypeName;
		
		if(documentList.getSubtypeName()!=null){
			baseSubtypeName = documentList.getBaseSubtypeName();
		}else{
			SegmenterPlugin segmenterPlugin = this.getSegmenterPlugin();
			try {
				baseSubtypeName = UIMAToolkit.segmentDocumentList(documentList, segmenterPlugin);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		predictionSubtypeName = UIMAToolkit.getHiddenPredictionAnnotationSubtypeName(this, baseSubtypeName);
		
		
//		if(documentList.getSubtypeName()!=null){
//			baseSubtypeName = documentList.getBaseSubtypeName();
//			predictionSubtypeName = UIMAToolkit.getHiddenPredictionAnnotationSubtypeName(this, baseSubtypeName);
//		}else{
//			SegmenterPlugin segmenterPlugin = this.getSegmenterPlugin();
//			try {
//				baseSubtypeName = UIMAToolkit.segmentDocumentList(documentList, segmenterPlugin);
//				predictionSubtypeName = UIMAToolkit.getHiddenPredictionAnnotationSubtypeName(this, DocumentList.tempSummarySubtypeName);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
		return new String[]{ baseSubtypeName, predictionSubtypeName };
	}

	public int getFold() {
		return fold;
	}

	public void setFold(int fold) {
		this.fold = fold;
	}

	public int[][] getConfusionMatrix() {
		PredictionResult selfPredictionResult = this.getSelfPredictionResult();
		if(selfPredictionResult==null){ return null; }
		String[] trueAnnotationArray;
		trueAnnotationArray = ((NominalPredictionResult)selfPredictionResult).getActualLabelStrings();
		String[] predictionAnnotationArray = selfPredictionResult.getMostLikelyLabelStrings();
		String[] labelArray = this.getLabelArray();
		
		YeriDebug.ASSERT_compareInteger(trueAnnotationArray.length, predictionAnnotationArray.length);
		
		int[][] result = new int[labelArray.length][labelArray.length];
		CollectionsToolkit.fill(result, 0);
		
		for(int i=0; i<trueAnnotationArray.length; i++){
			int trueIndex = CollectionsToolkit.indexOf(labelArray, trueAnnotationArray[i]);
			int predictionIndex = CollectionsToolkit.indexOf(labelArray, predictionAnnotationArray[i]);
			result[trueIndex][predictionIndex]++;
		}
		return result;
	}
}
