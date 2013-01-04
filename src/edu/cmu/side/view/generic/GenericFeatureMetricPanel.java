package edu.cmu.side.view.generic;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.plugin.FeatureMetricPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.FeatureTableModel;
import edu.cmu.side.view.util.SIDETable;

public abstract class GenericFeatureMetricPanel extends AbstractListPanel {

	SIDETable featureTable = new SIDETable();
	FeatureTableModel model = new FeatureTableModel();
	JTextField text = new JTextField(20);
	public GenericFeatureMetricPanel(){
		setLayout(new RiverLayout());
		JLabel label = new JLabel("Features in Table:");
		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		JScrollPane tableScroll = new JScrollPane(featureTable);
		add("left", label);
		add("br left", new JLabel("Search:"));
		add("hfill", text);
		add("br hfill vfill", tableScroll);
	}

	public void refreshPanel(Recipe recipe, Map<? extends FeatureMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins, boolean[] mask){
		model = new FeatureTableModel();
		if(recipe != null && recipe.getTrainingTable() != null){

			int countTrues = 0;
			for(FeatureMetricPlugin plug : tableEvaluationPlugins.keySet()){
				for(String s : tableEvaluationPlugins.get(plug).keySet()){
					if(tableEvaluationPlugins.get(plug).get(s)){
						countTrues++;
					}
				}
			}
			if(countTrues+1 != model.getColumnCount() || model.getRowCount() != recipe.getTrainingTable().getFeatureSet().size()){
				model.addColumn("Feature");
				int rowCount = 1;
				Map<FeatureMetricPlugin, Map<String, Map<Feature, Comparable>>> evals = new HashMap<FeatureMetricPlugin, Map<String, Map<Feature, Comparable>>>();
				for(FeatureMetricPlugin plug : tableEvaluationPlugins.keySet()){
					evals.put(plug, new TreeMap<String, Map<Feature, Comparable>>());
					for(String s : tableEvaluationPlugins.get(plug).keySet()){
						if(tableEvaluationPlugins.get(plug).get(s)){
							model.addColumn(s);
							rowCount++;
							Map<Feature, Comparable> values = plug.evaluateFeatures(recipe, mask, s, getTargetAnnotation());
							evals.get(plug).put(s, values);
						}
					}
				}
				for(Feature f : recipe.getTrainingTable().getFeatureSet()){
					Object[] row = new Object[rowCount];
					row[0] = f;
					int r = 1;
					for(FeatureMetricPlugin tep : evals.keySet()){
						for(String eval : evals.get(tep).keySet()){
							row[r++] = evals.get(tep).get(eval).get(f);
						}
					}
					model.addRow(row);
				}
			}
		}
		featureTable.setModel(model);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
		featureTable.setRowSorter(sorter);
	}
	
	public abstract String getTargetAnnotation();
}
