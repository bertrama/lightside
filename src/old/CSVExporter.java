package old;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import weka.core.Instances;
import edu.cmu.side.model.data.FeatureTable;

public class CSVExporter
{
	public static void exportToCSV(FeatureTable ft, File file){
		try {
			if (!file.getName().endsWith(".csv"))
				file = new File(file.getAbsolutePath() + ".csv");
			Instances data = ft.getInstances();
            FileWriter outf = new FileWriter(file);
            outf.write("#Instance");
            for (int i=0; i<data.numAttributes(); i++)
            	outf.write("," + data.attribute(i).name().replaceAll(",","_"));
            outf.write("\n");
            for (int i=0; i<data.numInstances(); i++){
            	outf.write((""+(i+1)));
            	for (int j=0; j<data.numAttributes(); j++)
            		if (data.instance(i).attribute(j).isNumeric())
            			outf.write("," + data.instance(i).value(j));
            		else
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
			if (!file.getName().endsWith(".csv"))
				file = new File(file.getAbsolutePath() + ".csv");
			
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
	
	//Older (newer?) version of this method I scavenged from elsewhere
	
//	public static void exportToCSV(TableModel model, File file){
////        if(!file2.endsWith(".csv"))
////        	file2 += ".csv";
////        chooser.setSelectedFile(new File( filename ));
////        chooser.setFileFilter(new FileFilter()
////        {
////
////			@Override
////			public boolean accept(File file)
////			{
////				return file.getPath().endsWith(".csv");
////			}
////
////			@Override
////			public String getDescription()
////			{
////				// TODO Auto-generated method stub
////				return "CSV Files";
////			}
////        	
////        });
////        int state = chooser.showDialog(null, "Export to CSV");
////        File file = new File(file2);
//        //if (file != null)
//        {
//            try
//            {
//                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
//                PrintWriter fileWriter = new PrintWriter(bufferedWriter);
//
//                int cols = model.getColumnCount();
//				for(int j=0; j<cols; ++j)
//                {
//                	String s = model.getColumnName(j);
//                    fileWriter.print(s);
//                    if(j < cols-1)
//                    	fileWriter.print(",");
//                }
//                fileWriter.println("");
//                
//                for(int i=0; i<model.getRowCount() ; ++i)
//                {
//                        for(int j=0; j<cols; ++j)
//                        {
//                                String s;
//                                Object obj = model.getValueAt(i, j);
//                                if (obj != null) 
//                                {
//                                	
//                                    s = obj.toString();
//                                    if(s.contains(","))
//                                    {
//                                    	s = "\""+s.replaceAll("\"", "\\\"")+"\"";
//                                    }
//                                }
//                                else 
//                                {
//                                	s = "";
//                                }
//                                fileWriter.print(s);
//                                if(j < cols-1)
//                                	fileWriter.print(",");
//                        }
//                        fileWriter.println("");
//                }
//                fileWriter.close();
//            }
//            
//            catch(Exception e)
//            {
//                JOptionPane.showMessageDialog(null, e.getMessage());
//            }
//        }
//    }
}