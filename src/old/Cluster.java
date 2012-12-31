package old;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.FeatureHit;
import edu.cmu.side.model.feature.LocalFeatureHit;

public abstract class Cluster
{
	private static  double SIMILARITY_EQUALITY_WINDOW = 0.075;//.07 to .01 for local_similarity
	private static  double SAME_DOCUMENT_BONUS = 0.0001;
	private static  boolean USE_LOCAL_SIMILARITY = false;
	private double internalSimilarity;

	public abstract double getSimilarity(Cluster other);
	public abstract Collection<? extends FeatureHit> getAllHits();
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

		List<FeatureHit> allHits;
		Feature feature;
		
		public LeafCluster(List<FeatureHit> hits, Feature feat)
		{
			super(1.0);
			allHits = hits;
			feature = feat;
		}
		
		@Override
		public double getSimilarity(Cluster other)
		{
			return similarity(getAllHits(), (List<? extends FeatureHit>) other.getAllHits());
		}
		
		public List<Feature> getFeatures()
		{
			ArrayList<Feature> list = new ArrayList<Feature>();
			list.add(feature);
			return list;
		}
		
		public List<FeatureHit> getAllHits()
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
		private List<FeatureHit> allHits = new ArrayList<FeatureHit>();
		
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
			return similarity(getAllHits(), (List<? extends FeatureHit>) other.getAllHits());
		}

		@Override
		public List<FeatureHit> getAllHits()
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
	

	private static Map<Feature, List<FeatureHit>> groupByFeature(List<FeatureHit> hits, int threshold)
	{
		Map<Feature, List<FeatureHit>> localMap = new HashMap<Feature, List<FeatureHit>>(10000);

		
		for(FeatureHit hit : hits)
		{
			Feature feature = hit.getFeature();
			if(! localMap.containsKey(feature))
			{
				localMap.put(feature, new ArrayList<FeatureHit>());
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
			for(int i = 0; i < gg.size()/2; i++)
			{	
				FeatureHit g = gg.get(i);
				if(!f.equals(g) && (f.getDocumentIndex() == g.getDocumentIndex()))
				{	
					totalSimilarity ++;
				}
			}
		double base = Math.max(ff.size(), gg.size());

		return Math.sqrt(totalSimilarity / (base*base));
		//return totalSimilarity / base;
	}
	
	private static double localSimilarity(List<SingleLocalFeatureHit> ff, List<SingleLocalFeatureHit> gg)
	{
		double totalSimilarity = 0.0;
		
		for(SingleLocalFeatureHit f : ff)
			for(int i = 0; i < gg.size()/2; i++)
			{	
				SingleLocalFeatureHit g = gg.get(i);
				if(!f.equals(g) && f.getDocumentIndex() == g.getDocumentIndex())
				{	
					double similarity = SAME_DOCUMENT_BONUS; //smoothing for same-document coexistence
					int fStart = f.getStart();
					int fEnd = f.getEnd();

					int gStart = g.getStart();
					int gEnd = g.getEnd();
					
					int overlap = Math.min(fEnd, gEnd) - Math.max(fStart, gStart);
					if(overlap > 0)
						similarity = overlap/(double)Math.max(fEnd-fStart, gEnd-gStart);
					totalSimilarity += similarity;
				}
			}
		double base = Math.max(ff.size(), gg.size());
		//return totalSimilarity / base;
		return Math.sqrt(2 * totalSimilarity / (base*base));
	}
	
	private static double multiLocalSimilarity(List<LocalFeatureHit> ff, List<LocalFeatureHit> gg)
	{
		double totalSimilarity = 0.0;
		
		for(LocalFeatureHit f : ff)
			for(int i = 0; i < gg.size()/2; i++)
			{	
				LocalFeatureHit g = gg.get(i);
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
		return Math.sqrt(2 * totalSimilarity / (base*base));
	}
	
	private static double similarity(List<? extends FeatureHit> ff, List<? extends FeatureHit> gg)
	{
		if(USE_LOCAL_SIMILARITY)
			return localSimilarity((List<SingleLocalFeatureHit>)ff, (List<SingleLocalFeatureHit>)gg);
		else return documentSimilarity(ff, gg);
	}
	
	public static Collection<Cluster> makeClusters(List<FeatureHit> allHits, int featureThreshold, double similarityThreshold, double similarityWindow, int numChunks, boolean localClusters)
	{
		HashSet<Cluster> clusters = new HashSet<Cluster>();

		Map<Feature, List<FeatureHit>> localMap = groupByFeature((List<FeatureHit>) allHits, featureThreshold);
		
		//double[][] similarityMatrix = makeMatrix( localMap);
		//initialize with singletons
		for(Feature key : localMap.keySet())
		{
			List<FeatureHit> hits = localMap.get(key);
			clusters.add(new LeafCluster(hits, key));
		}

		localMap.clear();
		allHits.clear(); //this may get us in trouble later
		
		Cluster.SIMILARITY_EQUALITY_WINDOW = similarityWindow;
		Cluster.USE_LOCAL_SIMILARITY = localClusters;
		
		clusterAllChunks(clusters, similarityThreshold, numChunks);
		
		return clusters;
	}
	private static void clusterAllChunks(Collection<Cluster> clusters, final double similarityThreshold, int chunks)
	{
		Iterator<Cluster> clusterIt = clusters.iterator();
		final int chunkSize = clusters.size() / chunks;
		
		final ArrayList<Cluster> result = new ArrayList<Cluster>();
		Thread[] threads = new Thread[chunks];
		
		for(int i = 0; i < chunks; i++)
		{
			final ArrayList<Cluster> clusterChunk = new ArrayList<Cluster>();
			for(int j = 0; j < chunkSize && clusterIt.hasNext(); j++)
			{
				clusterChunk.add(clusterIt.next());
				clusterIt.remove();
			}
			
			final String chunkS = "Chunk #"+i;
			threads[i] = new Thread()
			{
				public void run()
				{
					System.out.println("*** Starting Cluster "+chunkS+" ***");
					clusterAll(clusterChunk, similarityThreshold, chunkSize/100);
					result.addAll(clusterChunk);
					System.out.println("*** Finished Cluster "+chunkS+" ***");
					clusterChunk.clear();
				}
			};
			threads[i].start();
			try
			{
				Thread.sleep(1500);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < chunks; i++)
		{
			try
			{
				threads[i].join();
				System.out.println("chunk #"+i+" is clustered...");
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("*** Now is the Time on Sprockets When We Dance ("+result.size()+" Clusters) ***");
		clusterAll(result, similarityThreshold, result.size()/100);
		
		clusters.clear();
		clusters.addAll(result);
		System.gc();
	}
	
	private static void clusterAll(Collection<Cluster> clusters, double similiarityThreshold, int minClusters)
	{

		Map<TwoKey, Double> similarities = new HashMap<TwoKey, Double>();
		double sim = 1.0;
		minClusters = Math.max(1, minClusters);
		
		while(clusters.size() > minClusters && sim > similiarityThreshold)
		{
			Runtime.getRuntime().gc();
			Thread.yield();
			
			System.out.println("Clustering... "+clusters.size()+" clusters - "+sim);
			sim = clusterize(clusters, similarities);
			
		}

		count = 0;
	}
	
	static int count = 0;
	private static double clusterize(Collection<Cluster> clusters, Map<TwoKey, Double> similarities)
	{
		HashSet<Cluster> best = new HashSet<Cluster>();
		ArrayList<Cluster> aBest = new ArrayList<Cluster>();
		double maxSimilarity = 0.0;
		double aSim = 0.0;
		
		count++;
		
		int i = 0;

//		long start = System.currentTimeMillis();
		
//		Map<TwoKey, Double> chunk = new HashMap<TwoKey, Double>();
//		Map<TwoKey, Double> bestMap = new HashMap<TwoKey, Double>();
		
		for(Cluster a : clusters)
		{
			aSim = maxSimilarity;
			aBest.clear();
			if(i > 0 && i%200 == 0)
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
						//if(count > 1)
						//	System.err.println(count+":\tcalculating new sim??\n"+a+"\nvs\n"+b);
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
					else if (sim - aSim > -SIMILARITY_EQUALITY_WINDOW && !aBest.isEmpty())
					{
						aBest.add(b);
//						bestMap.put(key, sim);
//						bestMap.putAll(chunk);
					}
				}
			}

			if(aSim > maxSimilarity)
			{
				best.clear();
				best.addAll(aBest);
				maxSimilarity = aSim;
			}
		}

//		similarities.putAll(chunk);
//		chunk.clear();
		
		if(!best.isEmpty())
		{
			System.out.println("best cluster size "+best.size());
			System.out.println("best similarity "+maxSimilarity);
			clusters.removeAll(best);
			ClusterCluster bestCluster = new ClusterCluster(best, maxSimilarity);
			
			int j = 0;
			for(Cluster c : clusters)
			{
				if(j > 0 && j % 200 == 0)
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
