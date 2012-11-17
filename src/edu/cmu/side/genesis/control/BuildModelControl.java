package edu.cmu.side.genesis.control;

import java.util.*;

import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;

public class BuildModelControl extends GenesisControl{

	public void train(GenesisRecipe recipe, GenesisUpdater update){

	}
	
    public Map<Integer, Integer> getFoldsMapByNum(SimpleDocumentList documents, int num){
        Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
        for(int i = 0; i < documents.getSize(); i++){
                foldsMap.put(i, i%num);
        }
        return foldsMap;
    }

    public Map<Integer, Integer> getFoldsMapByFile(SimpleDocumentList documents){
        Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();
        int foldNum = 0;
        Map<String, Integer> folds = new TreeMap<String, Integer>();
        for(int i = 0; i < documents.getSize(); i++){
                String filename = documents.getFilename(i);
                if(!folds.containsKey(filename)){
                        folds.put(filename, foldNum++);
                }
                foldsMap.put(i, folds.get(filename));
        }
        return foldsMap;
    }
    
    
	public List<GenesisRecipe> getFilterRecipes(){
		return modelRecipes;
	}
	
}
