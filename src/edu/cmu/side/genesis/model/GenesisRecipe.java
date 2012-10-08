package edu.cmu.side.genesis.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import javax.swing.JLabel;

import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.features.FeaturePluginPanel;
import edu.cmu.side.genesis.control.GenesisUpdater;

public class GenesisRecipe {
	SimpleDocumentList documents;
	FeatureTable originalTable;
	FeatureTable modifiedTable;
	SimpleTrainingResult trainedModel;
	
	List<FeaturePlugin> extractors;
	List<FilterPlugin> filters;
	LearningPlugin learner;
	String recipeID;
	
	public void writeSerializedTable(ObjectOutputStream out) throws Exception{
		out.writeObject(documents);
		out.writeObject(originalTable);
		out.writeObject(modifiedTable);
		out.writeObject(trainedModel);
		out.writeObject(extractors);
		out.writeObject(filters);
		out.writeObject(learner);
		out.writeObject(recipeID);
	}

	public GenesisRecipe(ObjectInputStream in){
		try{
			documents = (SimpleDocumentList)in.readObject();
			originalTable = (FeatureTable)in.readObject();
			modifiedTable = (FeatureTable)in.readObject();
			trainedModel = (SimpleTrainingResult)in.readObject();
			extractors = (List<FeaturePlugin>)in.readObject();
			filters = (List<FilterPlugin>)in.readObject();
			learner = (LearningPlugin)in.readObject();
			recipeID = (String)in.readObject();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//Only used for provided doc evaluation and prediction panel
	//Here we might require the same name for training data and prediction data *****
	public GenesisRecipe(GenesisRecipe trainrecipe, SimpleDocumentList newData, boolean predict){
		documents = newData;
		if (!predict){
			documents.setTextColumn(trainrecipe.getDocumentList().getTextColumn());
			//documents.setDomain(trainrecipe.getDocumentList().getDomain());
			documents.setCurrentAnnotation(trainrecipe.getDocumentList().getCurrentAnnotation());
		} 
		this.setExtractor(trainrecipe.extractors);
		this.setFilter(trainrecipe.filters);
	
		extract(0);
		if (predict){
			originalTable.setClassValueType(trainrecipe.getOriginalTable().getClassValueType());
			originalTable.setPossibleLabels(trainrecipe.getOriginalTable().getPossibleLabels());
		}
		
		originalTable = FeatureTable.reconcileFeatures(originalTable, trainrecipe.getOriginalTable());
		modify(trainrecipe.originalTable);
		
	}
	
	public GenesisRecipe(SimpleDocumentList data){
		documents = data;
	}
	
	public static GenesisRecipe createAppendedRecipe(GenesisRecipe r, List<? extends SIDEPlugin> plugins){
		GenesisRecipe recipe = new GenesisRecipe(r.getDocumentList());
		for(SIDEPlugin plugin : plugins){
			if(plugin instanceof FeaturePlugin){
				recipe.addExtractor((FeaturePlugin)plugin);
			}else if(plugin instanceof FilterPlugin){
				recipe.addFilter((FilterPlugin)plugin);
			}else if(plugin instanceof LearningPlugin){
				recipe.setLearner((LearningPlugin)plugin);
			}
		}
		return recipe;
	}
	
	//for copy
	public GenesisRecipe(GenesisRecipe gr) {
		documents = gr.documents;
		originalTable = gr.originalTable;
		modifiedTable = gr.modifiedTable;
		trainedModel = gr.trainedModel;
		
		extractors = gr.extractors;
		filters = gr.filters;
		learner = gr.learner;
		recipeID = gr.recipeID;
	}
	
	
	public SimpleDocumentList getDocumentList(){
		return documents;
	}
	
	public List<FeaturePlugin> getExtractors(){
		return extractors;
	}
	
	public FeatureTable getOriginalTable(){
		return originalTable;
	}

	public FeatureTable getModifiedTable(){
		return modifiedTable;
	}

	public List<FilterPlugin> getFilters(){
		return filters;
	}
	
	public LearningPlugin getLearningPlugin(){
		return learner;
	}
	
	public SimpleTrainingResult getTrainedModel(){
		return trainedModel;
	}
	
	public void setTrainedModel(SimpleTrainingResult result){
		trainedModel = result;
	}
	
	public void setExtractor(List<FeaturePlugin> p){
		extractors = p;
	}
	
	public void addExtractor(FeaturePlugin p){
		extractors.add(p);
	}
	
	public void addFilter(FilterPlugin p){
		filters.add(p);
	}
	
	public void setFilter(List<FilterPlugin> flts){
		filters = flts;
	}
	
	public void setLearner(LearningPlugin p){
		learner = p;
	}
	
	
	
	public void extract(int thres) {
		Collection<FeatureHit> hits = null;
		for(FeaturePlugin extractor : extractors) {
			if(extractor == null) continue;
			if (hits == null)
				hits = extractor.extractFeatureHits(documents, FeaturePluginPanel.getProgressLabel());
			else
				hits.addAll(extractor.extractFeatureHits(documents, FeaturePluginPanel.getProgressLabel()));
		}
		System.out.println("feature extraction is done " + hits.size());
		if (documents.getAnnotationArray() != null)
			originalTable = new FeatureTable(hits, documents.getAnnotationArray().toArray(new String[0]), thres, documents.getSize());
		else 
			originalTable = new FeatureTable(hits, null, thres, documents.getSize());
			
	}
	
	// I don't know why there is an updater....
	// This is for cross validation, and table is going to be filtered.
	// As some filtering algorithm might want to
	// use the label information, it can only leverage the instances that mask[i] is true.
	//??????????????????????????????????????????????????????????????????????????????????????????????
	// Might have to filter the train and table together so that in each step we can train the 
	// filter from comparable feature space
	public void modify(boolean[] mask/*GenesisUpdater update*/){
		FeatureTable result = originalTable;
		if (filters != null)
			for (FilterPlugin filter: filters)
				result = filter.filter(mask, result);
		modifiedTable = result;
		
		String[] tmp= modifiedTable.getPossibleLabels();
		System.out.println("here");
		for (String x : tmp)
			System.out.println(x);
		System.out.println("done");
	}
	
	// This is for test on different file or prediction. table is going to be filtered. 
	// Train is the feature table that the model trained on, we can only leverage information
	// from train to do the filtering.
	//??????????????????????????????????????????????????????????????????????????????????????????????
	// Might have to filter the train and table together so that in each step we can train the 
	// filter from comparable feature space
	public void modify(FeatureTable train){
		FeatureTable result = originalTable;
		for (FilterPlugin filter: filters)
			result = filter.filter(train, result);
		modifiedTable = result;
	}
	
/* train model is better to be called from UI part because we need
 * desiredName, evaluationSettings, foldsMap, and progressIndicator 
 
	public void train(GenesisUpdater update){
		learner = l;
		trainedModel = train(this, , )
	}
*/	
}
