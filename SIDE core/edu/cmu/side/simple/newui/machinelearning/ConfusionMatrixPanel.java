package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.SIDETable;

/**
 * Stores the top left panel of the LearningRightPanel. This has two confusion matrices: one for the overall model,
 * and one as a distribution of a single feature, as populated by the MiniErrorAnalysisPanel.
 */
public class ConfusionMatrixPanel extends AbstractListPanel{

	private SIDETable matrixDisplay = new SIDETable();
	private DefaultTableModel matrixModel = new DefaultTableModel();
	
	private SIDETable zoomMatrixDisplay = new SIDETable();
	private DefaultTableModel zoomMatrixModel = new DefaultTableModel();
	
	private SimpleTrainingResult trainingResult = null;
	private static Integer[] selectedCell = {-1,-1};

	/** This is updated from the MiniErrorAnalysisPanel */
	private Feature selectedFeature = null;
	
	private JLabel selectedFeatureName = new JLabel("Select a feature to fill matrix.");
	
	public ConfusionMatrixPanel(){
		add("left", new JLabel("Model confusion matrix:"));
		matrixDisplay.setModel(matrixModel);
		matrixDisplay.setBorder(BorderFactory.createLineBorder(Color.gray));
		matrixDisplay.setShowHorizontalLines(true);
		matrixDisplay.setShowVerticalLines(true);
		matrixDisplay.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				int row = matrixDisplay.getSelectedRow();
				int col = matrixDisplay.getSelectedColumn();
				if(trainingResult==null||row<0||col<=0) return;
				selectedCell = new Integer[]{row, col-1};
				fireActionEvent();
			}
		});
		matrixDisplay.setRowSelectionAllowed(false);
		scroll = new JScrollPane(matrixDisplay);
		scroll.setPreferredSize(new Dimension(300,100));
		add("br hfill", scroll);
		
		add("br br left", new JLabel("Highlighted feature distribution matrix:"));
		zoomMatrixDisplay.setModel(zoomMatrixModel);
		zoomMatrixDisplay.setBorder(BorderFactory.createLineBorder(Color.gray));
		zoomMatrixDisplay.setShowHorizontalLines(true);
		zoomMatrixDisplay.setShowVerticalLines(true);
		zoomMatrixDisplay.setRowSelectionAllowed(false);
		JScrollPane zoomScroll = new JScrollPane(zoomMatrixDisplay);
		zoomScroll.setPreferredSize(new Dimension(300,100));
		add("br hfill", zoomScroll);
		add("br", selectedFeatureName);
	}

	/**
	 * The two confusion matrices are updated in parallel to keep it all in one loop.
	 * I'm not sure whether that justifies the confusion of bouncing back and forth between them in the code.
	 * Thoughts?
	 */
	public void refreshPanel(){
		SimpleTrainingResult clicked = ModelListPanel.getSelectedTrainingResult();
		Feature highlighted = MiniErrorAnalysisPanel.getSelectedFeature();
		if(clicked != trainingResult || selectedFeature != highlighted){
			trainingResult = clicked;
			selectedFeature = highlighted;
			if(selectedFeature == null){
				selectedFeatureName.setText("Select a feature to fill matrix.");
			}else{
				selectedFeatureName.setText("Distribution of feature " + selectedFeature.getFeatureName());
			}
			matrixModel = new DefaultTableModel();
			zoomMatrixModel = new DefaultTableModel();
			matrixModel.addColumn("Act \\ Pred");
			zoomMatrixModel.addColumn("Act \\ Pred");
			String[] labels = trainingResult.getDocumentList().getLabelArray();
			for(String s : labels){
				matrixModel.addColumn(s);
				zoomMatrixModel.addColumn(s);
			}
			DecimalFormat print = new DecimalFormat("#.###");
			for(String act : labels){
				Object[] row = new Object[labels.length+1];
				Object[] zoomRow = new Object[labels.length+1];
				row[0] = act;
				zoomRow[0] = act;
				int index = 1;
				for(String pred : labels){
					List<Integer> cellIndices = trainingResult.getConfusionMatrixCell(pred, act);
					row[index] = cellIndices.size();
					if(highlighted != null){
						zoomRow[index] = print.format(trainingResult.getAverageValue(cellIndices, highlighted));
					}
					index++;
				}
				matrixModel.addRow(row);
				zoomMatrixModel.addRow(zoomRow);
			}
			matrixDisplay.setModel(matrixModel);
			zoomMatrixDisplay.setModel(zoomMatrixModel);
		}
		repaint();
	}
	
	/** This is called by the MiniErrorAnalysisPanel */
	public static Integer[] getSelectedCell(){
		return selectedCell;
	}
}
