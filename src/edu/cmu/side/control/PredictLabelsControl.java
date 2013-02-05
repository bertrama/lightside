package edu.cmu.side.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.recipe.Predictor;
import edu.cmu.side.view.generic.ActionBarTask;
import edu.cmu.side.view.predict.PredictActionBar;
import edu.cmu.side.view.util.SwingUpdaterLabel;

public class PredictLabelsControl extends GenesisControl{

	private static Recipe trainedModel;
	private static Recipe highlightedUnlabeledData;
	
	private static Collection<Recipe> unlabeledDataRecipes = new ArrayList<Recipe>();
	
	private static StatusUpdater update = new SwingUpdaterLabel();

	public static StatusUpdater getUpdater()
	{
		return update;
	}

	public static boolean hasHighlightedTrainedModelRecipe(){
		return trainedModel!=null;
	}

	public static Recipe getHighlightedTrainedModelRecipe(){
		return trainedModel;
	}

	public static void setHighlightedTrainedModelRecipe(Recipe highlight){
		trainedModel = highlight;
	}
	
	public static boolean hasHighlightedUnlabeledData(){
		return highlightedUnlabeledData != null;
	}
	
	public static Recipe getHighlightedUnlabeledData(){
		return highlightedUnlabeledData;
	}
	
	public static void setHighlightedUnlabeledData(Recipe r){
		highlightedUnlabeledData = r;
	}
	
	public static Collection<Recipe> getUnlabeledDataRecipes(){
		return unlabeledDataRecipes;
	}

	public static void executePredictTask(final PredictActionBar predictActionBar, final String name, final boolean showMaxScore, final boolean showDists)
	{
		new ActionBarTask(predictActionBar){

			@Override
			public void requestCancel()
			{
				//TODO: halt nicely.
			}

			@Override
			protected void doTask()
			{
				Predictor predictor = new Predictor(trainedModel, name);
				DocumentList docs = highlightedUnlabeledData.getDocumentList();

				
				
				PredictionResult results = predictor.predict(docs);
				
				List<String> predictions = (List<String>) results.getPredictions();
				
				
				docs.addAnnotation(name, predictions);
				

				Map<String, List<Double>> distributions = results.getDistributions();
				if(distributions != null)
				{
					List<String> likely = new ArrayList<String>();
					if(showMaxScore)
					{
						for(int i = 0; i < predictions.size(); i++)
						{
							likely.add(String.format("%.3f", distributions.get(predictions.get(i)).get(i)));
						}
						docs.addAnnotation(name+"_score", likely);
					}
					
					if(showDists)
					{
						DocumentList documents = trainedModel.getDocumentList();
						for(String label : documents.getLabelArray())
						{
							List<String> dist = new ArrayList<String>();

							for(int i = 0; i < predictions.size(); i++)
							{
								dist.add(String.format("%.3f", distributions.get(label).get(i)));
							}
							
							docs.addAnnotation(name+"_"+label+"_score", dist);
						}
					}
				}
						
			}
		}.execute();
	}
	
}
