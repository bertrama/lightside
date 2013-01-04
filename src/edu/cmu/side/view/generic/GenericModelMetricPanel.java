package edu.cmu.side.view.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;

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
	JLabel label = new JLabel("Model Evaluation Metrics:");
	Map<String, String> allKeys = new TreeMap<String, String>();
	public void setLabel(String l){
		label.setText(l);
	}
	
	public Map<String, String> getKeys(){
		return allKeys;
	}
	
	public GenericModelMetricPanel(){
		setLayout(new RiverLayout());
		add("left", label);
		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		JScrollPane tableScroll = new JScrollPane(featureTable);
		add("br hfill vfill", tableScroll);
	}

	public void refreshPanel(Recipe recipe){
		model = new FeatureTableModel();
		model.addColumn("Metric");
		model.addColumn("Value");
		if(recipe != null && recipe.getTrainingResult() != null){
			allKeys.clear();
			TrainingResult result = recipe.getTrainingResult();
			Collection<ModelMetricPlugin> plugins = BuildModelControl.getModelEvaluationPlugins();
			for(ModelMetricPlugin plugin : plugins){
				Map<String, String> keys = plugin.evaluateModel(result, plugin.generateConfigurationSettings());
				for(String s : keys.keySet()){
					Object[] row = new Object[2];
					row[0] = s;
					row[1] = keys.get(s);
					model.addRow(row);
				}
				allKeys.putAll(keys);
			}			
		}
		featureTable.setModel(model);
	}

}
