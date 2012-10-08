package edu.cmu.side;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.export.*;
import edu.cmu.side.simple.FilterPlugin;

public class Shell {
	public static File rootFolder =	new File(System.getProperty("user.dir"));
	static public String PLATFORM_FILE_SEPARATOR = System.getProperty("file.separator");
	static public String BASE_PATH = rootFolder.getAbsolutePath()+ PLATFORM_FILE_SEPARATOR;
	static public File PLUGIN_FOLDER = new File(BASE_PATH, "plugins");
	public static PluginManager pluginManager = new PluginManager(PLUGIN_FOLDER);

	public static SIDEPlugin[] getPluginsByType(String type){
		return pluginManager.getSIDEPluginArrayByType(type);
	}
	
	public static Set<String> getfilelist(){
		Set<String> files = new HashSet<String>();
		files.add("data/MovieReviews.csv");
		return files;
	}
	
	public static String gettextfield(){
		return "text";
	}
	
	public static Set<String> getpredictfilelist(){
		Set<String> files = new HashSet<String>();
		files.add("data/MovieReviews.csv");
		return files;
	}
	
	public static String getpredicttextfield(){
		return "text";
	}
	
	public static String getpredictname(){
		return "newlabel";
	}
		
	public static String getclassfield(){
		return "class";
	}
	
	public static ArrayList<String> getFeatureExtractorNames(){
		ArrayList<String> fe = new ArrayList<String>();
		fe.add("th");
		return fe;
	}
	
	public static ArrayList<String> getExtractorConfigFiles(){
		ArrayList<String> feConfig = new ArrayList<String>();
		feConfig.add("feConfig.txt");
		return feConfig;
	}
	
	public static int getDfThreshold(){
		return 5;
	}
	
	public static String getLearnerName(){
		return "weka";
	}
	
	public static String getModelName(){
		return "model";
	}
	
	public static String getcvSetting(){
		return "10";
	}
	
	public static ArrayList<String> getFilterNames(){
		ArrayList<String> ans = new ArrayList<String>();
		return ans;
	}
	
	public static ArrayList<String> getFilterConfigFiles(){
		ArrayList<String> ans = new ArrayList<String>();
		return ans;	
	}
	
	
	
	public static void main(String[] args) throws Exception{
		Set<String> files = getfilelist();
		String corpusText = gettextfield();
		String corpusCurrentAnnot = getclassfield();
		SimpleDocumentList dl = new  SimpleDocumentList(files, corpusCurrentAnnot, corpusText);
		GenesisRecipe recipe = new GenesisRecipe(dl);
		
		ArrayList<FeaturePlugin> extractors = new ArrayList<FeaturePlugin>();
		ArrayList<String> pluginNames = getFeatureExtractorNames();
		ArrayList<String> extractorConfigFiles = getExtractorConfigFiles();
		SIDEPlugin[] featureExtractors = getPluginsByType("feature_hit_extractor");
		for(int k = 0; k < featureExtractors.length; k++){
			FeaturePlugin plug = (FeaturePlugin)featureExtractors[k];
			if(pluginNames.contains(plug.getOutputName())) extractors.add(plug);
			System.out.println(plug.getOutputName());
		}
		for(int i = 0; i < extractors.size(); i++) 
			extractors.get(i).configureFromFile(extractorConfigFiles.get(i));
		recipe.setExtractor(extractors);
		int threshold = getDfThreshold();
		recipe.extract(threshold);
		
		ARFFExporter.export(recipe.getOriginalTable(), new File("original.arff"));
		System.out.println("export done");
		
		
		ArrayList<FilterPlugin> filters = new ArrayList<FilterPlugin>();
		ArrayList<String> filternames = getFilterNames();
		ArrayList<String> filterConfigFiles = getFilterConfigFiles();
		SIDEPlugin[] allfilters = getPluginsByType("filter");
		for(int k = 0; k < allfilters.length; k++){
			FilterPlugin plug = (FilterPlugin)allfilters[k];
			if(filternames.contains(plug.getOutputName())) filters.add(plug);
			System.out.println(plug.getOutputName());
		}
		for(int i = 0; i < filters.size(); i++) 
			filters.get(i).configureFromFile(filterConfigFiles.get(i));
		recipe.setFilter(filters);
		
		
		LearningPlugin learner=null;
		String learnerName = getLearnerName();
		SIDEPlugin[] learners = getPluginsByType("model_builder");
		for(int j = 0; j < learners.length; j++){
			LearningPlugin plugin = (LearningPlugin)learners[j];
			if(plugin.getOutputName().equals(learnerName)){
				learner = plugin;
				recipe.setLearner(plugin);
			}
		}	
		String modelName = getModelName();
		String cvSetting = getcvSetting();
		Map<Integer, Integer> foldsMap;
		
		try{
			int thres = Integer.parseInt(cvSetting);
			foldsMap = recipe.getDocumentList().getFoldsMapByNum(thres);
		} catch (Exception x){
			foldsMap = recipe.getDocumentList().getFoldsMapByFile();
		}
		Map<String, String> config = new HashMap<String, String>();
		if (learner != null)
			learner.train(recipe, modelName, config, foldsMap, new JLabel());
	
		System.out.println(recipe.getTrainedModel().getSummary());
		
		Set<String> predictfiles = getpredictfilelist();
		String predicttext = getpredicttextfield();
		SimpleDocumentList predictdl = new  SimpleDocumentList(files, predicttext);
		String newname = getpredictname();
		GenesisRecipe predictResult = recipe.getTrainedModel().predictLabels(newname, predictdl);
		
		ArrayList<String> result = predictResult.getDocumentList().getAnnotationArray(newname);
		if (result.size() != predictResult.getDocumentList().getSize())
			System.out.println("len doesn't equal: " + result.size() + "\t" + predictResult.getDocumentList().getSize());
		
		
		for (int i=0; i<predictResult.getDocumentList().getSize(); i++)
			System.out.println(result.get(i));
	}
}