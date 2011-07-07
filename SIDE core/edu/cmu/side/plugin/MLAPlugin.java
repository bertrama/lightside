
package edu.cmu.side.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import com.yerihyo.yeritools.CalendarToolkit;

import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTableKey;
import edu.cmu.side.ml.PredictionToolkit.PredictionResult;
import edu.cmu.side.uima.UIMAToolkit.Datatype;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;

public abstract class MLAPlugin extends SIDEPlugin {

	public MLAPlugin() {
		super();
	}
	public static Map<String, FEPlugin> feplugins = new TreeMap<String, FEPlugin>();
	public static final String type = "classifier";
	public String getType() { return type; }

	// public abstract String Invoke (SegmentList featureSet, Element options)
	// throws Exception;
	// Given a list of FeatureSegment objects (which include both a label and
	// a set of feature classes), run the MLA on it and return
	// the text of the Evaluation which it produces.
	// new version as per Yi-Chia's implementation
	protected abstract SortedMap<String,String> trainModel (FeatureTable featureTable, String fold) throws Exception;

	public PredictionResult getSelfPredictionResult(TrainingResult trainingResult){
		PredictionResult predictionResult = getSelfPredictionResultForSubclass(trainingResult.getTrainSettingDescription());
		if(predictionResult==null){ return null; }

		predictionResult.setFeatureTable(trainingResult.getFeatureTable());
		return predictionResult;
	}
	protected abstract PredictionResult getSelfPredictionResultForSubclass(Map<? extends String,? extends String> map);


	//	public TrainingResult train(FeatureTable featureTable){ return train(featureTable, null, null); }
	//	public TrainingResult train(FeatureTable featureTable, String desiredName){ return train(featureTable, desiredName, null); }
	public TrainingResultInterface train(FeatureTable featureTable, String desiredName, SegmenterPlugin segmenterPlugin, String fold){
		long start = System.currentTimeMillis();

		this.uiToMemory();
		SortedMap<String, String> evaluationResult = null;
		try {
			evaluationResult = this.trainModel(featureTable, fold);
			System.out.println("plugin training done in "+CalendarToolkit.durationToString(System.currentTimeMillis()-start));
			TrainingResult trainingResult = new TrainingResult(featureTable, evaluationResult, this);
			trainingResult.setName(desiredName);
			trainingResult.setSegmenterPlugin(segmenterPlugin);
			Integer foldInt;
			try{
				foldInt = Integer.parseInt(fold);
			}catch(Exception e){ foldInt = -1; }
			trainingResult.setFold(foldInt);
			//			String[] annotationArray1 = trainingResult.getDocumentList().getAnnotationArray();
			//			String[] annotationArray2 = trainingResult.getTrainSettingDescription().get("self-actual").split("\n");
			//			YeriDebug.ASSERT(CollectionsToolkit.equals(annotationArray1, annotationArray2));

			Workbench.current.trainingResultListManager.add(trainingResult);

			System.out.println("SIDE training done in "+CalendarToolkit.durationToString(System.currentTimeMillis()-start));
			return trainingResult;
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(null, t.getMessage(), "ERROR! training halt due to exception", JOptionPane.ERROR_MESSAGE);
			t.printStackTrace();
			System.out.println("SIDE training done in "+CalendarToolkit.durationToString(System.currentTimeMillis()-start));
			return null;
		}
	}

	public PredictionResult predict(TrainingResult trainingResult, DocumentList documentList) throws Exception
	{	
		double time1 = 0.0; double time2 = 0.0; double time3 = 0.0; double time4 = 0.0; double time5 = 0.0;
		long start = System.currentTimeMillis();
		time1 = System.currentTimeMillis();
		String[] labelArray = trainingResult.getLabelArray();
		//		System.out.println(StringToolkit.toString(labelArray, ","+StringToolkit.newLine(), new String[]{"'","'"}));
		List<FEPlugin> fePluginList = new ArrayList<FEPlugin>();
		List<FeatureTableKey> trainFeatureTableKeyList = trainingResult.getFeatureTableKeyList();
		time2 = System.currentTimeMillis();
		double sumMethod1 = 0.0; double sumMethod2 = 0.0; double sumMethod3 = 0.0;
		int cHits = 0; int ncHits = 0;
		for(FeatureTableKey featureTableKey : trainFeatureTableKeyList){
			double intime1 = System.currentTimeMillis();
			String classname = featureTableKey.getFeatureExtractorClassName();
			double intime2 = System.currentTimeMillis();
			if(!feplugins.containsKey(classname)){				
				PluginWrapper pluginWrapper = Workbench.current.pluginManager.getPluginWrapperByPluginClassName(classname);
				if(pluginWrapper != null){
					SIDEPlugin sidePlugin = pluginWrapper.getSIDEPlugin();
					if(!(sidePlugin instanceof FEPlugin)){ continue; }
					FEPlugin fePlugin = (FEPlugin)sidePlugin;
					feplugins.put(classname, fePlugin);
					fePluginList.add(fePlugin);
				}
				ncHits++;
				double intime3 = System.currentTimeMillis();
				sumMethod2 += (intime3) - intime2;
			}else{
				cHits++;
				fePluginList.add(feplugins.get(classname));
				double intime3 = System.currentTimeMillis();
				sumMethod3 += (intime3) - intime2;
			}
			sumMethod1 += (intime2-intime1);
		}		
		time3 = System.currentTimeMillis();
		FeatureTable featureTable = FeatureTable.createAndBuild(documentList, fePluginList.toArray(new FEPlugin[0]), null, trainingResult.getFeatureTableKeyList());
		System.out.println("document list size:"+featureTable.getDocumentList().getSize());

		time4 = System.currentTimeMillis();
		PredictionResult predictionResult = predictForSubclass(trainingResult.getTrainSettingDescription(), featureTable, labelArray);
		predictionResult.setFeatureTable(featureTable);

		time5 = System.currentTimeMillis();
		System.out.println("prediction done in "+CalendarToolkit.durationToString(System.currentTimeMillis()-start));
		return predictionResult;
	}

	protected abstract PredictionResult predictForSubclass(SortedMap<String,String> trainSettingDescription, FeatureTable featureTable, String[] labelArray) throws Exception;

	protected Datatype datatype;
	public void setDatatype(Datatype datatype) { this.datatype = datatype; }
	public Datatype getDatatype(){ return this.datatype; }

}
