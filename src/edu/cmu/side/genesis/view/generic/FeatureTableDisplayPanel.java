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
import edu.cmu.side.simple.newui.SIDETable;

public class FeatureTableDisplayPanel extends AbstractListPanel {

	SIDETable featureTable = new SIDETable();
	DefaultTableModel model = new DefaultTableModel();
	public FeatureTableDisplayPanel(){
		setLayout(new BorderLayout());
		JPanel top = new JPanel(new RiverLayout());

		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		featureTable.setShowHorizontalLines(true);
		featureTable.setShowVerticalLines(true);
		JScrollPane tableScroll = new JScrollPane(featureTable);
		add(BorderLayout.NORTH, top);
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
				model = new DefaultTableModel();
				model.addColumn("Feature");
				int rowCount = 1;
				Map<TableEvaluationPlugin, Map<String, Map<Feature, Comparable>>> evals = new HashMap<TableEvaluationPlugin, Map<String, Map<Feature, Comparable>>>();
				for(TableEvaluationPlugin plug : tableEvaluationPlugins.keySet()){
					evals.put(plug, new TreeMap<String, Map<Feature, Comparable>>());
					System.out.println(plug.getOutputName() + " Evaluation FTDP62");
					for(String s : tableEvaluationPlugins.get(plug).keySet()){
						System.out.print(s + ", " + tableEvaluationPlugins.get(plug).get(s) + " ");
						if(tableEvaluationPlugins.get(plug).get(s)){
							model.addColumn(s);
							System.out.println(s + " ADDED COLUMN FTDP67");
							rowCount++;
							Map<Feature, Comparable> values = plug.evaluateTableFeatures(table, s);
							evals.get(plug).put(s, values);
						}
					}
					System.out.println();
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
