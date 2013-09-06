package edu.cmu.side.recipe.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import plugins.features.BasicFeatures;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.model.feature.LocalFeatureHit;
import edu.cmu.side.model.feature.LocalFeatureHit.HitLocation;
import edu.cmu.side.plugin.FeatureFetcher;

public class FeatureTableConverter implements Converter{

	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(FeatureTable.class);
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		if(obj==null){
			return;
		}
		FeatureTable table = (FeatureTable) obj;
		DocumentList docList = table.getDocumentList();
		if(docList!=null){
			convertModel(table,writer,context);
		}
			//If we need more models in the future, put them here
		//Obviously the docList null check is a short-sighted, quickfix.
		//In the future we should TODO: make an identifiable save type located in the object.
		else{
			convertPrediction(table,writer,context);
		}
	}
	public void convertPrediction(FeatureTable table, HierarchicalStreamWriter writer, MarshallingContext context){
		writer.addAttribute("type", "prediction");
		writer.startNode("name");
		writer.setValue(table.getName());
		writer.endNode();
		
		writer.startNode("Threshold");
		writer.setValue(((Integer)table.getThreshold()).toString());
		writer.endNode();
		
		writer.startNode("Annotation");
		writer.setValue(table.getAnnotation());
		writer.endNode();
		
		writer.startNode("Type");
		writer.setValue(table.getClassValueType().toString());
		writer.endNode();
		
		writer.startNode("LabelArray");
		for(String label: table.getLabelArray()){
			writer.startNode("Label");
			writer.setValue(label);
			writer.endNode();
		}
		writer.endNode();
		
		writer.startNode("Features");
		Set<Feature> featureSet = table.getFeatureSet();
		ArrayList<Feature> featureList = new ArrayList<Feature>(featureSet);
		for (Feature feat : featureList) {
			writer.startNode("Feature");
			writer.addAttribute("Type", feat.getFeatureType().toString());
			writer.addAttribute("Prefix", feat.getExtractorPrefix());
			if(feat.getFeatureType().equals(Feature.Type.NOMINAL)){
				for(String value: feat.getNominalValues()){
					writer.startNode("Value");
					writer.setValue(value);
					writer.endNode();
				}
			}
			writer.startNode("FeatureValue");
			writer.setValue(feat.toString());
			writer.endNode();

			writer.endNode();
		}
		writer.endNode();
		
		writer.startNode("NumericClassValues");
		
		HashMap<String, double[]> numericCV = (HashMap<String, double[]>)table.getNumericClassValues();
		writer.addAttribute("size", ((Integer)table.getNominalClassValues().size()).toString());
		for(String str: numericCV.keySet()){
			writer.startNode("label");
			writer.addAttribute("name", str);
			writer.startNode("values");
			for(double val: numericCV.get(str)){
				writer.startNode("value");
				writer.setValue(((Double)val).toString());
				writer.endNode();
			}
			writer.endNode();
			writer.endNode();
		}
		writer.endNode();
		
		writer.startNode("NominalClassValues");
		ArrayList<String> nomVals = (ArrayList<String>) table.getNominalClassValues();
		for(String value: nomVals){
			writer.startNode("value");
			writer.setValue(value);
			writer.endNode();
		}
		writer.endNode();
		
		
	}
	public void convertModel(FeatureTable table, HierarchicalStreamWriter writer, MarshallingContext context){
		writer.addAttribute("type", "default");
		DocumentList docList = table.getDocumentList();
		writer.startNode("DocumentList");
		context.convertAnother(docList);
		writer.endNode();
		
		writer.startNode("Threshold");
		writer.setValue(((Integer)table.getThreshold()).toString());
		writer.endNode();
		
		writer.startNode("Annotation");
		writer.setValue(table.getAnnotation());
		writer.endNode();
		
		writer.startNode("Type");
		writer.setValue(table.getClassValueType().toString());
		writer.endNode();
		
		writer.startNode("Features");
		Set<Feature> featureSet = table.getFeatureSet();
		ArrayList<Feature> featureList = new ArrayList<Feature>(featureSet);
		for (Feature feat : featureList) {
			writer.startNode("Feature");
			writer.addAttribute("Type", feat.getFeatureType().toString());
			writer.addAttribute("Prefix", feat.getExtractorPrefix());
			if(feat.getFeatureType().equals(Feature.Type.NOMINAL)){
				for(String value: feat.getNominalValues()){
					writer.startNode("Value");
					writer.setValue(value);
					writer.endNode();
				}
			}
			writer.startNode("FeatureValue");
			writer.setValue(feat.toString());
			writer.endNode();


			for(FeatureHit hit: table.getHitsForFeature(feat)){
				writer.startNode("FeatureHits");
				writer.addAttribute("DocumentIndex", ((Integer)hit.getDocumentIndex()).toString());
				writer.addAttribute("Value", hit.getValue().toString());
				if(hit.getClass().equals(LocalFeatureHit.class)){
					writer.startNode("Locations");
					String hitLocs= "";
					for(HitLocation hitLoc: ((LocalFeatureHit) hit).getHits()){
						hitLocs+=hitLoc.getColumn() +",";
						hitLocs+=((Integer) hitLoc.getStart()).toString()+",";
						hitLocs+=((Integer) hitLoc.getEnd()).toString()+";";
					}
					hitLocs=hitLocs.substring(0,hitLocs.length()-2);
					writer.setValue(hitLocs);
					writer.endNode();
				}
				writer.endNode();
			}
			writer.endNode();
		}
		writer.endNode();




	}
	//DocumentList sdl, 
	//Collection<FeatureHit> hits, int thresh, 
	//String annotation, Feature.Type type
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		Object toReturn = null;
		if(reader.getAttribute("type").equals("default")){
			toReturn = readDefault(reader,context);
		} else if (reader.getAttribute("type").equals("prediction")){
			toReturn = readPrediction(reader,context);
		} else {
			throw new UnsupportedOperationException("XML file cannot be parsed");
		}
		return toReturn;
	}
	
	private Object readPrediction(HierarchicalStreamReader reader, UnmarshallingContext context){
		reader.moveDown();
		String tableName = reader.getValue();
		reader.moveUp();
		reader.moveDown();
		Integer threshold = Integer.parseInt(reader.getValue());
		reader.moveUp();
		reader.moveDown();
		String currentAnnotation = reader.getValue();
		reader.moveUp();
		reader.moveDown();
		String typeString = reader.getValue();
		Feature.Type featTypeTotal = Feature.Type.valueOf(typeString);
		reader.moveUp();
		ArrayList<String> labelArrayList = new ArrayList<String>();
		reader.moveDown();
		while(reader.hasMoreChildren()){
			reader.moveDown();
			labelArrayList.add(reader.getValue());
			reader.moveUp();
		}
		reader.moveUp();
		String[] labelArray = Arrays.copyOf(labelArrayList.toArray(), labelArrayList.size(),String[].class);
		
		
		reader.moveDown();
		ArrayList<Feature> features = new ArrayList<Feature>();
		while(reader.hasMoreChildren()){
			reader.moveDown();
			String type = reader.getAttribute("Type");
			Feature.Type featType = Feature.Type.valueOf(type);
			String prefix = reader.getAttribute("Prefix");
			reader.moveDown();

			String value = reader.getValue();
			FeatureFetcher fetcher = new BasicFeatures();
			Feature toAdd = Feature.fetchFeature(prefix, value, featType, fetcher);
			features.add(toAdd);
			reader.moveUp();
			reader.moveUp();
		}
		reader.moveUp();
		HashMap<String, double[]> numClassValues = new HashMap<String, double[]>();
		reader.moveDown();
		Integer size = Integer.parseInt(reader.getAttribute("size"));
		while(reader.hasMoreChildren()){
			reader.moveDown();
			String name = reader.getAttribute("name");
			reader.moveDown();
			
			double[] valuesArray = new double[size];
			int currentValue = 0;
			while(reader.hasMoreChildren()){
				reader.moveDown();
				valuesArray[currentValue]=Double.parseDouble(reader.getValue());
				currentValue++;
				reader.moveUp();
			}
			numClassValues.put(name, valuesArray);
			reader.moveUp();
			reader.moveUp();
		}
		
		reader.moveUp();
		
		reader.moveDown();
		ArrayList<String> nominalClassValues = new ArrayList<String>();
		while(reader.hasMoreChildren()){
			reader.moveDown();
			nominalClassValues.add(reader.getValue());
			reader.moveUp();
		}
		reader.moveUp();
		
		
		return null;
	}
	
	private Object readDefault(HierarchicalStreamReader reader, UnmarshallingContext context){
		reader.moveDown();
		ArrayList<Feature> features = new ArrayList<Feature>();
		ArrayList<FeatureHit> hits = new ArrayList<FeatureHit>();
		//Need to do a null check here
		DocumentList docList =(DocumentList)context.convertAnother(null, DocumentList.class);
		
		reader.moveUp();
		reader.moveDown();
		Integer threshold = Integer.parseInt(reader.getValue());
		reader.moveUp();
		reader.moveDown();
		String annotation = reader.getValue();
		reader.moveUp();
		reader.moveDown();
		Feature.Type largerType = Feature.Type.valueOf(reader.getValue());
		reader.moveUp();
		reader.moveDown();
		while(reader.hasMoreChildren()){
			reader.moveDown();
			String type = reader.getAttribute("Type");
			Feature.Type featType = Feature.Type.valueOf(type);
			String prefix = reader.getAttribute("Prefix");
			reader.moveDown();

			String value = reader.getValue();
			FeatureFetcher fetcher = new BasicFeatures();
			Feature toAdd = Feature.fetchFeature(prefix, value, featType, fetcher);
			features.add(toAdd);
			reader.moveUp();
//			ArrayList<FeatureHit> localFeatureHits = new ArrayList<FeatureHit>();
			while(reader.hasMoreChildren()){
				reader.moveDown();
				Integer docIndex = Integer.parseInt(reader.getAttribute("DocumentIndex"));
				Object finalValue = "";
				String stringValue = reader.getAttribute("Value");
				switch(featType){
				case BOOLEAN:
					Boolean boolValue = Boolean.valueOf(stringValue);
					finalValue = (Object) boolValue;
					break;
				case NOMINAL: case STRING:
					finalValue = (Object) stringValue;
					break;
				case NUMERIC:
					Number numericValue = Double.parseDouble(stringValue);
					finalValue = (Object) numericValue;
					break;
				}


				reader.moveDown();
				String parseThis = reader.getValue();
				String[] parsed = parseThis.split(";");
				ArrayList<HitLocation> hitLoc = new ArrayList<HitLocation>();
				for(String str: parsed){
					String[] information = str.split(",");
					if(information.length==3){
						String column = information[0];
						Integer start = Integer.parseInt(information[1]);
						Integer end = Integer.parseInt(information[2]);
						hitLoc.add(new HitLocation(column,start,end));
					}
				}
				hits.add(new LocalFeatureHit(toAdd, finalValue, docIndex, hitLoc));
				reader.moveUp();
				reader.moveUp();
			}
			reader.moveUp();
		}
		reader.moveUp();
		return new FeatureTable(docList, hits, threshold, annotation, largerType);
	}

}
