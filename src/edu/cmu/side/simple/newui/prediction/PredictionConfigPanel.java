package edu.cmu.side.simple.newui.prediction;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class PredictionConfigPanel extends AbstractListPanel{

	JComboBox modelsList = new JComboBox();
	DefaultComboBoxModel modelsListModel = new DefaultComboBoxModel();
	JTextField annotationName = new JTextField();
	JButton predictButton = new JButton("predict");
	public PredictionConfigPanel(){
		setLayout(new RiverLayout());
		setPreferredSize(new Dimension(280,600));
		add("left", new JLabel("Model to apply:"));
		add("br hfill", modelsList);
		add("br left", new JLabel("Annotation name:"));
		add("br hfill", annotationName);
		add("br center", predictButton);
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
				PredictionFileSelectPanel.setPredictionDocuments(
						((SimpleTrainingResult)modelsListModel.getSelectedItem()).predictLabels(
								annotationName.getText(), 
								PredictionFileSelectPanel.getPredictionDocuments()).getDocumentList());
				PredictionConfigPanel.this.actionPerformed(null);
			}
		});
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
