package edu.cmu.side.simple.newui.machinelearning;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.features.FeatureFileManagerPanel;

/**
 * This panel allows the user to set up the cross-validation and model building parameters.
 * @author emayfiel
 *
 */
public class LearningConfigPanel extends AbstractListPanel{

	JComboBox tablesList = new JComboBox();
	DefaultComboBoxModel listModel = new DefaultComboBoxModel();

	JButton halt = new JButton(new ImageIcon("delete.png"));
	boolean halted = false;
	JRadioButton cvFold = new JRadioButton("CV by Fold");
	JTextField cvNumFolds = new JTextField(3);
	JRadioButton cvFile = new JRadioButton("CV by file");
	JRadioButton testSet = new JRadioButton("Supplied Test Set");
	File selectedTestFile;
	JButton loadFileButton = new JButton("Select");
	JLabel testFileName = new JLabel();
	ButtonGroup evalSetting = new ButtonGroup();
	JTextField modelName = new JTextField(5);
	JProgressBar progressBar = new JProgressBar();
	JLabel progressLabel = new JLabel();
	JButton build = new JButton("Build Model");


	public LearningConfigPanel(){
		tablesList.setModel(listModel);
		evalSetting.add(cvFold);
		evalSetting.add(cvFile);
		evalSetting.add(testSet);
		cvFold.setSelected(true);
		cvNumFolds.setText("10");
		testSet.setEnabled(true);

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
		halt.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				halted = true;
				LearningPluginPanel.getSelectedPlugin().stopWhenPossible();
			}
		});
		halt.setEnabled(false);
		loadFileButton.setEnabled(false);
		loadFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();

				chooser.setCurrentDirectory(SimpleWorkbench.csvFolder);
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
		add("br hfill", tablesList);
		add("br left", cvFold);
		add("left", cvNumFolds);
		add("br left", cvFile);
		add("br left", testSet);
		add("left", loadFileButton);
		add("br center", testFileName);

		add("br left", new JLabel("Name:"));
		modelName.setText("model");
		add("hfill", modelName);

		add("br hfill", build);
		add("br hfill", progressBar);
		add("", halt);
		add("br left", progressLabel);
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
		Map<Integer, Integer> foldsMap = null;

		public TrainModelTask(JProgressBar progressBar, FeatureTable t, LearningPlugin l){
			this.addProgressBar(progressBar);
			table = t;
			learn = l;
			if(cvFold.isSelected()){
				Integer folds = Integer.parseInt(cvNumFolds.getText());
				foldsMap = table.getDocumentList().getFoldsMapByNum(folds);
			}else if(cvFile.isSelected()){
				foldsMap = table.getDocumentList().getFoldsMapByFile();
			}else if(testSet.isSelected()){
				config.put("test-set", selectedTestFile.getAbsolutePath());
			}
		}

		@Override
		protected Void doInBackground(){
			try{
				halt.setEnabled(true);
				TrainingResultInterface result = learn.train(table, modelName.getText(), config, foldsMap, progressLabel);
				if(halted){
					halted = false;
				}else{
					SimpleWorkbench.addTrainingResult(result);					
					List<TrainingResultInterface> trs = SimpleWorkbench.getTrainingResults();
					String name = "model";
					boolean available = true;
					for(TrainingResultInterface tr : trs){
						if(name.equals(tr.toString())) available = false;
					}
					if(!available){
						int count = 0;
						while(!available){
							count++;
							name = "model" + count;
							available = true;
							for(TrainingResultInterface tr : trs){
								if(name.equals(tr.toString())) available = false;
							}
						}
					}
					modelName.setText(name);
				}
				halt.setEnabled(false);
			}catch(Exception e){
				JTextArea text = new JTextArea();
				text.setText(e.toString());
				JOptionPane.showMessageDialog(LearningConfigPanel.this, new JScrollPane(text), "Model Building Failed", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			fireActionEvent();
			return null;
		}
	}

	@Override
	public void refreshPanel(){
		List<FeatureTable> tables = SimpleWorkbench.getFeatureTables();
		int prevIndex = tablesList.getSelectedIndex();
		listModel.removeAllElements();
		for(FeatureTable table : tables){
			listModel.addElement(table);
		}
		if(listModel.getSize() > 0){
			tablesList.setSelectedIndex(Math.max(0, prevIndex));
		}
		loadFileButton.setEnabled(testSet.isSelected());
		if(selectedTestFile != null){
			testFileName.setText(selectedTestFile.getName());			
		}
	}
}
