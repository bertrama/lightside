package edu.cmu.side.view.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import plugins.learning.WekaTools;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.Feature.Type;
import edu.cmu.side.model.feature.FeatureHit;

public class RecipeExporter
{

	static JFileChooser tableChooser;
	static JFileChooser modelChooser;

	static FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV (Excel)", "csv", "CSV");
	static FileNameExtensionFilter arffFilter = new FileNameExtensionFilter("ARFF (Weka)", "arff", "ARFF");
	static FileNameExtensionFilter sideTableFilter = new FileNameExtensionFilter("LightSide Feature Table", "table.side", "side");
	static FileNameExtensionFilter sideModelFilter = new FileNameExtensionFilter("LightSide Trained Model", "model.side", "side");
	static FileNameExtensionFilter predictFilter = new FileNameExtensionFilter("Predict-Only", "predict", "model.predict");

	
	public static JFileChooser setUpChooser(JFileChooser chooser, FileNameExtensionFilter... filters)
	{
		if (chooser == null)
		{
			chooser = new JFileChooser(new File("saved"))
			{
				String lastName = null;

				@Override
				public void setFileFilter(FileFilter filter)
				{
					File f = this.getSelectedFile();
					if (f == null && lastName != null)
					{
						f = new File(lastName);
					}
					if (f != null)
					{
						String name = f.getName();
						String extension = "";

						List<String> extensions = Arrays.asList(((FileNameExtensionFilter) filter).getExtensions());
//						System.out.println(extensions);

						boolean changed = false;
						for (String ext : ((FileNameExtensionFilter) this.getFileFilter()).getExtensions())
						{
							if (name.endsWith("." + ext))
							{
								name = name.replace("." + ext, "." + extensions.get(0));
								this.setSelectedFile(new File(name));
								changed = true;
								break;
							}
						}

						// if (!extensions.contains(extension))
						// this.setSelectedFile(new File(name + "." +
						// extensions.get(0)));
						if (!changed) this.setSelectedFile(new File(name + "." + extensions.get(0)));
					}
					super.setFileFilter(filter);
				}

				@Override
				public void setSelectedFile(File f)
				{
					if (f != null) lastName = f.getName();
					super.setSelectedFile(f);
				}
			};
			
			for (FileNameExtensionFilter filter : filters)
			{
				if (filter == arffFilter)
				{
					try
					{
						Class.forName("plugins.learning.WekaTools");
						chooser.addChoosableFileFilter(filter);
					}
					catch (ClassNotFoundException cnf)
					{
						System.err.println("WekaTools not found - disabling ARFF exporter");
					}
				}

				else
					chooser.addChoosableFileFilter(filter);
			}
			
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(filters[0]);

		}
		return chooser;
	}

	public static void exportFeatures(Recipe tableRecipe)
	{
		tableChooser = setUpChooser(tableChooser, sideTableFilter, csvFilter, arffFilter);
		FeatureTable table = tableRecipe.getTrainingTable();
		try
		{
			tableChooser.setSelectedFile(new File(table.getName() + "." + ((FileNameExtensionFilter) tableChooser.getFileFilter()).getExtensions()[0]));

			int state = tableChooser.showDialog(null, "Save Feature Table");
			if (state == JFileChooser.APPROVE_OPTION)
			{
				File f = tableChooser.getSelectedFile();
				if (f.exists())
				{
					int confirm = JOptionPane.showConfirmDialog(null, f.getName() + " already exists. Do you want to overwrite it?");
					if (confirm != JOptionPane.YES_OPTION) return;
				}

				if (tableChooser.getFileFilter() == csvFilter)
					exportToCSV(table, f);
				else if (tableChooser.getFileFilter() == arffFilter)
					exportToARFF(table, f);
				else if (tableChooser.getFileFilter() == sideTableFilter) exportToSerialized(tableRecipe, f);
			}
		}
		catch (Exception e)
		{
			String message = e.getMessage();
			if (table == null)
				message = "Feature Table is null.";
			else if (message == null || message.isEmpty()) message = "Couldn't save feature table.";
			JOptionPane.showMessageDialog(null, message);
			e.printStackTrace();
		}

	}

	public static void exportToSerialized(Recipe recipe, File target) throws IOException
	{
		FileOutputStream fout = new FileOutputStream(target);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(recipe);

	}

	public static void exportToARFF(FeatureTable ft, File out) throws IOException
	{

		boolean[] mask = new boolean[ft.getSize()];
		for (int i = 0; i < mask.length; i++)
		{
			mask[i] = true;
		}

		Instances instances = WekaTools.getInstances(ft, mask);
		ArffSaver arffSaver = new ArffSaver();
		arffSaver.setInstances(instances);
		arffSaver.setFile(out);
		arffSaver.writeBatch();
	}

	public static void exportToCSV(FeatureTable ft, File file) throws IOException
	{

		if (!file.getName().endsWith(".csv")) file = new File(file.getAbsolutePath() + ".csv");
		// Instances data = ft.getInstances();
		FileWriter outf = new FileWriter(file);
		outf.write("Instance");
		DocumentList localDocuments = ft.getDocumentList();
		outf.write("," + ft.getAnnotation());

		for (Feature f : ft.getFeatureSet())
			outf.write("," + f.getFeatureName().replaceAll(",", "_"));
		outf.write("\n");

		List<String> annotations = ft.getAnnotations();

		for (int i = 0; i < localDocuments.getSize(); i++)
		{
			outf.write(("" + (i + 1)));

			outf.write("," + annotations.get(i));

			Collection<FeatureHit> hits = ft.getHitsForDocument(i);
			for (Feature f : ft.getFeatureSet())
			{
				boolean didHit = false;
				for (FeatureHit h : hits)
				{
					if (h.getFeature().equals(f))
					{
						outf.write("," + h.getValue());
						didHit = true;
						break;
					}

				}

				if (!didHit)
				{
					if (f.getFeatureType() == Type.NUMERIC)
					{
						outf.write("," + 0);
					}
					else if (f.getFeatureType() == Type.BOOLEAN)
					{
						outf.write("," + Boolean.FALSE);
					}
					else
						outf.write(",");
				}

			}
			outf.write("\n");
		}
		outf.close();

	}

	public static void exportTrainedModel(Recipe tableRecipe)
	{
		if(tableRecipe.getStage() == Stage.TRAINED_MODEL)
			modelChooser = setUpChooser(modelChooser, sideModelFilter, predictFilter);
		else
			modelChooser = setUpChooser(modelChooser, predictFilter);
			
		TrainingResult result = tableRecipe.getTrainingResult();
		try
		{
			modelChooser.setSelectedFile(new File((result == null ? tableRecipe.getRecipeName() : result.getName()) + "." + ((FileNameExtensionFilter) modelChooser.getFileFilter()).getExtensions()[0]));

			int state = modelChooser.showDialog(null, "Save Feature Table");
			if (state == JFileChooser.APPROVE_OPTION)
			{
				File f = modelChooser.getSelectedFile();
				if (f.exists())
				{
					int confirm = JOptionPane.showConfirmDialog(null, f.getName() + " already exists. Do you want to overwrite it?");
					if (confirm != JOptionPane.YES_OPTION) return;
				}

				if (modelChooser.getFileFilter() == predictFilter)
					exportForPrediction(tableRecipe, f);
				else if (modelChooser.getFileFilter() == sideModelFilter) exportToSerialized(tableRecipe, f);
			}
		}
		catch (Exception e)
		{
			String message = e.getMessage();
			if (result == null)
				message = "Training Result is null.";
			else if (message == null || message.isEmpty()) message = "Couldn't save feature table.";
			JOptionPane.showMessageDialog(null, message);
			e.printStackTrace();
		}

	}

	public static void exportForPrediction(Recipe recipe, File target) throws IOException
	{
		Recipe dupe = Recipe.copyPredictionRecipe(recipe);
		FileOutputStream fout = new FileOutputStream(target);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(dupe);
	}
}
