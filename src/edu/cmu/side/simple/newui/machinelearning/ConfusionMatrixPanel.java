package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.SIDETable;

/**
 * Stores the top left panel of the LearningRightPanel. This has two confusion matrices: one for the overall model,
 * and one as a distribution of a single feature, as populated by the MiniErrorAnalysisPanel.
 */
public class ConfusionMatrixPanel extends AbstractListPanel{
	private static final long serialVersionUID = 7462007601937212806L;
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
		setLayout(new GridLayout(2,1));
		JPanel modelPanel = new JPanel(new RiverLayout());
		modelPanel.add("left", new JLabel("Model Confusion Matrix:"));
		matrixDisplay.setModel(matrixModel);
		matrixDisplay.setBorder(BorderFactory.createLineBorder(Color.gray));
		matrixDisplay.setShowHorizontalLines(true);
		matrixDisplay.setShowVerticalLines(true);
		matrixDisplay.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e){
				int row = matrixDisplay.getSelectedRow();
				int col = matrixDisplay.getSelectedColumn();
				if(trainingResult==null||row<0||col<=0) return;
				selectedCell = new Integer[]{row, col-1};
				fireActionEvent();
			}
		});
		scroll = new JScrollPane(matrixDisplay);
		scroll.setPreferredSize(new Dimension(300,100));
		modelPanel.add("br hfill", scroll);

		JPanel zoomPanel = new JPanel(new RiverLayout());
		zoomPanel.add("left", new JLabel("Highlighted Feature Distribution:"));
		zoomMatrixDisplay.setModel(zoomMatrixModel);
		zoomMatrixDisplay.setShowHorizontalLines(true);
		zoomMatrixDisplay.setShowVerticalLines(true);
		JScrollPane zoomScroll = new JScrollPane(zoomMatrixDisplay);
		zoomScroll.setPreferredSize(new Dimension(300,100));
		zoomPanel.add("br hfill", zoomScroll);
		zoomPanel.add("br", selectedFeatureName);
		add(modelPanel);
		add(zoomPanel);
	}

	/**
	 * The two confusion matrices are updated in parallel to keep it all in one loop.
	 * I'm not sure whether that justifies the confusion of bouncing back and forth between them in the code.
	 * Thoughts?
	 */
	public void refreshPanel(){
		SimpleTrainingResult clicked = ModelListPanel.getSelectedTrainingResult();
		Feature highlighted = MiniErrorAnalysisPanel.getSelectedFeature();
		matrixModel = new DefaultTableModel();
		zoomMatrixModel = new DefaultTableModel();
		matrixModel.addColumn("Act \\ Pred");
		zoomMatrixModel.addColumn("Act \\ Pred");
		if(highlighted == null){
			selectedFeatureName.setText("Choose a feature to highlight.");
		}else{
			selectedFeatureName.setText("Distribution of feature " + highlighted.getFeatureName());
		}
		if(clicked != null){
			trainingResult = clicked;
			selectedFeature = highlighted;
			DecimalFormat print = new DecimalFormat("#.###");
			switch(trainingResult.getEvaluationTable().getClassValueType()){
			case NOMINAL:
			case BOOLEAN:
				String[] labels = trainingResult.getDocumentList().getLabelArray();
				for(String s : labels){
					matrixModel.addColumn(s);
					zoomMatrixModel.addColumn(s);
				}
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
				break;
			case NUMERIC:
				for(int i = 1; i <= 5; i++){
					matrixModel.addColumn("Q"+i);
					zoomMatrixModel.addColumn("Q"+i);
				}
				for(int i = 1; i <= 5; i++){
					Object[] row = new Object[6];
					Object[] zoomRow = new Object[6];
					row[0] = "Q"+i;
					zoomRow[0] = "Q"+i;
					for(int j = 1; j <= 5; j++){
						List<Integer> cellIndices = trainingResult.getConfusionMatrixCell("Q"+j, "Q"+i);
						row[j] = cellIndices.size();
						if(highlighted != null){
							zoomRow[j] = print.format(trainingResult.getAverageValue(cellIndices, highlighted));
						}
					}
					matrixModel.addRow(row);
					zoomMatrixModel.addRow(zoomRow);
				}
				break;
			}
			final int topRow = matrixDisplay.getSelectedRow();
			final int topCol = matrixDisplay.getSelectedColumn();
			final int botRow = zoomMatrixDisplay.getSelectedRow();
			final int botCol = zoomMatrixDisplay.getSelectedColumn();
//			generateDistributionsRow(topRow, topCol);
			matrixDisplay.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
				public Component getTableCellRendererComponent(JTable table, Object value,
			            boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
					DefaultTableCellRenderer rend = new DefaultTableCellRenderer();
					if(vColIndex > 0 && rowIndex == topRow && vColIndex == topCol){
						rend.setBackground(new Color(0,0,102));
						rend.setForeground(Color.white);
					}else if(vColIndex > 0 && ModelListPanel.getSelectedTrainingResult() != null && ModelListPanel.getSelectedTrainingResult().getDocumentList() != null){
						double total = 0.0+ModelListPanel.getSelectedTrainingResult().getDocumentList().getSize();
						Integer intensity = ((Double)(255.0*(Double.parseDouble(table.getValueAt(rowIndex, vColIndex).toString())/total))).intValue();
						rend.setBackground(new Color(255-intensity, 255-intensity,255));
						rend.setForeground(Color.black);
					}
					rend.setText(table.getValueAt(rowIndex, vColIndex).toString());
					return rend;
				}
			});
		}
		matrixDisplay.setModel(matrixModel);
		zoomMatrixDisplay.setModel(zoomMatrixModel);
		repaint();
	}

	private void generateDistributionsRow(int topRow, int topCol) {
		if(selectedFeature != null && topRow >= 0 && topCol >= 1){
			Object[] modelDistr = new Object[matrixModel.getColumnCount()];
			modelDistr[0] = "Distr:";
			for(int i = 1; i < modelDistr.length; i++){
				double totalCounts = 0.0;
				double nonNormalized = 0.0;
				for(int j = 0; j < matrixModel.getRowCount(); j++){
					double c = Double.parseDouble(matrixModel.getValueAt(j, i).toString());
					double p = Double.parseDouble(zoomMatrixModel.getValueAt(j, i).toString());
					totalCounts += c;
					nonNormalized += (c*p);
				}
				modelDistr[i] = nonNormalized/totalCounts;
				System.out.print(modelDistr[i] + ", ");
			}
			zoomMatrixModel.addRow(modelDistr);	
		}
	}

	/** This is called by the MiniErrorAnalysisPanel */
	public static Integer[] getSelectedCell(){
		return selectedCell;
	}
}
