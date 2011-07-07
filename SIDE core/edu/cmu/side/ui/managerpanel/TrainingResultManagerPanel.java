package edu.cmu.side.ui.managerpanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import oracle.xml.parser.v2.XMLDocument;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.w3c.dom.Element;

import com.mysterion.xml.XMLBoss;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.Workbench.ListManager;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.dataitem.TrainingResultInterface;

public class TrainingResultManagerPanel extends ManagerPanel<TrainingResult>{
	private static final long serialVersionUID = 1L;
	
	
	public TrainingResult getSelectedTrainingResult(){
		return super.getSelectedItem();
	}
	
	public static class TrainingResultListManager extends ListManager<TrainingResult>{
		public static File iconFile = new File(SIDEToolkit.imageFolder, "model.png");
		@Override
		public File getIconFile() { return iconFile; }
		
		@Override
		protected TrainingResult loadItem(File file) {
			try {
				return (TrainingResult)TrainingResult.create(file);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	private static class TrainingResultMenu extends JPopupMenu{
		private static final long serialVersionUID = 1L;
		private TrainingResult trainingResult;
		
		private JMenuItem createSaveMenuItem(){
			JMenuItem saveMenuItem = new JMenuItem("save");
			saveMenuItem.setMnemonic(KeyEvent.VK_S);
			saveMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					TrainingResultMenu rcm = TrainingResultMenu.this;
					String filename = rcm.trainingResult.getSavefileName()+".xml";
					File selectedFile = SIDEToolkit.getUserSelectedFile(SIDEToolkit.modelFolder, filename);
					
					if(selectedFile==null){ return ; }
					try {
						rcm.trainingResult.save(selectedFile);
					} catch (IOException e1) {
						System.err.println(e1);
					}
				}
			});
			return saveMenuItem;
		}
		
		private JMenuItem createLoadMenuItem(){
			JMenuItem loadMenuItem = new JMenuItem("load training result..");
			loadMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(SIDEToolkit.modelFolder);
					chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"xml"}, true));
					chooser.setMultiSelectionEnabled(true);
					int result = chooser.showOpenDialog(TrainingResultMenu.this);
					
					if(result!=JFileChooser.APPROVE_OPTION){
						return;
					}
					
					File[] fileArray = chooser.getSelectedFiles();
					Workbench.current.trainingResultListManager.loadFromFileArray(fileArray);
				}
			});
			return loadMenuItem;
		}
		public TrainingResultMenu(TrainingResult trainingResult){
			this.trainingResult = trainingResult;
			
			this.add(createLoadMenuItem());
			if(trainingResult!=null){
				this.add(createSaveMenuItem());
			}
		}
	}
	
	
	public static void main(String[] args) throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException{
		test01();
	}
	protected static void test01() throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException {
		TrainingResultManagerPanel ftmp = new TrainingResultManagerPanel();
		
		File xmiFile = new File(SIDEToolkit.modelFolder, "20090416_165757.xml");
		YeriDebug.ASSERT(xmiFile.exists());
		XMLDocument doc = XMLBoss.XMLFromFile(xmiFile);
		Element root = doc.getDocumentElement();
		TrainingResultInterface trainingResult = TrainingResult.create(root);
		
		ftmp.listModel.addElement(trainingResult);
		
		TestFrame testFrame = new TestFrame(ftmp);
		testFrame.setSize(new Dimension(600,400));
		testFrame.showFrame();
	}
	
	@Override
	protected JPopupMenu getPopupMenu(TrainingResult t) {
		return new TrainingResultMenu(t);
	}
	@Override
	protected String getLabelString() {
		return "list of models";
	}
	@Override
	protected ListManager<TrainingResult> getListManager() {
		return Workbench.current.trainingResultListManager;
	}
	
}