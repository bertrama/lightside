package edu.cmu.side.genesis.model;

import java.util.*;

import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.FilterPlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;

public class GenesisRecipe {

	SimpleDocumentList documents;
	FeatureTable originalTable;
	FeatureTable modifiedTable;
	SimpleTrainingResult trainedModel;
	
	List<FeaturePlugin> extractors;
	List<FilterPlugin> filters;
	LearningPlugin learner;
	String recipeID;
	
	public SimpleDocumentList getDocumentList(){
		throw new UnsupportedOperationException();
	}
	
	public List<FeaturePlugin> getExtractors(){
		throw new UnsupportedOperationException();
	}
	
	public FeatureTable getOriginalTable(){
		throw new UnsupportedOperationException();
	}

	public List<FilterPlugin> getFilters(){
		throw new UnsupportedOperationException();
	}
	
	public FeatureTable getModifiedTable(){
		throw new UnsupportedOperationException();
	}
	
	public LearningPlugin getLearningPlugin(){
		throw new UnsupportedOperationException();
	}
	
	public SimpleTrainingResult getTrainedModel(){
		throw new UnsupportedOperationException();
	}
	
	public void addExtractor(FeaturePlugin p){
		extractors.add(p);
	}
	
	public void addFilter(FilterPlugin p){
		filters.add(p);
	}
	
	public void addLearner(LearningPlugin p){
		learner = p;
	}
	
	public GenesisRecipe(SimpleDocumentList documents){}
	
	public static GenesisRecipe createAppendedRecipe(GenesisRecipe r, List<? extends SIDEPlugin> plugins){
		GenesisRecipe recipe = new GenesisRecipe(r.getDocumentList());
		for(SIDEPlugin plugin : plugins){
			if(plugin instanceof FeaturePlugin){
				recipe.addExtractor((FeaturePlugin)plugin);
			}else if(plugin instanceof FilterPlugin){
				recipe.addFilter((FilterPlugin)plugin);
			}else if(plugin instanceof LearningPlugin){
				recipe.addLearner((LearningPlugin)plugin);
			}
		}
		return recipe;
	}
	
	public void extract(GenesisUpdater update){
		if(originalTable == null){
			originalTable = new FeatureTable(extractors, documents, 1);			
		}
	}
	
	public void modify(GenesisUpdater update){
		if(modifiedTable == null){
			Collection<FeatureHit> modifiedFeatures = new TreeSet<FeatureHit>();
			for(FilterPlugin filter : filters){
				modifiedFeatures.addAll(filter.filter(originalTable));
			}
			modifiedTable = new FeatureTable(modifiedFeatures, originalTable.getDocumentList());			
		}
	}
	
	public void train(GenesisUpdater update){
		if(trainedModel == null){
			trainedModel = new SimpleTrainingResult(learner, recipeID, modifiedTable);
		}
	}
	
}
