package edu.cmu.side.ml;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.w3c.dom.Element;

import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.color.ColorToolkit;
import com.yerihyo.yeritools.csv.CSVWriter;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.swing.ColorLabelConfigPanel.ColorLabel;
import com.yerihyo.yeritools.text.Base64;
import com.yerihyo.yeritools.text.StringToolkit;
import com.yerihyo.yeritools.xml.XMLToolkit;
import com.yerihyo.yeritools.xml.XMLable;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.uima.UIMAToolkit.Datatype;

public class PredictionToolkit {

	public static abstract class PredictionResult implements XMLable{
		public static final String xmlTag = "predictionResult";
		
		protected FeatureTable featureTable;	
		
		
		public void setFeatureTable(FeatureTable featureTable) {
			this.featureTable = featureTable;
		}

		public FeatureTable getFeatureTable() {
			return featureTable;
		}
		
		public abstract String getMostLikelyLabel(int i) ;
		public abstract String[] getMostLikelyLabelStrings() ;
		public abstract String[] getActualLabelStrings();
		
		public abstract Datatype getPredictionDatatype();
		public abstract void fromXMLByItemForSubclass(Element element) throws Exception;
		public abstract CharSequence toXMLForSubclass();
		public abstract ColorLabel[] createColorLabelArray();
		
		public static PredictionResult create(Element root){
			Element datatypeElement = XMLToolkit.getLastChildElementByName(root, Datatype.xmlTag);
			Datatype datatype = Datatype.fromString(XMLToolkit.getTextContent(datatypeElement));
			
			PredictionResult pr = null;
			if(datatype==Datatype.NOMINAL){
				pr = new NominalPredictionResult();
				
			}else if(datatype==Datatype.NOMINAL){
				pr = new NumericPredictionResult();
			}else{
				throw new UnsupportedOperationException();
			}
			try { pr.fromXML(root); }
			catch (Exception e) { throw new RuntimeException(); }
			return pr;
		}
		@Override
		public void fromXML(Element root) throws Exception {
			
			for(Element element : XMLToolkit.getChildElements(root)){
				String tagName = element.getTagName();
				if(tagName.equals(FeatureTable.xmlTag)){
					featureTable = FeatureTable.createFromXML(element);
				}else if(tagName.equals(Datatype.xmlTag)){
					continue;
				}else{
					this.fromXMLByItemForSubclass(element);
				}
			}
		}
		@Override
		public String toXML() {
			StringBuilder builder = new StringBuilder();
			builder.append(featureTable.toXML());
			builder.append(XMLToolkit.wrapContentWithTag(getPredictionDatatype().toString(), Datatype.xmlTag));
			builder.append(toXMLForSubclass());
			return XMLToolkit.wrapContentWithTag(builder, xmlTag).toString();
		}
	}
	
	
	public static class NominalPredictionResult extends PredictionResult{
		private static final long serialVersionUID = 1L;
		
		protected String[] labelStrings;
		protected double[][] predictions;
		protected String[] truth;
		private boolean predictionProbabilityValid = false;
		
		private static final String labelStringsXMLTag = "labelStrings";
		private static final String predictionsXMLTag = "predictions";
		private static final String predictionProbabilityValidXMLTag = "predictionProbabilityValid";
		
		private NominalPredictionResult(){}
		public Datatype getPredictionDatatype(){ return Datatype.NOMINAL; }
		
		@Override
		public void fromXMLByItemForSubclass(Element element) throws Exception {
			String tagName = element.getTagName();
			if(tagName.equals(labelStringsXMLTag)){
				labelStrings = (String[])Base64.decodeToObject(XMLToolkit.getTextContent(element).toString());
			}else if(tagName.equals(predictionsXMLTag)){
				predictions = (double[][])Base64.decodeToObject(XMLToolkit.getTextContent(element).toString());
			}else if(tagName.equals(predictionProbabilityValidXMLTag)){
				CharSequence textContent = XMLToolkit.getTextContent(element);
				predictionProbabilityValid = Boolean.parseBoolean(textContent.toString());
			}else{
				throw new UnsupportedOperationException();
			}
		}

		@Override
		public CharSequence toXMLForSubclass() {
			StringBuilder builder = new StringBuilder();
			builder.append(XMLToolkit.wrapSerializedContentWithTag(labelStrings, labelStringsXMLTag));
			builder.append(XMLToolkit.wrapSerializedContentWithTag(predictions, predictionsXMLTag));
			builder.append(XMLToolkit.wrapContentWithTag(Boolean.toString(predictionProbabilityValid), predictionProbabilityValidXMLTag));
			return builder;
		}
		
		public NominalPredictionResult(String[] labels, double[][] prediction) {
			YeriDebug.ASSERT_compareInteger(labels.length, prediction[0].length);
			this.predictions = CollectionsToolkit.copyOf(prediction);
			this.labelStrings = Arrays.copyOf(labels, labels.length);
			predictionProbabilityValid = true;
		}
		
		public NominalPredictionResult(String[] labels, String[] trueArray, String[] predArray) {
			this.labelStrings = Arrays.copyOf(labels, labels.length);
			Arrays.sort(labelStrings);
			this.predictions = new double[predArray.length][labelStrings.length];
			
			for(int i=0; i< predArray.length; i++){
				for(int j=0; j<labelStrings.length; j++){
					predictions[i][j] = labelStrings[j].equals(predArray[i])?1:0;
				}
			}
			truth = trueArray;
			predictionProbabilityValid = false;
		}

		protected int getLabelStringIndex(String targetLabelString) {
			if (targetLabelString == null) {
				return -1;
			}
			for (int i = 0; i < labelStrings.length; i++) {
				if (targetLabelString.equals(labelStrings[i])) {
					return i;
				}
			}
			return -1;
		}

		public double getPredictionProbability(int documentIndex, String labelString) {
			int labelIndex = getLabelStringIndex(labelString);
			return getPredictionProbability(documentIndex, labelIndex);
		}

		public double getPredictionProbability(int documentIndex, int labelIndex) {
			return getPredictionProbabilityArray(documentIndex)[labelIndex];
		}
		
		public double[] getPredictionProbabilityArray(int documentIndex) {
			return predictions[documentIndex];
		}

		public int[] getMostLikelyLabelIndices() {
			int[] returnValue = new int[predictions.length];
			for (int i = 0; i < predictions.length; i++) {
				returnValue[i] = getMostLikelyLabelIndex(i);
			}
			return returnValue;
		}
		
		public int getMostLikelyLabelIndex(int i) {
			return CollectionsToolkit.getMinMaxIndex(predictions[i])[1];
		}
		
		public String getMostLikelyLabel(int i) {
			return labelStrings[getMostLikelyLabelIndex(i)];
		}

		public String[] getMostLikelyLabelStrings() {
			String[] results = new String[predictions.length];
			for (int i = 0; i < predictions.length; i++) {
				results[i] = getMostLikelyLabel(i);
			}
			return results;
		}
		
		public String[] getActualLabelStrings() {
			return truth;		
		}

		public boolean isPredictionProbabilityValid() {
			return predictionProbabilityValid;
		}

		public String[] getLabelStrings() {
			return labelStrings;
		}

		public void setPredictionProbabilityValid(boolean predictionProbabilityValid) {
			this.predictionProbabilityValid = predictionProbabilityValid;
		}
		
		public ColorLabel[] createColorLabelArray(){
			String[] labelArray = this.getLabelStrings();
			ColorLabel[] colorLabelArray = new ColorLabel[labelArray.length]; 
			for(int i=0; i<labelArray.length; i++){
				String label = labelArray[i];
				colorLabelArray[i] = ColorLabel.createFromLabel(label);
			}
			return colorLabelArray;
		}
	}
	
	public static class NumericPredictionResult extends PredictionResult{
		private static final long serialVersionUID = 1L;
		
		protected double[] trueValueArray;
		protected double[] predictionValueArray;
		private static final String predictionValueArrayXMLTag = "prediction_value_array";
		
		private NumericPredictionResult(){}
		
		
		@Override
		public void fromXMLByItemForSubclass(Element element) throws Exception {
			String tagName = element.getTagName();
			if(tagName.equals(predictionValueArrayXMLTag)){
				predictionValueArray = (double[])Base64.decodeToObject(XMLToolkit.getTextContent(element).toString());
			}else{
				throw new UnsupportedOperationException();
			}
		}

		@Override
		public CharSequence toXMLForSubclass() {
			StringBuilder builder = new StringBuilder();
			builder.append(XMLToolkit.wrapSerializedContentWithTag(predictionValueArray, predictionValueArrayXMLTag));
			return builder;
		}
		
		public NumericPredictionResult(double[] predictionValueArray) {
			this.predictionValueArray = predictionValueArray;
		}
		

		public NumericPredictionResult(double[] predictionValueArray, double[] trueArray) {
			this.predictionValueArray = predictionValueArray;
			this.trueValueArray = trueArray;
		}

		public String getMostLikelyLabel(int i){
			return Double.toString(predictionValueArray[i]);
		}

		public String[] getActualLabelStrings(){
			return StringToolkit.toStringArray(trueValueArray);
		}

		public String[] getMostLikelyLabelStrings() {
			return StringToolkit.toStringArray(predictionValueArray);
		}

		@Override
		public Datatype getPredictionDatatype() {
			return Datatype.NUMERIC;
		}
		
		public ColorLabel[] createColorLabelArray(){
			SortedSet<Double> valueSet = new TreeSet<Double>();
			for(double d : predictionValueArray){
				valueSet.add(d);
			}
			
			float startHue = SIDEToolkit.random.nextFloat();
			float endHue = SIDEToolkit.random.nextFloat();
			float saturation = 0.6f;
			float value = 0.4f;
			Color startColor = ColorToolkit.createByHSB(startHue, saturation, value);
			Color endColor = ColorToolkit.createByHSB(endHue, saturation, value);
			Map<Number,Color> colorMap = ColorToolkit.createColorMap(valueSet, startColor, endColor);
			
			return ColorLabel.createArrayFromColorMap(colorMap);
		}
	}
	
	private static Random r = new Random();
	public static void main(String[] args) throws IOException{
		test01();
	}
	protected static void test01() throws IOException{
		File trainFile = new File(SIDEToolkit.csvFolder, "train_numeric.csv");
		File testFile = new File(SIDEToolkit.csvFolder, "test_numeric.csv");
		
		
		CSVWriter csvTrainWriter = new CSVWriter(new FileWriter(trainFile));
		CSVWriter csvTestWriter = new CSVWriter(new FileWriter(testFile));
		
		csvTrainWriter.writeNext(new String[]{"A", "C"});
		csvTestWriter.writeNext(new String[]{"A"});
		
		test01_1(csvTrainWriter, true);
		test01_1(csvTestWriter, false);
		
		csvTrainWriter.close();
		csvTestWriter.close();
		
	}
	
	protected static String[] createContentItemArray(int n){
		String[] contentItemArray = new String[n];
		for(int i=0; i<n; i++){
			contentItemArray[i] = ""+(char)('a'+i);
		}
		return contentItemArray;
	}
	protected static void test01_1(CSVWriter writer, boolean valueIncluded){
		int n = 100;
		String[] contentItemArray = createContentItemArray(20);
		
		for(int i=0; i<n; i++){
			StringBuilder text = new StringBuilder();
			for(int t=0; t<contentItemArray.length; t++){
				if(r.nextBoolean()){ text.append(" ").append(contentItemArray[t]); }
			}
			
			String[] onelineContent = null;
			if(valueIncluded){ onelineContent = new String[]{text.toString(), Double.toString(r.nextInt(10))}; }
			else{ onelineContent = new String[]{text.toString()}; }
			writer.writeNext(onelineContent);
			
		}
	}
}
