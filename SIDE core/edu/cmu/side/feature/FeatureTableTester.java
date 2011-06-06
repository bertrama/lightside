package edu.cmu.side.feature;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import edu.cmu.side.plugin.FEPlugin;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;

public class FeatureTableTester extends FEPlugin
{

	/**
	 * Shows a basic feature-extraction/table-making pipeline.
	 * @param args
	 */
	public static void main(String[] args)
	{
		ArrayList<FEPlugin> extractors = new ArrayList<FEPlugin>();
		extractors.add(new FeatureTableTester());
		DocumentList documents = new DocumentList("test"){public int getSize(){return 5;}};
		FeatureTable foo = new FeatureTable(extractors, documents);
		
		foo.extractAll();
		
		Set<Feature> feats = foo.getFeatureSet();
		for(Feature f : feats)
		{
			System.out.println(f+":\t"+foo.getHitsForFeature(f));
		}
		
		for(int i = 0; i < documents.getSize(); i++)
		{
			System.out.println(i+":\t"+foo.getHitsForDocument(i));
		}

	}
	@Override
	public String getOutputName()
	{
		// TODO Auto-generated method stub
		return "test";
	}

	@Override
	public List<Map<String, Number>> extractFeatureMapsForSubclass(
			DocumentList documents) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
	

	public Collection<FeatureHit> extractFeatureHits(DocumentList documents)
	{
		ArrayList<FeatureHit> hits = new ArrayList<FeatureHit>();
		List<String> nominals = Arrays.asList("foo", "bar", "baz");
		
		Feature feature = new Feature(getOutputName(), "test", Feature.Type.BOOLEAN);
		Feature featureToo = new Feature(getOutputName(), "nominaltest", nominals);
		
		
		for(int i = 0; i < documents.getSize(); i++)
		{
			hits.add(new FeatureHit(feature, i%2 == 0, i));
			
			if(i%3 == 0)
				hits.add(new FeatureHit(featureToo, "foo", i));
		}

		System.err.println("this should fail... (nominal error)");
		try
		{
			hits.add(new FeatureHit(featureToo, "bingo", 0));
		}catch(Exception e){System.err.println("caught exception: "+e.getMessage());}
		

		System.err.println("this should fail... (type error)");
		try
		{
			hits.add(new FeatureHit(feature, "foo", 0));
		}catch(Exception e){System.err.println("caught exception: "+e.getMessage());}
		
		return hits;
	}

	@Override
	protected Component getConfigurationUIForSubclass()
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void fromXML(Element arg0) throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toXML()
	{
		// TODO Auto-generated method stub
		return null;
	}


}
