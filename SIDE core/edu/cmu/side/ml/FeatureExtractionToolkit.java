package edu.cmu.side.ml;

import java.awt.Component;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import oracle.xml.parser.v2.XMLDocument;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mysterion.xml.XMLBoss;
import com.yerihyo.yeritools.CalendarToolkit;
import com.yerihyo.yeritools.RTTIToolkit;
import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.collections.CountMap;
import com.yerihyo.yeritools.collections.MapToolkit;
import com.yerihyo.yeritools.collections.ValueMap;
import com.yerihyo.yeritools.collections.MapToolkit.ListValueMap;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.math.StatisticsToolkit;
import com.yerihyo.yeritools.text.StringToolkit;
import com.yerihyo.yeritools.xml.XMLToolkit;
import com.yerihyo.yeritools.xml.XMLable;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.SIDEToolkit.FileType;
import edu.cmu.side.dataitem.DataItem;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTableKey.FeatureTableKeyDefaultComparator;
import edu.cmu.side.plugin.FEPlugin;
import edu.cmu.side.plugin.PluginWrapper;
import edu.cmu.side.uima.DocumentListInterface;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;
import edu.cmu.side.uima.type.SIDEAnnotation;

/*
 * Feature extractor for the testw data
 */
public class FeatureExtractionToolkit {

	public static class FeatureTableKey implements Serializable {
		private static final long serialVersionUID = 1L;

		private String featureExtractorClassName;
		private String featureName;

		private boolean isMeta;

		public boolean isMeta(){ return isMeta; }
		public FeatureTableKey(String s1, String s2) {
			featureExtractorClassName = s1;
			featureName = s2;
			isMeta = s1.startsWith("meta"); 
		}

		public String getFeatureName() {
			return featureName;
		}

		public boolean equals(Object o) {
			if (!(o instanceof FeatureTableKey)) {
				return false;
			}
			FeatureTableKey key = (FeatureTableKey) o;
			return (this.featureExtractorClassName
					.equals(key.featureExtractorClassName) && this.featureName
					.equals(key.featureName));
		}

		public static class FeatureTableKeyDefaultComparator implements
		Comparator<FeatureTableKey>, Serializable {
			private static final long serialVersionUID = 1L;

			@Override
			public int compare(FeatureTableKey o1, FeatureTableKey o2) {
				int returnValue = o1.featureExtractorClassName.compareTo(
						o2.featureExtractorClassName);
				if (returnValue != 0) {
					return returnValue;
				}

				return o1.featureName.compareTo(o2.featureName);
			}
		}

		public static class FeatureTableKeyAlphabeticComparator implements
		Comparator<FeatureTableKey>, Serializable {
			private static final long serialVersionUID = 1L;

			@Override
			public int compare(FeatureTableKey o1, FeatureTableKey o2) {
				return o1.getFeatureIdentifyingString().compareTo(o2.getFeatureIdentifyingString());
			}
		}

		public String getFeatureIdentifyingString(){
			return featureExtractorClassName+"_"+featureName;
		}

		public String getFeatureExtractorClassName() {
			return featureExtractorClassName;
		}

		public String toString(){
			return getFeatureName();
		}

	}

	public static class NominalFeatureTableKey extends FeatureTableKey{
		public NominalFeatureTableKey(String s1, String s2) {
			super(s1, s2);
		}		
	}

	public static ListValueMap<String,FeatureTableKey> createFeatureTableKeyMap(List<FeatureTableKey> featureTableKeyList){
		ListValueMap<String,FeatureTableKey> featureTableKeyMap = new ListValueMap<String,FeatureTableKey>();
		for(FeatureTableKey featureTableKey : featureTableKeyList){
			featureTableKeyMap.add(featureTableKey.getFeatureExtractorClassName(), featureTableKey);
		}
		return featureTableKeyMap;
	}

	public static class FeatureTableKeyRenderer extends DefaultListCellRenderer{
		private static final long serialVersionUID = 1L;

		private boolean nullAsAll = false;
		private FeatureTableKeyRenderer(){}
		public static FeatureTableKeyRenderer create(){ return create(false); }
		public static FeatureTableKeyRenderer create(boolean nullAsAll){
			FeatureTableKeyRenderer ftkr = new FeatureTableKeyRenderer();
			ftkr.setNullAsAll(nullAsAll);
			return ftkr;
		}
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
			if(value==null){
				if(this.isNullAsAll()){
					return super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
				}else{
					return super.getListCellRendererComponent(list, "all", index, isSelected, cellHasFocus);
				}

			}

			if(!(value instanceof FeatureTableKey)){
				throw new IllegalArgumentException();
			}

			FeatureTableKey featureTableKey = (FeatureTableKey)value;
			String displayString = featureTableKey.getFeatureIdentifyingString();
			return super.getListCellRendererComponent(list, displayString, index, isSelected, cellHasFocus);

		}
		public boolean isNullAsAll() {
			return nullAsAll;
		}
		public void setNullAsAll(boolean nullAsAll) {
			this.nullAsAll = nullAsAll;
		}
	}

	/**
	 * Smoothings not done yet.
	 * May be required for future replicability of training.
	 * @author moonyoun
	 *
	 */
	public static class FeatureTable extends DataItem implements XMLable{

		private static final long serialVersionUID = 1L;

		private boolean meta = false;
		private Integer rareFeatureThreshold = -1;
		private FeatureFilterer featureFilterer = null;
		private DocumentList documentList = null;
		private SortedMap<String,FeatureResult> featureResultMap = new TreeMap<String,FeatureResult>();
		public static final String xmlTag = "featuretable";
		private FeatureTable evalSet = null;

		public void addEvalSet(FeatureTable fe){
			evalSet = fe;
		}

		public FeatureTable getEvalSet(){
			return evalSet;
		}

		public boolean hasEvalSet(){
			return evalSet != null;
		}

		public void setRareFeatureThreshold(Integer i){
			rareFeatureThreshold = i;
		}

		public Integer getRareFeatureThreshold(){
			return (rareFeatureThreshold==null?-1:rareFeatureThreshold);
		}
		/*
		 * Metafeatures used in:
		 * 
		 * WekaToolkit.getAttributes()
		 * WekaToolkit.createInstances()
		 *  
		 */
		public void setUseMetafeatures(boolean m){
			meta = m;
		}

		public boolean getUseMetafeatures(){
			return meta;
		}

		public void removeFeature(FeatureTableKey ftk){
			featureResultMap.keySet().remove(ftk.getFeatureExtractorClassName());
		}

		private static interface FeatureFilterer extends XMLable{
			public boolean isFeatureValid(FeatureTableKey key);
			public Set<String> getFilteredFeatures();
		}

		public FeatureTable clone(){
			FeatureTable ft = new FeatureTable();
			try {
				ft.fromXML(XMLBoss.XMLFromString(this.toXML()).getDocumentElement());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			ft.timestamp = System.currentTimeMillis();
			Workbench.current.featureTableListManager.add(ft);
			return ft;
		}

		public Number[] getValueOfFeature(FeatureTableKey k){
			DocumentListInterface documentList = this.getDocumentList();
			int documentListLength = documentList.getSize();

			Number[] returnValue = new Number[documentListLength];
			for(int i=0; i<documentListLength; i++){
				returnValue[i] = this.getValue(k, i);
			}
			return returnValue;
		}

		public String nominalValue(FeatureTableKey k, int docIndex){
			if(k instanceof NominalFeatureTableKey){
				String[] noms = metaFeatures.get(k.getFeatureIdentifyingString());
				return noms[docIndex];
			}else return null;
		}

		public Set<String> nominalValues(FeatureTableKey k){
			if(!(k instanceof NominalFeatureTableKey)){
				return null;
			}else return attributeSets.get(k);
		}
		
		public Number getSafeValue(FeatureTableKey k, int docIndex){
			Number out = featureResultMap.get(k.getFeatureExtractorClassName()).invertedIndex.get(k.getFeatureName()).get(docIndex);
			return out;
		}

		public Number getValue(FeatureTableKey k, int docIndex){
			if(featureFilterer!=null && !featureFilterer.isFeatureValid(k)){ return 0; }
			if(metaFeatures != null && k.isMeta()){
				if(k instanceof NominalFeatureTableKey){
					String[] options = attributeSets.get(k).toArray(new String[0]);
					return indexOf(metaFeatures.get(k.getFeatureIdentifyingString())[docIndex], options);
				}
				else{
					return Double.parseDouble(metaFeatures.get(k.getFeatureIdentifyingString())[docIndex]);
				}
			}
			Number value = featureResultMap.get(k.getFeatureExtractorClassName()).invertedIndex.get(k.getFeatureName()).get(docIndex);
			return value==null?0:value;
		}

		public Set<Integer> getDocsOf(FeatureTableKey k){
			if(k.isMeta()){
				TreeSet<Integer> metaOut = new TreeSet<Integer>();
				String[] m = metaFeatures.get(k.getFeatureIdentifyingString());
				for(int i = 0; i <  m.length; i++) metaOut.add(i);
				return metaOut;
			}
			else return featureResultMap.get(k.getFeatureExtractorClassName()).invertedIndex.get(k.getFeatureName()).keySet();				
		}

		public Number indexOf(String s, String[] options){
			for(int i = 0; i < options.length; i++){
				if(s.equals(options[i])) return i+1;
			}
			return 0;
		}

		public Object[] getAllValuesOfDocument(int docIndex, Collection<? extends FeatureTableKey> featureTableKeyCollection){
			if(featureTableKeyCollection==null){
				featureTableKeyCollection = this.getAllFeatureTableKeyList();
			}

			Object[] horizontalArray = new Object[featureTableKeyCollection.size()];
			Iterator<? extends FeatureTableKey> iterator = featureTableKeyCollection.iterator();
			for(int j=0; iterator.hasNext(); j++){
				FeatureTableKey next = iterator.next();
				if(next instanceof NominalFeatureTableKey){
					horizontalArray[j] = nominalValue(next, docIndex);
				}else{
					horizontalArray[j] = getValue(next,docIndex);	
				}
			}
			return horizontalArray;
		}

		@Deprecated
		public Number[] getValueOfDocument(int docIndex, Collection<? extends FeatureTableKey> featureTableKeyCollection){
			if(featureTableKeyCollection==null){
				featureTableKeyCollection = this.getAllFeatureTableKeyList();
			}

			Number[] horizontalArray = new Number[featureTableKeyCollection.size()];
			Iterator<? extends FeatureTableKey> iterator = featureTableKeyCollection.iterator();
			for(int j=0; iterator.hasNext(); j++){
				horizontalArray[j] = getValue(iterator.next(),docIndex);
			}
			return horizontalArray;
		}

		public List<FeatureTableKey> getAllFeatureTableKeyList(){
			List<FeatureTableKey> featureTableKeyList = new ArrayList<FeatureTableKey>();
			Collection<FeatureResult> featureResultCollection = featureResultMap.values();

			for(FeatureResult featureResult : featureResultCollection){
				List<FeatureTableKey> list = featureResult.getAllFeatureTableKeyList();

				for(FeatureTableKey key : list){
					if(featureFilterer!=null && !featureFilterer.isFeatureValid(key)){ 
						continue; 
					}	
					featureTableKeyList.add(key);
				}
			}
			if(getAttributeSets() != null){
				for(FeatureTableKey key : getAttributeSets().keySet()){
					if(!featureTableKeyList.contains(key)){
						featureTableKeyList.add(key);						
					}
				}
			}
			Collections.sort(featureTableKeyList, new FeatureTableKeyDefaultComparator());
			return featureTableKeyList;
		}

		public List<NominalFeatureTableKey> metaList(){
			List<NominalFeatureTableKey> keys = new ArrayList<NominalFeatureTableKey>();
			for(String s : getMetaFeatures().keySet()){
				keys.add(new NominalFeatureTableKey("meta", s));
			}
			return keys;
		}

		public boolean areFeaturesExtracted(){
			if(this.featureResultMap.size()==0){ return false; }
			return this.featureResultMap.values().iterator().next().getGlobalResult().size()!=0;
		}

		public static class NameFeatureFilterer implements FeatureFilterer{
			private Set<String> invalidFeatureNameSet = new HashSet<String>();

			public Set<String> getFilteredFeatures(){
				return invalidFeatureNameSet;
			}

			public NameFeatureFilterer(Set<String> invalidNames){
				invalidFeatureNameSet = invalidNames;
			}

			public NameFeatureFilterer(FeatureTableKey[] featureTableKeyArray){
				for(FeatureTableKey key : featureTableKeyArray){
					invalidFeatureNameSet.add(key.getFeatureIdentifyingString());
				}
			}


			private static final String featureNameXMLTag = "feature_name";

			@Override
			public boolean isFeatureValid(FeatureTableKey key) {
				return !invalidFeatureNameSet.contains(key.getFeatureIdentifyingString());
			}

			@Override
			public void fromXML(Element root) throws Exception {
				invalidFeatureNameSet.clear();

				for(Element element: XMLToolkit.getChildElements(root)){
					if(!element.getTagName().equals(featureNameXMLTag)){ continue; }

					invalidFeatureNameSet.add(XMLToolkit.getTextContent(element).toString());
				}
			}

			@Override
			public String toXML(){
				StringBuilder builder = new StringBuilder();
				for(String name : invalidFeatureNameSet){
					builder.append(XMLToolkit.wrapContentWithTag(XMLToolkit.wrapCdata(name), featureNameXMLTag));
				}
				return XMLToolkit.wrapContentWithTag(builder, "filter").toString();
			}

			public Set<String> getInvalidFeatureNameSet() {
				return invalidFeatureNameSet;
			}
		}

		public String toXML(){
			StringBuilder builder = new StringBuilder();

			builder.append(this.itemsToXML());
			if(documentList!=null){
				builder.append(documentList.toXML());
			}

			StringBuilder results = new StringBuilder();
			for(FeatureResult featureResult : featureResultMap.values()){
				results.append(featureResult.getFePlugin().toXML());
			}
			builder.append(XMLToolkit.wrapContentWithTag(results, "feplugins") + "\n");
			builder.append(XMLToolkit.wrapContentWithTag(""+getRareFeatureThreshold(), "rare") + "\n");
			if(getFeatureFilterer() != null) builder.append(getFeatureFilterer().toXML() + "\n");
			return XMLToolkit.wrapContentWithTag(builder, xmlTag).toString();
		}

		@Override
		public void fromXML(Element element) throws Exception {
			List<URI> sourceURI = new ArrayList<URI>();
			for(Element childElement : XMLToolkit.getChildElements(element)){
				String elementName = childElement.getTagName();

				if(elementName.equalsIgnoreCase("docs")){
					for(Element docElement : XMLToolkit.getChildElements(childElement)){
						sourceURI.add(new URI(XMLToolkit.getTextContent(docElement).toString()));
					}
				}
				else if(elementName.equalsIgnoreCase(DocumentList.tagName)){
					documentList = DocumentList.create(childElement);
				}
				else if(elementName.equalsIgnoreCase("feplugins")){
					for(Element fepluginElement : XMLToolkit.getChildElements(childElement)){
						String fepluginClassName = fepluginElement.getAttribute("classname");
						PluginWrapper pluginWrapper = Workbench.current.pluginManager.getPluginWrapperByPluginClassName(fepluginClassName);
						FEPlugin fePlugin = (FEPlugin)pluginWrapper.getSIDEPlugin();
						fePlugin.fromXML(fepluginElement);
						this.addFEPlugin(fePlugin);
					}
				}else if(elementName.equalsIgnoreCase("rare")){
					setRareFeatureThreshold(Integer.parseInt(childElement.getTextContent()));
				}else if(elementName.equalsIgnoreCase("filter")){
					FeatureFilterer f = new NameFeatureFilterer(new FeatureTableKey[0]);
					f.fromXML(childElement);
					setFeatureFilterer(f);
				}
				else{
					this.itemsFromXML(childElement);
				}
			}

			for(URI uri : sourceURI){
				File f = new File(uri);
				if(!f.exists()){
					f = new File(SIDEToolkit.xmiFolder+f.getName());
					if(!f.exists()) continue;
				}
				CAS cas = UIMAToolkit.createSIDECAS();
				JCas jCas = cas.getJCas();
				documentList.getJCasList().add(jCas);
			}
			rebuild(null, true);
		}

		public class FeatureResult
		{
			/**
			 * fePlugin cannot be changed once set.
			 */
			private FEPlugin fePlugin;
			private transient ValueMap<String> globalResult = new ValueMap<String>();
			Map<String, Map<Integer, Number>> invertedIndex = new TreeMap<String, Map<Integer, Number>>();
			private transient CountMap<String> featureCounts = new CountMap<String>();
			public FeatureResult(FEPlugin fePlugin) {
				this.fePlugin = fePlugin;
			}

			public Integer getDocumentCount(String feature){
				return (featureCounts.containsKey(feature) ?featureCounts.get(feature):0);
			}

			public FEPlugin getFePlugin() {
				return fePlugin;
			}

			/**
			 * For training, always pass in null for replicability.
			 * For smoothing, future operations will be provided.
			 * For testing, pass in features that was in the training set.
			 * @param constraintFeatureNameList
			 */
			public void rebuild(List<String> constraintFeatureNameList) {
				invertedIndex.clear();
				globalResult.clear();
				featureCounts.clear();
				addAllDocumentResults(constraintFeatureNameList);
			}

			private void addAllDocumentResults(List<String> constraintFeatureNameList){
				Map<String,Number> result;
				try{
					List<Map<String,Number>> features = fePlugin.extractFeatureMap(documentList);
					for(int i = 0; i < features.size(); i++){
						result = (Map<String, Number>)features.get(i);
						if(constraintFeatureNameList != null){
							CollectionsToolkit.removeMapEntryNotInList(result, constraintFeatureNameList);
							CollectionsToolkit.fillEmptyMapEntryWithDefaultValue(result, constraintFeatureNameList, new Double(0));							
						}
						for(String feat : result.keySet()){
							Number val = result.get(feat);
							if(val != null && val.doubleValue() > 0) featureCounts.increCount(feat);
							if(!invertedIndex.containsKey(feat)) invertedIndex.put(feat, new TreeMap<Integer, Number>());
							invertedIndex.get(feat).put(i, result.get(feat));
						}
						globalResult.addValueAll(features.get(i));
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}

			public ValueMap<String> getGlobalResult() {
				return globalResult;
			}

			public List<FeatureTableKey> getAllFeatureTableKeyList(){
				List<FeatureTableKey> featureTableKeyList = new ArrayList<FeatureTableKey>();

				String fePluginName = this.getFePlugin().getOutputName();

				ValueMap<String> valueMap = this.getGlobalResult();
				for(String featureName : valueMap.keySet()){
					FeatureTableKey featureTableKey = new FeatureTableKey(fePluginName, featureName);
					featureTableKeyList.add(featureTableKey);
				}
				return featureTableKeyList;
			}
		}

		public CharSequence toHtmlTable(){
			StringBuilder builder = new StringBuilder();
			builder.append("<TABLE border='1' bordercolor='#999999'>");
			

			StringBuilder documentNames = new StringBuilder("<TR>");
			StringBuilder documentTots = new StringBuilder("<TR>");
			StringBuilder[] documentRows = new StringBuilder[documentList.getSize()];
			for(int i = 0; i < documentRows.length; i++) documentRows[i] = new StringBuilder("<TR>");
			for(FeatureTableKey ftk : getAllFeatureTableKeyList()){
				Set<Integer> docs = getDocsOf(ftk);
				for(int i = 0; i < documentRows.length; i++){
					documentRows[i].append("<TD>0</TD>");
				}
				double tot = 0.0;
				for(Integer i : docs){
					double val = getValue(ftk, i).doubleValue();
					tot += val;
					documentRows[i].replace(documentRows[i].length()-6, documentRows[i].length()-5, val+"");
				}
				documentTots.append("<TD>"+tot+"</TD>");
				documentNames.append("<TD>"+ftk.getFeatureName()+"</TD>");
			}
			documentTots.append("</TR>");
			
			for(FeatureResult featureResult : featureResultMap.values()){
				builder.append("<TR>");
				builder.append("<TD></TD>");builder.append("<TD>");
				builder.append(featureResult.getFePlugin().getClass().getName()).append("</TD></TR>");
				builder.append(documentNames.toString());
				builder.append(documentTots.toString());
				for(int i = 0; i < documentRows.length; i++){
					builder.append(documentRows[i].toString()+"</TR>");
				}
			}
			builder.append("</TR>");


			builder.append("</TABLE>");
			return builder;
		}

		private boolean booleanFeatures;

		public boolean booleanFeatures(){
			return booleanFeatures;
		}

		public void setBooleanFeatures(boolean b){
			booleanFeatures = b;
		}

		private FeatureTable() {
			timestamp = System.currentTimeMillis();
		}
		
		public static FeatureTable createFromXML(Element root) throws Exception{
			FeatureTable featureTable = new FeatureTable();
			featureTable.fromXML(root);
			return featureTable;
		}
		public static FeatureTable createFromXMLFile(File file) throws Exception{
			XMLDocument doc = XMLBoss.XMLFromFile(file);
			Element root = doc.getDocumentElement();
			return createFromXML(root);
		}

		public static FeatureTable createAndBuild(DocumentList documentList, FEPlugin[] fePluginArray, Integer rareFeatureThreshold, List<FeatureTableKey> constraintFeatureTableKeyList){
			FeatureTable featureTable = new FeatureTable();
			featureTable.setDocumentList(documentList);
			if(rareFeatureThreshold != null) featureTable.setRareFeatureThreshold(rareFeatureThreshold);
			featureTable.addFEPluginArray(fePluginArray);
			featureTable.rebuild(constraintFeatureTableKeyList, true);
			return featureTable;
		}

		private HashMap<String, String[]> metaFeatures;
		private HashMap<FeatureTableKey, Set<String>> attributeSets;

		private static Set<String> attributeSet(String[] vect){
			HashSet<String> attributes = new HashSet<String>();
			for(String s : vect){
				attributes.add(s);
			}
			return attributes;
		}

		private HashMap<FeatureTableKey, Set<String>> getAttributeSets(){
			return attributeSets;
		}

		public HashMap<String, String[]> getMetaFeatures(){
			return metaFeatures;
		}

		public HashMap<String, String[]> generateMetaFeatures(String[] annotationArray){
			if(attributeSets == null) attributeSets = new HashMap<FeatureTableKey, Set<String>>();
			if(metaFeatures == null) metaFeatures = new HashMap<String, String[]>();
			HashMap<String, Iterator<?>> its = documentList.iterators();
			HashMap<String, ArrayList<SIDEAnnotation>> lists = convertIteratorsToLists(its);
			if(annotationArray != null && lists != null){
				String trueAnnotation = findTrueAnnotation(annotationArray, lists);
				createMetaFeatures(lists, trueAnnotation);	
			}
			return metaFeatures;
		}

		private void createMetaFeatures(
				HashMap<String, ArrayList<SIDEAnnotation>> lists,
				String trueAnnotation) {
			ArrayList<SIDEAnnotation> classValueAnnotations = lists.get(trueAnnotation);
			for(String n : lists.keySet()){
				if(!n.equals(trueAnnotation)){
					ArrayList<SIDEAnnotation> testAnnotation = lists.get(n);
					if(sameSegmentation(classValueAnnotations, testAnnotation)){
						ArrayList<String> features = new ArrayList<String>();
						for(int i = 0; i < testAnnotation.size(); i++){
							features.add(testAnnotation.get(i).getLabelString());
						}
						String[] annotation = features.toArray(new String[0]);
						try{
							for(String anno : annotation){
								Double.parseDouble(anno);
							}
							FeatureTableKey numKey = new FeatureTableKey("meta", n);
							attributeSets.put(numKey, null);
							metaFeatures.put(numKey.getFeatureIdentifyingString(), annotation);
						}catch(Exception e){
							NominalFeatureTableKey nkey = new NominalFeatureTableKey("meta", n);
							attributeSets.put(nkey, attributeSet(annotation));
							metaFeatures.put(nkey.getFeatureIdentifyingString(), annotation);							
						}
					}
				}
			}
		}

		private HashMap<String, ArrayList<SIDEAnnotation>> convertIteratorsToLists(HashMap<String, Iterator<?>> its) {
			HashMap<String, ArrayList<SIDEAnnotation>> lists = new HashMap<String, ArrayList<SIDEAnnotation>>();
			for(String n : its.keySet()){
				Iterator<?> it = its.get(n);
				createAnnotationList(lists, it);
			}
			return lists;
		}

		private void createAnnotationList(HashMap<String, ArrayList<SIDEAnnotation>> lists, Iterator<?> it) {
			while(it.hasNext()){
				Object o = it.next();
				if(o instanceof SIDEAnnotation){
					SIDEAnnotation s = ((SIDEAnnotation)o);
					if(s != null){
						ArrayList<SIDEAnnotation> list = lists.get(s.getSubtypeName());
						if(list == null){
							lists.put(s.getSubtypeName(), new ArrayList<SIDEAnnotation>());
						}
						lists.get(s.getSubtypeName()).add(s);
					}
				}
			}
		}

		private boolean sameSegmentation(ArrayList<SIDEAnnotation> ann1, ArrayList<SIDEAnnotation> ann2){
			if(ann1 == null || ann2 == null) return false;
			if(ann1.size() != ann2.size()) return false;
			for(int i = 0; i < ann1.size(); i++){
				if(ann1.get(i).getBegin() != ann2.get(i).getBegin() ||
						ann1.get(i).getEnd() != ann2.get(i).getEnd()) return false;
			}
			return true;
		}

		private String findTrueAnnotation(String[] annotationArray,
				HashMap<String, ArrayList<SIDEAnnotation>> lists) {
			for(String n : lists.keySet()){
				if(checkSameAnnotation(annotationArray, lists.get(n))){
					return n;
				}
			}
			return null;
		}

		private boolean checkSameAnnotation(String[] annotationArray,
				ArrayList<SIDEAnnotation> list) {
			if(annotationArray == null || list == null || annotationArray.length != list.size()) return false;
			for(int i = 0; i < list.size(); i++){
				if(list.get(i) == null || list.get(i).getLabelString() == null || !list.get(i).getLabelString().equals(annotationArray[i])){
					return false;
				}
			}
			return true;
		}

		public DocumentList getDocumentList() {
			return documentList;
		}

		public void setFeatureFilterer(Set<String> filt){
			if(getFeatureFilterer() != null){
				getFeatureFilterer().getFilteredFeatures().addAll(filt);
			}else{
				setFeatureFilterer(new NameFeatureFilterer(filt));
			}

		}

		public void rebuild(List<FeatureTableKey> constraintFeatureTableKeyList, boolean filter) {
			ListValueMap<String,FeatureTableKey> constraintFeatureTableKeyMap = null;
			if(constraintFeatureTableKeyList!=null){
				constraintFeatureTableKeyMap = createFeatureTableKeyMap(constraintFeatureTableKeyList);
			}

			for (String key : featureResultMap.keySet()) {
				FeatureResult featureResult = featureResultMap.get(key);

				List<String> constraintFeatureNameList = null;
				if(constraintFeatureTableKeyMap!=null){
					constraintFeatureNameList = UIMAToolkit.getFeatureNameList(constraintFeatureTableKeyMap.get(key));
				}
				featureResult.rebuild(constraintFeatureNameList);
			}
			FeatureTableKey[] feats = getAllFeatureTableKeyList().toArray(new FeatureTableKey[0]);
			if(filter){
				for (FeatureTableKey ftk : feats) {
					int count = 0;
					for (String result : getFeatureResultMap().keySet()) {
						count += getFeatureResultMap().get(result)
						.getDocumentCount(ftk.getFeatureName());
					}
					if (count < getRareFeatureThreshold()) {
						getFeatureResultMap().get(
								ftk.getFeatureExtractorClassName())
								.getGlobalResult().keySet().remove(
										ftk.getFeatureName());
					}
				}				
			}
		}


		public SortedMap<String, FeatureResult> getFeatureResultMap() {
			return featureResultMap;
		}

		public List<FEPlugin> getFEPluginList() {
			List<FEPlugin> fePluginList = new ArrayList<FEPlugin>();

			for(FeatureResult featureResult : this.featureResultMap.values()){
				FEPlugin fePlugin = featureResult.getFePlugin();
				fePluginList.add(fePlugin);
			}
			return fePluginList;
		}

		public void addFEPlugin(FEPlugin fePlugin) {
			FeatureResult featureResult = new FeatureResult(fePlugin);
			this.getFeatureResultMap().put(fePlugin.getOutputName(), featureResult);
		}
		public void addFEPluginArray(FEPlugin[] fePluginArray) {
			for(FEPlugin fePlugin: fePluginArray){
				addFEPlugin(fePlugin);
			}
		}

		public String[] getLabelArray() {
			return this.documentList.getLabelArray();
		}

		public void setDocumentList(DocumentList documentList) {
			this.documentList = documentList;
		}

		public String getDescription() {
			StringBuilder builder = new StringBuilder();

			String timeString = CalendarToolkit.toString(timestamp, CalendarToolkit.YERI_DEFAULT_TIME_FORMAT);
			builder.append("Created time:" ).append(timeString).append(StringToolkit.newLine());
			builder.append(documentList.getDescription()).append(StringToolkit.newLine());
			return builder.toString();
		}

		public void save(){
			File currentSaveFile = null;
			//			currentSaveFile = this.saveFile;
			if(currentSaveFile==null){
				String filename = this.getSavefileName()+".xml";
				File selectedFile = SIDEToolkit.getUserSelectedFile(SIDEToolkit.featureTableFolder, filename);
				currentSaveFile = selectedFile;
			}

			if(currentSaveFile==null){ return ; }

			try {
				FileToolkit.writeTo(currentSaveFile, this.toXML());
			} catch (IOException e1) {
				System.err.println(e1);
			}
			//			this.saveFile = currentSaveFile;
		}

		public static final File folder = SIDEToolkit.featureTableFolder;

		@Override
		public FileType getFileType() {
			return FileType.featureTable;
		}

		public void setFeatureFilterer(FeatureFilterer featureFilterer) {
			this.featureFilterer = featureFilterer;
		}

		public FeatureFilterer getFeatureFilterer() {
			return featureFilterer;
		}

	}
}
