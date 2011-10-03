package edu.cmu.side;

import java.awt.Component;

import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SimpleOKDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.ResultOption;
import com.yerihyo.yeritools.xml.XMLToolkit;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.simple.newui.FastListModel;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimplePredictionResult;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.SimpleWorkbenchPanel;
import edu.cmu.side.simple.newui.features.FeatureTableListPanel;
import edu.cmu.side.simple.newui.machinelearning.ModelListPanel;
import edu.cmu.side.simple.newui.prediction.PredictionFileSelectPanel;

public class SimpleWorkbench {

	public static void main(String[] args) throws Exception {
		SimpleWorkbench workbench = new SimpleWorkbench();
	}
	
	static SimpleWorkbenchPanel panel;
	public SimpleWorkbench(){
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(1050,768));
		frame.setTitle("LightSIDE");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	public static File savedFolder = new File(BASE_PATH, "saved");
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
	
	public static void clearTrainingResults(){
		trainingResults = new ArrayList<TrainingResultInterface>();
		panel.actionPerformed(null);
	}
	
	public static void clearFeatureTables(){
		featureTables = new ArrayList<FeatureTable>();
		panel.actionPerformed(null);
	}
	
	public static void removeTrainingResult(int i){
		trainingResults.remove(i);
		panel.actionPerformed(null);
	}
	
	public static void removeFeatureTable(int i){
		featureTables.remove(i);
		panel.actionPerformed(null);
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
	
	public static class FeatureTableSaveListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent ae){
			JFileChooser chooser = new JFileChooser();
			FeatureTable ft = FeatureTableListPanel.getSelectedFeatureTable();
			chooser.setCurrentDirectory(savedFolder);
			chooser.setMultiSelectionEnabled(false);
			chooser.setSelectedFile(new File(ft.getTableName() + ".ser"));
			int result = chooser.showSaveDialog(null);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			try{
				ft.serialize(chooser.getSelectedFile());
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	public static class FeatureTableLoadListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent ae){
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(savedFolder);
			chooser.setMultiSelectionEnabled(false);
			int result = chooser.showOpenDialog(null);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			try{
				featureTables.add(new FeatureTable(new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()))));
				panel.actionPerformed(null);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	public static class FeatureTableExportListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			JComboBox combo = new JComboBox();
			combo.addItem("ARFF");
			JPanel pane = new JPanel();
			pane.add(combo);
			ResultOption option = SimpleOKDialog.show(null, "export to...", pane);
			if(option == ResultOption.APPROVE_OPTION){
				JFileChooser chooser = new JFileChooser();
				FeatureTable ft = FeatureTableListPanel.getSelectedFeatureTable();
				chooser.setCurrentDirectory(SimpleWorkbench.dataFolder);
				chooser.setMultiSelectionEnabled(false);
				chooser.setSelectedFile(new File(ft.getTableName() + "." + combo.getSelectedItem().toString().toLowerCase()));
				int result = chooser.showSaveDialog(null);
				if (result != JFileChooser.APPROVE_OPTION) {
					return;
				}
				FeatureTableListPanel.getSelectedFeatureTable().export(chooser.getSelectedFile(), combo.getSelectedItem().toString());
			}
		}
	}
	
	public static class TrainingResultSaveListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent ae){
			JFileChooser chooser = new JFileChooser();
			SimpleTrainingResult tr = ModelListPanel.getSelectedTrainingResult();
			chooser.setCurrentDirectory(savedFolder);
			chooser.setMultiSelectionEnabled(false);
			chooser.setSelectedFile(new File(tr.toString() + ".ser"));
			int result = chooser.showSaveDialog(null);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			try{
				tr.serialize(chooser.getSelectedFile());
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	public static class TrainingResultLoadListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent ae){
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(savedFolder);
			chooser.setMultiSelectionEnabled(false);
			int result = chooser.showOpenDialog(null);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			try{
				trainingResults.add(new SimpleTrainingResult(chooser.getSelectedFile()));
				panel.actionPerformed(null);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	public static class PredictionResultSaveListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent ae){
			JFileChooser chooser = new JFileChooser();
			SimpleDocumentList output = PredictionFileSelectPanel.getPredictionDocuments();
			chooser.setCurrentDirectory(savedFolder);
			chooser.setMultiSelectionEnabled(false);
			chooser.setSelectedFile(new File(output.getCurrentAnnotation() + ".csv"));
			int result = chooser.showSaveDialog(null);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(chooser.getSelectedFile()));
				out.write(output.toCSVString());
				out.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	public static SimpleWorkbenchPanel getWorkbench(){
		return panel;
	}
}
