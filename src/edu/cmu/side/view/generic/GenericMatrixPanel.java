package edu.cmu.side.view.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.SIDETable;

public abstract class GenericMatrixPanel extends AbstractListPanel{
	private SIDETable matrixDisplay = new SIDETable();
	private DefaultTableModel matrixModel = new DefaultTableModel();
	private JLabel selectedFeatureName = new JLabel("Select a Feature");
	private static Integer[] selectedCell = {-1,-1};

	private double sum = 0.0;

	public Double getSum(){
		return sum;
	}
	
	public GenericMatrixPanel(){
		setLayout(new BorderLayout());
		add(BorderLayout.NORTH, new JLabel("Model Confusion Matrix:"));
		matrixDisplay.setModel(matrixModel);
		matrixDisplay.setBorder(BorderFactory.createLineBorder(Color.gray));
		matrixDisplay.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e){
				int row = matrixDisplay.getSelectedRow();
				int col = matrixDisplay.getSelectedColumn();
				if(row<0||col<=0) return;
				selectedCell = new Integer[]{row, col-1};
			}
		});
		matrixDisplay.setDefaultRenderer(java.lang.Object.class, new ConfusionCellRenderer(this));
		describeScroll = new JScrollPane(matrixDisplay);
		add(BorderLayout.CENTER, describeScroll);
	}

	@Override
	public abstract void refreshPanel();

	public void refreshPanel(Map<String, Map<String, List<Integer>>> confusion){
		try{
			Collection<String> labels = new TreeSet<String>();
			for(String s : confusion.keySet()){
				labels.add(s);
				for(String p : confusion.get(s).keySet()){
					labels.add(p);
				}
			}
			matrixModel = new DefaultTableModel();
			matrixModel.addColumn("Act \\ Pred");

			for(String s : labels){
				matrixModel.addColumn(s);
			}
			sum = 0;
			for(String act : labels){
				Object[] row = new Object[labels.size()+1];
				row[0] = act;
				int index = 1;
				for(String pred : labels){
					if(confusion.containsKey(pred) && confusion.get(pred).containsKey(act)){
						List<Integer> cellIndices = confusion.get(pred).get(act);
						sum += confusion.get(pred).get(act).size();
						row[index] = cellIndices.size();					
					}else{
						row[index] = 0;
					}
					index++;
				}
				matrixModel.addRow(row);
			}
			matrixDisplay.setModel(matrixModel);
		} catch(ArrayIndexOutOfBoundsException e){
		}

	}
}

class ConfusionCellRenderer extends DefaultTableCellRenderer{
	GenericMatrixPanel parent;
	
	public ConfusionCellRenderer(GenericMatrixPanel p){
		parent = p;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
		double sum = parent.getSum();
		Component rend = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		rend.setBackground(Color.white);
		if(vColIndex > 0){
			Integer intensity = 0;
			try{
				intensity = ((Double)(255.0*(Double.parseDouble(table.getValueAt(rowIndex, vColIndex).toString())/sum))).intValue();
			}catch(Exception e){
				e.printStackTrace();
			}
			rend.setBackground(new Color(255-intensity, 255-intensity,255));
			rend.setForeground(Color.black);
		}
		return rend;
	}
}
