package edu.cmu.side.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.feature.FeatureHit;

public abstract class ParallelFeaturePlugin extends FeaturePlugin
{
	
	/**
	 * 
	 * @param documents in a corpus
	 * @return All features that this plugin should extract from each document in this corpus.
	 */
	@Override
	public Collection<FeatureHit> extractFeatureHits(DocumentList documents, Map<String, String> configuration, StatusUpdater update)
	{
		halt = false;
		this.configureFromSettings(configuration);
		return extractFeatureHitsForSubclass(documents, update);
	}

	@Override
	public Collection<FeatureHit> extractFeatureHitsForSubclass(final DocumentList documents, final StatusUpdater update)
	{
		final int maxThreads = Runtime.getRuntime().availableProcessors();
		final ArrayList<FeatureHit> allHits = new ArrayList<FeatureHit>();
		final ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
		
		long start = System.currentTimeMillis();
		
		Collection<Callable<Collection<FeatureHit>>> tasks = new ArrayList<Callable<Collection<FeatureHit>>>();
		
		final int size = documents.getSize();
		final int chunk = size/maxThreads;
		for(int t = 0; t < maxThreads; t++)
		{ 
			final int threadIndex = t;
			final int offset = t*chunk;
			Callable<Collection<FeatureHit>> extraction = new Callable<Collection<FeatureHit>>()
			{
				final ArrayList<FeatureHit> hits = new ArrayList<FeatureHit>();
				
				@Override
				public Collection<FeatureHit> call()
				{
					String pluginName = ParallelFeaturePlugin.this.toString();
					for(int index = offset; index < size && index < offset + chunk; index++)
					{
						if(halt)
						{
							pool.shutdownNow();
							return hits;
						}
						
						//if(index % maxThreads == threadIndex)
						{
							if((index+1)%50 == 0 || index == size)
							{
								System.out.println("Thread "+threadIndex+": Extracting doc "+(index)+"/"+(offset+chunk)+" for "+pluginName);
								synchronized(update)
								{update.update("Extracting " + pluginName, index+1, size);}
							}
							
							//System.out.println("Extracting doc "+index+" for "+ParallelFeaturePlugin.this.toString());
							
							Collection<FeatureHit> docHits = extractFeatureHitsFromDocument(documents, index);
							//System.out.println("Thread "+threadIndex+": Adding hits for doc "+index+".");
							hits.addAll(docHits);
						}
					}
					System.out.println("Thread "+threadIndex + " complete.");
					return hits;
				}
			};
			tasks.add(extraction);
		}

		System.out.println("invoking "+tasks.size()+" tasks...");
		try
		{
			List<Future<Collection<FeatureHit>>> results = pool.invokeAll(tasks);
			System.out.println("tasks complete?");
			for(Future<Collection<FeatureHit>> result: results)
			{
				allHits.addAll(result.get());
			}
			
			pool.shutdown();
			while(!pool.isShutdown())
			{
				Thread.sleep(500);
			}
		}
		catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (ExecutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Parallel extraction complete in "+(System.currentTimeMillis()-start)/1000+" seconds.");
		
		update.update(this+" Extraction complete.");
		
		return allHits;
	}
	
	public abstract Collection<FeatureHit> extractFeatureHitsFromDocument(DocumentList documents, int i);
	
}
