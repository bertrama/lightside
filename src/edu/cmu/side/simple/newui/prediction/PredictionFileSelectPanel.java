package edu.cmu.side.simple.newui.prediction;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.DefaultFileListCellRenderer;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.features.FeatureFileManagerPanel;

public class PredictionFileSelectPanel extends AbstractListPanel{

	private static Set<String> filenames = new TreeSet<String>();
	private static SimpleDocumentList documents;
	private static JComboBox textColumnComboBox;
	
	public PredictionFileSelectPanel(){

		list.setCellRenderer(new DefaultFileListCellRenderer(list));
		documents = null;
		JButton fileAddButton = new JButton("Add");
		JButton fileRemoveButton = new JButton("Clear");
		this.add("hfill", new JLabel("CSV File:"));
		this.add("", fileAddButton);
		this.add("", fileRemoveButton);

		scroll.setPreferredSize(new Dimension(250, 80));
		this.add("br hfill ", scroll);		
		this.add("br left", new JLabel("Text Field:"));
		textColumnComboBox = new JComboBox();
		this.add("br hfill", textColumnComboBox);
		fileAddButton.addActionListener(new SimpleWorkbench.FileAddActionListener(this, listModel));
		fileRemoveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				System.out.println("frb listens " + filenames.size() + "< " + textColumnComboBox.getSelectedIndex());
				documents = null;
				List<ListDataListener> listeners = new ArrayList<ListDataListener>();
				for(ListDataListener al : listModel.getListDataListeners()){
					listeners.add(al);
				}
				for(ListDataListener al : listeners){
					listModel.removeListDataListener(al);					
				}
				listModel.clear();
				for(ListDataListener al : listeners){
					listModel.addListDataListener(al);
				}
				filenames.clear();
				textColumnComboBox.setSelectedIndex(-1);
				fireActionEvent();
			}
		});
		textColumnComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				System.out.println("tccb listens " + filenames.size() + "< " + textColumnComboBox.getSelectedIndex());
				if(filenames.size() > 0 && textColumnComboBox.getSelectedIndex()>=0){
					try{
						System.out.println("Building docs from tccb");
						documents = new SimpleDocumentList(filenames, textColumnComboBox.getSelectedItem().toString());						
					}catch(Exception e){
						JOptionPane.showMessageDialog(PredictionFileSelectPanel.this, "Document loading failed. Check the terminal for detail.", "Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					fireActionEvent();					
				}
			}
		});
		listModel.addListDataListener(new ListDataListener() {
			@Override
			public void contentsChanged(ListDataEvent e) {
				intervalAdded(e);
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				BufferedReader in;
				textColumnComboBox.removeAllItems();
				try{
					Set<String> annotationTitles = new TreeSet<String>();
					String textIndex = null;
					for(int i = 0; i < listModel.getSize(); i++){
						in = new BufferedReader(new FileReader(((File)listModel.get(i)).getAbsolutePath()));
						String[] annotations = in.readLine().split(",");
						for(String annot : annotations) annotationTitles.add(annot.replaceAll("\"", "").trim());
						in.close();
					}
					for(String s : annotationTitles){ 
						if(s.equalsIgnoreCase("text")) textIndex = s;
					}
					if(textIndex != null){
						SwingToolkit.reloadComboBoxContent(textColumnComboBox, 
								annotationTitles.toArray(new String[0]), textIndex, 
								textColumnComboBox.getActionListeners(), true);						
					}else{
						SwingToolkit.reloadComboBoxContent(textColumnComboBox, 
								annotationTitles.toArray(new String[0]), 
								textColumnComboBox.getActionListeners(), true);												
					}
					textColumnComboBox.addItem("[No Text]");
					if(documents == null){
						if(listModel.getSize()>0){
							System.out.println("Building docs from aldl");
							filenames.clear();
							for(int i = 0; i < listModel.getSize(); i++){
								filenames.add(((File)listModel.get(i)).getAbsolutePath());
							}
							documents = new SimpleDocumentList(filenames, textColumnComboBox.getSelectedItem().toString());
						}
					}
				}catch(Exception ex){
					AlertDialog.show("Error!", "CSV File improperly formatted", PredictionFileSelectPanel.this);
					ex.printStackTrace();
				}
				PredictionFileSelectPanel.this.refreshPanel();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				intervalAdded(e);
			}
		});
	}
	
	public static void setPredictionDocuments(SimpleDocumentList docs){
		documents = docs;
	}
	
	public static SimpleDocumentList getPredictionDocuments(){
		return documents;
	}

	public void refreshPanel(){
		super.refreshPanel();
	}
}
