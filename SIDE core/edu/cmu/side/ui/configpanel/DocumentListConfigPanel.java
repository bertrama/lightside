/*
 * SIDEAnnotationChooser.java
 *
 * Created on __DATE__, __TIME__
 */

package edu.cmu.side.ui.configpanel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.swing.JEasyList;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.DefaultFileListCellRenderer;
import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;
import edu.cmu.side.uima.type.SIDEDocumentSetting;
import edu.cmu.side.viewer.FastListModel;

/**
 *
 * @author  __USER__
 */
public class DocumentListConfigPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	public static enum DocumentListType{
		BASE, ANNOTATION;
	}
	private DocumentListType documentListType = DocumentListType.ANNOTATION;
	
	
	private FastListModel fileListModel = new FastListModel();
	private JButton fileAddButton;
	private JButton fileRemoveButton;
	private JComboBox subtypeComboBox;
	private JLabel annotationLabel;
	
	private String annotationLabelText = "annotation:";
	
	private boolean nullSubtypeEnabled = false;

	/** Creates new form SIDEAnnotationChooser */
	public DocumentListConfigPanel() {
		yeriInit();
	}

	private File[] getSelectedXmiFileArray(){
		File[] fileArray = new File[fileListModel.size()];
		for(int i=0; i<fileListModel.size(); i++){
			Object o = fileListModel.getElementAt(i);
			fileArray[i] = (File)o;
		}
		return fileArray;
	}
	private String getCheckedSubtype(){
		return (String)this.subtypeComboBox.getSelectedItem();
	}
	
	public DocumentList getDocumentList(){
		File[] xmiFileArray  = this.getSelectedXmiFileArray();
		if(xmiFileArray.length==0){
			return null;
		}
		
		String subtype = this.getCheckedSubtype();
		if(subtype==null && !this.isNullSubtypeEnabled()){
			return null;
		}
		
		DocumentList documentList = new DocumentList(subtype);
		
		for(File xmiFile : xmiFileArray){
			CAS cas = null;
			JCas jCas = null;
			try {
				cas = UIMAToolkit.createSIDECAS(xmiFile);
				jCas = cas.getJCas();
			} catch (Exception ex) {
				/** TODO : handle exception **/
				System.err.println(ex);
				continue;
			}
			
			documentList.getJCasList().add(jCas);
		}
		return documentList;
	}
	
	public void addFileArray(File[] fileArray){
		for(File file : fileArray){
			this.fileListModel.addElement(file);
		}
	}
	
	public void setDocumentList(DocumentList documentList){
		this.fileListModel.removeAllElements();
		
		for(JCas jCas : documentList.getJCasList()){
			SIDEDocumentSetting setting = UIMAToolkit.getSIDEDocumentSetting(jCas);
			File file;
			try { file = new File(new URI(setting.getSourceURI())); }
			catch (URISyntaxException e) { throw new RuntimeException(e); }
			this.fileListModel.addElement(file);
		}
		
		this.refreshPanel();
		
		String subtypeName = documentList.getSubtypeName();
		this.setSubtype(subtypeName);
		
	}
	public void setSubtype(String subtype){
		this.subtypeComboBox.setSelectedItem(subtype);
	}
	
	private void yeriInit() {

		this.setLayout(new RiverLayout());
		
		fileAddButton = new JButton("add");
		fileRemoveButton = new JButton("clear");
		this.add("hfill", new JLabel("xmi file:"));
		this.add("", fileAddButton);
		this.add("", fileRemoveButton);
		
		JEasyList fileList = new JEasyList();
		fileList.setCellRenderer(new DefaultFileListCellRenderer(fileList));
		fileList.setModel(fileListModel);		
		this.add("br hfill ", new JScrollPane(fileList));		
		
		fileAddButton.addActionListener(new SIDEToolkit.XMIFileAddActionListener(this, fileListModel));
		fileRemoveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				fileListModel.clear();
			}
		});
		this.subtypeComboBox = new JComboBox();
		subtypeComboBox.setRenderer(new DefaultListCellRenderer(){
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if(value==null && DocumentListConfigPanel.this.isNullSubtypeEnabled()){
					return super.getListCellRendererComponent(list, "(re-segment using segmentplugin)", index, isSelected, cellHasFocus);
				}
				else if( (value instanceof String) || value==null){
					return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}else{
					throw new UnsupportedOperationException();
				}
			}
		});
		
		subtypeComboBox.addActionListener(subtypeComboBoxActionListener);
		annotationLabel = new JLabel(annotationLabelText);
		this.add("br", annotationLabel);
		this.add("br hfill", subtypeComboBox);
		
		fileListModel.addListDataListener(new ListDataListener() {

			@Override
			public void contentsChanged(ListDataEvent e) {
				DocumentListConfigPanel.this.refreshPanel();
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				DocumentListConfigPanel.this.refreshPanel();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				DocumentListConfigPanel.this.refreshPanel();
			}
		});
	}

	private ActionListener subtypeComboBoxActionListener = new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			DocumentListConfigPanel.this.refreshPanel();
		}
	};
	


	private List<ActionListener> actionListenerList = new ArrayList<ActionListener>();
	public void addActionListner(ActionListener al){
		this.actionListenerList.add(al);
	}
	public void removeActionListener(ActionListener al){
		this.actionListenerList.remove(al);
	}
	
	private int actionEventID = 1;
	public void fireActionEvent(){
		ActionEvent ae = new ActionEvent(this, actionEventID++, "changed!");
		for(ActionListener al : this.actionListenerList){
			al.actionPerformed(ae);
		}
	}
	
	protected void refreshPanel() {
		annotationLabel.setText(annotationLabelText);
		Set<String> subtypeSet = null;
		for (int i = 0; i < fileListModel.getSize(); i++) {
			File xmiFile = (File) fileListModel.get(i);
			CAS cas;
			try {
				cas = UIMAToolkit.createSIDECAS(xmiFile);
				JCas jCas = cas.getJCas();
				Set<String> currentSubtypeSet = null;
				if(this.documentListType==DocumentListType.ANNOTATION){
					currentSubtypeSet = UIMAToolkit.getAnnotationSubtypeSet(jCas);
				}else{
					currentSubtypeSet = UIMAToolkit.getAnnotationBaseSubtypeSet(jCas);
				}
				subtypeSet = CollectionsToolkit.intersection(subtypeSet,
						currentSubtypeSet, null);

			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}

		List<Object> itemList = new ArrayList<Object>();
		if(this.isNullSubtypeEnabled()){ itemList.add(null); }
		if(subtypeSet != null){ itemList.addAll(subtypeSet); }
		
		SwingToolkit.reloadComboBoxContent(subtypeComboBox, itemList.toArray(),
				new ActionListener[]{subtypeComboBoxActionListener}, false);
		
		this.revalidate();
		this.fireActionEvent();
	}

	public boolean isNullSubtypeEnabled() {
		if(nullSubtypeEnabled){ annotationLabelText = "segmentation";} else { annotationLabelText = "annotation:"; }
		return nullSubtypeEnabled;
	}

	public void setNullSubtypeEnabled(boolean nullSubtypeEnabled) {
		this.nullSubtypeEnabled = nullSubtypeEnabled;
	}

	public DocumentListType getDocumentListType() {
		return documentListType;
	}

	public void setDocumentListType(DocumentListType documentListType) {
		this.documentListType = documentListType;
	}

	public String getAnnotationLabelText() {
		return annotationLabelText;
	}

	public void setAnnotationLabelText(String annotationLabelText) {
		this.annotationLabelText = annotationLabelText;
	}
}