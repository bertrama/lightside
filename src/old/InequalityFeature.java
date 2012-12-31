package old;

import java.util.Collection;
import java.util.TreeSet;

import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.FeatureHit;

public class InequalityFeature extends LabFeature{
	
	Feature base;
	String ineq;
	Double comparison;
	
	public InequalityFeature(Feature b, String i, Double c){
		base = b;
		ineq = i;
		comparison = c;
		this.featureName = base.getFeatureName()+ineq+comparison;
		this.extractorPrefix = "lab";
		this.featureType = Type.BOOLEAN;
	}

	@Override
	public void exportLabFeature(FeatureTable newTable){
		newTable.addAllHits(buildHits(newTable));
	}
	
	@Override
	public Collection<FeatureHit> buildHits(FeatureTable newTable){
		Feature combine = Feature.fetchFeature(extractorPrefix, base.getFeatureName()+ineq+comparison, featureType);
		TreeSet<FeatureHit> hits = new TreeSet<FeatureHit>();
		Comp comp;
		if(">=".equals(ineq)){
			comp = new Comp(){ boolean compare(Double val){ return val >= comparison; }};
		}else if(">".equals(ineq)){
			comp = new Comp(){ boolean compare(Double val){ return val > comparison; }};	
		}else if("=".equals(ineq)){
			comp = new Comp(){ boolean compare(Double val){ return val == comparison; }};		
		}else if("<".equals(ineq)){
			comp = new Comp(){ boolean compare(Double val){ return val < comparison; }};					
		}else if("<=".equals(ineq)){
			comp = new Comp(){ boolean compare(Double val){ return val <= comparison; }};								
		}else{
			comp = new Comp();
		}
		for(Feature f : newTable.getFeatureSet()){
			System.out.print(f.getFeatureName()+",");
			if(f.equals(base) && f.getFeatureType()==Type.NUMERIC){
				System.out.println(f.getFeatureName() + "IF50");
				for(FeatureHit fh : newTable.getHitsForFeature(f)){
					Double val = (Double)fh.getValue();
					System.out.println("   " + val + ", " + fh.getDocumentIndex() + " IF53");
					if(comp.compare(val)){
						hits.add(new FeatureHit(combine, Boolean.TRUE, fh.getDocumentIndex()));
					}
				}
			}
		}
		return hits;
	}
	
	private class Comp{
		boolean compare(Double val){
			return false;
		}
	}
}
