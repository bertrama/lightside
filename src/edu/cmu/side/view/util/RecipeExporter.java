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
import edu.cmu.side.recipe.converters.ConverterControl;

public class RecipeExporter
{

	static JFileChooser tableChooser;
	static JFileChooser modelChooser;
	static JFileChooser predictChooser;
	
	static class EndsWithFileFilter extends FileFilter
	{
		String[] extensions;
		String description;
		
		
		public EndsWithFileFilter(String description, String... ext)
		{
			super();
			this.extensions = ext;
			this.description = description;
		}
		
		public String[] getExtensions()
		{
			return extensions;
		}

		@Override
		public boolean accept(File file)
		{
			String fileName = file.getName();
			for(String ext : extensions)
			{
				if(fileName.endsWith(ext))
					return true;
			}
			return false;
		}

		@Override
		public String getDescription()
		{
			return description;
		}
		
	}

	//TODO: multi-extension names aren't detected correctly by FileNameExtensionFilter
	public final static FileFilter csvFilter = new EndsWithFileFilter("CSV (Excel)", "csv", "CSV");
	public final static FileFilter arffFilter = new EndsWithFileFilter("ARFF (Weka)", "arff", "ARFF");
	public final static FileFilter xmlTableFilter = new EndsWithFileFilter("LightSide Feature Table XML", "table.side.xml");
	public final static FileFilter xmlModelFilter = new EndsWithFileFilter("LightSide Trained Model XML", "model.side.xml");
	public final static FileFilter xmlPredictFilter = new EndsWithFileFilter("Predict-Only", "predict.xml", "xml");
	public final static FileFilter xmlGenericFilter = new EndsWithFileFilter("LightSide XML", "xml");
	public final static FileFilter serializedTableFilter = new EndsWithFileFilter("LightSide Feature Table", "table.side");
	public final static FileFilter serializedModelFilter = new EndsWithFileFilter("LightSide Trained Model", "model.side");
	public final static FileFilter serializedGenericFilter = new EndsWithFileFilter("LightSide", "side");
	public final static FileFilter serializedPredictFilter = new EndsWithFileFilter("Predict-Only", "predict");

	protected static boolean useXML = false;
	
	public static JFileChooser setUpChooser(JFileChooser chooser, FileFilter... filters)
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

						List<String> extensions = Arrays.asList(((EndsWithFileFilter) filter).getExtensions());
//						System.out.println(extensions);

						boolean changed = false;
						for (String ext : ((EndsWithFileFilter) this.getFileFilter()).getExtensions())
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

			
			for (FileFilter filter : filters)
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
		if(useXML)
			tableChooser = setUpChooser(tableChooser, xmlTableFilter, csvFilter, arffFilter);
		else
			tableChooser = setUpChooser(tableChooser, serializedTableFilter, csvFilter, arffFilter);
		
		FeatureTable table = tableRecipe.getTrainingTable();
		try
		{
			tableChooser.setSelectedFile(new File(table.getName() + "." + ((EndsWithFileFilter) tableChooser.getFileFilter()).getExtensions()[0]));

			int state = tableChooser.showDialog(null, "Save Feature Table");
			if (state == JFileChooser.APPROVE_OPTION)
			{
				File f = tableChooser.getSelectedFile();
				if (f.exists())
				{
					int confirm = JOptionPane.showConfirmDialog(null, f.getName() + " already exists. Do you want to overwrite it?");
					if (confirm != JOptionPane.YES_OPTION) return;
				}

				if (tableChooser.getFileFilter() == csvFilter) exportToCSV(table, f);
				else if (tableChooser.getFileFilter() == arffFilter) exportToARFF(table, f);
				else if (tableChooser.getFileFilter() == xmlTableFilter) exportToXML(tableRecipe, f);
				else if (tableChooser.getFileFilter() == serializedTableFilter) exportToSerialized(tableRecipe, f);
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

	public static void exportToXML(Recipe recipe, File target) throws IOException
	{

		ConverterControl.writeToXML(target, recipe);
	}
	
	@Deprecated
	public static void exportToSerialized(Recipe recipe, File target) throws IOException
	{
		//TODO: write out here
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

	public static void exportTrainedModel(Recipe modelRecipe)
	{
		JFileChooser chooser;
		if(modelRecipe.getStage() == Stage.TRAINED_MODEL)
		{
			if(useXML)
				chooser = modelChooser = setUpChooser(modelChooser, xmlModelFilter, xmlPredictFilter);
			else
				chooser = modelChooser = setUpChooser(modelChooser, serializedModelFilter, serializedPredictFilter);
				
		}
		else
		{
			if(useXML)
				chooser = predictChooser = setUpChooser(predictChooser, xmlPredictFilter);
			else
				chooser = modelChooser = setUpChooser(predictChooser, serializedPredictFilter);
		}
			
		TrainingResult result = modelRecipe.getTrainingResult();
		try
		{
			chooser.setSelectedFile(new File((result == null ? modelRecipe.getRecipeName() : result.getName()) + "." + ((EndsWithFileFilter) modelChooser.getFileFilter()).getExtensions()[0]));

			int state = chooser.showDialog(null, "Save Trained Model");
			if (state == JFileChooser.APPROVE_OPTION)
			{
				File f = chooser.getSelectedFile();
				if (f.exists())
				{
					int confirm = JOptionPane.showConfirmDialog(null, f.getName() + " already exists. Do you want to overwrite it?");
					if (confirm != JOptionPane.YES_OPTION) return;
				}

				if (chooser.getFileFilter() == xmlPredictFilter) exportToXMLForPrediction(modelRecipe, f);
				else if (chooser.getFileFilter() == xmlModelFilter) exportToXML(modelRecipe, f);
				else if (chooser.getFileFilter() == serializedModelFilter) exportToSerialized(modelRecipe, f);
				else if (chooser.getFileFilter() == serializedPredictFilter) exportToSerializedForPrediction(modelRecipe, f);
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

	@Deprecated
	public static void exportToSerializedForPrediction(Recipe recipe, File target) throws IOException
	{
		Recipe dupe = Recipe.copyPredictionRecipe(recipe);
		FileOutputStream fout = new FileOutputStream(target);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(dupe);
	}
	
	public static void exportToXMLForPrediction(Recipe recipe, File target) throws IOException
	{
		//TODO: Setup Rewrite Here
		Recipe dupe = Recipe.copyPredictionRecipe(recipe);
		ConverterControl.writeToXML(target, dupe);
	}

	public static FileFilter getTrainedModelFilter()
	{
		if(useXML())
		{
			return xmlModelFilter;
		}
		else
		{
			return serializedModelFilter;
		}
	}
	
	public static FileFilter getPredictModelFilter()
	{

		if(useXML())
		{
			return xmlPredictFilter;
		}
		else
		{
			return serializedPredictFilter;
		}
	}
	
	public static FileFilter getFeatureTableFilter()
	{

		if(useXML())
		{
			return xmlTableFilter;
		}
		else
		{
			return serializedTableFilter;
		}
	}
	
	public static FileFilter getGenericFilter()
	{
		if(useXML())
		{
			return xmlGenericFilter;
		}
		else
		{
			return serializedGenericFilter;
		}
	}
	
	public static FileFilter getARFFFilter()
	{
		return arffFilter;
	}
	
	public static FileFilter getCSVFilter()
	{
		return csvFilter;
	}
	
	public static boolean useXML()
	{
		return useXML;
	}

	public static void setUseXML(boolean useXML)
	{
		RecipeExporter.useXML = useXML;
	}
}
