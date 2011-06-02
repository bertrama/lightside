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
import org.w3c.dom.Element;

import com.mysterion.xml.XMLBoss;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.SIDEToolkit.FileType;
import edu.cmu.side.Workbench.ListManager;
import edu.cmu.side.dataitem.Recipe;

public class RecipeManagerPanel extends ManagerPanel<Recipe>{
	private static final long serialVersionUID = 1L;
	
	public static class RecipeListManager extends ListManager<Recipe>{
		public static File iconFile = new File(SIDEToolkit.imageFolder, "text_recipe.png");
		@Override
		public File getIconFile() { return iconFile; }
		
		@Override
		protected Recipe loadItem(File file) {
			try {
				Element root = XMLBoss.XMLFromFile(file).getDocumentElement();
				return Recipe.create(root);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private static class RecipeMenu extends JPopupMenu{
		private static final long serialVersionUID = 1L;
		private Recipe recipe;
		
		private JMenuItem createSaveMenuItem(){
			JMenuItem saveMenuItem = new JMenuItem("save");
			saveMenuItem.setMnemonic(KeyEvent.VK_S);
			saveMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					RecipeMenu sfm = RecipeMenu.this;
					String filename = sfm.recipe.getSavefileName()+".xml";
					File selectedFile = SIDEToolkit.getUserSelectedFile(FileType.recipe.getDefaultFolder(), filename);
					
					if(selectedFile==null){ return ; }
					try {
						sfm.recipe.save(selectedFile);
					} catch (IOException e1) {
						System.err.println(e1);
					}
				}
			});
			return saveMenuItem;
		}
		
		private JMenuItem createLoadMenuItem(){
			JMenuItem loadMenuItem = new JMenuItem("load recipe..");
			loadMenuItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(FileType.recipe.getDefaultFolder());
					chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"xml"}, true));
					chooser.setMultiSelectionEnabled(true);
					int result = chooser.showOpenDialog(RecipeMenu.this);
					
					if(result!=JFileChooser.APPROVE_OPTION){
						return;
					}
					
					File[] fileArray = chooser.getSelectedFiles();
					Workbench.current.recipeListManager.loadFromFileArray(fileArray);
				}
			});
			return loadMenuItem;
		}
		public RecipeMenu(Recipe recipe){
			this.recipe = recipe;
			
			this.add(createLoadMenuItem());
			if(recipe!=null){
				this.add(createSaveMenuItem());
			}
		}
	}
	
	
	public static void main(String[] args) throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException{
		test01();
	}
	protected static void test01() throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException {
		RecipeManagerPanel rmp = new RecipeManagerPanel();
		TestFrame testFrame = new TestFrame(rmp);
		testFrame.showFrame();
	}
	
	@Override
	protected JPopupMenu getPopupMenu(Recipe t) {
		return new RecipeMenu(t);
	}
	@Override
	protected String getLabelString() {
		return "summary recipe";
	}
	@Override
	protected ListManager<Recipe> getListManager() {
		// TODO Auto-generated method stub
		return Workbench.current.recipeListManager;
	}
	
}