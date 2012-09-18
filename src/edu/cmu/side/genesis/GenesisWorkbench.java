package edu.cmu.side.genesis;

import java.util.*;

import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;

public class GenesisWorkbench {

	
	Map<String, Integer> documentRecipeKeys = new TreeMap<String, Integer>();
	Map<String, Integer> featureTableRecipeKeys = new TreeMap<String, Integer>();
	Map<String, Integer> trainedModelRecipeKeys = new TreeMap<String, Integer>();
	
	ArrayList<SimpleDocumentList> documents = new ArrayList<SimpleDocumentList>();
	ArrayList<FeatureTable> tables = new ArrayList<FeatureTable>();
	ArrayList<SimpleTrainingResult> models = new ArrayList<SimpleTrainingResult>();
	
}
