package edu.cmu.side.model.feature;

import java.util.Map;


public class RegroupFeatureHit extends FeatureHit{

	protected Integer originalIndex;
	public RegroupFeatureHit(FeatureHit original, Map<Integer, Integer> indexMap) {
		super(original.getFeature(), original.getValue(), indexMap.get(original.getDocumentIndex()));
		originalIndex = original.getDocumentIndex();
	}
	
	public RegroupFeatureHit(FeatureHit original, Map<Integer, Integer> indexMap, int index){
		super(original.getFeature(), original.getValue(), indexMap.get(original.getDocumentIndex()));
		originalIndex = index;
	}
	
	public int getOriginalIndex(){
		return originalIndex;
	}
	
	@Override
	public int compareTo(FeatureHit o){
		if(o instanceof RegroupFeatureHit && super.compareTo(o)==0){
			return originalIndex.compareTo(((RegroupFeatureHit)o).originalIndex);
		}else{
			return super.compareTo(o);
		}
	}
	
	public String toString()
	{
		return this.feature+"@"+this.documentIndex+"/"+this.getOriginalIndex();
	}
}
