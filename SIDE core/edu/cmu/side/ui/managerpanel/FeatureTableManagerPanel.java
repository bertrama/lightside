package edu.cmu.side.ui.managerpanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.Workbench.ListManager;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.plugin.FEPlugin;
import edu.cmu.side.plugin.FeatureTableConsumer;
import edu.cmu.side.plugin.PluginWrapper;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;

public class FeatureTableManagerPanel extends ManagerPanel<FeatureTable>{
	private static final long serialVersionUID = 1L;
	
	public FeatureTable getSelectedFeatureTable(){
		return super.getSelectedItem();
	}
	
	public static class FeatureTableListManager extends ListManager<FeatureTable>{
		
		public static File iconFile = new File(SIDEToolkit.imageFolder, "feature_table.png");
		@Override
		public File getIconFile(){ return iconFile; }
		
		@Override
		protected FeatureTable loadItem(File file) {
			try {
				FeatureTable ft = FeatureTable.createFromXMLFile(file);
				System.out.println(ft.getAllFeatureTableKeyList().size() + " after all XMLing.");
				return ft;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class FeatureTableRightClickMenu extends JPopupMenu{
		private static final long serialVersionUID = 1L;
		private FeatureTable featureTable;
		
		private JMenu createExportMenu(){
			JMenu exportMenu = new JMenu("export using...");
			List<PluginWrapper> pluginWrapperList = Workbench.current.pluginManager.getPluginWrapperCollectionByType(FeatureTableConsumer.type);
			
			for(PluginWrapper wrapper : pluginWrapperList){
				FeatureTableConsumer featureTableConsumer = (FeatureTableConsumer)wrapper.getSIDEPlugin();
				String className = featureTableConsumer.getClass().getName();
				JMenuItem menuItem = new JMenuItem(className);
				menuItem.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						String pluginClassName = ((JMenuItem)e.getSource()).getText();
						FeatureTableConsumer featureTableConsumer = (FeatureTableConsumer)Workbench.current.pluginManager.getPluginWrapperByPluginClassName(pluginClassName).getSIDEPlugin();
						try {
							featureTableConsumer.comsumeFeatureTable(FeatureTableRightClickMenu.this.featureTable);
						} catch (Exception e1) {
							System.err.println("Error while consuming feature table");
							e1.printStackTrace();
						}
					}
				});
				exportMenu.add(menuItem);
				exportMenu.setMnemonic(KeyEvent.VK_O);
			}
			return exportMenu;
		}
		private JMenuItem createSaveMenuItem(){
			JMenuItem saveMenuItem = new JMenuItem("save");
			saveMenuItem.setMnemonic(KeyEvent.VK_S);
			saveMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					featureTable.save();
				}
			});
			return saveMenuItem;
		}
		private JMenuItem createLoadMenuItem(){
			JMenuItem loadMenuItem = new JMenuItem("load feature table..");
			loadMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(SIDEToolkit.featureTableFolder);
					chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"xml"}, true));
					chooser.setMultiSelectionEnabled(true);
					int result = chooser.showOpenDialog(FeatureTableRightClickMenu.this);
					
					if(result!=JFileChooser.APPROVE_OPTION){
						return;
					}
					
					File[] fileArray = chooser.getSelectedFiles();
					Workbench.current.featureTableListManager.loadFromFileArray(fileArray);
				}
			});
			return loadMenuItem;
		}
		
		public FeatureTableRightClickMenu(FeatureTable featureTable){
			this.featureTable = featureTable;
			if(featureTable!=null){
				this.add(createSaveMenuItem());
				this.add(createExportMenu());
			}
			this.add(createLoadMenuItem());
		}
	}
	
	@Override
	protected JPopupMenu getPopupMenu(FeatureTable t) {
		return new FeatureTableRightClickMenu(t);
	}
	@Override
	protected String getLabelString() {
		return "feature table";
	}
	@Override
	protected ListManager<FeatureTable> getListManager() {
		return Workbench.current.featureTableListManager;
	}
	
}