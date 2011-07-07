package edu.cmu.side;

import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.SimplePredictionResult;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.SimpleWorkbenchPanel;

public class SimpleWorkbench {

	public static void main(String[] args) throws Exception {
		SimpleWorkbench workbench = new SimpleWorkbench();
	}
	
	static SimpleWorkbenchPanel panel;
	public SimpleWorkbench(){
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(1100,800));
		frame.setTitle("LightSIDE");
		panel = new SimpleWorkbenchPanel();
		frame.add(panel);
		frame.setVisible(true);
		panel.actionPerformed(new ActionEvent(this,1,"plugins"));
	}
	
	static PluginManager pluginManager = new PluginManager(SIDEToolkit.PLUGIN_FOLDER);
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
	
	public static SimpleWorkbenchPanel getWorkbench(){
		return panel;
	}
}
