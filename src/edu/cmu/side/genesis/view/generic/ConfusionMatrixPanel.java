package edu.cmu.side.genesis.view.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.SIDETable;
import edu.cmu.side.simple.newui.machinelearning.ModelListPanel;

public class ConfusionMatrixPanel extends AbstractListPanel{
	private SIDETable matrixDisplay = new SIDETable();
	private DefaultTableModel matrixModel = new DefaultTableModel();
	private JLabel selectedFeatureName = new JLabel("Select a Feature");
	private static Integer[] selectedCell = {-1,-1};

	private static double sum = 0.0;
	
	public ConfusionMatrixPanel(){
		setLayout(new GridLayout(2,1));
		JPanel modelPanel = new JPanel(new BorderLayout());
		modelPanel.add(BorderLayout.NORTH, new JLabel("Model Confusion Matrix:"));
		matrixDisplay.setModel(matrixModel);
		matrixDisplay.setBorder(BorderFactory.createLineBorder(Color.gray));
		matrixDisplay.setShowHorizontalLines(true);
		matrixDisplay.setShowVerticalLines(true);
		matrixDisplay.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e){
				int row = matrixDisplay.getSelectedRow();
				int col = matrixDisplay.getSelectedColumn();
				if(row<0||col<=0) return;
				selectedCell = new Integer[]{row, col-1};
			}
		});
		describeScroll = new JScrollPane(matrixDisplay);
		modelPanel.add(BorderLayout.CENTER, describeScroll);
		add(modelPanel);
	}

	public void refreshPanel(Map<String, Map<String, ArrayList<Integer>>> confusion){
		Collection<String> labels = confusion.keySet();
		for(String s : labels){
			matrixModel.addColumn(s);
		}
		sum = 0;
		for(String act : labels){
			Object[] row = new Object[labels.size()+1];
			row[0] = act;
			int index = 1;
			for(String pred : labels){
				List<Integer> cellIndices = confusion.get(pred).get(act);
				sum += confusion.get(pred).get(act).size();
				row[index] = cellIndices.size();
				index++;
			}
			matrixModel.addRow(row);
		}
		matrixDisplay.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
				DefaultTableCellRenderer rend = new DefaultTableCellRenderer();
				if(vColIndex > 0){
					Integer intensity = ((Double)(255.0*(Double.parseDouble(table.getValueAt(rowIndex, vColIndex).toString())/sum))).intValue();
					rend.setBackground(new Color(255-intensity, 255-intensity,255));
					rend.setForeground(Color.black);
				}
				rend.setText(table.getValueAt(rowIndex, vColIndex).toString());
				return rend;
			}
		});

	}
}
