package edu.cmu.side.dataitem;

/*
 * 	<recipe name="">
 <conditions>
 <condition name="First Paragraph" id="1">
 <filter name="Paragraphs" path="c:/side workspace/filters/testfilter.flt"/>
 <label>first paragraph</label>
 </condition>
 <condition name="Key Sentence" id="2">
 <filter name="Sentences" path="c:/side workspace/filters/testfilter.flt"/>
 <label name="key sentence"/>
 </condition> 
 </conditions>
 <expression>
 <and>
 <Recipe id="1"/>
 <or>
 <Recipe id="2"/>
 <Recipe id="3"/>
 </or>
 </and>
 </expression>
 <options>
 <rank apply="yes|no">
 <metric>
 <pluginWrapper name="Average TFIDF Score" classname="edu.cmu.side.tfidf"/>
 <options>
 <option name="scale"><![CDATA[0.75]]></option>
 </options>
 </metric>
 </rank>
 <limit apply="yes|no">
 <type top="" percent="yes|no">5</type>
 </limit>
 <restore apply="yes"/>
 </options>
 </recipe> 

 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.uima.jcas.JCas;
import org.w3c.dom.Element;

import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.collections.CollectionsToolkit.BooleanOperator;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.xml.XMLToolkit;
import com.yerihyo.yeritools.xml.XMLable;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.dataitem.DataItem.SIDEFilterOperatorSetting.OperatorType;
import edu.cmu.side.dataitem.DataItem.SIDEFilterOperatorSetting.SIDEFilterOperator;
import edu.cmu.side.plugin.EMPlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.SegmenterPlugin;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;
import edu.cmu.side.uima.type.SIDESegment;

public class TextRecipe extends Recipe implements XMLable{
	private DefaultMutableTreeNode booleanExpressionRoot;
	private EMPlugin emPlugin;
	private Limit limit = Limit.TOP_N_ITEMS;
	private double number = 5;
	private boolean restoreOrder = true;
	
	public static enum Limit{
		TOP_N_ITEMS, BOTTOM_N_ITEMS, TOP_N_PERCENT, BOTTOM_N_PERCENT,;
		
		private static String[] comboBoxTextArray = new String[]{
			"top n items", "bottom n items", "top n percent", "bottom n percent"
		};
		
		public static Limit create(String comboBoxText){
			return Limit.values()[CollectionsToolkit.indexOf(comboBoxTextArray, comboBoxText)];
		}
		
		public String getComboBoxText(){
			return comboBoxTextArray[this.ordinal()];
		}
		
		
		// top should be reversed since comparators are low-to-high by default
		public boolean isReverse(){ 
			return this==TOP_N_ITEMS || this==TOP_N_PERCENT;
		}
		
		public boolean isPercent(){ 
			return this==TOP_N_PERCENT || this==BOTTOM_N_PERCENT;
		}
		
		public static List<SIDESegment> trimList(Limit limit, DocumentList documentList, boolean[] suitabilityArray, int[] orderIndex, double number){
			
			List<SIDESegment> sideSegmentList = documentList.getSIDESegmentList();
			List<SIDESegment> returnList = new ArrayList<SIDESegment>();
			
			int documentListSegmentCount = orderIndex.length;
			YeriDebug.ASSERT_compareInteger(documentListSegmentCount, suitabilityArray.length);
			
			if(limit.isReverse()){
				orderIndex = CollectionsToolkit.getReverseOrderIndex(orderIndex);
			}
			
			int cutNumber = (int)(limit.isPercent()?(number*documentListSegmentCount):number);
			
			for(int i=0, count=0; count<cutNumber && i<documentListSegmentCount; i++){
				int index = CollectionsToolkit.indexOf(orderIndex, i);
				if(!suitabilityArray[index]){
					continue;
				}
				
				returnList.add(sideSegmentList.get(index));
				count++;
			}
			return returnList;
		}
	};
	
	private static final String booleanTreeTag = "boolean_expression"; 
	public static final String recipeTypeName = "text";
	public static final String limitTag = "limit";
	public static final String numberTag = "number";
	
	public String getRecipeTypeName(){
		return recipeTypeName;
	}
	
	private TextRecipe(){}
	public TextRecipe(DefaultMutableTreeNode booleanExpressionRoot, EMPlugin emPlugin, Limit limit, double number, boolean restoreOrder){
		this.booleanExpressionRoot = booleanExpressionRoot;
		this.emPlugin = emPlugin;
		this.limit = limit;
		this.number = number;
		this.restoreOrder = restoreOrder;
	}
	
	public static TextRecipe create(Element root){
		TextRecipe textRecipe = new TextRecipe();
		try {
			textRecipe.fromXML(root);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return textRecipe;
	}
	
	@Override
	public void fromXML(Element root) throws Exception {
		for(Element element : XMLToolkit.getChildElements(root)){
			String tagName = element.getTagName();
			if(tagName.equals(EMPlugin.type)){
				this.emPlugin = (EMPlugin)SIDEPlugin.createFromXML(element);
			}else if(tagName.equals(booleanTreeTag)){
				this.booleanExpressionRoot = SIDEFilterOperatorSetting.fromXML(XMLToolkit.getChildElements(element).get(0));
			}else if(tagName.equals(limitTag)){
				this.limit = Limit.valueOf(XMLToolkit.getTextContent(element).toString());
			}else if(tagName.equals(numberTag)){
				this.number = Double.parseDouble(XMLToolkit.getTextContent(element).toString());
			}
			else{
				this.itemsFromXML(element);
			}
		}
	}
	
	
	@Override
	public String toXML() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.itemsToXML());
		
		builder.append(XMLToolkit.wrapContentWithTag(SIDEFilterOperatorSetting.toXML(booleanExpressionRoot), booleanTreeTag) );
		builder.append(emPlugin.toXML());
		
		builder.append(XMLToolkit.wrapContentWithTag(limit.name(), limitTag) );
		builder.append(XMLToolkit.wrapContentWithTag(Double.toString(number), numberTag) );
		
		return wrapContentByRecipeTag(builder).toString();
	}
	
	private static boolean[] getSuitabilityArray(DefaultMutableTreeNode node, DocumentList targetDocumentList, DocumentList documentList){
		SIDEFilterOperator operator = (SIDEFilterOperator)node.getUserObject();
		OperatorType type = operator.getOperatorType();
		
		if(type==OperatorType.IS){
			TrainingResult trainingResult = operator.getTrainingResult();
			
			String predictionAnnotationSubtypeName = trainingResult.getPredictionSubtypeNameArray(documentList)[1];
			
			List<String> predictedLabelList = new ArrayList<String>();
			for(Iterator<?> iterator = targetDocumentList.iterator(); iterator.hasNext();){
				SIDESegment sideSegment = (SIDESegment)iterator.next();
				
				String predictedLabel = UIMAToolkit.getEnclosingAnnotationLabelName(sideSegment, predictionAnnotationSubtypeName);
				predictedLabelList.add(predictedLabel);
			}
			String[] predictedLabelArray = predictedLabelList.toArray(new String[0]);
			
			
			String[] preferredLabelArray = operator.getLabelArray();
			boolean[] suitabilityArray = CollectionsToolkit.contains(preferredLabelArray, predictedLabelArray);
			return suitabilityArray;
		}else{
			BooleanOperator booleanOperator = null;
			if(type==OperatorType.AND){ booleanOperator = BooleanOperator.AND; }
			else if(type==OperatorType.OR){ booleanOperator = BooleanOperator.OR; }
			else{ throw new UnsupportedOperationException(); }
			
			boolean[] suitabilityArray = null;
			for(Enumeration<?> childEnumeration = node.children(); childEnumeration.hasMoreElements(); ){
				boolean[] childSuitabilityArray = getSuitabilityArray((DefaultMutableTreeNode)childEnumeration.nextElement(), targetDocumentList, documentList);
				if(suitabilityArray==null){ suitabilityArray = childSuitabilityArray; }
				else{ suitabilityArray = CollectionsToolkit.calculateEach(suitabilityArray, childSuitabilityArray, booleanOperator); }
			}
			return suitabilityArray;
		}
	}
	
	protected static void segmentUnsavedSegmentation(DocumentList documentList, TrainingResult[] trainingResultCollection){
		for(TrainingResult trainingResult : trainingResultCollection){
//			SIDEFilterOperator sideFilterOperator = (SIDEFilterOperator)o;
//			TrainingResult trainingResult = sideFilterOperator.getTrainingResult();
//			if(trainingResult==null){ return; }
			SegmenterPlugin segmenterPlugin = trainingResult.getSegmenterPlugin();
			
			for(JCas jCas : documentList.getJCasList()){
				try {
					UIMAToolkit.segmentJCas(jCas, segmenterPlugin);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}
	
	public static Set<String> addPredictionAnnotation(DocumentList documentList, DefaultMutableTreeNode booleanExpressionRoot){
		TrainingResult[] trainingResultArray = TrainingResult.extract(booleanExpressionRoot);
//		Set<Object> userObjectSet = TreeNodeToolkit.getUserObjectSet(booleanExpressionRoot);
		if(documentList.getSubtypeName()==null){
			segmentUnsavedSegmentation(documentList, trainingResultArray);
		}
		
		Set<String> documentSubtypeNameSet = UIMAToolkit.getSubtypeNameSet(documentList);
		Set<String> predictionSubtypeNameSet = new HashSet<String>();
		for(TrainingResult trainingResult : trainingResultArray){
//			SIDEFilterOperator sideFilterOperator = (SIDEFilterOperator)o;
//			TrainingResult trainingResult = sideFilterOperator.getTrainingResult();
			
			String[] subtypeNameArray = trainingResult.getPredictionSubtypeNameArray(documentList);
			String baseSubtypeName = subtypeNameArray[0];	// segmentation which annotation goes on top of...
			String predictionSubtypeName = subtypeNameArray[1];
//			String predictionSubtypeName = UIMAToolkit.getHiddenPredictionAnnotationSubtypeName(trainingResult, DocumentList.tempSummaryBaseSubtypeName);
			
			predictionSubtypeNameSet.add(predictionSubtypeName);
			
			// if already predicted using this training result
			if(documentSubtypeNameSet.contains(predictionSubtypeName)){ continue; }
			
			if(documentList.getSubtypeName()==null){
				documentList.setSubtypeName(baseSubtypeName);
				UIMAToolkit.addPredictionAnnotation(trainingResult, documentList, predictionSubtypeName, true);
				documentList.setSubtypeName(null);
			}else{
				UIMAToolkit.addPredictionAnnotation(trainingResult, documentList, predictionSubtypeName, true);
			}
			
			try { documentList.saveEach(); }
			catch (Exception e) { e.printStackTrace(); throw new RuntimeException(e); }
		}
		
		return predictionSubtypeNameSet;
	}
	
	@Override
	public List<SIDESegment> summarize(DocumentList documentList) {
		Set<String> predictionSubtypeNameSet = addPredictionAnnotation(documentList, booleanExpressionRoot);
		Map<String,int[]> segmentLastIndexArrayMap = documentList.getSegmentEndIndexArrayMap(predictionSubtypeNameSet);
		DocumentList targetDocumentList = documentList.createSummaryDocumentList(segmentLastIndexArrayMap);
		boolean[] suitabilityArray = getSuitabilityArray(booleanExpressionRoot, targetDocumentList, documentList);
		int[] orderIndex;
		try {
			orderIndex = emPlugin.getOrderIndex(targetDocumentList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		List<SIDESegment> trimmedList = Limit.trimList(limit, targetDocumentList, suitabilityArray, orderIndex, number);
		if(restoreOrder){ Collections.sort(trimmedList, SIDEToolkit.getSIDESegmentComparator(documentList)); }
		for(SIDESegment t : trimmedList){
			System.out.println(t.getCoveredText());
		}
		return trimmedList;
	}


	public EMPlugin getEmPlugin() {
		return emPlugin;
	}

	public DefaultMutableTreeNode getBooleanExpressionRoot() {
		return booleanExpressionRoot;
	}
	
	public TrainingResult[] getTrainingResultArray(){
		return TrainingResult.extract(booleanExpressionRoot);
	}
	
	public static void main(String[] args){
		test01();
	}
	protected static void test01(){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("child 1");
		DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("child 2");
		DefaultMutableTreeNode child3 = new DefaultMutableTreeNode("child 3");
		DefaultMutableTreeNode gc11 = new DefaultMutableTreeNode("gc 11");
		DefaultMutableTreeNode gc31 = new DefaultMutableTreeNode("gc 31");
		DefaultMutableTreeNode gc32 = new DefaultMutableTreeNode("gc 32");
		
		root.add(child1);
		root.add(child2);
		root.add(child3);
		
		child1.add(gc11);
		child3.add(gc31);
		child3.add(gc32);
		
		for(Enumeration<?> enumeration = root.depthFirstEnumeration(); enumeration.hasMoreElements(); ){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumeration.nextElement();
			System.out.println(node.getUserObject());
		}
	}
}
