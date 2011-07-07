package edu.cmu.side.simple.newui.features;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.SIDETable;

import se.datadosen.component.RiverLayout;

/**
 * Feature table details appear in this panel, as a giant JTable full of evaluation columns.
 * @author emayfiel
 *
 */
public class FeatureTablePanel extends JPanel{
	private static final long serialVersionUID = 8864372850829867678L;

	private JLabel selectedTableName = new JLabel();
	private JLabel selectedTableSize = new JLabel();
	private JTextField filterField = new JTextField();
	private JButton filterButton = new JButton("filter");
	private SIDETable featureTable = new SIDETable();
	private DefaultTableModel tableModel = new DefaultTableModel();
	private FeatureTable currentFeatureTable = null;
	
	private JButton activateButton = new JButton("deactivate selected");
	private JButton labButton = new JButton("move to lab");
	private JButton freezeButton = new JButton("freeze");
	private JButton exportButton = new JButton("export");
	
	public FeatureTablePanel(){
		setLayout(new RiverLayout());
		add("left", new JLabel("Feature Table: "));
		add("left", selectedTableName);
		add("left", selectedTableSize);
		filterField.setBorder(BorderFactory.createLineBorder(Color.gray));
		add("br hfill", filterField);
		add("right", filterButton);
		featureTable.setModel(tableModel);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		featureTable.setShowHorizontalLines(true);
		featureTable.setShowVerticalLines(true);
		JScrollPane tableScroll = new JScrollPane(featureTable);
		tableScroll.setPreferredSize(new Dimension(700, 275));
		add("br hfill", tableScroll);
		add("br left", activateButton);
		add("left", labButton);
		add("left", freezeButton);
		add("left", exportButton);
		featureTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	public void refreshPanel(){
		FeatureTable table = FeatureTableListPanel.getSelectedFeatureTable();
		if(table != null && table != currentFeatureTable){
			Map<String, Map<Feature, Comparable>> evals = table.getEvaluations();
			selectedTableName.setText(table.getTableName());
			selectedTableSize.setText("("+table.getFeatureSet().size() + " features)");
			tableModel = new DefaultTableModel();
			tableModel.addColumn("from");
			tableModel.addColumn("feature name");
			tableModel.addColumn("type");
			for(String eval : evals.keySet()){
				tableModel.addColumn(eval);
			}
			for(Feature f : table.getFeatureSet()){
				Object[] row = new Object[3+evals.keySet().size()];
				row[0] = f.getExtractorPrefix();
				row[1] = f.getFeatureName();
				row[2] = f.getFeatureType();
				int i = 3;
				for(String eval : evals.keySet()){
					row[i++] = evals.get(eval).get(f);
				}
				tableModel.addRow(row);
			}
			currentFeatureTable = table;
			featureTable.setModel(tableModel);
			featureTable.setRowSorter(new TableRowSorter<TableModel>(tableModel));
			TableColumnModel columnModel = featureTable.getColumnModel();
			columnModel.getColumn(0).setPreferredWidth(30);
			columnModel.getColumn(1).setPreferredWidth(120);
			featureTable.setColumnModel(columnModel);
		}
		repaint();
	}

}
