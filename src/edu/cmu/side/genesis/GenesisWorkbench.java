package edu.cmu.side.genesis;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.control.GenesisUpdater;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.model.RecipeManager;
import edu.cmu.side.genesis.view.GlassPane;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;
import edu.cmu.side.genesis.view.extract.ExtractFeaturesPane;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;

public class GenesisWorkbench{

	public static File rootFolder = new File(System.getProperty("user.dir"));
	static public String PLATFORM_FILE_SEPARATOR = System.getProperty("file.separator");
	static public String BASE_PATH = rootFolder.getAbsolutePath() + PLATFORM_FILE_SEPARATOR;
	static public File PLUGIN_FOLDER = new File(BASE_PATH, "plugins");
	public static File dataFolder = new File(rootFolder, "data");
	public static File stopwordsFolder = new File(dataFolder, "stopwords");
	public static File csvFolder = dataFolder;
	public static File toolkitsFolder = new File(rootFolder, "toolkits");
	public static File savedFolder = new File(BASE_PATH, "saved");

	public static PluginManager pluginManager = new PluginManager(PLUGIN_FOLDER);
	public static RecipeManager recipeManager = new RecipeManager();
	static GenesisWorkbenchPanel panel;
	static GlassPane pane;

	static boolean serverMode = false;
	
	public GenesisWorkbench(){
		JFrame frame = new JFrame();
		panel = new GenesisWorkbenchPanel();
		frame.setContentPane(panel);
		pane = new GlassPane(frame.getContentPane());
		frame.setGlassPane(pane);
		frame.setSize(new Dimension(1050,768));
		frame.setTitle("LightSIDE");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		pane.setVisible(true);
		update();
	}
	
	

	public static void main(String[] args) throws Exception{
		String file = "/Users/emayfiel/movies_train.csv";
		GenesisUpdater update = new SwingUpdaterLabel();
		GenesisWorkbench workbench = new GenesisWorkbench();
	}

	public static void update(){
		pane.repaint();
		panel.actionPerformed(null);
	}

	public static Collection<GenesisRecipe> getRecipesByPane(String type){
		return recipeManager.getRecipeCollectionByType(type);
	}

	//Parameterized collections? Who needs 'em!
	public static void reloadComboBoxContent(JComboBox dropdown, Collection<? extends Object> options, Object selected){
		Object[] obj = new Object[options.size()];
		int i = 0;
		for(Object o : options){ obj[i] = o; i++; }
		reloadComboBoxContent(dropdown, obj, selected);
	}
	
	public static void reloadComboBoxContent(JComboBox dropdown, Object[] options, Object selected){	
		ActionListener[] listeners = dropdown.getActionListeners();
		for(ActionListener al : listeners){
			dropdown.removeActionListener(al);
		}
		dropdown.removeAllItems();
		int select = -1;
		int i = 0;
		for(Object option : options){
			dropdown.addItem(option);
			if(option == selected){
				select = i;
			}
			i++;
		}
		for(ActionListener al : listeners){
			dropdown.addActionListener(al);
		}
		if(selected != null){
			dropdown.setSelectedIndex(select);			
		}else{
			dropdown.setSelectedIndex(-1);
		}
	}
}
