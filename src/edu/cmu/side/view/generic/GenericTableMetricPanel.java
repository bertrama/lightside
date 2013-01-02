package edu.cmu.side.view.generic;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.plugin.TableMetricPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.FeatureTableModel;
import edu.cmu.side.view.util.SIDETable;

public class GenericTableMetricPanel extends AbstractListPanel {

	SIDETable featureTable = new SIDETable();
	FeatureTableModel model = new FeatureTableModel();
	public GenericTableMetricPanel(){
		setLayout(new RiverLayout());
		JLabel label = new JLabel("Features:");
		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		JScrollPane tableScroll = new JScrollPane(featureTable);
		add("left", label);
		add("br hfill vfill", tableScroll);
	}

	public void refreshPanel(FeatureTable table, Map<TableMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins){
		model = new FeatureTableModel();
		if(table != null){
			int countTrues = 0;
			for(TableMetricPlugin plug : tableEvaluationPlugins.keySet()){
				for(String s : tableEvaluationPlugins.get(plug).keySet()){
					if(tableEvaluationPlugins.get(plug).get(s)){
						countTrues++;
					}
				}
			}
			if(countTrues+1 != model.getColumnCount() || model.getRowCount() != table.getFeatureSet().size()){
				model.addColumn("Feature");
				boolean[] mask = new boolean[table.getDocumentList().getSize()];
				for(int i = 0; i < mask.length; i++) mask[i] = true;
				int rowCount = 1;
				Map<TableMetricPlugin, Map<String, Map<Feature, Comparable>>> evals = new HashMap<TableMetricPlugin, Map<String, Map<Feature, Comparable>>>();
				for(TableMetricPlugin plug : tableEvaluationPlugins.keySet()){
					evals.put(plug, new TreeMap<String, Map<Feature, Comparable>>());
					for(String s : tableEvaluationPlugins.get(plug).keySet()){
						if(tableEvaluationPlugins.get(plug).get(s)){
							model.addColumn(s);
							rowCount++;
							Map<Feature, Comparable> values = plug.evaluateTableFeatures(table, mask, s);
							evals.get(plug).put(s, values);
						}
					}
				}
				for(Feature f : table.getFeatureSet()){
					Object[] row = new Object[rowCount];
					row[0] = f;
					int r = 1;
					for(TableMetricPlugin tep : evals.keySet()){
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
}
