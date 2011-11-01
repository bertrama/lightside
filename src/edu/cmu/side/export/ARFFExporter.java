package edu.cmu.side.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Set;
import java.util.TreeSet;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.feature.Feature.Type;

public class ARFFExporter {


	/**
	 * Outputs a feature table to a file that can be read by some other software package,
	 * based on some format selected elsewhere in the GUI (as of 10/3/11, only does ARFF format).
	 */
	public static void export(FeatureTable ft, File out){
		try{
			ft.resetCurrentAnnotation();
			BufferedWriter write = new BufferedWriter(new FileWriter(out));
			StringBuilder sb = new StringBuilder("@relation " + ft.getTableName().replaceAll("[\\s\\p{Punct}]","_") + "\n\n");
			Set<String> existingFeatures = new TreeSet<String>();
			for(Feature f : ft.getFeatureSet()){
				if(ft.getActivated(f)){
					String fName = f.getFeatureName().replaceAll("[\\s\\p{Punct}]","_");
					sb.append("@attribute " + fName + " ");
					switch(f.getFeatureType()){
					case NUMERIC:
						sb.append("numeric");
						break;
					case BOOLEAN:
						sb.append("{false, true}");
						break;
					case NOMINAL:
						sb.append("{");
						for(String s : f.getNominalValues()){
							sb.append(s.toLowerCase() + ", ");
						}
						sb.replace(sb.length()-2, sb.length(),"}");
						break;
					}
					sb.append("\n");
				}
			}
			sb.append("@attribute CLASS ");
			switch(ft.getClassValueType()){
			case NUMERIC:
				sb.append("numeric");
				break;
			case BOOLEAN:
				sb.append("{false, true}");
				break;
			case NOMINAL:
				sb.append("{");
				for(String s : ft.getDocumentList().getLabelArray()){
					sb.append(s.toLowerCase() + ", ");
				}
				sb.replace(sb.length()-2, sb.length(),"}");
				break;
			}
			sb.append("\n\n@data\n");
			write.write(sb.toString());
			StringBuilder[] documentStrings = new StringBuilder[ft.getDocumentList().getSize()];
			for(int i = 0; i < documentStrings.length; i++){
				documentStrings[i] = new StringBuilder();
			}
			int featInd = 0;
			for(Feature f : ft.getFeatureSet()){
				Set<Integer> indicesHit = new TreeSet<Integer>();
				for(FeatureHit hit : ft.getHitsForFeature(f)){
					if(!indicesHit.contains(hit.getDocumentIndex())){
						if(f.getFeatureType()!=Type.NUMERIC || !((Double)hit.getValue()).isNaN()){
							indicesHit.add(hit.getDocumentIndex());
							documentStrings[hit.getDocumentIndex()].append(featInd + " " + hit.getValue().toString().toLowerCase() + ", ");						
						}
					}
				}
				featInd++;
			}
			for(int i = 0; i < documentStrings.length; i++){
				documentStrings[i].append(featInd + " " + ft.getDocumentList().getAnnotationArray().get(i).toLowerCase());
			}
			for(StringBuilder string : documentStrings){
				write.write("{"+string.toString() + "}\n");
			}
			write.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
