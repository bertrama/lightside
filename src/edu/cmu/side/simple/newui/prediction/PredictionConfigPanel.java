package edu.cmu.side.simple.newui.prediction;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.simple.LearningPlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.machinelearning.LearningConfigPanel;

public class PredictionConfigPanel extends AbstractListPanel{

	JComboBox modelsList = new JComboBox();
	DefaultComboBoxModel modelsListModel = new DefaultComboBoxModel();
	JTextField annotationName = new JTextField();
	JButton predictButton = new JButton("Predict");
	JProgressBar progress = new JProgressBar();
	public PredictionConfigPanel(){
		setLayout(new RiverLayout());
		setPreferredSize(new Dimension(280,600));
		add("left", new JLabel("Model to apply:"));
		add("br hfill", modelsList);
		add("br left", new JLabel("Annotation Name:"));
		add("br hfill", annotationName);
		add("br hfill", predictButton);
		add("br hfill", progress);
		modelsList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(modelsList.getSelectedIndex()> -1){
					annotationName.setText("predict_"+((TrainingResultInterface)modelsList.getSelectedItem()).getDocumentList().getCurrentAnnotation());
				}
			}
		});
		predictButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PredictLabelsTask task = new PredictLabelsTask(progress, 
						((SimpleTrainingResult)modelsListModel.getSelectedItem()), 
						annotationName.getText(), 
						PredictionFileSelectPanel.getPredictionDocuments());
				task.execute();
				PredictionConfigPanel.this.actionPerformed(null);
			}
		});
	}


	/**
	 * Model building is split off into a separate thread so that work can continue while training.
	 * @author emayfiel
	 *
	 */
	private class PredictLabelsTask extends OnPanelSwingTask{
		
		SimpleTrainingResult model;
		String annotName;
		SimpleDocumentList newDocuments;
		
		public PredictLabelsTask(JProgressBar progressBar,SimpleTrainingResult m, String a, SimpleDocumentList n){
			this.addProgressBar(progressBar);
			model = m;
			annotName = a;
			newDocuments = n;
		}

		@Override
		protected Void doInBackground(){
			try{
				PredictionFileSelectPanel.setPredictionDocuments(model.predictLabels(annotName, newDocuments).getDocumentList());		
			}catch(Exception e){
				JOptionPane.showMessageDialog(PredictionConfigPanel.this, "Model building failed. Check the terminal for detail.", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			fireActionEvent();
			return null;
		}
	}


	public void refreshPanel(){
		List<TrainingResultInterface> models = SimpleWorkbench.getTrainingResults();
		int prevIndex = modelsList.getSelectedIndex();
		modelsListModel.removeAllElements();
		for(TrainingResultInterface model : models){
			modelsListModel.addElement(model);
		}
		modelsList.setModel(modelsListModel);
		if(modelsListModel.getSize() > 0){
			modelsList.setSelectedIndex(Math.max(0, prevIndex));
		}
	}
}
