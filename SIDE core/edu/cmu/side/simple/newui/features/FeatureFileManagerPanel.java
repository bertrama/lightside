package edu.cmu.side.simple.newui.features;

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

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.uima.DocumentListInterface;

/**
 * Straightforward Swing UI for selecting files to load into memory and creating a DocumentList out of them.
 * This is where the current DocumentList is stored (outside of FeatureTables).
 * 
 * @author emayfiel
 *
 */
public class FeatureFileManagerPanel extends AbstractListPanel{

	private static final long serialVersionUID = 8927680502421484611L;

	private static JComboBox textColumnComboBox;
	private static JComboBox annotationComboBox;

	private static Set<String> filenames = new TreeSet<String>();
	private static SimpleDocumentList documents;


	/**
	 * Add all the bells and whistles in the Swing UI.
	 */
	public FeatureFileManagerPanel(){
		/** List model initialized and defined in the abstract class */
		super();

		list.setCellRenderer(new DefaultFileListCellRenderer(list));
		documents = null;
		JButton fileAddButton = new JButton("add");
		JButton fileRemoveButton = new JButton("clear");
		this.add("hfill", new JLabel("csv file:"));
		this.add("", fileAddButton);
		this.add("", fileRemoveButton);

		scroll.setPreferredSize(new Dimension(250, 100));
		this.add("br hfill ", scroll);		

		fileAddButton.addActionListener(new SIDEToolkit.FileAddActionListener(this, listModel));
		fileRemoveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				documents = null;
				listModel.clear();
				annotationComboBox.setSelectedIndex(-1);
				textColumnComboBox.setSelectedIndex(-1);
			}
		});

		annotationComboBox = new JComboBox();
		textColumnComboBox = new JComboBox();

		annotationComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(documents != null){
					documents.setCurrentAnnotation(annotationComboBox.getSelectedItem().toString());
				}
			}
		});
		textColumnComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(filenames.size() > 0){
					documents = new SimpleDocumentList(filenames, annotationComboBox.getSelectedItem().toString(), textColumnComboBox.getSelectedItem().toString());
					fireActionEvent();					
				}
			}
		});

		this.add("br", new JLabel("annotation:"));
		this.add("br hfill", annotationComboBox);

		this.add("br", new JLabel("text field:"));
		this.add("br hfill", textColumnComboBox);

		listModel.addListDataListener(new ListDataListener() {
			@Override
			public void contentsChanged(ListDataEvent e) {
				intervalAdded(e);
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				BufferedReader in;
				annotationComboBox.removeAllItems();
				textColumnComboBox.removeAllItems();
				try{
					Set<String> annotationTitles = new TreeSet<String>();
					for(int i = 0; i < listModel.getSize(); i++){
						in = new BufferedReader(new FileReader(((File)listModel.get(i)).getAbsolutePath()));
						String[] annotations = in.readLine().split(",");
						for(String annot : annotations) annotationTitles.add(annot.replaceAll("\"", "").trim());
						in.close();
					}
					String textIndex = null;
					String annotIndex = null;
					for(String s : annotationTitles){ 
						if(annotIndex == null && !s.contains("text")) annotIndex = s; 
						if(s.equalsIgnoreCase("text")) textIndex = s; 
					}
					SwingToolkit.reloadComboBoxContent(annotationComboBox, 
							annotationTitles.toArray(new String[0]), 
							annotationComboBox.getActionListeners(), true);
					if(textIndex != null){
						SwingToolkit.reloadComboBoxContent(textColumnComboBox, 
								annotationTitles.toArray(new String[0]), textIndex, 
								annotationComboBox.getActionListeners(), true);						
					}else{
						SwingToolkit.reloadComboBoxContent(textColumnComboBox, 
								annotationTitles.toArray(new String[0]), 
								annotationComboBox.getActionListeners(), true);												
					}
					textColumnComboBox.addItem("[No Text]");
					if(documents == null){
						if(listModel.getSize()==0){
							AlertDialog.show("Error!","No CSV files have been selected.", null);
						}else{
							filenames.clear();
							for(int i = 0; i < listModel.getSize(); i++){
								filenames.add(((File)listModel.get(i)).getAbsolutePath());
							}
							documents = new SimpleDocumentList(filenames, annotationComboBox.getSelectedItem().toString(), textColumnComboBox.getSelectedItem().toString());
						}
					}
				}catch(Exception ex){
					AlertDialog.show("Error!", "CSV File improperly formatted", FeatureFileManagerPanel.this);
					ex.printStackTrace();
				}
				FeatureFileManagerPanel.this.refreshPanel();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				intervalAdded(e);
			}
		});

	}

	/**
	 * Attempts to use a cached DocumentList, unless there's been a changed to the 
	 * selected CSVs recently, in which case it builds a new DocumentList.
	 * 
	 * This is where everything else in the Feature tab finds out the current document list.
	 * 
	 * @return The currently active DocumentList, according to the file chooser UI.
	 */
	public static SimpleDocumentList getDocumentList(){
		return documents;
	}

	public void refreshPanel() {
		if(listModel.getSize()>0) getDocumentList();
	}


}
