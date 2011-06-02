/*
 * Workbench.java
 *
 * Created on __DATE__, __TIME__
 */

package edu.cmu.side;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.dataitem.DataItem;
import edu.cmu.side.plugin.PluginManager;
import edu.cmu.side.ui.configpanel.DocumentReaderConfigPanel;
import edu.cmu.side.ui.managerpanel.FeatureTableManagerPanel.FeatureTableListManager;
import edu.cmu.side.ui.managerpanel.RecipeManagerPanel.RecipeListManager;
import edu.cmu.side.ui.managerpanel.SummaryManagerPanel.SummaryListManager;
import edu.cmu.side.ui.managerpanel.TrainingResultManagerPanel.TrainingResultListManager;
import edu.cmu.side.ui.tabbedpane.MachineLearningPanel;
import edu.cmu.side.ui.tabbedpane.SummaryBuilder;
import edu.cmu.side.viewer.AnnotationEditor;

/**
 * 
 * @author __USER__
 */
public class Workbench extends JFrame {
	private static final long serialVersionUID = 1L;
	public static Workbench current;
	
	public static boolean testMode = false;
	
	public PluginManager pluginManager = null;
	
	static{
		Workbench.create();
	}
	
	public static Workbench create(){
		try {
			Workbench.current = new Workbench();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return Workbench.current;
	}
	
	public static final String loadFolderPathKey = "load folder";
	public Map<String,String> parameterMap = new HashMap<String,String>();
	public File getLoadFolder(){
		String loadFolderPath = parameterMap.get(loadFolderPathKey);
		if(loadFolderPath==null){ return null; }
		return new File(loadFolderPath);
	}

	

	private Workbench() throws Exception {
		yeriInit();
	}
	private void yeriInit() {
		Workbench.current = this;
		pluginManager = new PluginManager(SIDEToolkit.PLUGIN_FOLDER);
		
	}

	
	public static void main(String[] args) throws Exception {
		testMain(args);
	}
	private static class MainButtonListener implements ActionListener{

		private JComponent c;
		private Dimension dimension;
		private String name;
		
		public MainButtonListener(JComponent c, Dimension d){
			this.c = c;
			this.dimension = d;
			name = "Test";
		}
		
		public MainButtonListener(String n, JComponent c, Dimension d){
			this.c = c;
			this.dimension = d;
			this.name = n;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			TestDialog testDialog = new TestDialog(c, null, Dialog.ModalityType.MODELESS);
			testDialog.setSize(dimension);
			testDialog.setTitle(name);
			testDialog.showDialog();			
		}
		
	}
	private static JButton buildButton(String name, Dimension dimension, JComponent c){
		JButton button = new JButton(name);
		button.addActionListener(new MainButtonListener(name, c, dimension));
		return button;
	}

	protected static void testMain(String[] args) {
		if(args.length>0 && args[0].equalsIgnoreCase("test")){
			testMode = true;
		}
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new RiverLayout());
		
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(1100,800));
		frame.setTitle("SIDE");
		frame.setVisible(true);
		frame.add(new MachineLearningPanel());
		
		JButton loadAllButton = new JButton("load all files");
		loadAllButton.setMnemonic(KeyEvent.VK_L);
		loadAllButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SIDEToolkit.FileType.loadAll();
			}
		});
		
		JButton cleanupButton = new JButton("clean up save files (x)");
		cleanupButton.setMnemonic(KeyEvent.VK_X);
		cleanupButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog((Component)e.getSource(),
						"Remove all saved files?",
						"Remove",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if(result!=JOptionPane.OK_OPTION){
					return;
				}
				
				SIDEToolkit.cleanupSaveFiles();
			}
		});
		
		JButton testButton = new JButton("test all");
		testButton.setMnemonic(KeyEvent.VK_T);
		testButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SIDEToolkit.testAll();
			}
		});
		
//		mainPanel.add("p hfill", documentReaderButton);
//		mainPanel.add("br hfill", annotationButton);
//		mainPanel.add("br hfill", machineLearningButton);
////		mainPanel.add("br hfill", predictionButton);
////		mainPanel.add("p hfill", filterButton);
//		mainPanel.add("br hfill", summaryButton);
		
		if(testMode){		
			mainPanel.add("p hfill", new JSeparator());
			mainPanel.add("p hfill", loadAllButton);
			mainPanel.add("br hfill", cleanupButton);
			mainPanel.add("br hfill", testButton);
		}
				
		
		TestFrame testFrame = new TestFrame(mainPanel);
		testFrame.setTitle(SIDEToolkit.titleLaunchPanel);
		testFrame.pack();
		
//		SIDEToolkit.FileType.loadAll();
//		testFrame.showFrame();
	}
	
	public static abstract class ListManager<T>{
		private int eventID = 1;
		private List<T> list = new ArrayList<T>();
		
		public abstract File getIconFile(); 
		public ImageIcon createImageIcon(){ return new ImageIcon(this.getIconFile().getAbsolutePath()); }
		public JLabel createImageIconLabel(){ return new JLabel(new ImageIcon(this.getIconFile().getAbsolutePath())); }
		
		public void removeAll(){
			list.clear();
		}
		
		public Iterator<T> iterator(){
			return list.iterator();
		}
		
		public void add(T t){
			this.list.add(t);
			this.fireActionEvent();
		}
		
		public ListManagerComboBox createComboBox(){
			ListManagerComboBox comboBox =  new ListManagerComboBox();
			this.addActionListener(comboBox);
			return comboBox;
		}
		public class ListManagerComboBox extends JComboBox{
			private static final long serialVersionUID = 1L;
			
			public ListManagerComboBox(){
				yeriInit();
			}
			
			private void yeriInit(){
				this.setRenderer(new SwingToolkit.ValueCustomizedListCellRenderer(this.getRenderer()){
					@Override
					protected String getText(Object value) {
						if(value==null){ return null; }
						else if((value instanceof DataItem)){
							DataItem item = (DataItem)value;
							return item.getDisplayText();
						}else{
							System.out.println(value.getClass().getName());
							throw new UnsupportedOperationException();
						}
					}
				});
				refresh();
			}
			
			public T getSelectedItem(){
				int index = super.getSelectedIndex();
				return index>=0?(T)super.getSelectedItem():null;
			}
			public void actionPerformed(ActionEvent evt){
				super.actionPerformed(evt);
				
				if(!listManagerEventName.equals(evt.getActionCommand())){ return; }
				refresh();
			}
			
			private void refresh(){
				SwingToolkit.reloadComboBoxContent(ListManagerComboBox.this, list.toArray(), null, false);
			}
			
		}
		
		private List<ActionListener> listenerList = new ArrayList<ActionListener>();
		public void addActionListener(ActionListener al){
			listenerList.add(al);
		}
		public void removeActionListener(ActionListener al){
			listenerList.remove(al);
		}
		
		private static final String listManagerEventName = "ListManager event";
		public void fireActionEvent(){
			ActionEvent ae = new ActionEvent(list, eventID++, listManagerEventName);
			for(ActionListener al : this.listenerList){
				al.actionPerformed(ae);
			}
		}
		
		public void loadFromFileArray(File[] fileArray) {
			for(File file : fileArray){
				try {
					T t = loadItem(file);
					list.add(t);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			fireActionEvent();
		}
		
		protected abstract T loadItem(File file);
	}
	
	public FeatureTableListManager featureTableListManager = new FeatureTableListManager();
	public TrainingResultListManager trainingResultListManager = new TrainingResultListManager();
//	public SIDEFilterListManager sideFilterListManager = new SIDEFilterListManager();
	public RecipeListManager recipeListManager = new RecipeListManager();
	public SummaryListManager summaryListManager = new SummaryListManager();
}


