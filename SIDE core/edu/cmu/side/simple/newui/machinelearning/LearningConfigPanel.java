package edu.cmu.side.simple.newui.machinelearning;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;

/**
 * This panel allows the user to set up the cross-validation and model building parameters.
 * @author emayfiel
 *
 */
public class LearningConfigPanel extends AbstractListPanel{

	JComboBox tablesList = new JComboBox();
	DefaultComboBoxModel listModel = new DefaultComboBoxModel();

	JRadioButton cvFold = new JRadioButton("CV by Fold");
	JTextField cvNumFolds = new JTextField(3);
	JRadioButton cvFile = new JRadioButton("CV by file");
	JRadioButton testSet = new JRadioButton("Supplied Test Set");
	File selectedTestFile;
	JButton loadFileButton = new JButton("select");
	JLabel testFileName = new JLabel();
	ButtonGroup evalSetting = new ButtonGroup();
	JTextField modelName = new JTextField(5);
	JProgressBar progressBar = new JProgressBar();
	JButton build = new JButton("build model");


	public LearningConfigPanel(){
		tablesList.setModel(listModel);
		evalSetting.add(cvFold);
		evalSetting.add(cvFile);
		evalSetting.add(testSet);
		cvFold.setSelected(true);
		cvNumFolds.setText("10");
		testSet.setEnabled(false);

		build.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FeatureTable trainData = (FeatureTable)tablesList.getSelectedItem();
				LearningPlugin learner = LearningPluginPanel.getSelectedPlugin();
				if(trainData != null && learner != null){
					TrainModelTask task = new TrainModelTask(progressBar, trainData, learner);
					task.execute();
				}
			}
		});
		loadFileButton.setEnabled(false);
		loadFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();

				File loadFolder = Workbench.current.getLoadFolder();
				chooser.setCurrentDirectory(loadFolder==null?SIDEToolkit.csvFolder:loadFolder);
				chooser.setFileFilter(FileToolkit.createExtensionListFileFilter(new String[]{"csv"}, true));
				int selection = chooser.showOpenDialog(LearningConfigPanel.this);
				if(selection!=JFileChooser.APPROVE_OPTION){
					return;
				}
				selectedTestFile = chooser.getSelectedFile();
				refreshPanel();
			}
		});
		cvFold.addActionListener(this);
		cvFile.addActionListener(this);
		testSet.addActionListener(this);
		add("left", new JLabel("Feature Table:"));
		add("left", tablesList);
		add("br left", cvFold);
		add("left", cvNumFolds);
		add("br left", cvFile);
		add("br left", testSet);
		add("left", loadFileButton);
		add("br center", testFileName);

		add("br left", new JLabel("name:"));
		modelName.setText("model");
		add("hfill", modelName);

		add("br center", build);
		add("br hfill", progressBar);
	}

	/**
	 * Model building is split off into a separate thread so that work can continue while training.
	 * @author emayfiel
	 *
	 */
	private class TrainModelTask extends OnPanelSwingTask{

		FeatureTable table;
		LearningPlugin learn;
		Map<String, String> config = new TreeMap<String, String>();
		Map<Integer, Integer> foldsMap = new TreeMap<Integer, Integer>();

		public TrainModelTask(JProgressBar progressBar, FeatureTable t, LearningPlugin l){
			this.addProgressBar(progressBar);
			table = t;
			learn = l;
			if(cvFold.isSelected()){
				Integer folds = Integer.parseInt(cvNumFolds.getText());
				for(int i = 0; i < table.getDocumentList().getSize(); i++){
					foldsMap.put(i, i%folds);
				}
			}else if(cvFile.isSelected()){
				int foldNum = 0;
				Map<String, Integer> folds = new TreeMap<String, Integer>();
				for(int i = 0; i < table.getDocumentList().getSize(); i++){
					String filename = table.getDocumentList().getFilename(i);
					if(!folds.containsKey(filename)){
						folds.put(filename, foldNum++);
					}
					foldsMap.put(i, folds.get(filename));
				}
			}else if(testSet.isSelected()){
				config.put("test-set", selectedTestFile.getAbsolutePath());
			}
		}

		@Override
		protected Void doInBackground(){
			try{
				TrainingResultInterface result = learn.train(table, modelName.getText(), config, foldsMap);
				System.out.println(result);
				SimpleWorkbench.addTrainingResult(result);				
			}catch(Exception e){
				e.printStackTrace();
			}
			fireActionEvent();
			return null;
		}
	}

	@Override
	public void refreshPanel(){
		List<FeatureTable> tables = SimpleWorkbench.getFeatureTables();
		listModel.removeAllElements();
		for(FeatureTable table : tables){
			listModel.addElement(table);
		}
		if(listModel.getSize() > 0 && tablesList.getSelectedIndex() == -1){
			tablesList.setSelectedIndex(0);
		}
		loadFileButton.setEnabled(testSet.isSelected());
		if(selectedTestFile != null){
			testFileName.setText(selectedTestFile.getName());			
		}
	}
}
