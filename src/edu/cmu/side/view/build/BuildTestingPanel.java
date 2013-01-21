package edu.cmu.side.view.build;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.io.FileToolkit;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.view.util.AbstractListPanel;

public class BuildTestingPanel extends AbstractListPanel {

	ButtonGroup highOptions = new ButtonGroup();
	JRadioButton radioCV = new JRadioButton("Cross-Validation");
	JRadioButton radioTestSet = new JRadioButton("Supplied Test Set");
	JRadioButton radioNone = new JRadioButton("No Evaluation");
	
	JCheckBox checkCustomCV = new JCheckBox("Custom");
	
	ButtonGroup cvOptions = new ButtonGroup();
	JRadioButton radioRandom = new JRadioButton("Random");
	JRadioButton radioByAnnotation = new JRadioButton("By Annotation:");
	JRadioButton radioByFile = new JRadioButton("By File");

	
	JTextField txtNumFolds = new JTextField(3);
	ButtonGroup foldNums = new ButtonGroup();
	JRadioButton radioAuto = new JRadioButton("Auto");
	JRadioButton radioManual = new JRadioButton("Manual:");
	
	JComboBox annotations = new JComboBox();
//	JTextArea testSetSummary = new JTextArea();
	TestSetLoadPanel testSetLoadPanel = new TestSetLoadPanel("Select Test Set");
	
	JPanel cvControlPanel = new JPanel(new RiverLayout(0, 3));
	JPanel testSetControlPanel = new JPanel(new BorderLayout());
	
	JPanel controlPanel = new JPanel(new RiverLayout(0,0));
	JPanel selectPanel = new JPanel(new RiverLayout(10, 3));
	
	Map<JRadioButton, Component> configPanels = new HashMap<JRadioButton, Component>();
	
	static BuildModelControl.ValidationButtonListener numFoldsListener = new BuildModelControl.ValidationButtonListener("numFolds","10");

	public BuildTestingPanel(){
		highOptions.add(radioCV);
		highOptions.add(radioTestSet);
		highOptions.add(radioNone);
		radioCV.addActionListener(new BuildModelControl.ValidationButtonListener("type", "CV"));
		radioCV.addActionListener(new BuildModelControl.ValidationButtonListener("test", Boolean.TRUE.toString()));
		radioTestSet.addActionListener(new BuildModelControl.ValidationButtonListener("type", "SUPPLY"));
		radioTestSet.addActionListener(new BuildModelControl.ValidationButtonListener("test", Boolean.TRUE.toString()));
		radioNone.addActionListener(new BuildModelControl.ValidationButtonListener("test", Boolean.FALSE.toString()));
		radioCV.setSelected(true);
		

		BuildModelControl.updateValidationSetting("test", Boolean.TRUE.toString());
		BuildModelControl.updateValidationSetting("type", "CV");
		BuildModelControl.updateValidationSetting("source", "RANDOM");
		BuildModelControl.updateValidationSetting("numFolds", "10");
		BuildModelControl.updateValidationSetting("foldMethod", "AUTO");
		

		addConfigPanelRadioListeners();
		
		this.setLayout(new RiverLayout(10,0));
		this.setBorder(new EmptyBorder(0,0,0,0));
		selectPanel.setBorder(new EmptyBorder(0,0,0,0));
		controlPanel.setBorder(new EmptyBorder(0,0,0,0));
		selectPanel.add("vtop", radioCV);
		selectPanel.add("br vtop", radioTestSet);
		selectPanel.add("br vtop", radioNone);
		
		this.add("vtop left", selectPanel);

		buildCVControlPanel();
		buildTestSetControlPanel();

		controlPanel.add(cvControlPanel);
		this.add("hfill", controlPanel);
	}

	/**
	 * attach listeners to show appropriate sub-config panel.
	 */
	protected void addConfigPanelRadioListeners()
	{
		configPanels.put(radioCV, cvControlPanel);
		configPanels.put(radioTestSet, testSetControlPanel);
		configPanels.put(radioNone, new JPanel());
		ActionListener testRadioListener = new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				for (Entry<JRadioButton, Component> e : configPanels.entrySet())
				{
					if (e.getKey().isSelected())
					{
						Component config = e.getValue();
						controlPanel.removeAll();
						controlPanel.add(config);
						revalidate();
						repaint();
						return;
					}
				}
				BuildTestingPanel.this.add(new JPanel());

			}
			
		};
		for(JRadioButton radio : configPanels.keySet())
		{
			radio.addActionListener(testRadioListener);
		}
	}

	/**
	 * 
	 */
	protected void buildCVControlPanel()
	{

		cvOptions.add(radioRandom);
		cvOptions.add(radioByAnnotation);
		cvOptions.add(radioByFile);
		radioRandom.addActionListener(new BuildModelControl.ValidationButtonListener("source", "RANDOM"));
		radioByAnnotation.addActionListener(new BuildModelControl.ValidationButtonListener("source", "ANNOTATIONS"));
		radioByFile.addActionListener(new BuildModelControl.ValidationButtonListener("source", "FILES"));
		radioRandom.setSelected(true);
		radioAuto.addActionListener(new BuildModelControl.ValidationButtonListener("foldMethod", "AUTO"));
		radioManual.addActionListener(new BuildModelControl.ValidationButtonListener("foldMethod", "MANUAL"));
		
		foldNums.add(radioAuto);
		foldNums.add(radioManual);
		radioAuto.setSelected(true);
		
		txtNumFolds.setText("10");
		txtNumFolds.setEnabled(false);
		numFoldsListener.actionPerformed(null);
		final ActionListener foldsActionListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateNumFolds();
			}
		};
		//how to catch the setting without an enter-press?
		txtNumFolds.addActionListener(foldsActionListener);
		
		radioByAnnotation.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				annotations.setEnabled(radioByAnnotation.isSelected());	
				refreshPanel();
			}});
		annotations.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String annotation = (String) annotations.getSelectedItem();
				BuildModelControl.updateValidationSetting("annotation", annotation);
			}});
		
		radioManual.addActionListener(foldsActionListener);
		radioAuto.addActionListener(foldsActionListener);
		
		cvControlPanel.setBorder(new EmptyBorder(0,0,0,0));
		annotations.setBorder(new EmptyBorder(0,20,0,20));

		JLabel howToFoldLabel = new JLabel("Fold Assignment:");
		cvControlPanel.add("br left", howToFoldLabel);
		cvControlPanel.add("br left", radioRandom);
		cvControlPanel.add("br left", radioByAnnotation);
		cvControlPanel.add("br hfill",annotations);
		cvControlPanel.add("br left", radioByFile);

//		radioManual.setBorder(new EmptyBorder(0,20,0,0));
//		radioAuto.setBorder(new EmptyBorder(0,20,0,0));
		
		JLabel foldLabel = new JLabel("Number of Folds:");
		cvControlPanel.add("br left", foldLabel);
		cvControlPanel.add("br left", radioAuto);
//		manualCVControlPanel.add("left", new JLabel("or"));
		cvControlPanel.add("br left", radioManual);
		cvControlPanel.add("left", txtNumFolds);
	}

	/**
	 * 
	 */
	protected void buildTestSetControlPanel()
	{

		testSetControlPanel.add(testSetLoadPanel, BorderLayout.CENTER);
//		add.setText("");
//		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_table.png");
//		add.setToolTipText("Load Test Set...");
//		add.setIcon(iconLoad);
//		
////		testSetSummary.setPreferredSize(new Dimension(120, 80));
//		testSetSummary.setText("Load Test Set...");
//		testSetSummary.setEditable(false);
//		JScrollPane summaryScroll = new JScrollPane(testSetSummary);
//		
//		summaryScroll.setPreferredSize(new Dimension(120, 100));
		
//		testSetControlPanel.setBorder(new EmptyBorder(0,0,0,0));
//		testSetControlPanel.add("left", add);
//		testSetControlPanel.add("left", new JLabel("Load Test Files"));
//		testSetControlPanel.add("br hfill vfill", summaryScroll);

		
//		add.addActionListener(new ActionListener() 
//		{
//			private JFileChooser chooser = new JFileChooser(Workbench.csvFolder);
//			
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				chooser.setFileFilter(FileToolkit
//						.createExtensionListFileFilter(new String[] { "csv" }, true));
//				chooser.setMultiSelectionEnabled(true);
//				int result = chooser.showOpenDialog(BuildTestingPanel.this);
//				if (result != JFileChooser.APPROVE_OPTION) {
//					return;
//				}
//				
//				File[] selectedFiles = chooser.getSelectedFiles();
//				HashSet<String> docNames = new HashSet<String>();
//				
//				String description = "";
//				for(File f : selectedFiles)
//				{
//					docNames.add(f.getPath());
//					description += f.getName()+"\n";
//				}	
//				
//				DocumentList testDocs = new DocumentList(docNames);
//				BuildModelControl.updateValidationSetting("testSet", testDocs);
//				testSetSummary.setText(description);
//				
//				Workbench.update();
//			}
//			
//		});
	}
	
	public void refreshPanel()
	{
		reloadAnnotationList();
		updateNumFolds();
		testSetLoadPanel.refreshPanel();
	}

	/**
	 * 
	 */
	protected void reloadAnnotationList()
	{
		Recipe recipe = BuildModelControl.getHighlightedFeatureTableRecipe();
		if(recipe != null)
		{
			annotations.setModel(new DefaultComboBoxModel(recipe.getDocumentList().getAnnotationNames()));
		}
	}

	/**
	 * 
	 */
	protected void updateNumFolds()
	{
		if(radioManual.isSelected())
			numFoldsListener.setValue(txtNumFolds.getText());
		else
			numFoldsListener.setValue("10");
		txtNumFolds.setEnabled(radioManual.isSelected());
		
		numFoldsListener.actionPerformed(null);
	}


}
