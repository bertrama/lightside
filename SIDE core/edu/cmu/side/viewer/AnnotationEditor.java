/*
 * AnnotationEditor_new.java
 *
 * Created on __DATE__, __TIME__
 */

package edu.cmu.side.viewer;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.InvalidXMLException;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.ColorLabelConfigPanel;
import com.yerihyo.yeritools.swing.ColorMapperUI;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.ColorLabelConfigPanel.ColorLabel;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;
import com.yerihyo.yeritools.swing.SwingToolkit.SizeType;
import com.yerihyo.yeritools.swing.SwingToolkit.TestDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;
import com.yerihyo.yeritools.text.StringToolkit;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.SegmenterPlugin;
import edu.cmu.side.plugin.VisualizationToolkit.JCasListVisualizationPlugin;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.Datatype;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;
import edu.cmu.side.uima.type.SIDEAnnotation;
import edu.cmu.side.uima.type.SIDEAnnotationSetting;
import edu.cmu.side.uima.type.SIDESegment;

/**
 *
 * @author  __USER__
 */
public class AnnotationEditor extends javax.swing.JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;

	private JCas jCas;

	public AnnotationEditor() {
		yeriInit();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AnnotationEditor.this.refreshPanel();
	}
	
	public void loadXmiFile(File xmiFile){
		try {
			this.setJCas(UIMAToolkit.createSIDECAS(xmiFile).getJCas());
		} catch (Exception ex) {
			ex.printStackTrace();
			AlertDialog.show("Error", "File is not in CAS format", this);
		}
	}
	
	private int getSelectedDatatypeButtonIndex(){
		for(int i=0; i<this.datatypeButtonArray.length; i++){
			if(this.datatypeButtonArray[i].isSelected()){
				return i;
			}
		}
		throw new UnsupportedOperationException();
	}
	private Datatype getSelectedDatatype(){
		return Datatype.values()[this.getSelectedDatatypeButtonIndex()];
	}
	
	private class VisualizationPanel extends JPanel implements ActionListener{
		private static final long serialVersionUID = 1L;

		public VisualizationPanel(){
			yeriInit();
		}
		
		private JComboBox visualizationPluginComboBox;
		private JPanel jFreeChartPanel;
		
		private void yeriInit(){
			this.setLayout(new BorderLayout());
			
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			this.add(splitPane, BorderLayout.CENTER);
			
			List<SIDEPlugin> pluginList = Workbench.current.pluginManager.getPluginCollectionByType(JCasListVisualizationPlugin.type);
			visualizationPluginComboBox = SwingToolkit.createClassNameComboBox(pluginList.toArray());
			visualizationPluginComboBox.addActionListener(VisualizationPanel.this);
			
			JPanel topPanel = new JPanel(new RiverLayout());			
			topPanel.add("", new JLabel("visualization plugins:"));
			topPanel.add("tab hfill", visualizationPluginComboBox);
			
			jFreeChartPanel = new JPanel(new BorderLayout());
			splitPane.setTopComponent(topPanel);
			splitPane.setBottomComponent(jFreeChartPanel);
		}
		
		public void refreshPanel(){
			boolean runnable = true;
			List<CharSequence> messageList = new ArrayList<CharSequence>();
			
			JCasListVisualizationPlugin plugin = (JCasListVisualizationPlugin)visualizationPluginComboBox.getSelectedItem();
			runnable = YeriDebug.updateValidity(runnable, plugin!=null, messageList, "select visualization plugin");
			
			String subtype = AnnotationEditor.this.getSubtypeName();
			runnable = YeriDebug.updateValidity(runnable, subtype!=null, messageList, "select subtype name");
			
			JCas jCas = AnnotationEditor.this.jCas;
			runnable = YeriDebug.updateValidity(runnable, jCas!=null, messageList, "load xmi file");
			
			
			jFreeChartPanel.removeAll();
			Component c = null;
			if(!runnable){
				c = AlertDialog.createMessagePanel(messageList);
			}else{
				c = plugin.buildVisualizationPanel(new JCas[]{jCas});
				plugin.setSubtypeName(subtype);
				plugin.setSubtypeChangeEnabled(false);
			}
			jFreeChartPanel.add(c, BorderLayout.CENTER);
			jFreeChartPanel.revalidate();
			jFreeChartPanel.repaint();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			VisualizationPanel.this.refreshPanel();
		}
	}
	
	private void yeriInit() {
		leftPanel = new JPanel();
		leftPanel.setLayout(new RiverLayout());
		
		filenameLabel = new JLabel(" ");
		filenameLabel.setBorder(BorderFactory.createLineBorder(SystemColor.gray));
		leftPanel.add("left hfill", filenameLabel);
		loadFileButton = new JButton("load file");
		loadFileButton.setMnemonic(KeyEvent.VK_L);
		leftPanel.add("p hfill", loadFileButton);
		loadProgressBar = new JProgressBar();
		leftPanel.add("br hfill", loadProgressBar);
		
		leftPanel.add("p hfill", new JSeparator());
		
		annotationComboBox = new JComboBox();
		this.annotationComboBox.addActionListener(AnnotationEditor.this);
		leftPanel.add("p hfill", annotationComboBox);
		
		
		datatypeButtonArray = new JRadioButton[]{
				new JRadioButton("auto"),
				new JRadioButton("nominal"),
				new JRadioButton("numeric"),
		};
		
		datatypeButtonActionListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				AnnotationEditor ae = AnnotationEditor.this;
				String subtypeName = ae.getSubtypeName();
				if(subtypeName==null){ return; }
				
				SIDEAnnotationSetting setting = UIMAToolkit.getSIDEAnnotationSetting(jCas, subtypeName);
				setting.setDatatypeString(ae.getSelectedDatatype().toString());
			}
		};
		
		ButtonGroup datatypeButtonGroup = new ButtonGroup();
		JPanel datatypePanel = new JPanel();
		datatypePanel.setLayout(new BoxLayout(datatypePanel, BoxLayout.X_AXIS));
		for(JRadioButton radioButton : datatypeButtonArray){
			datatypeButtonGroup.add(radioButton);
			datatypePanel.add(radioButton);
			radioButton.addActionListener(datatypeButtonActionListener);
		}
		datatypeButtonArray[0].setSelected(true);
		
		leftPanel.add("br hfill", datatypePanel);
		
		newLayerButton = new JButton("new annotation scheme");
		newLayerButton.setMnemonic(KeyEvent.VK_N);
		leftPanel.add("br hfill", newLayerButton);
		deleteLayerButton = new JButton("delete annotation scheme (x)");
		deleteLayerButton.setMnemonic(KeyEvent.VK_X);
		leftPanel.add("br hfill", deleteLayerButton);
		
		leftPanel.add("p hfill", new JSeparator());
		
		colorMapperUI = new ColorMapperUI();
		colorMapperUI.setGenerateButtonVisible(false);
		leftPanel.add("p hfill vfill", colorMapperUI);
		
		leftPanel.add("p hfill", new JSeparator());
		
		trainingResultComboBox = Workbench.current.trainingResultListManager.createComboBox();
		leftPanel.add("br", Workbench.current.trainingResultListManager.createImageIconLabel());
		leftPanel.add(" ", new JLabel("model:"));
		leftPanel.add("hfill", trainingResultComboBox);
		
		trainingResultComboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox trainingResultComboBox = (JComboBox)e.getSource();
				TrainingResult trainingResult = (TrainingResult)trainingResultComboBox.getSelectedItem();
				if(trainingResult==null){ return; }
				
//				DocumentList documentList = new DocumentList(AnnotationEditor.this.getSubtypeName());
//				documentList
				
				String desiredPredictionSubtypeName = UIMAToolkit.getRecommendedPredictionAnnotationSubtypeName(trainingResult, null);
				predictSubtypeNameField.setText(desiredPredictionSubtypeName);
				
				AnnotationEditor.this.refreshPanel();
			}
		});
		
		predictionSegmentationComboBox = new JComboBox(new String[]{
				"use current segmentation",
				"use model segmentation"});
		leftPanel.add("br ", new JLabel("segmentation:"));
		leftPanel.add("hfill", predictionSegmentationComboBox);
		predictionSegmentationComboBox.addActionListener(AnnotationEditor.this);
		
		annotateNullLabelSegmentsOnlyCheckBox = new JCheckBox("annotate empty segments only?");
		annotateNullLabelSegmentsOnlyCheckBox.setSelected(false);
		annotateNullLabelSegmentsOnlyCheckBox.addActionListener(this);
		leftPanel.add("br hfill", annotateNullLabelSegmentsOnlyCheckBox);
		
		String[] annotateNullInfoMessage = new String[]{
				"'annotate null segments only' enables only when...",
				"segmentation is 'use current segment'",
		};
		leftPanel.add("", SwingToolkit.InfoButton.create(annotateNullInfoMessage));
		

		predictSubtypeNameField = new JTextField();
		leftPanel.add("br", new JLabel("annotation name:"));
		leftPanel.add("hfill", predictSubtypeNameField);
		
		predictAnnotationButton = new JButton("annotate using model");
		predictAnnotationButton.setMnemonic(KeyEvent.VK_M);
		predictAnnotationButton.addActionListener(new ActionListener(){
			public String toString(){ return "predictAnnotationButton ActionListener"; }
			@Override
			public void actionPerformed(ActionEvent e) {
				
				boolean runnable = true;
				StringBuilder comment = new StringBuilder();
				
				TrainingResult trainingResult = (TrainingResult)trainingResultComboBox.getSelectedItem();
				if(trainingResult==null){
					runnable = false;
					comment.append("No model selected");
				}
				
				int baseSubtypeIndex = predictionSegmentationComboBox.getSelectedIndex();
				String subtypeName = getSubtypeName();
				if(baseSubtypeIndex==0 && subtypeName==null){
					runnable = false;
					comment.append("No subtype selected above");
				}

				if(!runnable){
					JOptionPane.showMessageDialog(AnnotationEditor.this,
							comment,
							"error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// run!!
				DocumentList documentList;
				if(baseSubtypeIndex==0){
					documentList = new DocumentList(subtypeName);
				}else{
//					String trainingResultBaseSubtypeName = trainingResult.getDocumentList().getBaseSubtypeName();
					SegmenterPlugin segmenterPlugin = trainingResult.getSegmenterPlugin();
					String baseSubtypeName;
					try {
						baseSubtypeName = UIMAToolkit.segmentJCas(jCas, segmenterPlugin);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
					System.out.println(StringToolkit.toString(UIMAToolkit.getSubtypeNameSet(jCas, SIDESegment.type).toArray(new String[0]), ","+StringToolkit.newLine()));
					documentList = new DocumentList(baseSubtypeName);
				}
				
				documentList.getJCasList().add(jCas);
//				String baseSubtypeName = documentList.getBaseSubtypeName();
				String desiredSubtypeName = getDesiredPredictionSubtypeName();
//					UIMAToolkit.getRecommendedPredictionAnnotationSubtypeName(trainingResult, baseSubtypeName);
					
				
				boolean override = !AnnotationEditor.this.isAnnotateNullSegmentOnlyEnabled();
				JCas[] jCasArray = documentList.getJCasList().toArray(new JCas[0]);
				if(override && !UIMAToolkit.overrideAndContinueSubtype(jCasArray, SIDEAnnotation.type, desiredSubtypeName, null)){
					JOptionPane.showMessageDialog(null, "Prediction stopped by user", "Message", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				UIMAToolkit.addPredictionAnnotation(trainingResult, documentList, desiredSubtypeName, override);
//				System.out.println("Desired subtype: "+desiredSubtypeName);

				Set<String> subtypeNameSet = UIMAToolkit.getSubtypeNameSet(jCas, SIDEAnnotation.type);
				SwingToolkit.reloadComboBoxContent(annotationComboBox, subtypeNameSet.toArray(new String[0]), new ActionListener[]{AnnotationEditor.this}, false);
				SwingToolkit.selectWithoutAction(annotationComboBox, desiredSubtypeName, new ActionListener[]{AnnotationEditor.this});
				refreshPanel();
			}
		});
		leftPanel.add("br hfill", predictAnnotationButton);
		
		leftPanel.add("p hfill", new JSeparator());
		
		exportToCSVButton = new JButton("export to csv");
		exportToCSVButton.setMnemonic(KeyEvent.VK_O);
		exportToCSVButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean runnable = true;
				List<CharSequence> csList = new ArrayList<CharSequence>();
				
				AnnotationEditor ae = AnnotationEditor.this;
				
				runnable = YeriDebug.updateValidity(runnable, ae.jCas!=null, csList, "select an xmi file!");
				
				String subtypeName = ae.getSubtypeName();
				runnable = YeriDebug.updateValidity(runnable, subtypeName!=null, csList, "select subtype!");
				
				if(!runnable){ AlertDialog.show("error", csList, ae); return; }
				
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(SIDEToolkit.etcFolder);
				fileChooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"csv"}, true));
				int result = fileChooser.showSaveDialog(AnnotationEditor.this);
				if(result!=JFileChooser.APPROVE_OPTION){ return; }
				
				File csvFile = fileChooser.getSelectedFile();
				csvFile = FileToolkit.addExtensionIfNecessary(csvFile, new String[]{"csv"}, "csv");
				
				
				try {
					UIMAToolkit.exportToCSV(jCas, subtypeName, csvFile);
					JOptionPane.showMessageDialog(ae, "Convert succeeded!", "done", JOptionPane.INFORMATION_MESSAGE);
				}catch (IOException ex) {
					JOptionPane.showMessageDialog(ae, "Convert failed!", "error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		leftPanel.add("p hfill", exportToCSVButton);
		
		this.saveButton = new JButton("save");
		saveButton.setMnemonic(KeyEvent.VK_S);
		leftPanel.add("br hfill", saveButton);
		
		scrollPane = new JScrollPane();
		visualizationPanel = new VisualizationPanel();
		
		rightTabbedPane = new JTabbedPane();
		rightTabbedPane.addTab("annotation", scrollPane);
		rightTabbedPane.addTab("visualization", visualizationPanel);
//		rightTabbedPane.addTab("visualization", new JLabel("HELLO"));
		
		
		splitPanel = new JSplitPane();
		splitPanel.setDividerLocation(250);
		splitPanel.setLeftComponent(new JScrollPane(leftPanel));
		splitPanel.setRightComponent(rightTabbedPane);
		
		this.setLayout(new BorderLayout());
		this.add(splitPanel, BorderLayout.CENTER);
		
		

		this.saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//				checkNewAnnotationName();
				AnnotationEditor.this.saveAnnotation();
			}
		});

		this.newLayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SegmentationEditor segmentationEditor = new SegmentationEditor(jCas, AnnotationEditor.this);
				TestDialog testDialog = new TestDialog(segmentationEditor, AnnotationEditor.this, Dialog.ModalityType.DOCUMENT_MODAL);
				testDialog.showDialog();
				AnnotationEditor.this.refreshPanel();
			}
		});

		this.deleteLayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AnnotationEditor ae = AnnotationEditor.this;
				String subtypeName = ae.getSubtypeName();
				UIMAToolkit.removeSIDEAnnotationList(jCas, SIDEAnnotation.type, subtypeName);
				try {
					UIMAToolkit.saveJCasToSource(jCas);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				ae.refreshPanel();
			}
		});

		this.loadFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				
				File loadFolder = Workbench.current.getLoadFolder();
				chooser.setCurrentDirectory(loadFolder==null?SIDEToolkit.xmiFolder:loadFolder);
				chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"xmi"}, true));
				int selection = chooser.showOpenDialog(AnnotationEditor.this);
				if(selection!=JFileChooser.APPROVE_OPTION){
					return;
				}
				
				File xmiFile = chooser.getSelectedFile();
				
				(new FileLoadTask(loadProgressBar, xmiFile)).execute();
			}
		});
		
		colorMapperUI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AnnotationEditor ae = AnnotationEditor.this;
				String subtypeName = ae.getSubtypeName();
				if(subtypeName==null){ return; }
				
				SIDEAnnotationSetting setting = UIMAToolkit
						.getSIDEAnnotationSetting(jCas, subtypeName);
				Map<Long, ColorLabel> original = UIMAToolkit
						.getColorLabelMap(setting);

				ColorLabelConfigPanel clcp = (ColorLabelConfigPanel) e
						.getSource();
				int eventID = e.getID();

				if (eventID == ColorLabelConfigPanel.labelChangeEvent) {
					List<? extends ColorLabel> changed = clcp
							.getColorLabelList();
					List<String[]> changeList = UIMAToolkit
							.getColorLabelChangeList(original.values(), changed);

					for (String[] change : changeList) {
						List<SIDESegment> sideSegmentList = UIMAToolkit
								.getSIDESegmentList(jCas, SIDEAnnotation.type,
										subtypeName);
						for (SIDESegment sideSegment : sideSegmentList) {
							SIDEAnnotation sideAnnotation = (SIDEAnnotation) sideSegment;
							String labelName = sideAnnotation.getLabelString();
							if(labelName==null){
								continue;
							}
							if (labelName.equals(change[0])) {
								sideAnnotation.setLabelString(change[1]);
							}
						}
					}
				}

				// in whatever change, update labelMap				
				//				Map<String,Color> colorLabelMap = CollectionsToolkit.createHashMap(clcp.getLabelArray(), clcp.getColorArray());
				String labelColorMapString = UIMAToolkit.toLabelColorString(
						clcp.getColorLabelList().toArray(new ColorLabel[0]))
						.toString();
				setting.setLabelColorMapString(labelColorMapString);

				segmentedTextViewer.refreshColors();
				AnnotationEditor.this.visualizationPanel.refreshPanel();
			}
		});

		refreshPanel();
	}
	
	public String getDesiredPredictionSubtypeName(){
		return predictSubtypeNameField.getText();
	}
	
	private class FileLoadTask extends OnPanelSwingTask{
		private File xmiFile;
		
		public FileLoadTask(JProgressBar progressBar, File xmiFile){
			this.addProgressBar(progressBar);
			this.xmiFile = xmiFile;
		}

		@Override
		protected Void doInBackground() throws Exception {
			AnnotationEditor yae = AnnotationEditor.this;
			yae.loadXmiFile(xmiFile);
            return null;
		}
	}
	
	public String getSubtypeName() {
		return (String) this.annotationComboBox.getSelectedItem();
	}
	public void setSubtypeName(String annotationSubtypeName) {
		this.annotationComboBox.setSelectedItem(annotationSubtypeName);
	}

	public boolean isAnnotateNullSegmentOnlyEnabled(){
		return annotateNullLabelSegmentsOnlyCheckBox.isEnabled() && annotateNullLabelSegmentsOnlyCheckBox.isSelected();
	}
	private void saveAnnotation() {
		try {
			URI uri = new URI(UIMAToolkit.getXmiURIString(jCas));
			System.out.println(uri.toString());
			File file = new File(uri);
			
			UIMAToolkit.saveCas(jCas.getCas(), file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private SegmentedTextViewer segmentedTextViewer = null;

	
	private void setJCas(JCas jCas) {
		this.jCas = jCas;
		refreshPanel();
	}

	public void refreshPanel() {
		
//		this.rightTabbedPane.setSelectedIndex(tabIndex);
		
		if(jCas==null){
			return;
		}
		
//		Set<String> baseSubtypeSet = UIMAToolkit.getBaseSubtypeNameSet(jCas);
//		SwingToolkit.reloadComboBoxContent(baseSubtypeComboBox, baseSubtypeSet.toArray(new String[0]),
//				null, false);
		
		filenameLabel.setText(FileToolkit.getFilename(UIMAToolkit.getXmiURIString(jCas)));
		
		Set<String> subtypeNameSet = UIMAToolkit.getSubtypeNameSet(jCas, SIDEAnnotation.type);
		SwingToolkit.reloadComboBoxContent(this.annotationComboBox, subtypeNameSet.toArray(new String[0]), new ActionListener[]{AnnotationEditor.this}, true);
		
		SIDEAnnotationSetting setting = UIMAToolkit.getSIDEAnnotationSetting(
				jCas, this.getSubtypeName());
		Map<String, Color> labelColorMap = UIMAToolkit.getLabelColorMap(setting);
		
		try {
			segmentedTextViewer = new SegmentedTextViewer(jCas, this.getSubtypeName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Datatype datatype = Datatype.create(setting);
		this.datatypeButtonArray[datatype.ordinal()].setSelected(true);
		this.colorMapperUI.setDataMap(labelColorMap);			
		this.scrollPane.setViewportView(segmentedTextViewer);
		SwingToolkit.adjustScrollBar(scrollPane, JScrollBar.VERTICAL);
		
		TrainingResult trainingResult = (TrainingResult)this.trainingResultComboBox.getSelectedItem();
		boolean annotateSelectedOnlyCheckBoxEnabled =
//			(trainingResult!=null) &&
//			trainingResult.getSubtypeName().equals(this.getSubtypeName()) &&
			predictionSegmentationComboBox.getSelectedIndex()==0;
		this.annotateNullLabelSegmentsOnlyCheckBox.setEnabled(annotateSelectedOnlyCheckBoxEnabled);
		
		
		boolean annotateNullLabelSegmentsOnly = isAnnotateNullSegmentOnlyEnabled();
		predictSubtypeNameField.setEnabled(!annotateNullLabelSegmentsOnly);
		if(annotateNullLabelSegmentsOnly){
			predictSubtypeNameField.setText(trainingResult.getSubtypeName());
		}
		
//		boolean prevAnnotateSelectedOnly = this.annotateSelectedOnlyCheckBox.isSelected(); 
//		this.annotateSelectedOnlyCheckBox.setSelected( annotateSelectedOnlyEnabled && prevAnnotateSelectedOnly);
		
		AnnotationEditor.this.visualizationPanel.refreshPanel();
		
//		baseSubtypeComboBox.setSelectedIndex(this.annotationComboBox.getSelectedIndex()>=0?0:-1);
	}

	public static void main(String[] args) throws InvalidXMLException,
			ResourceInitializationException, IOException, CASException,
			CollectionException, ResourceProcessException {
		test04();
	}
	
	protected static void test05() throws InvalidXMLException, ResourceInitializationException, IOException, CollectionException, ResourceProcessException{
		// load xmi
		for(File file : SIDEToolkit.csvFolder.listFiles(FileToolkit.createExtensionListFileFilter(new String[]{"csv"}, false))){
			String xmiFileName = file.getName()+".xmi";
			CAS cas = UIMAToolkit.createSIDECAS();
			UIMAToolkit.readCSVIntoCas(cas, file, "Text", true, true);
			UIMAToolkit.saveCas(cas, new File(SIDEToolkit.xmiFolder, xmiFileName));
		}
		

		AnnotationEditor ae = new AnnotationEditor();
		File testFile = new File(SIDEToolkit.xmiFolder, "test_timestamp2.csv.xmi");
		ae.loadXmiFile(testFile);
		
		TestFrame testFrame = new TestFrame(ae);
		testFrame.setSize(new Dimension(1200, 900));
		testFrame.showFrame();
		
		ae.rightTabbedPane.setSelectedIndex(1);
//		ae.visualizationPanel.visualizationPluginComboBox.setSelectedIndex(0);
//		ae.visualizationComboBox.setSelectedIndex(2);
//		SwingToolkit.fireNullActionEvent(ae);
	}
	protected static void test04() throws InvalidXMLException, ResourceInitializationException, IOException, CollectionException, ResourceProcessException{
		SIDEToolkit.FileType.loadAll();
		
		// load xmi
		for(File file : SIDEToolkit.csvFolder.listFiles(FileToolkit.createExtensionListFileFilter(new String[]{"csv"}, false))){
			String xmiFileName = file.getName()+".xmi";
			CAS cas = UIMAToolkit.createSIDECAS();
			UIMAToolkit.readCSVIntoCas(cas, file, "Text", true, true);
			UIMAToolkit.saveCas(cas, new File(SIDEToolkit.xmiFolder, xmiFileName));
		}
		

		AnnotationEditor ae = new AnnotationEditor();
		File testFile = new File(SIDEToolkit.xmiFolder, "361-Gold.csv.xmi");
		ae.loadXmiFile(testFile);
		
		TestFrame testFrame = new TestFrame(ae);
		testFrame.setSize(new Dimension(1200, 900));
		testFrame.showFrame();
		
//		ae.visualizationComboBox.setSelectedIndex(2);
//		SwingToolkit.fireNullActionEvent(ae.visualizeButton);
	}
	
	protected static void test03() throws InvalidXMLException, ResourceInitializationException, IOException, CollectionException, ResourceProcessException{
		SIDEToolkit.FileType.loadAll();
//		SIDEToolkit.cleanupSaveFiles();
		
		// load xmi
		for(File file : SIDEToolkit.csvFolder.listFiles(FileToolkit.createExtensionListFileFilter(new String[]{"csv"}, false))){
			String xmiFileName = file.getName()+".xmi";
			CAS cas = UIMAToolkit.createSIDECAS();
			UIMAToolkit.readCSVIntoCas(cas, file, "Text", true, true);
			UIMAToolkit.saveCas(cas, new File(SIDEToolkit.xmiFolder, xmiFileName));
		}
		

		AnnotationEditor ae = new AnnotationEditor();
		File testFile = new File(SIDEToolkit.xmiFolder, "361-Gold.csv.xmi");
		ae.loadXmiFile(testFile);
		
		ae.trainingResultComboBox.setSelectedIndex(0);
		ae.predictionSegmentationComboBox.setSelectedIndex(0);
		
		TestFrame testFrame = new TestFrame(ae);
		testFrame.setSize(new Dimension(800, 600));
		testFrame.showFrame();
		
		SwingToolkit.fireNullActionEvent(ae.predictAnnotationButton);
	}
	protected static void test02() throws InvalidXMLException, ResourceInitializationException, ResourceProcessException, IOException, CollectionException{

		SIDEToolkit.FileType.loadAll();
//		SIDEToolkit.cleanupSaveFiles();
		
		// load xmi
		File readfolder = new File("C:/Documents and Settings/moonyoun/Desktop/SIDE files");
		for(File file : readfolder.listFiles(FileToolkit.createExtensionListFileFilter(new String[]{"csv"}, false))){
			String xmiFileName = file.getName()+".xmi";
			CAS cas = UIMAToolkit.createSIDECAS();
			UIMAToolkit.readCSVIntoCas(cas, file, "Text", true, true);
			UIMAToolkit.saveCas(cas, new File(SIDEToolkit.xmiFolder, xmiFileName));
		}
		

		AnnotationEditor ae = new AnnotationEditor();
		File testFile = new File(SIDEToolkit.xmiFolder, "UnlabeledExamples.csv.xmi");
		ae.loadXmiFile(testFile);
		
		ae.trainingResultComboBox.setSelectedIndex(0);
		
		TestFrame testFrame = new TestFrame(ae);
		testFrame.setSize(new Dimension(800, 600));
		testFrame.showFrame();
		
		SwingToolkit.fireNullActionEvent(ae.predictAnnotationButton);
	}
	protected static void test01(){
//		CAS cas = UIMAToolkit.getSIDETestCasArray()[0];

		AnnotationEditor ae = new AnnotationEditor();
		for(File file : SIDEToolkit.xmiFolder.listFiles(FileToolkit.createExtensionListFileFilter(new String[]{"xmi"}, false))){
			ae.loadXmiFile(file);
			break;
		}
		TestFrame testFrame = new TestFrame(ae);
		SwingToolkit.setSize(testFrame, new Dimension(800, 600), SizeType
				.values());
		testFrame.showFrame();
	}


	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JComboBox annotationComboBox;
	private javax.swing.JButton loadFileButton;
	private JProgressBar loadProgressBar;
//	private javax.swing.JLabel annotationLabel;
	private javax.swing.JLabel filenameLabel;
	private javax.swing.JPanel leftPanel;
	
	private JRadioButton[] datatypeButtonArray;
	private ActionListener datatypeButtonActionListener;
	
//	private javax.swing.JPanel loadFilePanel;
	private javax.swing.JButton newLayerButton;
	private JButton deleteLayerButton;
	private javax.swing.JButton saveButton;
//	private JComboBox visualizationComboBox;
//	private JButton visualizeButton;
	private javax.swing.JScrollPane scrollPane;
	private JTabbedPane rightTabbedPane;
	private VisualizationPanel visualizationPanel;
	private javax.swing.JSplitPane splitPanel;
	private ColorMapperUI colorMapperUI;
	
	private JComboBox predictionSegmentationComboBox;
	private JCheckBox annotateNullLabelSegmentsOnlyCheckBox;
	private JButton predictAnnotationButton;
	private JButton exportToCSVButton;
	private JComboBox trainingResultComboBox;
	private JTextField predictSubtypeNameField;
	// End of variables declaration//GEN-END:variables

}