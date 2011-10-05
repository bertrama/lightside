package edu.cmu.side.feature.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.LocalFeatureHit;

public abstract class Cluster
{
	private static final double SIMILARITY_EQUALITY_WINDOW = 0.01;
	private static final double SAME_DOCUMENT_BONUS = 0.0;
	private static final boolean USE_LOCAL_SIMILARITY = false;
	private double internalSimilarity;

	public abstract double getSimilarity(Cluster other);
	public abstract List<LocalFeatureHit> getAllHits();
	public abstract String toString(String indent);
	public abstract List<Feature> getFeatures();
	public abstract int getHitsSize();
	
	public Cluster(double internalSim)
	{
		internalSimilarity = internalSim;
	}
	
	public double getInternalSimilarity()
	{
		return internalSimilarity;
	}
	
	public static class LeafCluster extends Cluster
	{

		List<LocalFeatureHit> allHits;
		Feature feature;
		
		public LeafCluster(List<LocalFeatureHit> hits, Feature feat)
		{
			super(1.0);
			allHits = hits;
			feature = feat;
		}
		
		@Override
		public double getSimilarity(Cluster other)
		{
			return similarity(getAllHits(), other.getAllHits());
		}
		
		public List<Feature> getFeatures()
		{
			ArrayList<Feature> list = new ArrayList<Feature>();
			list.add(feature);
			return list;
		}
		
		public List<LocalFeatureHit> getAllHits()
		{
			return allHits;
		}
		
		public String toString(String indent)
		{
			return indent+toString();
		}
		
		public String toString()
		{
			return feature.getFeatureName();
		}

		@Override
		public int getHitsSize()
		{
			return allHits.size();
		}
		
		@Override
		public int hashCode()
		{
			return 1+feature.hashCode();
		}

	}
	
	public static class ClusterCluster extends Cluster
	{

		private List<Cluster> clusters = new ArrayList<Cluster>();
		private int size;
		private List<LocalFeatureHit> allHits = new ArrayList<LocalFeatureHit>();
		
		public ClusterCluster(Collection<Cluster> best, double sim)
		{
			super(sim);
			this.clusters.addAll(best);
			for(Cluster c : best)
			{
				size += c.getHitsSize();
				allHits.addAll(c.getAllHits());
			}
		}
		
		@Override
		public double getSimilarity(Cluster other)
		{
			return similarity(getAllHits(), other.getAllHits());
		}

		@Override
		public List<LocalFeatureHit> getAllHits()
		{
			return allHits;
		}
		
		public String toString(String indent)
		{
			List<Feature> features = getFeatures();
			if(features.size() < 10)
				return indent+"("+getInternalSimilarity()+")" + "{"+features + "}";
			else
			{
				String representation = indent+"("+getInternalSimilarity()+")"+":\n ";
				for(int i = 0; i < clusters.size(); i++)
				{	
					Cluster c = clusters.get(i);
					representation += c.toString(indent+i+" ")+"\n";
				}
				return representation;
			}
		}
		
		public String toString()
		{
			return "Cluster:\n"+toString("\t");
		}

		@Override
		public List<Feature> getFeatures()
		{
			ArrayList<Feature> features = new ArrayList<Feature>();
			
			for(Cluster c : clusters)
				features.addAll(c.getFeatures());
			
			return features;
		}

		@Override
		public int getHitsSize()
		{
			return size;
		}
	}
	

	private static Map<Feature, List<LocalFeatureHit>> groupByFeature(List<LocalFeatureHit> hits, int threshold)
	{
		Map<Feature, List<LocalFeatureHit>> localMap = new HashMap<Feature, List<LocalFeatureHit>>(10000);

		
		for(LocalFeatureHit hit : hits)
		{
			Feature feature = hit.getFeature();
			if(! localMap.containsKey(feature))
			{
				localMap.put(feature, new ArrayList<LocalFeatureHit>());
			}
			localMap.get(feature).add(hit);
		}
		Feature[] features = localMap.keySet().toArray(new Feature[0]);
		for(Feature f : features)
		{
			if(localMap.get(f).size() < threshold)
			{
				localMap.remove(f);
			}
		}
		
		return localMap;
	}
	
	private static double documentSimilarity(List<? extends FeatureHit> ff, List<? extends FeatureHit> gg)
	{
		double totalSimilarity = 0.0;
		
		for(FeatureHit f : ff)
			for(FeatureHit g : gg)
			{	
				if(!f.equals(g) && (f.getDocumentIndex() == g.getDocumentIndex()))
				{	
					totalSimilarity ++;
				}
			}
		double base = Math.max(ff.size(), gg.size());

		return Math.sqrt(totalSimilarity / (base*base));
		//return totalSimilarity / base;
	}
	
	private static double localSimilarity(List<LocalFeatureHit> ff, List<LocalFeatureHit> gg)
	{
		double totalSimilarity = 0.0;
		
		for(LocalFeatureHit f : ff)
			for(LocalFeatureHit g : gg)
			{	
				if(!f.equals(g) && f.getDocumentIndex() == g.getDocumentIndex())
				{	
					double similarity = SAME_DOCUMENT_BONUS; //smoothing for same-document coexistence
					int[] frange = (int[]) ((List<int[]>)f.getHits()).get(0);
					int[] grange = (int[]) ((List<int[]>)g.getHits()).get(0);
					
					int overlap = Math.min(frange[1], grange[1]) - Math.max(frange[0], grange[0]);
					if(overlap > 0)
						similarity = overlap/(double)Math.max(frange[1]-frange[0], grange[1]-grange[0]);
					totalSimilarity += similarity;
				}
			}
		double base = Math.max(ff.size(), gg.size());
		//return totalSimilarity / base;
		return Math.sqrt(totalSimilarity / (base*base));
	}
	
	private static double similarity(List<? extends FeatureHit> ff, List<? extends FeatureHit> gg)
	{
		if(USE_LOCAL_SIMILARITY)
			return localSimilarity((List<LocalFeatureHit>)ff, (List<LocalFeatureHit>)gg);
		else return documentSimilarity(ff, gg);
	}
	
	public static Collection<Cluster> makeClusters(List<LocalFeatureHit> allHits, int featureThreshold, double similiarityThreshold)
	{
		HashSet<Cluster> clusters = new HashSet<Cluster>();

		Map<Feature, List<LocalFeatureHit>> localMap = groupByFeature(allHits, featureThreshold);
		
		//double[][] similarityMatrix = makeMatrix( localMap);
		//initialize with singletons
		for(Feature key : localMap.keySet())
		{
			List<LocalFeatureHit> hits = localMap.get(key);
			clusters.add(new LeafCluster(hits, key));
		}

		localMap.clear();
		
		
		Map<TwoKey, Double> similarities = new HashMap<TwoKey, Double>(); //DYNAMIC PROGRAMMING GOES HERE
		//Map<TwoKey, Double> similarities = new BackedMap<TwoKey, Double>("similarities"); //DYNAMIC PROGRAMMING GOES HERE
		
		double sim = 1.0;
		while(clusters.size() > 0 && sim > similiarityThreshold)
		{
			Runtime.getRuntime().gc();
			Thread.yield();
			
			System.err.println("Clustering... "+clusters.size()+" clusters - "+sim);
			sim = clusterize(clusters, similarities);
			
		}
		
		return clusters;
	}
	
	private static double clusterize(Collection<Cluster> clusters, Map<TwoKey, Double> similarities)
	{
		HashSet<Cluster> best = new HashSet<Cluster>();
		ArrayList<Cluster> aBest = new ArrayList<Cluster>();
		double maxSimilarity = 0.0;
		double aSim = 0.0;
		
		
		int i = 0;

//		long start = System.currentTimeMillis();
		
//		Map<TwoKey, Double> chunk = new HashMap<TwoKey, Double>();
//		Map<TwoKey, Double> bestMap = new HashMap<TwoKey, Double>();
		
		for(Cluster a : clusters)
		{
			aSim = maxSimilarity;
			aBest.clear();
			if(i%100 == 0)
			{
//				long mstart = System.currentTimeMillis();
//
//				System.out.println((mstart - start) +" ms since last tick");
				System.out.println("considering cluster "+(i));
				
//				System.out.println("managing map...");
//				
//				similarities.putAll(chunk);
//				chunk.clear();
//				
//				System.out.println((System.currentTimeMillis()-mstart)+" ms to manage map");
//				
//				long totalHeap = Runtime.getRuntime().totalMemory();
//				//Runtime.getRuntime().gc();
//				System.out.println("heap size: "+(totalHeap/(1024*1024))+" MB");
//				start = System.currentTimeMillis();
			}
			i++;
			
			//if(!similarities.containsKey(a))
			//	similarities.put(a, new HashMap<Cluster, Double>());
			
			//Map<Cluster, Double> aSims = similarities.get(a);
			
			for(Cluster b : clusters)
			{
				if(!a.equals(b))
				{
					
					TwoKey key = new TwoKey(a, b);
					if(! similarities.containsKey(key) )
					{
						double abSim = a.getSimilarity(b);
						similarities.put(key, abSim);
					}
					
					double sim = similarities.get(key);
//					if(sim == null)
//						sim = similarities.get(key);
					
					if(sim - aSim > SIMILARITY_EQUALITY_WINDOW || (sim > aSim && aBest.isEmpty()))
					{
						aBest.clear();
						aBest.add(a);
						aBest.add(b);
						//bestMap.clear();
						//bestMap.putAll(chunk);
						aSim = sim;
					}
					else if (sim - aSim > -SIMILARITY_EQUALITY_WINDOW && maxSimilarity > 0)
					{
						aBest.add(a);
						aBest.add(b);
//						bestMap.put(key, sim);
//						bestMap.putAll(chunk);
					}
				}
				if(aSim > maxSimilarity)
				{
					best.clear();
					best.addAll(aBest);
					maxSimilarity = aSim;
				}
			}
		}

//		similarities.putAll(chunk);
//		chunk.clear();
		
		if(!best.isEmpty())
		{
			clusters.removeAll(best);
			ClusterCluster bestCluster = new ClusterCluster(best, maxSimilarity);
			
			int j = 0;
			for(Cluster c : clusters)
			{
				if(j % 500 == 0)
					System.out.println("merging cluster "+j);
				j++;
				
				double cSum = 0;
				for(Cluster b: best)
				{
					TwoKey bc = new TwoKey(b, c);
					if(similarities.containsKey(bc))
					{

						double sim = similarities.get(bc);
						cSum += sim*b.getHitsSize();
						similarities.remove(bc);
					}
					else
					{

						System.err.println("******No Key! "+bc+"******");
						double sim = b.getSimilarity(c);
						cSum += sim*b.getHitsSize();
						
					}

				}
				
				TwoKey bestKey = new TwoKey(bestCluster, c);
				similarities.put(bestKey, cSum/bestCluster.getHitsSize());
				
			}
//			similarities.putAll(chunk);
//			chunk.clear();
			
			clusters.add(bestCluster);
		}	
		return maxSimilarity;
		
			
	}
	
	private static class TwoKey
	{
		Object a;
		Object b;
		String stringAB;
		String stringBA;
		
		public TwoKey(Object a, Object b)
		{
			this.a = a;
			this.b = b;
			
			stringAB = "("+a+", "+b+")";
			stringBA = "("+b+", "+a+")";
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof TwoKey)
			{
				TwoKey c = (TwoKey)o;
				return (c.a.equals(a) && c.b.equals(b)) || (c.a.equals(b) && c.b.equals(a));
			}
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return a.hashCode() + b.hashCode();
		}
		
		public String toString()
		{
			return stringAB;
		}
	}
	
}
