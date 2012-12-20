package edu.cmu.side.genesis.view.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.BuildModelControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.ModelEvaluationPlugin;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FeatureTableModel;
import edu.cmu.side.simple.newui.SIDETable;

public class BuildResultPanel extends AbstractListPanel {

	SIDETable featureTable = new SIDETable();
	FeatureTableModel model = new FeatureTableModel();

	public BuildResultPanel(){
		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, new JLabel("Model Evaluation Metrics:"));
		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		featureTable.setShowHorizontalLines(true);
		featureTable.setShowVerticalLines(true);
		JScrollPane tableScroll = new JScrollPane(featureTable);
		add(BorderLayout.CENTER, tableScroll);
	}
	
	public void refreshPanel(){
		model = new FeatureTableModel();
		model.addColumn("Metric");
		model.addColumn("Value");
		if(BuildModelControl.hasHighlightedTrainedModelRecipe()){
			SimpleTrainingResult result = BuildModelControl.getHighlightedTrainedModelRecipe().getTrainingResult();
			Collection<ModelEvaluationPlugin> plugins = BuildModelControl.getModelEvaluationPlugins();
			for(ModelEvaluationPlugin plugin : plugins){
				Map<String, String> keys = plugin.evaluateModelFeatures(result, plugin.generateConfigurationSettings());
				for(String s : keys.keySet()){
					Object[] row = new Object[2];
					row[0] = s;
					row[1] = keys.get(s);
					model.addRow(row);
				}
			}
		}
		featureTable.setModel(model);
	}
	
}
