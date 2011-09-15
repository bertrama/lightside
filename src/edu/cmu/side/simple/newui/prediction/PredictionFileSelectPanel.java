package edu.cmu.side.simple.newui.prediction;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.DefaultFileListCellRenderer;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class PredictionFileSelectPanel extends AbstractListPanel{

	private static Set<String> filenames = new TreeSet<String>();
	private static SimpleDocumentList documents;
	private static JComboBox textColumnComboBox;

	public PredictionFileSelectPanel(){

		list.setCellRenderer(new DefaultFileListCellRenderer(list));
		documents = null;
		JButton fileAddButton = new JButton("add");
		JButton fileRemoveButton = new JButton("clear");
		this.add("hfill", new JLabel("csv file:"));
		this.add("", fileAddButton);
		this.add("", fileRemoveButton);

		scroll.setPreferredSize(new Dimension(250, 80));
		this.add("br hfill ", scroll);		
		this.add("br left", new JLabel("text field:"));
		textColumnComboBox = new JComboBox();
		this.add("br hfill", textColumnComboBox);
		fileAddButton.addActionListener(new SimpleWorkbench.FileAddActionListener(this, listModel));
		fileRemoveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				documents = null;
				listModel.clear();
				filenames.clear();
				textColumnComboBox.setSelectedIndex(-1);
			}
		});
		textColumnComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(filenames.size() > 0 && textColumnComboBox.getSelectedIndex()>=0){
					documents = new SimpleDocumentList(filenames, textColumnComboBox.getSelectedItem().toString());
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
