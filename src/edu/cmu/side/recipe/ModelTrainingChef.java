package edu.cmu.side.recipe;

import java.io.File;

import plugins.metrics.models.BasicModelEvaluations;

import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.model.data.TrainingResult;

public class ModelTrainingChef extends Chef
{
	
	public static void main(String[] args) throws Exception
	{
		String recipePath = "saved/self-model.side";
		String outPath  = "saved/self-output.side";
		if (args.length < 2)
		{
			System.err.println("usage: modelchef.sh path/to/my.recipe.side path/to/output.model.side");
		}
		else
		{
			recipePath = args[0];
			outPath= args[1];
		}
			
			
			Recipe recipe = loadRecipe(recipePath);
//			System.out.println(recipe.getLearnerSettings());
			
		    broilModel(recipe);
	
			displayTrainingResults(recipe);

			System.out.println("Saving trained model to "+outPath);
			saveRecipe(recipe, new File(outPath));
		
		
	}

	/**
	 * @param recipe
	 */
	protected static void displayTrainingResults(Recipe recipe)
	{
		if(recipe.getStage().compareTo(Stage.TRAINED_MODEL) >= 0)
		{
			TrainingResult trainingResult = recipe.getTrainingResult();
			System.out.println(trainingResult.getTextConfusionMatrix());
			BasicModelEvaluations eval = new BasicModelEvaluations();
			System.out.println("Accuracy\t"+eval.getAccuracy(trainingResult));
			System.out.println("Kappa\t"+eval.getKappa(trainingResult));
		}
	}
}
