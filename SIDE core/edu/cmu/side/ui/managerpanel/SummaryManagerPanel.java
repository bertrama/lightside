package edu.cmu.side.ui.managerpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.SIDEToolkit.FileType;
import edu.cmu.side.Workbench.ListManager;
import edu.cmu.side.dataitem.Summary;

public class SummaryManagerPanel extends ManagerPanel<Summary>{
	private static final long serialVersionUID = 1L;
	
	public static class SummaryListManager extends ListManager<Summary>{
		public static File iconFile = new File(SIDEToolkit.imageFolder, "summary.png");
		@Override
		public File getIconFile() { return iconFile; }
		
		@Override
		protected Summary loadItem(File file) {
			try {
				return Summary.loadFile(file);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class SummaryMenu extends JPopupMenu{
		private static final long serialVersionUID = 1L;
		private Summary summary;
		
		private JMenuItem createSaveMenuItem(){
			JMenuItem saveMenuItem = new JMenuItem("save");
			saveMenuItem.setMnemonic(KeyEvent.VK_S);
			saveMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					SummaryMenu sfm = SummaryMenu.this;
					String filename = sfm.summary.getSavefileName()+".xml";
					File selectedFile = SIDEToolkit.getUserSelectedFile(FileType.summary.getDefaultFolder(), filename);
					
					if(selectedFile==null){ return ; }
					try {
						sfm.summary.save(selectedFile);
					} catch (IOException e1) {
						System.err.println(e1);
					}
				}
			});
			return saveMenuItem;
		}
		
		private JMenuItem createLoadMenuItem(){
			JMenuItem loadMenuItem = new JMenuItem("load summary..");
			loadMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(FileType.summary.getDefaultFolder());
					chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"xml"}, true));
					chooser.setMultiSelectionEnabled(true);
					int result = chooser.showOpenDialog(SummaryMenu.this);
					
					if(result!=JFileChooser.APPROVE_OPTION){
						return;
					}
					
					File[] fileArray = chooser.getSelectedFiles();
					Workbench.current.summaryListManager.loadFromFileArray(fileArray);
				}
			});
			return loadMenuItem;
		}
		public SummaryMenu(Summary summary){
			this.summary = summary;
			
			this.add(createLoadMenuItem());
			if(summary!=null){
				this.add(createSaveMenuItem());
			}
		}
	}
	
	
	public static void main(String[] args) throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException{
		test01();
	}
	protected static void test01() throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException {
		SummaryManagerPanel rmp = new SummaryManagerPanel();
		TestFrame testFrame = new TestFrame(rmp);
		testFrame.showFrame();
	}
	
	@Override
	protected JPopupMenu getPopupMenu(Summary t) {
		return new SummaryMenu(t);
	}
	@Override
	protected String getLabelString() {
		return "summary";
	}
	@Override
	protected ListManager<Summary> getListManager() {
		return Workbench.current.summaryListManager;
	}
	
}