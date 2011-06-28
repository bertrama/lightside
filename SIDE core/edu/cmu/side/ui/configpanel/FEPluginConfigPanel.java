/*
 * FEPluginConfigPanel.java
 *
 * Created on __DATE__, __TIME__
 */

package edu.cmu.side.ui.configpanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.swing.JCheckBoxList;
import com.yerihyo.yeritools.swing.SimpleOKDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.ResultOption;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.Workbench;
import edu.cmu.side.plugin.FEPlugin;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;


/**
 *
 * @author  __USER__
 */
public class FEPluginConfigPanel extends javax.swing.JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;

	private DocumentList documents;
	
	public void setDocumentList(DocumentList d){
		documents = d;
	}
	
	public static Component createNoConfigPanel() {
		JLabel label = new JLabel("No Config Panel");
		label.setHorizontalAlignment(JLabel.CENTER);
		//		label.setHorizontalTextPosition(JLabel.CENTER);
		return label;
	}


	/** Creates new form FEPluginConfigPanel */
	public FEPluginConfigPanel() {
//		initComponents();
		yeriInit();
	}

	public static void main(String[] args) {
//		PluginManager pluginManager = new PluginManager();
		
		FEPluginConfigPanel panel = new FEPluginConfigPanel();
		TestFrame testFrame = new TestFrame(panel);
		testFrame.setSize(new Dimension(800, 600));
		testFrame.showFrame();
	}

	private DefaultListModel listModel;
	private JCheckBoxList fePluginCheckboxList;
	private FEPlugin[] fePluginArray;
	private JCheckBox rareCheckbox;
	private JTextField thresholdTextArea;
	private JLabel thresholdLabel;
	
	public void updateFEPluginArray(FEPlugin[] updatePluginArray){
		
		if(this.fePluginArray==null){ this.fePluginArray = Workbench.current.pluginManager.getFEPluginArray(); }
		this.fePluginArray = CollectionsToolkit.updateObjectsOfSameClass(this.fePluginArray, updatePluginArray);
		
		listModel.removeAllElements();
		for (FEPlugin fePlugin : fePluginArray) {
			String className = fePlugin.getClass().getName();
			JCheckBox checkBox = new JCheckBox(className);
			
			checkBox.setSelected(CollectionsToolkit.containsByEquals(updatePluginArray, fePlugin));
			
			listModel.add(listModel.getSize(), checkBox);
		}
	}
	
	@Override
	public void repaint(){
		super.repaint();
	}
	
	protected void yeriInit() {
		this.setLayout(new RiverLayout());
		
		listModel = new DefaultListModel();
		fePluginCheckboxList = new JCheckBoxList();
		fePluginCheckboxList.setModel(listModel);
		
		this.add("hfill", new JLabel("feature extractor plugins:"));
		this.add("br hfill", new JScrollPane(fePluginCheckboxList));
		
		fePluginCheckboxList.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt){
				int index = fePluginCheckboxList.locationToIndex(evt.getPoint());
                if(index == -1){ return; }
                if(evt.getButton()!=MouseEvent.BUTTON3){ return; }
                fePluginArray[index].setContext(documents);
                Component configUI = fePluginArray[index].getConfigurationUI();
                ResultOption resultOption = SimpleOKDialog.show(fePluginCheckboxList, "config", configUI);
                if(resultOption!=ResultOption.APPROVE_OPTION){
                	return;
                }
                fePluginArray[index].uiToMemory();
			}
		});
		rareCheckbox = new JCheckBox("remove rare features");
		rareCheckbox.setSelected(true);
		this.add("br hfill", rareCheckbox);
		thresholdLabel = new JLabel("threshold: ");
		thresholdTextArea = new JTextField();
		thresholdTextArea.setText("5");
		this.add("br left", thresholdLabel);
		this.add("hfill", thresholdTextArea);
		rareCheckbox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				thresholdLabel.setVisible(rareCheckbox.isSelected());
				thresholdTextArea.setVisible(rareCheckbox.isSelected());
			}
		});
	}
	
	public boolean isRareFilterEnabled(){
		return rareCheckbox.isSelected();
	}
	
	public Integer rareFilterThreshold(){
		if(isRareFilterEnabled()){
			return Integer.parseInt(thresholdTextArea.getText());
		}else return -1;
	}
	
	public FEPlugin[] getCheckedFEPlugin(){
		int[] indices = fePluginCheckboxList.getCheckedIndices();
		return CollectionsToolkit.getObjectsAtIndices(fePluginArray, indices);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		setDocumentList(((DocumentListConfigPanel)ae.getSource()).getDocumentList());
	}
}