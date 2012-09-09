package edu.cmu.side.export;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;

import edu.cmu.side.simple.feature.FeatureTable;
import weka.core.Instances;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

public class CSVExporter
{
	public static void exportToCSV(FeatureTable ft, File file){
		try {
			Instances data = ft.getInstances();
            FileWriter outf = new FileWriter(file);
            outf.write("#Instance");
            for (int i=0; i<data.numAttributes(); i++)
            	outf.write("," + data.attribute(i).name().replaceAll(",","_"));
            outf.write("\n");
            for (int i=0; i<data.numInstances(); i++){
            	outf.write((""+(i+1)));
            	for (int j=0; j<data.numAttributes(); j++)
            		outf.write("," + data.instance(i).stringValue(j));
            	outf.write("\n");
            }
            outf.close();
		} catch(Exception x) {
			JOptionPane.showMessageDialog(null, x.getMessage());
		}
	}
	
	public static void exportToCSV(TableModel model, File file){
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            PrintWriter fileWriter = new PrintWriter(bufferedWriter);

            int cols = model.getColumnCount();
			for(int j=0; j<cols; ++j) {
				String s = model.getColumnName(j).replaceAll(",","_");
				fileWriter.print(s);
				if(j < cols-1) fileWriter.print(",");
            }
			fileWriter.println("");
                
			for(int i=0; i<model.getRowCount() ; ++i) {
				for(int j=0; j<cols; ++j) {
					String s;
					Object obj = model.getValueAt(i, j);
					if (obj != null)
						s = obj.toString().replaceAll(",","_");
					else 
						s = "";
					fileWriter.print(s);
					if(j < cols-1) fileWriter.print(",");
				}
                fileWriter.println("");
			}
            fileWriter.close();
		} catch (Exception x) {
                JOptionPane.showMessageDialog(null, x.getMessage());
        }
	}
}