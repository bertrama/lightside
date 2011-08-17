package edu.cmu.side.simple.newui.machinelearning;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.newui.AbstractListPanel;

/** In progress. Will hold the output of the model building process, including accuracy, kappa, etc. */
public class LearningOutputPanel extends AbstractListPanel{	
	private static final long serialVersionUID = -3937539493535019798L;

	private JComboBox outputs = new JComboBox();
	private JTextArea text = new JTextArea();
	
	/** Retrieved from ModelListPanel */
	private SimpleTrainingResult trainingResult = null;
	
	public LearningOutputPanel(){
		add("left", new JLabel("View output: "));
		add("hfill", outputs);
		outputs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshPanel();
			}
		});
		scroll = new JScrollPane(text);
		add("br hfill vfill", scroll);
	}

	public void refreshPanel(){
		SimpleTrainingResult clickedModel = ModelListPanel.getSelectedTrainingResult();
		if(clickedModel != trainingResult){
			trainingResult = clickedModel;
			Map<String, String> evaluation = trainingResult.getEvaluation();
			if(evaluation != null){
				outputs.removeAllItems();
				for(String key : evaluation.keySet()){
					outputs.addItem(key);
				}
				if(outputs.getItemCount() > 0){
					try{
						outputs.setSelectedItem("summary");
					}catch(Exception e){
						System.out.println("Learning plugin should have set a summary field, but didn't.");
						outputs.setSelectedIndex(0);
						e.printStackTrace();
					}
				}
			}
		}
		if(trainingResult != null && outputs.getSelectedIndex() != -1){
			text.setText(trainingResult.getEvaluation().get(outputs.getSelectedItem().toString()));			
		}else{
			text.setText("");
		}
	}
}
