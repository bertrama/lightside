package edu.cmu.side.newui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class FeatureExtractionPanel extends JPanel{

	public static void main(String[] args){
		JFrame frame = new JFrame();
		FeatureExtractionPanel fep = new FeatureExtractionPanel();
		fep.setPreferredSize(new Dimension(1100,700));
		frame.add(fep);
		frame.setSize(1100, 700);
		frame.setVisible(true);
		
	}
	
	public FeatureExtractionPanel(){
		FeatureLeftPanel left = new FeatureLeftPanel();
		FeatureTablePanel right = new FeatureTablePanel();
		JSplitPane split = new JSplitPane();
		JScrollPane scrollLeft = new JScrollPane(left);
		JScrollPane scrollRight = new JScrollPane(right);
		scrollLeft.setPreferredSize(new Dimension(350,675));
		scrollRight.setPreferredSize(new Dimension(725,675));
		split.setLeftComponent(scrollLeft);
		split.setRightComponent(scrollRight);
		add(split);
	}
}
