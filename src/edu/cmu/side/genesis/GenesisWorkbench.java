package edu.cmu.side.genesis;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.JFrame;

import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;

public class GenesisWorkbench {

	
	Map<String, Integer> documentRecipeKeys = new TreeMap<String, Integer>();
	Map<String, Integer> featureTableRecipeKeys = new TreeMap<String, Integer>();
	Map<String, Integer> trainedModelRecipeKeys = new TreeMap<String, Integer>();
	
	ArrayList<SimpleDocumentList> documents = new ArrayList<SimpleDocumentList>();
	ArrayList<FeatureTable> tables = new ArrayList<FeatureTable>();
	ArrayList<SimpleTrainingResult> models = new ArrayList<SimpleTrainingResult>();
	
	GenesisWorkbenchPanel panel = new GenesisWorkbenchPanel();
	public GenesisWorkbench(){

		JFrame frame = new JFrame();
		frame.setSize(new Dimension(1050,768));
		frame.setTitle("LightSIDE");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(BorderLayout.CENTER, panel);
		frame.setVisible(true);
		panel.actionPerformed(new ActionEvent(this,1,"plugins"));
	}
	
	public static void main(String[] args) throws Exception{
		GenesisWorkbench bench = new GenesisWorkbench();
	}
}
