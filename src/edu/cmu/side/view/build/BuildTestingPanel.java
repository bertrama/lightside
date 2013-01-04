package edu.cmu.side.view.build;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.view.util.AbstractListPanel;

public class BuildTestingPanel extends AbstractListPanel {

	ButtonGroup highOptions = new ButtonGroup();
	JRadioButton radioCV = new JRadioButton("Cross-Validation");
	JRadioButton radioTestSet = new JRadioButton("Supplied Test Set");
	JRadioButton radioNone = new JRadioButton("No Evaluation");
	
	ButtonGroup cvOptions = new ButtonGroup();
	JRadioButton radioRandom = new JRadioButton("Random");
	JRadioButton radioByAnnotation = new JRadioButton("By Annotation");
	JRadioButton radioByFile = new JRadioButton("By File");
	
	JTextField txtNumFolds = new JTextField(3);
	ButtonGroup foldNums = new ButtonGroup();
	JRadioButton radioAuto = new JRadioButton("Auto");
	JRadioButton radioManual = new JRadioButton("Manual:");
	
	JComboBox annotations = new JComboBox();
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
	
		cvOptions.add(radioRandom);
		cvOptions.add(radioByAnnotation);
		cvOptions.add(radioByFile);
		radioRandom.addActionListener(new BuildModelControl.ValidationButtonListener("source", "RANDOM"));
		radioByAnnotation.addActionListener(new BuildModelControl.ValidationButtonListener("source", "ANNOTATIONS"));
		radioByFile.addActionListener(new BuildModelControl.ValidationButtonListener("source", "FILES"));
		radioRandom.setSelected(true);
		
		foldNums.add(radioAuto);
		foldNums.add(radioManual);
		radioAuto.setSelected(true);
		
		txtNumFolds.setText("10");
		numFoldsListener.actionPerformed(null);
		txtNumFolds.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				numFoldsListener.setValue(txtNumFolds.getText());
				numFoldsListener.actionPerformed(null);
			}
		});

		BuildModelControl.updateValidationSetting("test", Boolean.TRUE.toString());
		BuildModelControl.updateValidationSetting("type", "CV");
		BuildModelControl.updateValidationSetting("source", "RANDOM");
		BuildModelControl.updateValidationSetting("numFolds", "10");
		
		setLayout(new RiverLayout());
		add("br left", radioCV);
		add("br left", radioTestSet);
		add("br left", radioNone);

		
//		radioRandom.setBorder(new EmptyBorder(0,30,0,0));
//		add("br left", radioRandom);
//		radioByAnnotation.setBorder(new EmptyBorder(0,30,0,0));
//		add("br left", radioByAnnotation);
//		combo.setBorder(new EmptyBorder(0,30,0,0));
//		add("br hfill", combo);
//		radioByFile.setBorder(new EmptyBorder(0,30,0,0));
//		add("br left", radioByFile);
//		
//		radioAuto.setBorder(new EmptyBorder(0,30,0,0));
//		add("br left", new JLabel("Number of Folds:"));
//		add("br left", radioAuto);
//		add("left", new JLabel("or"));
//		add("left", radioManual);
//		add("left", txtNumFolds);
//		add.setText("");
//		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_table.png");
//		add.setToolTipText("Open");
//		add.setIcon(iconLoad);
//		add("left", add);
		
//		
//		listScroll.setBorder(new EmptyBorder(0,30,0,0));
//		add("br hfill", listScroll);	
//		

	}
	
	public void refreshPanel(){
		combo.setEnabled(radioByAnnotation.isSelected());
		add.setEnabled(radioTestSet.isSelected());
		listScroll.setEnabled(radioTestSet.isEnabled());
	}
}
