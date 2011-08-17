package edu.cmu.side;

import java.awt.Component;

import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import com.yerihyo.yeritools.io.FileToolkit;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.simple.newui.FastListModel;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.SimplePredictionResult;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.SimpleWorkbenchPanel;

public class SimpleWorkbench {

	public static void main(String[] args) throws Exception {
		SimpleWorkbench workbench = new SimpleWorkbench();
	}
	
	static SimpleWorkbenchPanel panel;
	public SimpleWorkbench(){
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(1024,768));
		frame.setTitle("LightSIDE");
		frame.setLayout(new RiverLayout());
		panel = new SimpleWorkbenchPanel();
		frame.add("hfill vfill", new JScrollPane(panel));
		frame.setVisible(true);
		panel.actionPerformed(new ActionEvent(this,1,"plugins"));
	}


	/**
	 * Static fields that everything else references for file lookup
	 */
	public static File rootFolder =
		new File(System.getProperty("user.dir"));
	static public String PLATFORM_FILE_SEPARATOR = System
			.getProperty("file.separator");
	static public String BASE_PATH = rootFolder.getAbsolutePath()
			+ PLATFORM_FILE_SEPARATOR;
	static public File PLUGIN_FOLDER = new File(BASE_PATH, "plugins");
	public static File dataFolder = new File(rootFolder, "data");
	public static File stopwordsFolder = new File(dataFolder, "stopwords");
	public static File csvFolder = dataFolder;
	public static File toolkitsFolder = new File(rootFolder, "toolkits");
	
	/**
	 * Static collections of various data structures used throughout the program and accessible from anywhere.
	 */
	public static PluginManager pluginManager = new PluginManager(PLUGIN_FOLDER);
	static List<FeatureTable> featureTables = new ArrayList<FeatureTable>();
	static List<TrainingResultInterface> trainingResults = new ArrayList<TrainingResultInterface>();
	static List<SimplePredictionResult> predictionResults = new ArrayList<SimplePredictionResult>();
	public static SIDEPlugin[] getPluginsByType(String type){
		return pluginManager.getSIDEPluginArrayByType(type);
	}
	
	public static void addFeatureTable(FeatureTable table){
		featureTables.add(table);
	}
	
	public static void addTrainingResult(TrainingResultInterface result){
		trainingResults.add(result);
	}
	
	public static void addPredictionResult(SimplePredictionResult result){
		predictionResults.add(result);
	}
	
	public static List<FeatureTable> getFeatureTables(){
		return featureTables;
	}
	
	public static List<TrainingResultInterface> getTrainingResults(){
		return trainingResults;
	}
	
	public static List<SimplePredictionResult> getPredictionResults(){
		return predictionResults;
	}
	

	/**
	 * ActionListener for adding CSV files to SimpleListManagerPanel.
	 * @author emayfiel
	 *
	 */
	public static class FileAddActionListener implements ActionListener {
		private Component parentComponent;
		private FastListModel model;
		
		public FileAddActionListener(Component parentComponent, FastListModel model){
			this.parentComponent = parentComponent;
			this.model = model;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(csvFolder);
			chooser.setFileFilter(FileToolkit
					.createExtensionListFileFilter(new String[] { "csv" }, true));
			chooser.setMultiSelectionEnabled(true);
			int result = chooser.showOpenDialog(parentComponent);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			FastListModel fileListModel = model;
			fileListModel.addAll(chooser.getSelectedFiles());
		}
	}
	
	
	public static SimpleWorkbenchPanel getWorkbench(){
		return panel;
	}
}
