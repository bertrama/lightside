package edu.cmu.side.genesis.view.extract;

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
import edu.cmu.side.simple.TableEvaluationPlugin;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.SIDETable;

public class ExtractTableDisplayPanel extends AbstractListPanel {

	SIDETable featureTable = new SIDETable();
	DefaultTableModel model = new DefaultTableModel();
	public ExtractTableDisplayPanel(){
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

	public void refreshPanel(){
		if(ExtractFeaturesControl.hasHighlightedFeatureTable()){
			int countTrues = 0;
			for(TableEvaluationPlugin plug : ExtractFeaturesControl.getTableEvaluationPlugins().keySet()){
				for(String s : ExtractFeaturesControl.getTableEvaluationPlugins().get(plug).keySet()){
					if(ExtractFeaturesControl.getTableEvaluationPlugins().get(plug).get(s)){
						countTrues++;
					}
				}
			}
			if(countTrues+1 != model.getColumnCount() || model.getRowCount() != ExtractFeaturesControl.getHighlightedFeatureTableRecipe().getFeatureTable().getFeatureSet().size()){
				FeatureTable table = ExtractFeaturesControl.getHighlightedFeatureTableRecipe().getFeatureTable();
				model = new DefaultTableModel();
				model.addColumn("Feature");
				int rowCount = 1;
				Map<TableEvaluationPlugin, Map<String, Map<Feature, Comparable>>> evals = new HashMap<TableEvaluationPlugin, Map<String, Map<Feature, Comparable>>>();
				for(TableEvaluationPlugin plug : ExtractFeaturesControl.getTableEvaluationPlugins().keySet()){
					evals.put(plug, new TreeMap<String, Map<Feature, Comparable>>());
					System.out.println(plug.getOutputName() + " Evaluation ETDP62");
					for(String s : ExtractFeaturesControl.getTableEvaluationPlugins().get(plug).keySet()){
						System.out.print(s + ", " + ExtractFeaturesControl.getTableEvaluationPlugins().get(plug).get(s) + " ");
						if(ExtractFeaturesControl.getTableEvaluationPlugins().get(plug).get(s)){
							model.addColumn(s);
							System.out.println(s + " ADDED COLUMN ETDP67");
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
