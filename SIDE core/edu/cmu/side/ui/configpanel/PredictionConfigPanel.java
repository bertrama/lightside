package edu.cmu.side.ui.configpanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.ui.managerpanel.TrainingResultManagerPanel;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;

public class PredictionConfigPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	
	public PredictionConfigPanel(){
		yeriInit();
	}
	
	private void yeriInit(){
		this.setLayout(new RiverLayout());
		
		openFileField = new JTextField();
		openFileField.setEditable(false);
		this.add("br hfill", openFileField);
		
		JButton openButton = new JButton("open");
		this.add("right", openButton);
		openButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				
				File loadFolder = Workbench.current.getLoadFolder();
				loadFolder = loadFolder==null?SIDEToolkit.xmiFolder:loadFolder;
				chooser.setCurrentDirectory(loadFolder);
				chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"xmi"}, true));
				
				int result = chooser.showOpenDialog(PredictionConfigPanel.this);
				
				if(result!=JFileChooser.APPROVE_OPTION){
					return;
				}
				
				File xmiFile = chooser.getSelectedFile();
				try {
					jCas = UIMAToolkit.getJCas(UIMAToolkit.createSIDECAS(xmiFile));
				} catch (Exception ex) {
					AlertDialog.show("Error", ex, PredictionConfigPanel.this);
					return;
				}
				refreshPanel();
			}
		});
		
		baseSubtypeComboBox = new JComboBox();
		this.add("br", new JLabel("target base subtype: "));
		this.add(" hfill", baseSubtypeComboBox);
		comboBoxListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshPanel();
			}
		};
		baseSubtypeComboBox.addActionListener(comboBoxListener);
		
		newSubtypeNameField = new JTextField();
		newSubtypeNameField.setEditable(false);
		this.add("br", new JLabel("predicted label subtype: "));
		this.add("hfill", newSubtypeNameField);
		
		this.add("br hfill", new JSeparator());
		
		trainingResultManagerPanel = new TrainingResultManagerPanel();
		trainingResultManagerPanel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshPanel();
			}
		});
		this.add("br hfill vfill", trainingResultManagerPanel);
		
		predictButton = new JButton("predict annotation");
		this.add("br hfill", predictButton);
		predictButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String subtypeName = (String)baseSubtypeComboBox.getSelectedItem();
				if(subtypeName==null){ 
					AlertDialog.show("Error", "Subtype must be selected!", PredictionConfigPanel.this);
					return; 
				}
				
				DocumentList documentList = new DocumentList(subtypeName);
				documentList.getJCasList().add(jCas);
				
				TrainingResult trainingResult = trainingResultManagerPanel.getSelectedTrainingResult();
				System.out.println(trainingResult.getFeatureTableKeyList().size() + " features at click time (predictionconfigpanel)");
				if(trainingResult==null){
					AlertDialog.show("Error", "No TrainingResult selected!", PredictionConfigPanel.this);
					return;
				}
				
				String desiredSubtypeName = newSubtypeNameField.getText();
				
				UIMAToolkit.addPredictionAnnotation(trainingResult, documentList, desiredSubtypeName, true);
			}
		});
	}
	
	protected void refreshPanel() {
		if(jCas!=null){
			String uriString = UIMAToolkit.getXmiURIString(jCas);
			String filename = FileToolkit.getFilename(uriString);
			this.openFileField.setText(filename);
			
			SortedSet<String> baseSubtypeSet = UIMAToolkit.getAnnotationBaseSubtypeSet(jCas);
			
			this.baseSubtypeComboBox.removeActionListener(comboBoxListener);
			SwingToolkit.reloadComboBoxContent(this.baseSubtypeComboBox, baseSubtypeSet.toArray(new String[0]), null, false);
			this.baseSubtypeComboBox.addActionListener(comboBoxListener);
			
			String selectedBaseSubtype = (String)this.baseSubtypeComboBox.getSelectedItem();
			
			TrainingResultInterface trainingResult = trainingResultManagerPanel.getSelectedTrainingResult();
			if(trainingResult!=null && selectedBaseSubtype!=null){
				this.newSubtypeNameField.setText(UIMAToolkit.getRecommendedPredictionAnnotationSubtypeName(trainingResult, selectedBaseSubtype));
			}
		}
	}

	public static void main(String[] args) throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException{
		test01();
	}
	protected static void test01() throws InvalidXMLException, ResourceInitializationException, CollectionException, IOException{
		PredictionConfigPanel predictionConfigPanel = new PredictionConfigPanel();
		
		TestFrame testFrame = new TestFrame(predictionConfigPanel);
		testFrame.setSize(new Dimension(400,300));
		
		SIDEToolkit.FileType.loadAll();
		File xmiFile = new File(SIDEToolkit.xmiFolder, "test_numeric.csv.xmi");
		predictionConfigPanel.jCas = UIMAToolkit.getJCas(UIMAToolkit.createSIDECAS(xmiFile));
		predictionConfigPanel.refreshPanel();
		predictionConfigPanel.baseSubtypeComboBox.setSelectedIndex(0);
		testFrame.showFrame();
		
		SwingToolkit.fireNullActionEvent(predictionConfigPanel.predictButton);
	}
	
	
	private JComboBox baseSubtypeComboBox;
	private JCas jCas;
	
	private transient TrainingResultManagerPanel trainingResultManagerPanel;
	private transient ActionListener comboBoxListener;
	private transient JTextField openFileField;
	private transient JTextField newSubtypeNameField;
	private transient JButton predictButton;

}
