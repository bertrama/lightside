package edu.cmu.side.genesis.view.build;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.simple.newui.AbstractListPanel;

public class BuildTestingPanel extends AbstractListPanel {

	ButtonGroup highOptions = new ButtonGroup();
	JRadioButton radioCV = new JRadioButton("Cross-Validation");
	JRadioButton radioTestSet = new JRadioButton("Supplied Test Set");
	
	ButtonGroup cvOptions = new ButtonGroup();
	JRadioButton radioRandom = new JRadioButton("Random");
	JRadioButton radioByAnnotation = new JRadioButton("By Annotation");
	JRadioButton radioByFile = new JRadioButton("By File");
	
	JTextField txtNumFolds = new JTextField(3);
	ButtonGroup foldNums = new ButtonGroup();
	JRadioButton radioAuto = new JRadioButton("Auto");
	JRadioButton radioManual = new JRadioButton("Manual:");
	
	JComboBox annotations = new JComboBox();
	
	public BuildTestingPanel(){
		highOptions.add(radioCV);
		highOptions.add(radioTestSet);
		radioCV.setSelected(true);
	
		cvOptions.add(radioRandom);
		cvOptions.add(radioByAnnotation);
		cvOptions.add(radioByFile);
		radioRandom.setSelected(true);
		
		foldNums.add(radioAuto);
		foldNums.add(radioManual);
		radioAuto.setSelected(true);
		
		txtNumFolds.setText("10");
		
		setLayout(new RiverLayout());
		add("left", new JLabel("Test Settings:"));
		add("br left", radioCV);
		radioRandom.setBorder(new EmptyBorder(0,30,0,0));
		add("br left", radioRandom);
		radioByAnnotation.setBorder(new EmptyBorder(0,30,0,0));
		add("br left", radioByAnnotation);
		combo.setBorder(new EmptyBorder(0,30,0,0));
		add("br hfill", combo);
		radioByFile.setBorder(new EmptyBorder(0,30,0,0));
		add("br left", radioByFile);
		
		radioAuto.setBorder(new EmptyBorder(0,30,0,0));
		add("br left", radioAuto);
		add("left", new JLabel("or"));
		add("left", radioManual);
		add("left", txtNumFolds);
		add("br left", radioTestSet);
		add("left", add);
		add("br hfill", listScroll);		
	}
	
	public void refreshPanel(){
		combo.setEnabled(radioByAnnotation.isSelected());
		add.setEnabled(radioTestSet.isSelected());
		listScroll.setEnabled(radioTestSet.isEnabled());
	}
}
