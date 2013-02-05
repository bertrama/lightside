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
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.model.feature.Feature.Type;
import edu.cmu.side.model.feature.FeatureHit;

public class FeatureTableExporter
{

	static JFileChooser chooser;

	static FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV (Excel)", "csv", "CSV");
	static FileNameExtensionFilter arffFilter = new FileNameExtensionFilter("ARFF (Weka)", "arff", "ARFF");
	static FileNameExtensionFilter sideFilter = new FileNameExtensionFilter("LightSIDE", "side", "table.side");

	public static void setUpChooser()
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
						if (name.contains("."))
						{
							extension = name.substring(name.lastIndexOf(".") + 1);
							name = name.substring(0, name.lastIndexOf("."));
						}
						List<String> extensions = Arrays.asList(((FileNameExtensionFilter) filter).getExtensions());

						if (!extensions.contains(extension)) this.setSelectedFile(new File(name + "." + extensions.get(0)));
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
			chooser.addChoosableFileFilter(sideFilter);
			chooser.addChoosableFileFilter(csvFilter);
			
			try
			{
				Class.forName("plugins.learning.WekaTools");
				chooser.addChoosableFileFilter(arffFilter);
			}
			catch(ClassNotFoundException cnf)
			{
				System.err.println("WekaTools not found - disabling ARFF exporter");
			}
			
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(sideFilter);
		}
	}

	public static void exportFeatures(Recipe tableRecipe)
	{
		setUpChooser();
		FeatureTable table = tableRecipe.getTrainingTable();
		try
		{
			chooser.setSelectedFile(new File(table.getName() + "." + ((FileNameExtensionFilter) chooser.getFileFilter()).getExtensions()[0]));

			int state = chooser.showDialog(null, "Save Feature Table");
			if (state == JFileChooser.APPROVE_OPTION)
			{
				File f = chooser.getSelectedFile();
				if (f.exists())
				{
					int confirm = JOptionPane.showConfirmDialog(null, f.getName() + " already exists. Do you want to overwrite it?");
					if (confirm != JOptionPane.YES_OPTION) return;
				}

				if (chooser.getFileFilter() == csvFilter)
					exportToCSV(table, f);
				else if (chooser.getFileFilter() == arffFilter)
					exportToARFF(table, f);
				else if (chooser.getFileFilter() == sideFilter) exportToSerialized(tableRecipe, f);
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
		FileWriter outf = new FileWriter(out);
		
		boolean[] mask = new boolean[ft.getSize()];
		for(int i = 0; i < mask.length; i++)
		{
			mask[i]=true;
		}
		
		Instances instances = WekaTools.getInstances(ft, mask);
		outf.write(instances.toString());
		
//		for (int i = 0; i < ft.getDocumentList().getSize(); i++)
//		{
//			Collection<FeatureHit> hits = ft.getHitsForDocument(i);
//			for (FeatureHit hit : hits)
//				outf.write(hit.getFeature().toString() + ": " + hit.getValue().toString() + "\t");
//			outf.write("\n");
//		}
		outf.close();
		System.out.println("moof!");
	}

	public static void exportToCSV(FeatureTable ft, File file) throws IOException
	{

		if (!file.getName().endsWith(".csv")) file = new File(file.getAbsolutePath() + ".csv");
		// Instances data = ft.getInstances();
		FileWriter outf = new FileWriter(file);
		outf.write("Instance");
		DocumentList localDocuments = ft.getDocumentList();
		outf.write("," + localDocuments.getCurrentAnnotation());

		for (Feature f : ft.getFeatureSet())
			outf.write("," + f.getFeatureName().replaceAll(",", "_"));
		outf.write("\n");

		List<String> annotations =	 localDocuments.getAnnotationArray();

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
}
