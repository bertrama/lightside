package edu.cmu.side.view.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.plugin.ModelMetricPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.FeatureTableModel;
import edu.cmu.side.view.util.SIDETable;

public class GenericModelMetricPanel extends AbstractListPanel{

	SIDETable featureTable = new SIDETable();
	FeatureTableModel model = new FeatureTableModel();

	public GenericModelMetricPanel(){
		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, new JLabel("Model Evaluation Metrics:"));
		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		featureTable.setShowHorizontalLines(true);
		featureTable.setShowVerticalLines(true);
		JScrollPane tableScroll = new JScrollPane(featureTable);
		add(BorderLayout.CENTER, tableScroll);
	}

	public void refreshPanel(Recipe recipe){
		model = new FeatureTableModel();
		model.addColumn("Metric");
		model.addColumn("Value");
		if(recipe != null && recipe.getTrainingResult() != null){
			TrainingResult result = recipe.getTrainingResult();
			Collection<ModelMetricPlugin> plugins = BuildModelControl.getModelEvaluationPlugins();
			for(ModelMetricPlugin plugin : plugins){
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