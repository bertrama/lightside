package edu.cmu.side.simple.newui.prediction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.SIDETable;

public class PredictionRightPanel extends AbstractListPanel{
	SIDETable display = new SIDETable();
	DefaultTableModel displayModel = new DefaultTableModel();
	
	JButton saveButton = new JButton("export");
	SimpleDocumentList localDocuments;
	
	public PredictionRightPanel(){
		setLayout(new RiverLayout());
		scroll = new JScrollPane(display);
		add("left", new JLabel("Documents to Annotate:"));
		add("br hfill vfill", scroll);
		add("br right", saveButton);
		saveButton.addActionListener(new SimpleWorkbench.PredictionResultSaveListener());
	}
	
	public void refreshPanel(){
		SimpleDocumentList clickedDocs = PredictionFileSelectPanel.getPredictionDocuments();
		if(clickedDocs != null){
			System.out.println(clickedDocs);
			localDocuments = clickedDocs;
			displayModel = new DefaultTableModel();
			List<String> text = clickedDocs.getCoveredTextList();
			Set<String> annots = clickedDocs.allAnnotations().keySet();
			for(String s : annots) displayModel.addColumn(s);
			displayModel.addColumn("text");
			for(int i = 0; i < text.size(); i++){
				Object[] row = new Object[annots.size()+1];
				int j = 0;
				for(String s : annots){
					if(i < clickedDocs.allAnnotations().get(s).size()){
						row[j] = clickedDocs.allAnnotations().get(s).get(i);						
					}
					j++;
				}
				row[row.length-1] = text.get(i);
				displayModel.addRow(row);
			}
			display.setModel(displayModel);
			for(int i = 0; i < display.getColumnCount()-1; i++){
				display.getColumnModel().getColumn(i).setPreferredWidth(50);			
			}
		}else{
			localDocuments = null;
			displayModel = new DefaultTableModel();
			display.setModel(displayModel);
		}
		repaint();
	}
}
