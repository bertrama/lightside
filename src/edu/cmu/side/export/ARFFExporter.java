package edu.cmu.side.export;

import java.io.File;
import java.io.FileWriter;
import weka.core.Instances;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.feature.Feature.Type;

public class ARFFExporter {


	/**
	 * Outputs a feature table to a file that can be read by some other software package,
	 * based on some format selected elsewhere in the GUI (as of 10/3/11, only does ARFF format).
	 */
	public static void export(FeatureTable ft, File out){
		try{
			if (!out.getName().endsWith(".arff"))
				out = new File(out.getAbsolutePath() + ".arff");
			ft.resetCurrentAnnotation();
			Instances data = ft.getInstances();
			FileWriter outf = new FileWriter(out);			
			outf.write("@relation " + ft.getTableName().replaceAll("[\\s\\p{Punct}]","_") + "\n\n");
			for (int i=0; i<data.numAttributes(); i++)
				outf.write(data.attribute(i).toString() + "\n");
			outf.write("\n@data\n");
			for (int i=0; i<data.numInstances(); i++)
				outf.write(data.instance(i).toString() + "\n");
			outf.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
