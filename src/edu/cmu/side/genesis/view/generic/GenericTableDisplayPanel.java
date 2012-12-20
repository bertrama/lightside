package edu.cmu.side.genesis.view.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.TableEvaluationPlugin;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FeatureTableModel;
import edu.cmu.side.simple.newui.SIDETable;

public class GenericTableDisplayPanel extends AbstractListPanel {

	SIDETable featureTable = new SIDETable();
	FeatureTableModel model = new FeatureTableModel();
	public GenericTableDisplayPanel(){
		setLayout(new BorderLayout());

		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		featureTable.setShowHorizontalLines(true);
		featureTable.setShowVerticalLines(true);
		JScrollPane tableScroll = new JScrollPane(featureTable);
		add(BorderLayout.CENTER, tableScroll);
	}

	public void refreshPanel(FeatureTable table, Map<TableEvaluationPlugin, Map<String, Boolean>> tableEvaluationPlugins){
		if(table != null){
			int countTrues = 0;
			for(TableEvaluationPlugin plug : tableEvaluationPlugins.keySet()){
				for(String s : tableEvaluationPlugins.get(plug).keySet()){
					if(tableEvaluationPlugins.get(plug).get(s)){
						countTrues++;
					}
				}
			}
			if(countTrues+1 != model.getColumnCount() || model.getRowCount() != table.getFeatureSet().size()){
				model = new FeatureTableModel();
				model.addColumn("Feature");
				boolean[] mask = new boolean[table.getDocumentList().getSize()];
				for(int i = 0; i < mask.length; i++) mask[i] = true;
				int rowCount = 1;
				Map<TableEvaluationPlugin, Map<String, Map<Feature, Comparable>>> evals = new HashMap<TableEvaluationPlugin, Map<String, Map<Feature, Comparable>>>();
				for(TableEvaluationPlugin plug : tableEvaluationPlugins.keySet()){
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
					for(TableEvaluationPlugin tep : evals.keySet()){
						for(String eval : evals.get(tep).keySet()){
							row[r++] = evals.get(tep).get(eval).get(f);
						}
					}
					model.addRow(row);
				}

				featureTable.setModel(model);
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
				featureTable.setRowSorter(sorter);
			}
		}
	}
}
