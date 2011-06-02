package edu.cmu.side.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListModel;
import javax.swing.filechooser.FileFilter;

import org.apache.uima.cas.CAS;
import org.w3c.dom.Element;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.swing.JEasyList;
import com.yerihyo.yeritools.swing.SwingToolkit;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.uima.UIMAToolkit;

public abstract class DocumentReaderPlugin extends SIDEPlugin implements ActionListener{
	private transient JEasyList openFileList;
	private transient JEasyList saveFileList;
	private transient JButton openButton;
	private JButton clearButton;
	private transient JPanel configPanel;
	private transient JPanel optionConfigPanel;

	public DocumentReaderPlugin () {
		super();
		yeriInit();
	}
	
	public static final String type = "document_reader";
	public String getType () {return type;}
	
	public DocumentReaderPlugin (File rootFolder)
	{
		super (rootFolder);
		yeriInit();
	}
	
	protected abstract void readDocumentIntoCas(CAS cas, File infile) throws Exception;

	private void yeriInit() {
		configPanel = new JPanel();
		configPanel.setLayout(new RiverLayout());
		
		
		openButton = new JButton("select files");
		clearButton = new JButton("clear files");
		clearButton.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e){
				openFileList.getModel().removeAllElements();
				saveFileList.getModel().removeAllElements();
				refreshPanel();
			}
		});
		openButton.setMnemonic(KeyEvent.VK_L);
		openButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				ListModel model = openFileList.getModel();
				
				int lastOpenFileIndex = model.getSize()-1;
				File lastOpenFile = lastOpenFileIndex<0?null:(File)model.getElementAt(lastOpenFileIndex);
				
				SwingToolkit.setSelectedFile(chooser,
						new File[]{lastOpenFile},
						new File[]{Workbench.current.getLoadFolder(), SIDEToolkit.workspaceFolder} );
				chooser.setFileFilter(getFileFilter());
				chooser.setMultiSelectionEnabled(true);
				
				int result = chooser.showOpenDialog(configPanel);
				if(result!=JFileChooser.APPROVE_OPTION){
					return;
				}
				
				SwingToolkit.addElementArrayWithoutAction(new ActionListener[]{DocumentReaderPlugin.this}, openFileList, chooser.getSelectedFiles());
				DocumentReaderPlugin.this.refreshPanel();
			}
		});

		openFileList = new JEasyList();
		
//		for(File file : SIDEToolkit.csvFolder.listFiles(FileToolkit.createExtensionListFileFilter(new String[]{"csv"}, false))){ 
//			openFileList.getModel().addElement(file);
//		}
		
		openFileList.addActionListener(this);

		configPanel.add("br hfill", new JLabel("open file list:"));
		configPanel.add("", openButton);
		configPanel.add("", clearButton);
		configPanel.add("br hfill", new JScrollPane(openFileList));
		
		saveFileList = new JEasyList();
		saveFileList.setRemoveEnabled(false);
		saveFileList.setMoveEnabled(false);
		
//		saveFileArea.setEditable(false);
		configPanel.add("p hfill", new JLabel("save file list:"));
		configPanel.add("br hfill", new JScrollPane(saveFileList));
		
		configPanel.add("br hfill", new JSeparator());
		
		optionConfigPanel = new JPanel();
		optionConfigPanel.setBorder(BorderFactory.createTitledBorder("option"));
		optionConfigPanel.setLayout(new BorderLayout());
		configPanel.add("br hfill", optionConfigPanel);
		
		this.refreshPanel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		refreshPanel();		
	}
	
	@Override
	public boolean doValidation(StringBuffer msg) {
		return true;
	}

	protected void refreshPanel(){
		this.memoryToUI();
		
		optionConfigPanel.removeAll();
		Component optionConfigUI = this.getOptionConfigUI();
		if(optionConfigUI!=null){ 
			optionConfigPanel.add(optionConfigUI, BorderLayout.CENTER);
		}
		
		DefaultListModel saveFileModel = saveFileList.getModel();
		saveFileModel.removeAllElements();
		
		ListModel openFileModel = openFileList.getModel();
		for(int i=0; i<openFileModel.getSize(); i++ ){
			File openFile = (File)openFileModel.getElementAt(i);
			File xmiFile = new File(SIDEToolkit.xmiFolder, openFile.getName()+".xmi");
			saveFileModel.addElement(xmiFile);
		}
		configPanel.revalidate();
		configPanel.repaint();
	}

	@Override
	public void fromXML(Element element) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toXML() {
		throw new UnsupportedOperationException();
	}
	
	protected abstract FileFilter getFileFilter();
	
	protected abstract Component getOptionConfigUI();
	public Component getConfigurationUIForSubclass(){
		this.refreshPanel();
		return this.configPanel;
	}
	
	
	public void readDocuments() throws Exception{
		this.refreshPanel();
		
		ListModel openFileModel = openFileList.getModel();
		DefaultListModel saveFileModel = saveFileList.getModel();
		
		if(openFileModel.getSize()<=0){
//			JOptionPane.showMessageDialog(null,
//					"no open file!", "error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("no open file!");
		}
		
		for(int i=0; i<openFileModel.getSize(); i++){
			File infile = (File)openFileModel.getElementAt(i);
			File ofile = (File)saveFileModel.getElementAt(i);
			
			CAS cas = UIMAToolkit.createSIDECAS();
			this.readDocumentIntoCas(cas, infile);
			
			UIMAToolkit.saveCas(cas, ofile);
		}
	}
	
	public static void main(String[] args){
		test01();
	}
	protected static void test01(){
		
	}

	protected abstract void initUIForSubclass();
	public void initUI() {
		openFileList.getModel().removeAllElements();
		this.initUIForSubclass();
		this.refreshPanel();
	}
}

