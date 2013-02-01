package edu.cmu.side;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import edu.cmu.side.control.GenesisControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.plugin.control.PluginManager;
import edu.cmu.side.view.WorkbenchPanel;
import edu.cmu.side.view.extract.ExtractCombinedLoadPanel;
import edu.cmu.side.view.extract.ExtractLoadPanel;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.GlassPane;


public class Workbench{

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

	
	
	static WorkbenchPanel panel;
	static GlassPane pane;

	static boolean serverMode = false;
	static Image iconImage; 

	public Workbench(){

		
		
		JFrame frame = new JFrame();
		frame.setIconImages(getIcons("toolkits/icons/bulbs/bulb_128.png", "toolkits/icons/bulbs/simple_32.png", "toolkits/icons/bulbs/simple_16.png")); //for windows?

		panel = new WorkbenchPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		frame.setContentPane(panel);
		//		pane = new GlassPane(frame.getContentPane());
		//		frame.setGlassPane(pane);
		frame.setSize(new Dimension(1024,768));
		frame.setTitle("LightSIDE");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		//		pane.setVisible(true);
	}

	public List<? extends Image> getIcons(String... paths)
	{
		ArrayList<Image> icons = new ArrayList<Image>();

		Toolkit kit = Toolkit.getDefaultToolkit();
		for(String iconPath : paths)
		{
			icons.add(kit.createImage(iconPath));
		}
		return icons;
	}
	
	public static void main(String[] args) throws Exception
	{

		Workbench workbench = new Workbench();
	}

	public static RecipeManager getRecipeManager(){
		return recipeManager;
	}
	
	static long updateCount = 0;
	public static void update(Object source){
		if(!GenesisControl.isCurrentlyUpdating(source)){

			Collection<AbstractListPanel> listeners = GenesisControl.getListeners(source);

			if(!listeners.isEmpty())
			{
				updateCount++;
//				long update = updateCount;
				GenesisControl.setCurrentlyUpdating(source, true);
//				System.out.println("Workbench.update begin update #"+update+" for source "+source.getClass().getName());
				
				for(AbstractListPanel listen : listeners)
				{
//						System.out.println("Workbench.update #" + updateCount + ":\n\tsource  " + source.getClass().getName() + "\n\trefresh "
//								+ listen.getClass().getName());

						listen.refreshPanel();
//						System.out.println("Workbench.update end refresh #"+update+" for "+listen.getClass().getName() );
					
				}	
//				System.out.println("Workbench.update end update #"+update);
				GenesisControl.setCurrentlyUpdating(source, false);
			}
		}
	}

	public static Collection<Recipe> getRecipesByPane(RecipeManager.Stage type){
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
			if(option.equals(selected)){
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
