package edu.cmu.side.view.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

public class CSVExporter
{

	// Older (newer?) version of this method scavenged from David
	static JFileChooser chooser = new JFileChooser(new File("data"));
	
	{
		chooser.setFileFilter(new FileFilter()
		{

			@Override
			public boolean accept(File file)
			{
				return file.getPath().endsWith(".csv");
			}

			@Override
			public String getDescription()
			{
				// TODO Auto-generated method stub
				return "CSV Files";
			}

		});
	}
	
	public static void exportToCSV(TableModel model)
	{
		chooser.setSelectedFile(new File("export.csv"));
		
		int state = chooser.showDialog(null, "Export to CSV");
		if(state == chooser.APPROVE_OPTION)
		{
			File f = chooser.getSelectedFile();
			if(f.exists())
			{
				int confirm = JOptionPane.showConfirmDialog(null, f.getName()+" already exists. Overwrite?");
				if(confirm != JOptionPane.YES_OPTION)
					return;
			}
			
			try
			{
				exportToCSV(model, f);
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
		
	}
	
	public static void exportToCSV(TableModel model, File file) throws IOException
	{
		if (file != null)
		{
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
				PrintWriter fileWriter = new PrintWriter(bufferedWriter);

				int cols = model.getColumnCount();
				for (int j = 0; j < cols; ++j)
				{
					String s = model.getColumnName(j);
					fileWriter.print(s);
					if (j < cols - 1) fileWriter.print(",");
				}
				fileWriter.println("");

				for (int i = 0; i < model.getRowCount(); ++i)
				{
					for (int j = 0; j < cols; ++j)
					{
						String s;
						Object obj = model.getValueAt(i, j);
						if (obj != null)
						{

							s = obj.toString();
							if (s.contains(","))
							{
								s = "\"" + s.replaceAll("\"", "\\\"") + "\"";
							}
						}
						else
						{
							s = "";
						}
						fileWriter.print(s);
						if (j < cols - 1) fileWriter.print(",");
					}
					fileWriter.println("");
				}
				fileWriter.close();
			
		}
	}
	
	// public static void exportToCSV(FeatureTable ft, File file){
	// try {
	// if (!file.getName().endsWith(".csv"))
	// file = new File(file.getAbsolutePath() + ".csv");
	// //Instances data = ft.getInstances();
	// FileWriter outf = new FileWriter(file);
	// outf.write("#Instance");
	// for (Feature f : ft.getFeatureSet())
	// outf.write("," + f.getFeatureName().replaceAll(",","_"));
	// outf.write("\n");
	// for (int i=0; i< ft.getDocumentList().getSize(); i++){
	// outf.write((""+(i+1)));
	// Collection<FeatureHit> hits = ft.getHitsForDocument(i);
	// for (Feature f : ft.getFeatureSet())
	// {
	// if()
	// if (f.getFeatureType() == Type.NUMERIC)
	// outf.write("," + data.instance(i).value(j));
	// else
	// outf.write("," + data.instance(i).stringValue(j));
	// }
	// outf.write("\n");
	// }
	// outf.close();
	// } catch(Exception x) {
	// JOptionPane.showMessageDialog(null, x.getMessage());
	// }
	// }

//	public static void exportToCSV(TableModel model, File file)
//	{
//		try
//		{
//			if (!file.getName().endsWith(".csv")) file = new File(file.getAbsolutePath() + ".csv");
//
//			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
//			PrintWriter fileWriter = new PrintWriter(bufferedWriter);
//
//			int cols = model.getColumnCount();
//			for (int j = 0; j < cols; ++j)
//			{
//				String s = model.getColumnName(j).replaceAll(",", "_");
//				fileWriter.print(s);
//				if (j < cols - 1) fileWriter.print(",");
//			}
//			fileWriter.println("");
//
//			for (int i = 0; i < model.getRowCount(); ++i)
//			{
//				for (int j = 0; j < cols; ++j)
//				{
//					String s;
//					Object obj = model.getValueAt(i, j);
//					if (obj != null)
//						s = obj.toString().replaceAll(",", "_");
//					else
//						s = "";
//					fileWriter.print(s);
//					if (j < cols - 1) fileWriter.print(",");
//				}
//				fileWriter.println("");
//			}
//			fileWriter.close();
//		}
//		catch (Exception x)
//		{
//			JOptionPane.showMessageDialog(null, x.getMessage());
//		}
//	}

}