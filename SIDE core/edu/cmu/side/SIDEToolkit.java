package edu.cmu.side;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import javax.swing.DefaultButtonModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import opennlp.tools.lang.english.PosTagger;
import opennlp.tools.lang.english.SentenceDetector;
import opennlp.tools.postag.POSDictionary;

import com.yerihyo.yeritools.RTTIToolkit;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.ColorLabelConfigPanel;

import edu.cmu.side.Workbench.ListManager;
import edu.cmu.side.dataitem.Recipe;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;
import edu.cmu.side.uima.type.SIDESegment;
import edu.cmu.side.viewer.FastListModel;


public class SIDEToolkit {
//	public static boolean debugMode = true;

	public static File[] getTmpXmiFileArray(){
		return xmiFolder.listFiles(FileToolkit.createExtensionListFileFilter(new String[]{"xmi"}, false));
	}
	
	public static File getDescriptorsFolder(){
		return new File(rootFolder, "descriptors");
	}
	
	
	public static class XMIFileAddActionListener implements ActionListener {
		private Component parentComponent;
		private FastListModel model;
		
		public XMIFileAddActionListener(Component parentComponent, FastListModel model){
			this.parentComponent = parentComponent;
			this.model = model;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(SIDEToolkit.xmiFolder);
			chooser.setFileFilter(FileToolkit
					.createExtensionListFileFilter(new String[] { "xmi" }, true));
			chooser.setMultiSelectionEnabled(true);
			int result = chooser.showOpenDialog(parentComponent);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}

			FastListModel fileListModel = model;

			fileListModel.addAll(chooser.getSelectedFiles());
		}

	}

	public static final String TMP_TIMESTAMP_COLUMN = "SIDE_Timestamp";
	
	public static File rootFolder =
//		new File("C:/yeri/projects/summarization/SIDE project/program/SIDE");
		new File(System.getProperty("user.dir"));
	
	public static File dataFolder = new File(rootFolder, "data");
	public static File stopwordsFolder = new File(dataFolder, "stopwords");
	public static File csvFolder = new File(dataFolder, "articles/csv");
	
	public static File workspaceFolder = new File(rootFolder, "side workspace");
	public static File modelFolder = FileType.trainingResult.getDefaultFolder();
//	public static File sideFilterFolder = FileType.sideFilter.getDefaultFolder();
	public static File featureTableFolder = FileType.featureTable.getDefaultFolder();
	public static File etcFolder = new File(workspaceFolder, "etc");
	public static File xmiFolder = new File(workspaceFolder, "xmi");
	public static File predictionResultFolder = new File(workspaceFolder, "prediction_result");
	
	public static File toolkitsFolder = new File(rootFolder, "toolkits");
	public static File opennlpFolder = new File(toolkitsFolder, "opennlp");
	public static File opennlpModelsFolder = new File(opennlpFolder, "models");
	
	static public String PLATFORM_FILE_SEPARATOR = System
			.getProperty("file.separator");
	static public String PLATFORM_FILE_SEPARATOR_SLASH_ADDED = addSlashesToFileSeparator(PLATFORM_FILE_SEPARATOR);
	static public String SIDE_FILE_SEPARATOR = "/";
	static public String JAR_PATH_FILE_SEPERATOR = "/";
	static public String JAR_ENTRY_FILE_SEPERATOR = RTTIToolkit.JAR_ENTRY_FILE_SEPERATOR;
	
	public static void main(String[] args){
		test01();
	}
	protected static void test01(){
		getDefaultStopwordsFile(Locale.GERMANY);
	}
	
	public static File getDefaultStopwordsFile(Locale locale){
		String languageString = locale.getDisplayLanguage().toLowerCase();
		File stopwordsFile = new File(stopwordsFolder, languageString+".stp");
		return stopwordsFile;
	}
	
	public static enum FileType{
		recipe, featureTable, trainingResult, summary;
		
		public static void loadAll(){
			for(FileType fileType : FileType.values()){
				fileType.loadFileArray(fileType.getFileArray());
			}
		}
		protected String getExtension(){
			return "xml";
		}
		protected String getDefaultFolderName(){
			if(this==recipe){ return "recipes"; }
			else if(this==summary){ return "summaries"; }
//			else if(this==sideFilter){ return "filters"; }
			else if(this==featureTable){ return "feature_table"; }
			else if(this==trainingResult){ return "model"; }
			else{ throw new UnsupportedOperationException(); }
		}
		
		public File getDefaultFolder(){
			return new File(workspaceFolder, this.getDefaultFolderName());
		}
		
		public File[] getFileArray(){
			File folder = this.getDefaultFolder();
			FileFilter fileFilter = FileToolkit.createExtensionListFileFilter(new String[]{"xml"}, false);
			return folder.listFiles(fileFilter);
		}
		
		public void addItemToList(Object o){
			if(this==recipe){ Workbench.current.recipeListManager.add((Recipe)o); }
//			else if(this==sideFilter){ Workbench.current.sideFilterListManager.add((SIDEFilter)o); }
			else if(this==featureTable){ Workbench.current.featureTableListManager.add((FeatureTable)o); }
			else if(this==trainingResult){ Workbench.current.trainingResultListManager.add((TrainingResult)o); }
			else{ throw new UnsupportedOperationException(); }
		}
		
		public ListManager<?> getListManager(){
			if(this==recipe){ return Workbench.current.recipeListManager; }
//			else if(this==sideFilter){ return Workbench.current.sideFilterListManager; }
			else if(this==featureTable){ return Workbench.current.featureTableListManager; }
			else if(this==trainingResult){ return Workbench.current.trainingResultListManager; }
			else if(this==summary){ return Workbench.current.summaryListManager; }
			else{ throw new UnsupportedOperationException(); }
		}
		
		public void loadFileArray(File[] fileArray){
			this.getListManager().loadFromFileArray(fileArray);
		}
	}
	

	private static String addSlashesToFileSeparator(String s) {
		if (s.equals("\\")) {
			return "\\\\";
		}
		return s;
	}

	public static String fileSeparatorFromSIDEToPlatform(String path) {
		if (PLATFORM_FILE_SEPARATOR.equals(SIDE_FILE_SEPARATOR)) {
			return path;
		}

		String quotedPlatformSeparator = addSlashesToFileSeparator(PLATFORM_FILE_SEPARATOR);
		String returnString = path.replaceAll(SIDE_FILE_SEPARATOR,
				quotedPlatformSeparator);
		return returnString;
	}

	public static String fileSeparatorFromPlatformToJAR(String path) {
		if (PLATFORM_FILE_SEPARATOR.equals(JAR_PATH_FILE_SEPERATOR)) {
			return path;
		}

		String quotedPlatformSeparator = addSlashesToFileSeparator(PLATFORM_FILE_SEPARATOR);
		String returnString = path.replaceAll(quotedPlatformSeparator,
				JAR_PATH_FILE_SEPERATOR);
		return returnString;
	}

	public static String fileSeparatorFromPlatformToSIDE(String path) {
		if (PLATFORM_FILE_SEPARATOR.equals(SIDE_FILE_SEPARATOR)) {
			return path;
		}

		String quotedPlatformSeparator = addSlashesToFileSeparator(PLATFORM_FILE_SEPARATOR);
		String returnString = path.replaceAll(quotedPlatformSeparator,
				SIDE_FILE_SEPARATOR);
		return returnString;
	}

	static public String BASE_PATH = rootFolder.getAbsolutePath()
			+ PLATFORM_FILE_SEPARATOR;
	static public String SIDECORE_PATH = BASE_PATH + "SIDE core"
			+ PLATFORM_FILE_SEPARATOR;
//	public static getAutorun
//	static public String CONFIG_PATH = BASE_PATH + "config"
//			+ PLATFORM_FILE_SEPARATOR;
	static public String SIDE_WORKSPACE_PATH = BASE_PATH + "side workspace"
			+ PLATFORM_FILE_SEPARATOR;

	static public String SFM_INIT_FILE = BASE_PATH + "SFMInit.xml";
	static public File PLUGIN_FOLDER = new File(BASE_PATH, "plugins");
	static public String BLANK_CATALOG_FILE = "<catalog></catalog>";

	static public String RESOURCE_URL = "";

	static public String GOOD_PLUGIN_ICON = RESOURCE_URL + "images"
			+ PLATFORM_FILE_SEPARATOR + "pluginWrapper.png";
	static public String BAD_PLUGIN_ICON = RESOURCE_URL + "images"
			+ PLATFORM_FILE_SEPARATOR + "plugin_error.png";
	static public String PLUGIN_FOLDER_ICON = RESOURCE_URL + "images"
			+ PLATFORM_FILE_SEPARATOR + "plugin_folder.png";
	static public String JTREE_BAD_PLUGIN_CONFIG_ICON = RESOURCE_URL + "images"
			+ PLATFORM_FILE_SEPARATOR + "jtree_bad_plugin_config.png";
	static public String JTREE_BAD_PLUGIN_ICON = RESOURCE_URL + "images"
			+ PLATFORM_FILE_SEPARATOR + "jtree_bad_plugin.png";
	static public String JTREE_PLUGIN_ICON = RESOURCE_URL + "images"
			+ PLATFORM_FILE_SEPARATOR + "jtree_plugin_icon.png";

	// SHOULD NOT CHANGE '/' below to FILE_SEPARATOR since they are not
	// FILE_SEPERATOR but classpath
	static public String OK_ICON = RESOURCE_URL + "images/ok.png";
	static public String FILTER_ICON = RESOURCE_URL + "images/filter.png";
	static public String EXPRESSION_ICON = RESOURCE_URL
			+ "images/expression.png";
	static public String BLANK_ICON = RESOURCE_URL + "images/tshim.gif";
	static public String LOADING_ICON = RESOURCE_URL + "images/loading.gif";
	static public String BAD_FILE_ICON = RESOURCE_URL + "images/bad_file.png";
	static public String SUMMARY_ICON = RESOURCE_URL + "images/summary.png";
	static public String FILE_ICON = RESOURCE_URL + "images/xmiFile.png";
	static public String OPEN_ICON = RESOURCE_URL + "images/open.png";
	static public String OPEN_ICON_DISABLED = RESOURCE_URL
			+ "images/open_disabled.png";
	static public String STOP_ICON = RESOURCE_URL + "images/stopIcon.png";
	static public String WARNING_ICON = RESOURCE_URL + "images/warningIcon.png";
	static public String FOLDER_OPEN_ICON = RESOURCE_URL
			+ "images/folder_open.png";
	static public String FOLDER_CLOSED_ICON = RESOURCE_URL
			+ "images/folder_closed.png";
	static public String ADD_ICON = RESOURCE_URL + "images/add.png";
	static public String ADD_ICON_DISABLED = RESOURCE_URL
			+ "images/adddisabled.png";
	static public String SAVE_ICON = RESOURCE_URL + "images/save.png";
	static public String SAVE_ICON_DISABLED = RESOURCE_URL
			+ "images/savedisabled.png";
	static public String REMOVE_ICON = RESOURCE_URL + "images/remove.png";
	static public String DROP_DOWN_ICON = RESOURCE_URL + "images/dropdown.png";
	static public String REFRESH_ICON = RESOURCE_URL + "images/refresh.png";
	static public String DELETE_ICON = RESOURCE_URL + "images/delete.png";
	static public String DELETE_ICON_DISABLED = RESOURCE_URL
			+ "images/deletedisabled.png";
	static public String ADD_FOLDER_ICON = RESOURCE_URL
			+ "images/add_folder.png";
	static public String ADD_FOLDER_ICON_DISABLED = RESOURCE_URL
			+ "images/add_folder_disabled.png";
	static public String ADD_FILTER_ICON = RESOURCE_URL
			+ "images/add_filter.png";
	static public String ADD_SUMMARY_ICON = RESOURCE_URL
			+ "images/add_summary.png";
	static public String ADD_EXPRESSION_ICON = RESOURCE_URL
			+ "images/add_expression.png";
	static public String ADD_TEXT_ICON = RESOURCE_URL
			+ "images/add_document.png";
	static public String PLUGIN_ICON = RESOURCE_URL + "images/plugin_icon.png";
	static public String CONFIGURE_ICON = RESOURCE_URL + "images/configure.png";
	static public String TAB_ICON_SAVE = RESOURCE_URL
			+ "images/savetabicon.png";
	static public String TAB_ICON_SAVE_DISABLED = RESOURCE_URL
			+ "images/savetabicondisabled.png";
	static public String TAB_ICON_CLOSE = RESOURCE_URL
			+ "images/closetabicon.png";
	static public String TAB_ICON_CLOSE_DISABLED = RESOURCE_URL
			+ "images/closetabicondisabled.png";
	static public String COLOR_PICKER_IMAGE = RESOURCE_URL
			+ "images/colorpicker.png";

	public static File imageFolder = new File(rootFolder, "images"); 
	public static File dropdownImageFile = new File(imageFolder, "dropdown.png");
	public static File deleteImageFile = new File(imageFolder, "delete.png");
	public static File addImageFile = new File(imageFolder, "add.png");

	

	public static final int CMD_ADD_FOLDER = 1;
	public static final int CMD_ADD_FILE = 2;
	public static final int CMD_CONFIGURE = 3;
	public static final int CMD_DELETE = 4;
	public static final int CMD_PLUGIN_MANAGER = 5;
	public static final int CMD_SWITCH_WORKSPACES = 6;
	public static final int CMD_CLOSE_ALL = 7;
	public static final int CMD_CLOSE = 8;
	public static final int CMD_SAVE = 9;
	public static final int CMD_ACTIVATE = 10;
	public static final int CMD_SAVEALL = 11;
	public static final int CMD_NEW = 12;
	public static final int CMD_NEW_EXPRESSION = 13;
	public static final int CMD_NEW_FILTER = 14;
	public static final int CMD_NEW_SUMMARY = 15;
	public static final int CMD_NEW_DOCUMENT = 16;
	public static final int CMD_RENAME = 17;
	public static final int CMD_DUPLICATE = 18;
	public static final int CMD_DELETE_ALL = 19;
	public static final int CMD_REFRESH = 20;
	public static final int CMD_NEW_WORKSPACE = 21;
	public static final int CMD_IMPORT = 22;
	public static final int CMD_QUIT = 23;
	public static final int CMD_IMPORT_CSV = 24;

	// Message icon codes for the Ask... methods
	public static final int ASK_ICON_ERROR = JOptionPane.ERROR_MESSAGE;
	public static final int ASK_ICON_INFO = JOptionPane.INFORMATION_MESSAGE;
	public static final int ASK_ICON_WARNING = JOptionPane.WARNING_MESSAGE;
	public static final int ASK_ICON_QUESTION = JOptionPane.QUESTION_MESSAGE;
	public static final int ASK_ICON_NONE = JOptionPane.PLAIN_MESSAGE;

	// Result codes returned by Ask... methods
	public static final int ASK_RESULT_YES = JOptionPane.YES_OPTION;
	public static final int ASK_RESULT_NO = JOptionPane.NO_OPTION;
	public static final int ASK_RESULT_CANCEL = JOptionPane.CANCEL_OPTION;
	public static final int ASK_RESULT_OK = JOptionPane.OK_OPTION;
	public static final int ASK_RESULT_CLOSED = JOptionPane.CLOSED_OPTION;

	public static final Charset defaultCharset = Charset.defaultCharset();

	public static final Random random = new Random();

	public static final String titleLaunchPanel = "SIDE";
	
	public static javax.swing.JDialog CenterDialog(javax.swing.JDialog dialog) {
		java.awt.Dimension dim = dialog.getToolkit().getScreenSize();
//		java.awt.Rectangle abounds = dialog.getBounds();
		dialog.setLocation((dim.width - dialog.getWidth()) / 2,
				(dim.height / 3) - (dialog.getHeight() / 2));
		return dialog;
	}

	public static javax.swing.JFrame CenterWindow(javax.swing.JFrame window) {
		java.awt.Dimension dim = window.getToolkit().getScreenSize();
//		java.awt.Rectangle abounds = window.getBounds();
		window.setLocation((dim.width - window.getWidth()) / 2,
				(dim.height / 3) - (window.getHeight() / 2));
		return window;
	}

	public static String HistoryFile() {
		StringBuffer contents = new StringBuffer();
		File temp = new File("history.txt");
		BufferedReader input = null;
		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			input = new BufferedReader(new FileReader(temp));
			String line = null; // not declared within while loop
			/*
			 * readLine is a bit quirky : it returns the content of a line MINUS
			 * the newline. it returns null only for the END of the stream. it
			 * returns an empty String if two newlines appear in a row.
			 */
			while ((line = input.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
			input.close();
		} catch (Exception e) {
		}

		return contents.toString();
	}

	public static String DateToString(Date inDate) {
		SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
		String sDate = df.format(inDate);
		return sDate;
	}

	public static Date DateFromString(String inString) {
		Date result = null;
		try {
			SimpleDateFormat df = new SimpleDateFormat(
					"E, dd MMM yyyy HH:mm:ss");
			result = df.parse(inString);
		} catch (Exception e) {
			System.out.println(e.getMessage());

		}

		return result;
	}

	public static boolean RBSelected(JRadioButton btn) {
		DefaultButtonModel model = (DefaultButtonModel) btn.getModel();
		return model.getGroup().isSelected(model);
	}


	public static int AskYesNo(String theMessage, int messageType) {
		int result = JOptionPane.showConfirmDialog((Component) null,
				theMessage, "alert", JOptionPane.YES_NO_OPTION, messageType);
		return result;
	}

	public static int AskYesNoCancel(String theMessage, int messageType) {
		int result = JOptionPane.showConfirmDialog((Component) null,
				theMessage, "alert", JOptionPane.YES_NO_CANCEL_OPTION,
				messageType);
		return result;
	}

	public static int AskOKCancel(String theMessage, int messageType) {
		int result = JOptionPane.showConfirmDialog((Component) null,
				theMessage, "alert", JOptionPane.OK_CANCEL_OPTION, messageType);
		return result;
	}

	public static int AskOK(String theMessage, int messageType) {
		int result = JOptionPane.showConfirmDialog((Component) null,
				theMessage, "alert", JOptionPane.DEFAULT_OPTION, messageType);
		return result;
	}

	public static void ShowErrorMessage(String message) {
		// (new Exception()).printStackTrace();
		JOptionPane.showMessageDialog((Component) null, message,
				"SIDE Alert (error)", SIDEToolkit.ASK_ICON_ERROR);
	}

	public static void ShowWarningMessage(String message) {
		// (new Exception()).printStackTrace();
		JOptionPane.showMessageDialog((Component) null, message,
				"SIDE Alert (warning)", SIDEToolkit.ASK_ICON_WARNING);
	}

	public static void ShowInfoMessage(String message) {
		// (new Exception()).printStackTrace();
		JOptionPane.showMessageDialog((Component) null, message,
				"SIDE Alert (info)", SIDEToolkit.ASK_ICON_INFO);
	}

	public static boolean isDebugMode() {
		return true;
	}

	public static File getUserSelectedFile(File folder, String filename){
		JFileChooser fileChooser = new JFileChooser();
		File loadFolder = Workbench.current.getLoadFolder();
		loadFolder = (loadFolder==null)?folder:loadFolder;
		
		File htmlFile = new File(loadFolder, filename);
		fileChooser.setSelectedFile(htmlFile);
		
		int result = fileChooser.showSaveDialog(null);
		if(result!=JFileChooser.APPROVE_OPTION){
			return null;
		}
		File selectedFile = fileChooser.getSelectedFile();
		return selectedFile;
	}
	
	public static void cleanupSaveFiles(){
		File[] folderArray = new File[]{
				SIDEToolkit.FileType.featureTable.getDefaultFolder(),
//				SIDEToolkit.FileType.sideFilter.getDefaultFolder(),
				SIDEToolkit.FileType.trainingResult.getDefaultFolder(),
				SIDEToolkit.FileType.recipe.getDefaultFolder(),
				SIDEToolkit.FileType.summary.getDefaultFolder(),
				SIDEToolkit.xmiFolder,
				SIDEToolkit.predictionResultFolder,
				SIDEToolkit.etcFolder,
		};
		
		for(File folder : folderArray){
			for(File file : folder.listFiles()){
				if(file.isFile()){ file.delete(); }
			}
		}
	}
	
	public static void testAll(){
		File docFile = SIDEToolkit.csvFolder.listFiles(FileToolkit.createExtensionListFileFilter(new String[]{"csv"}, false))[0];
		throw new UnsupportedOperationException();
	}

	public static File sentenceDetectorModelFile = new File(SIDEToolkit.opennlpModelsFolder, "EnglishSD.bin.gz");
	private static SentenceDetector sentenceDetector = null;
	public static SentenceDetector getSentenceDetector() {
		if(sentenceDetector!=null){ return sentenceDetector; }
		
		try {
			sentenceDetector = new SentenceDetector(sentenceDetectorModelFile.getCanonicalPath());
			return sentenceDetector;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static File posTaggerModelFile = new File(SIDEToolkit.opennlpModelsFolder, "tag.bin.gz");
	private static File tagDictionary = new File(SIDEToolkit.opennlpModelsFolder, "tagdict.txt");
	private static PosTagger posTagger = null;
	public static PosTagger getPosTagger(){
		if(posTagger!=null){ return posTagger; }
		try {
			posTagger = new PosTagger(posTaggerModelFile.getCanonicalPath(), new POSDictionary(tagDictionary.getCanonicalPath()));
			return posTagger;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static class SIDESegmentComparatorByDocumentList implements Comparator<SIDESegment>{
		private DocumentList documentList;
		public SIDESegmentComparatorByDocumentList(DocumentList documentList){
			this.documentList = documentList;
		}
		@Override
		public int compare(SIDESegment o1, SIDESegment o2) {
			int index1 = documentList.getJCasIndex(UIMAToolkit.getJCas(o1.getCAS()));
			int index2 = documentList.getJCasIndex(UIMAToolkit.getJCas(o2.getCAS()));
			
			if(index1==-1 || index2==-1){ throw new UnsupportedOperationException(); }
			
			if(index1!=index2){
				return index1-index2;
			}
			
			return o1.getBegin()-o2.getBegin();
		}
	}
	
	public static Comparator<? super SIDESegment> getSIDESegmentComparator(
			DocumentList documentList) {
		return new SIDESegmentComparatorByDocumentList(documentList);
		
	}
	
	public static String allowedCharPatternString = ColorLabelConfigPanel.allowedCharPatternString;
	public static Pattern allowedStringPattern = Pattern.compile(allowedCharPatternString+"*");
	public static String unallowedCharPatternString = allowedCharPatternString.charAt(0)+"^"+allowedCharPatternString.substring(1);
	public static char replaceCharacter = '_';
	public static String getLegalLabelString(String rawString){
		if(rawString==null){ return null; }
		
		String trimmedString = rawString.trim();
		if(trimmedString.length()==0){ return null; }
		return trimmedString.replaceAll(unallowedCharPatternString, Character.toString(replaceCharacter));
	}
}


