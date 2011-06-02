/*
 * MLAPluginConfigPanel.java
 *
 * Created on __DATE__, __TIME__
 */

package edu.cmu.side.ui.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import se.datadosen.component.RiverLayout;
import com.yerihyo.yeritools.CalendarToolkit;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.InfoButton;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;
import com.yerihyo.yeritools.swing.SwingToolkit.SizeType;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;
import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.plugin.MLAPlugin;
import edu.cmu.side.plugin.PluginWrapper;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.SegmenterPlugin;
import edu.cmu.side.ui.managerpanel.TrainingResultManagerPanel;
import edu.cmu.side.uima.UIMAToolkit.Datatype;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;

/**
 * 
 * @author __USER__
 */
public class MLAPluginConfigPanel extends javax.swing.JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;

	private MLAPlugin[] mlaPluginArray = new MLAPlugin[0];
	private TrainTask task = null;

	/** Creates new form MLAPluginConfigPanel */
	public MLAPluginConfigPanel() {
		yeriInit();
	}

	public MLAPlugin getSelectedMLAPlugin() {
		int index = this.mlaPluginCombobox.getSelectedIndex();
		if (index < 0 || index >= mlaPluginArray.length) {
			return null;
		}

		return mlaPluginArray[index];
	}
	
	@Override
	public void actionPerformed(ActionEvent e) { this.refreshPanel(); }
	
	protected void setDatatype(Datatype datatype){
		buttonArray[datatype.ordinal()].setSelected(true);
	}
	protected Datatype getDatatype(){
		for(int i=0; i<this.buttonArray.length; i++){
			if(this.buttonArray[i].isSelected()){ return Datatype.values()[i]; }
		}
		throw new UnsupportedOperationException();
	}
	
	private static String[] segmenterPluginInfoContentArray = new String[]{
		"choosing a segmenter is only relevant for summarization.",
		"choose a dummy value if you are interested in machine learning only.",
		"metafeatures are not compatible with summarization.",
		"leave that box unchecked if you are performing summarization."
	};
	
	private void yeriInit() {
		// leftPanel
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new RiverLayout());
		
		leftPanel.add("hfill", new JLabel("Machine learning plugin:"));
		
		mlaPluginCombobox = new JComboBox();
		leftPanel.add("br hfill", mlaPluginCombobox);
		
		mlaPanel = new JPanel();
		mlaPanel.setLayout(new BorderLayout());
		leftPanel.add("br hfill", mlaPanel);
		
		leftPanel.add("br hfill", new JSeparator());
		
		featureTableComboBox = Workbench.current.featureTableListManager.createComboBox();
		leftPanel.add("p", Workbench.current.featureTableListManager.createImageIconLabel());
		leftPanel.add(" ", new JLabel("feature table:"));
		leftPanel.add("hfill", featureTableComboBox);
//		featureTableManagerPanel = new FeatureTableManagerPanel();
//		leftPanel.add("br hfill", featureTableManagerPanel);
		
		featureTableComboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				MLAPluginConfigPanel.this.setDatatype(Datatype.AUTO);
				refreshPanel();
			}
		});
		
		datatypeLabel = new JLabel();
		leftPanel.add("br", datatypeLabel);
		
		ButtonGroup datatypeButtonGroup = new ButtonGroup();
		buttonArray = new JRadioButton[]{
				new JRadioButton("auto"),
				new JRadioButton("nominal"),
				new JRadioButton("numeric"),
		};
		for(int i=0; i<buttonArray.length; i++){
			JRadioButton radioButton = buttonArray[i];
			datatypeButtonGroup.add(radioButton);
			leftPanel.add("", radioButton);
			radioButton.addActionListener(MLAPluginConfigPanel.this);
		}
		
		crossValidationCheckBox = new JRadioButton("cross-validation:");
		crossValidationCheckBox.setSelected(true);
		crossValidationCheckBox.addActionListener(MLAPluginConfigPanel.this);
		suppliedRadioButton = new JRadioButton("supplied test set:");
		ButtonGroup testGroup = new ButtonGroup();
		testGroup.add(crossValidationCheckBox);
		testGroup.add(suppliedRadioButton);
		cvFoldField = new JTextField();
		cvFoldField.setText("10");
		leftPanel.add("br", crossValidationCheckBox);
		leftPanel.add("hfill", cvFoldField);
		
		foldButton = new JRadioButton("fold");
		foldButton.setSelected(true);
		leaveOutButton = new JRadioButton("leave-out");
		ButtonGroup group = new ButtonGroup();
		group.add(foldButton);
		group.add(leaveOutButton);
		filenameLabel = new JLabel(" ");
		filenameLabel.setBorder(BorderFactory.createLineBorder(SystemColor.gray));
		final JButton loadFileButton = new JButton("select");
		loadFileButton.setMnemonic(KeyEvent.VK_L);
		loadFileButton.setEnabled(false);
		loadFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				
				File loadFolder = Workbench.current.getLoadFolder();
				chooser.setCurrentDirectory(loadFolder==null?SIDEToolkit.xmiFolder:loadFolder);
				chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"xmi"}, true));
				int selection = chooser.showOpenDialog(MLAPluginConfigPanel.this);
				if(selection!=JFileChooser.APPROVE_OPTION){
					return;
				}
				suppliedTestFile = chooser.getSelectedFile();
				refreshPanel();
			}
		});
		foldButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				cvFoldField.setText("10");
				loadFileButton.setEnabled(false);
			}
		});
		leaveOutButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				cvFoldField.setText("set");
				loadFileButton.setEnabled(false);
			}
		});
		suppliedRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				loadFileButton.setEnabled(true);
			}
		});

		leaveOutButton.setEnabled(false);
		leftPanel.add("right", foldButton);
		leftPanel.add("right", leaveOutButton);
		SwingToolkit.checkWithoutAction(buttonArray[0], new ActionListener[]{MLAPluginConfigPanel.this}, true);
		leftPanel.add("br hfill", suppliedRadioButton);
		leftPanel.add("left hfill", filenameLabel);
		leftPanel.add("p hfill", loadFileButton);
		
		leftPanel.add("p hfill", new JSeparator());
		
		segmenterComboBox = new JComboBox();
		segmenterComboBox.setRenderer(SwingToolkit.createClassNameRenderer(segmenterComboBox.getRenderer()));
		SIDEPlugin[] pluginArray = Workbench.current.pluginManager.getPluginCollectionByType(SegmenterPlugin.type).toArray(new SIDEPlugin[0]);
		SwingToolkit.reloadComboBoxContent(segmenterComboBox, pluginArray, null, false);
//		segmenterComboBox.setSelectedIndex(1);
		InfoButton infoButton = InfoButton.create(segmenterPluginInfoContentArray);
		SwingToolkit.setSize(infoButton, new Dimension(16,16), SizeType.values());
		leftPanel.add("br left", new JLabel("summarization help: "));
		leftPanel.add("tab", infoButton);
		
		leftPanel.add("p ", new JLabel("default segmenter: "));
		leftPanel.add("br hfill", segmenterComboBox);
		leftPanel.add("br hfill", new JSeparator());
		
		trainingResultNameTextField = new JTextField();
		trainingResultNameTextField.setText("trained_model");
		leftPanel.add("p ", new JLabel("New model name:"));
		leftPanel.add("hfill", trainingResultNameTextField);
		
		metaFeaturesCheckBox = new JCheckBox("Use Metafeatures");
		metaFeaturesCheckBox.setSelected(false);
		leftPanel.add("br hfill", metaFeaturesCheckBox);
		
		trainButton = new JButton("train model");
		trainButton.setMnemonic(KeyEvent.VK_T);
		leftPanel.add("p hfill", trainButton);
		
		progressBar = new JProgressBar();
		leftPanel.add("br hfill", progressBar);
		
		leftPanel.add("p hfill", new JSeparator());
		
		trainingResultManagerPanel = new TrainingResultManagerPanel();
		trainingResultManagerPanel.addActionListener(MLAPluginConfigPanel.this);
		trainingResultManagerPanel.addDoubleClickListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				TrainingResult trainingResult = (TrainingResult)e.getSource();
				if(trainingResult==null){ return; }
				
				int result = JOptionPane.showConfirmDialog(trainingResultManagerPanel, "load settings of this model?", "load", JOptionPane.YES_NO_OPTION);
				if(result!=JOptionPane.YES_OPTION){ return; }
				
				MLAPluginConfigPanel configPanel = MLAPluginConfigPanel.this;
				configPanel.loadTrainingResult(trainingResult);
			}
		});
		
		leftPanel.add("br hfill", trainingResultManagerPanel);
		
		// right Panel
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new RiverLayout());
		
		trainingResultViewPanel = new TrainingResultViewPanel();
		rightPanel.add("hfill vfill", trainingResultViewPanel);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(new JScrollPane(leftPanel));
		splitPane.setRightComponent(rightPanel);
		
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
		
		
		SIDEPlugin[] mlaSIDEPluginArray = Workbench.current.pluginManager.getSIDEPluginArrayByType(MLAPlugin.type);
		MLAPlugin[] mlaPluginArray = Arrays.copyOf(mlaSIDEPluginArray, mlaSIDEPluginArray.length, (new MLAPlugin[0]).getClass());
		this.setMLAPluginArray(mlaPluginArray);
		
		mlaPluginCombobox.addActionListener(MLAPluginConfigPanel.this);
		this.trainButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				task = new TrainTask(progressBar);
				task.execute();
			}
		});
		int selectedIndex = mlaPluginCombobox.getSelectedIndex();
		if(selectedIndex<0){ return; }
		
		MLAPlugin selectedMLAPlugin = mlaPluginArray[selectedIndex];
		Component mlConfigUI = selectedMLAPlugin.getConfigurationUI();
		if (mlConfigUI != null) {
			mlaPanel.add(mlConfigUI, BorderLayout.CENTER);
		}
		this.refreshPanel();
	}
	
	
	protected void loadTrainingResult(TrainingResult trainingResult){
		if(trainingResult==null){ return; }

		MLAPlugin mlaPlugin = trainingResult.getMlaPlugin();
		
		/** TODO : set MLAPlugin and FeatureTable **/
//		this.featureTableComboBox.
		FeatureTable featureTable = trainingResult.getFeatureTable();
		if(!SwingToolkit.hasItem(this.featureTableComboBox, featureTable)){
			Workbench.current.featureTableListManager.add(featureTable);
		}
		SwingToolkit.selectWithoutAction(this.featureTableComboBox, featureTable, new ActionListener[]{this});
		
		
		this.setDatatype(mlaPlugin.getDatatype());
		
		int fold = trainingResult.getFold();
		this.crossValidationCheckBox.setSelected(fold!=0);
		this.cvFoldField.setText(Integer.toString(trainingResult.getFold()));
		
		SwingToolkit.updateSameClassObjectsWithoutAction(this.segmenterComboBox,
				new Object[]{trainingResult.getSegmenterPlugin()}, new ActionListener[]{this});
		this.setName(trainingResult.getName());
		
		this.refreshPanel();
	}
	
	private class TrainTask extends OnPanelSwingTask{
		public TrainTask(JProgressBar progressBar){
			this.addProgressBar(progressBar);
		}

		@Override
		protected Void doInBackground() throws Exception {
            //Initialize progress property.
			
			long start = System.currentTimeMillis();
			train();
			MLAPluginConfigPanel.this.fireActionEvent();
			
			System.out.println("Button click to end: "+CalendarToolkit.durationToString(System.currentTimeMillis()-start));
            return null;
		}
	}
	
	private void train(){
		boolean runnable = true;
		List<CharSequence> messageList = new ArrayList<CharSequence>();
		
		// create FeatureTable		
		String fold;
		if(this.suppliedRadioButton.isSelected()){
			fold = "SUPPLIED;" + suppliedTestFile.getAbsolutePath();
		}else{
			fold = "";
			if(this.leaveOutButton.isSelected()){
				fold += "LEAVEOUT;";
			}
			fold += cvFoldField.getText().trim();
		}
		
		FeatureTable featureTable = (FeatureTable)featureTableComboBox.getSelectedItem();
		featureTable.setUseMetafeatures(metaFeaturesCheckBox.isSelected());
		SegmenterPlugin segmenterPlugin = (SegmenterPlugin)segmenterComboBox.getSelectedItem();
		String trainingResultName = trainingResultNameTextField.getText().trim();
		
		runnable = YeriDebug.updateValidity(runnable, featureTable!=null, messageList, "Feature table not selected!");
		runnable = YeriDebug.updateValidity(runnable, featureTable!=null, messageList, "Feature table not selected!");
		runnable = YeriDebug.updateValidity(runnable, segmenterPlugin!=null, messageList, "Segmenter plugin not selected!");
		runnable = YeriDebug.updateValidity(runnable, trainingResultName.length()>0, messageList, "Please type in name!");
		if(!runnable){
			AlertDialog.show("error", messageList, MLAPluginConfigPanel.this);
			return;
		}
		
		MLAPlugin mlaPlugin = this.getSelectedMLAPlugin();
//		Datatype inferredDatatype = this.getInferredDatatype();
		
		// train
		mlaPlugin.train(featureTable, trainingResultName, segmenterPlugin, fold);
		
	}
	
	private Datatype getInferredDatatype(){
		Datatype selectedDatatype = this.getDatatype();
		
		Object selectedItem = this.featureTableComboBox.getSelectedItem();
		if(selectedItem==null){ return Datatype.NOMINAL; }
//		System.out.println(selectedItem.getClass().getName());
		
		FeatureTable featureTable = (FeatureTable)selectedItem;
		Datatype inferredDatatype = selectedDatatype;
		if(inferredDatatype==Datatype.AUTO){
			DocumentList documentList = featureTable.getDocumentList();
			inferredDatatype = documentList.getInferredDatatype();
		}
		return inferredDatatype;
	}
	
//	private transient Map<String, MLAPlugin> mlaPluginCache = new HashMap<String,MLAPlugin>();
	public void refreshPanel() {
//		refreshSegmenter();
		if(suppliedTestFile == null){
			filenameLabel.setText(" ");
		}else{
			filenameLabel.setText(suppliedTestFile.getName());
		}
		this.cvFoldField.setEnabled(crossValidationCheckBox.isSelected());
		
		Datatype selectedDatatype = this.getDatatype();
		Datatype inferredDatatype = this.getInferredDatatype();
		
		datatypeLabel.setText( (selectedDatatype==Datatype.AUTO?"(infer) ":"") + inferredDatatype.getName()); 
		int selectedIndex = mlaPluginCombobox.getSelectedIndex();
		if(selectedIndex<0){ return; }
		MLAPlugin selectedMLAPlugin = mlaPluginArray[selectedIndex];
		selectedMLAPlugin.setDatatype(inferredDatatype);
		selectedMLAPlugin.getConfigurationUI().repaint();
		TrainingResult trainingResult = trainingResultManagerPanel.getSelectedTrainingResult();
		trainingResultViewPanel.setTrainingResult(trainingResult);
	}

	public static void main(String[] args) {
		test02();
	}
	protected static void test02(){
		SIDEToolkit.FileType.loadAll();
		
		MLAPluginConfigPanel mlaPluginConfigPanel = new MLAPluginConfigPanel();
		mlaPluginConfigPanel.featureTableComboBox.setSelectedIndex(0);
		mlaPluginConfigPanel.mlaPluginCombobox.setSelectedIndex(0);
		mlaPluginConfigPanel.trainingResultNameTextField.setText("b");
		mlaPluginConfigPanel.segmenterComboBox.setSelectedIndex(1);
		
		TestFrame testFrame = new TestFrame(mlaPluginConfigPanel);
		testFrame.setSize(new Dimension(800, 800));
		testFrame.showFrame();
		
		SwingToolkit.fireNullActionEvent(mlaPluginConfigPanel.trainButton);
	}

	
	protected static void test01(){
		MLAPluginConfigPanel mlaPluginConfigPanel = new MLAPluginConfigPanel();
		List<PluginWrapper> list = Workbench.current.pluginManager.getPluginWrapperCollectionByType(MLAPlugin.type);
		
		MLAPlugin[] mlaPluginArray = new MLAPlugin[list.size()];
		for(int i=0; i<list.size(); i++){
			mlaPluginArray[i] = (MLAPlugin)list.get(i).getSIDEPlugin();
		}
		mlaPluginConfigPanel.setMLAPluginArray(mlaPluginArray);

		TestFrame testFrame = new TestFrame(mlaPluginConfigPanel);
		testFrame.setSize(new Dimension(800, 800));
		
		SIDEToolkit.FileType.loadAll();
		testFrame.showFrame();
	}

	private javax.swing.JPanel mlaPanel;
	private javax.swing.JComboBox mlaPluginCombobox;
	
	private JRadioButton crossValidationCheckBox;
	private JRadioButton suppliedRadioButton;
	private JTextField cvFoldField;
	private JRadioButton foldButton;
	private JRadioButton leaveOutButton;
	
	private JCheckBox metaFeaturesCheckBox;
	private JLabel filenameLabel;
	private JComboBox segmenterComboBox;
	private JTextField trainingResultNameTextField;
	private javax.swing.JButton trainButton;
	private JProgressBar progressBar;
	
	private File suppliedTestFile;
	
//	private FeatureTableManagerPanel featureTableManagerPanel;
	private JComboBox featureTableComboBox;
	private TrainingResultManagerPanel trainingResultManagerPanel;
	private TrainingResultViewPanel trainingResultViewPanel;
	
	private JRadioButton[] buttonArray;
	private JLabel datatypeLabel;
//	private JButton saveDatatypeChangeButton;
//	private JComboBox segmenterComboBox;

	private static int actionEventID = 1;
	private List<ActionListener> actionListenerList = new ArrayList<ActionListener>();

	public void addActionListener(ActionListener actionListener) {
		actionListenerList.add(actionListener);
	}

	public void removeActionListener(ActionListener actionListener) {
		actionListenerList.remove(actionListener);
	}

	protected void fireActionEvent() {
		ActionEvent actionEvent = new ActionEvent(this, actionEventID, "train");
		for (ActionListener actionListener : actionListenerList) {
			actionListener.actionPerformed(actionEvent);
		}
	}

	private void setMLAPluginArray(MLAPlugin[] mlaPluginArray) {
		this.mlaPluginArray = mlaPluginArray;
		mlaPluginCombobox.removeActionListener(MLAPluginConfigPanel.this);
		
		String selectedMLAPluginClassName = (String)mlaPluginCombobox.getSelectedItem();
		mlaPluginCombobox.removeAllItems();
		for (MLAPlugin mlaPlugin : mlaPluginArray) {
			this.mlaPluginCombobox.addItem(mlaPlugin.getClass().getName());
		}
		mlaPluginCombobox.setSelectedItem(selectedMLAPluginClassName);
		
		if(mlaPluginCombobox.getSelectedIndex()<0 && mlaPluginCombobox.getItemCount()>0){
			mlaPluginCombobox.setSelectedIndex(0);
		}
		
		mlaPluginCombobox.addActionListener(MLAPluginConfigPanel.this);
		this.refreshPanel();
	}
}
