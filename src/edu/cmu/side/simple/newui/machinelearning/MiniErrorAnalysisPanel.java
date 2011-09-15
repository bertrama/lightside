package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Dimension;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.ModelEvaluationPlugin;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.EvalTableModel;
import edu.cmu.side.simple.newui.SIDETable;

/**
 * Holds some basic output about a model and its features. Uses concepts from the old error analysis panel.
 * @author emayfiel
 *
 */
public class MiniErrorAnalysisPanel extends AbstractListPanel{
	private static final long serialVersionUID = -7752641565734779041L;

	private JComboBox metricsList = new JComboBox();
	/** Retrieved from ConfusionMatrixPanel */
	private Integer[] localCell = {-1, -1};
	/** Retrieved from ModelListPanel */
	private SimpleTrainingResult trainingResult = null;

	private SIDETable featureTable = new SIDETable();
	private EvalTableModel tableModel = new EvalTableModel();
	private TableRowSorter<TableModel> sorter;
	private JLabel selectedLabel = new JLabel("Select a cell to evaluate confusion. ");
	private ModelEvaluationPlugin selectedPlugin = null;
	private static Feature selectedFeature = null;

	public MiniErrorAnalysisPanel(){
		tableModel.addColumn("Feature name");
		featureTable.setModel(tableModel);
		/** Update the confusion matrix panel when a feature is clicked. */
		featureTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				selectedFeature = (Feature)featureTable.getValueAt(featureTable.getSelectedRow(), 0);
				fireActionEvent();
			}
		});
		scroll = new JScrollPane(featureTable);
		scroll.setPreferredSize(new Dimension(300,200));
		add("left", new JLabel("Evaluate with: "));
		add("left", metricsList);
		add("br left", selectedLabel);
		add("br hfill", scroll);
	}

	/**
	 * Based on clicked confusion matrix cell, calls model evaluators to get the most
	 * confusing features in that cell. Then the UI is updated, tables are sorted, etc.
	 */
	public void refreshPanel(){
		SIDEPlugin[] evaluators = SimpleWorkbench.getPluginsByType("model_evaluation");
		if(evaluators.length != metricsList.getItemCount()){
			metricsList.removeAllItems();
			for(SIDEPlugin eval : evaluators){
				metricsList.addItem(eval);
			}
		}
		SimpleTrainingResult clicked = ModelListPanel.getSelectedTrainingResult();
		Integer[] clickedCell = ConfusionMatrixPanel.getSelectedCell();
		ModelEvaluationPlugin clickedPlugin = (ModelEvaluationPlugin)metricsList.getSelectedItem();			
		if(clickedPlugin != selectedPlugin || clicked != trainingResult || clickedCell[0] != localCell[0] || clickedCell[1] != localCell[1]){
			trainingResult = clicked;
			localCell = clickedCell;
			selectedPlugin = clickedPlugin;
			tableModel = new EvalTableModel();
			tableModel.addColumn("Feature name");
			if(selectedPlugin != null && trainingResult != null && localCell[0] >= 0 && localCell[1] >= 0){
				tableModel.addColumn(selectedPlugin.getOutputName());
				String act = ""; String pred = "";
				switch(clicked.getFeatureTable().getClassValueType()){
				case NOMINAL:
				case BOOLEAN:
					act = trainingResult.getDocumentList().getLabelArray()[localCell[0]];
					pred = trainingResult.getDocumentList().getLabelArray()[localCell[1]];
					break;
				case NUMERIC:
					act = "Q"+(localCell[0]+1);
					pred = "Q"+(localCell[1]+1);
					break;
				}
				Map<String, String> settings = new TreeMap<String, String>();
				settings.put("pred", pred);
				settings.put("act", act);
				switch(trainingResult.getFeatureTable().getClassValueType()){
				case NOMINAL:
				case BOOLEAN:
					selectedLabel.setText("Predicted: " + pred + ", Actual: " + act);
					break;
				case NUMERIC:
					selectedLabel.setText("Predicted: Q" + (localCell[1]+1) + ", Actual: Q" + (localCell[0]+1));
				}
				Map<Feature, Double> featureEval = selectedPlugin.evaluateModelFeatures(trainingResult, settings);
				for(Feature f : trainingResult.getFeatureTable().getFeatureSet()){
					Object[] row = new Object[]{f, featureEval.get(f)};
					tableModel.addRow(row);
				}					
			}else{
				selectedLabel.setText("Select a cell to evaluate confusion. ");
				if(trainingResult != null){
					for(Feature f : trainingResult.getFeatureTable().getFeatureSet()){
						Object[] row = new Object[]{f};
						tableModel.addRow(row);
					}					
				}
			}
			featureTable.setModel(tableModel);
			sorter = new TableRowSorter<TableModel>(tableModel);
			featureTable.setRowSorter(sorter);
			if(tableModel.getColumnCount()>1){
				ArrayList<RowSorter.SortKey> sortKey = new ArrayList<RowSorter.SortKey>();
				sortKey.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
				sorter.setSortKeys(sortKey);
				sorter.sort();				
			}
		}

	}

	public static Feature getSelectedFeature(){
		return selectedFeature;
	}
}
