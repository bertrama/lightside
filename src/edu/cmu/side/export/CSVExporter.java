package edu.cmu.side.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

public class CSVExporter
{
	public static void exportToCSV(TableModel model, File file){

        //if (file != null)
        {
            try
            {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                PrintWriter fileWriter = new PrintWriter(bufferedWriter);

                int cols = model.getColumnCount();
				for(int j=0; j<cols; ++j)
                {
                	String s = model.getColumnName(j);
                    fileWriter.print(s);
                    if(j < cols-1)
                    	fileWriter.print(",");
                }
                fileWriter.println("");
                
                for(int i=0; i<model.getRowCount() ; ++i)
                {
                        for(int j=0; j<cols; ++j)
                        {
                                String s;
                                Object obj = model.getValueAt(i, j);
                                if (obj != null) 
                                {
                                	
                                    s = obj.toString();
                                    if(s.contains(","))
                                    {
                                    	s = "\""+s.replaceAll("\"", "\\\"")+"\"";
                                    }
                                }
                                else 
                                {
                                	s = "";
                                }
                                fileWriter.print(s);
                                if(j < cols-1)
                                	fileWriter.print(",");
                        }
                        fileWriter.println("");
                }
                fileWriter.close();
            }
            
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }
}
