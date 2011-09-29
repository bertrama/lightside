package edu.cmu.side.util;

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
	private static JFileChooser chooser = new JFileChooser();
	public static void exportToCSV(TableModel model, File file){
//        if(!file2.endsWith(".csv"))
//        	file2 += ".csv";
//        chooser.setSelectedFile(new File( filename ));
//        chooser.setFileFilter(new FileFilter()
//        {
//
//			@Override
//			public boolean accept(File file)
//			{
//				return file.getPath().endsWith(".csv");
//			}
//
//			@Override
//			public String getDescription()
//			{
//				// TODO Auto-generated method stub
//				return "CSV Files";
//			}
//        	
//        });
//        int state = chooser.showDialog(null, "Export to CSV");
//        File file = new File(file2);
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
