package edu.cmu.side.view.generic;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.plugin.ModelFeatureMetricPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.ConfusionCellRenderer;
import edu.cmu.side.view.util.SIDETable;

public abstract class GenericMatrixPanel extends AbstractListPanel{
	private SIDETable matrixDisplay = new SIDETable();
	private DefaultTableModel matrixModel = new DefaultTableModel();

	protected JLabel label;
	
	protected ModelFeatureMetricPlugin plugin;
	protected String setting;
	
	private double sum = 0.0;

	public Double getSum(){
		return sum;
	}

	public SIDETable getDisplayTable(){
		return matrixDisplay;
	}
	
	public GenericMatrixPanel(ModelFeatureMetricPlugin p, String s){
		this();
		plugin = p;
		setting = s;
	}
	
	public GenericMatrixPanel(){
		setLayout(new RiverLayout());
		label = new JLabel("Model Confusion Matrix:");
		add("left", label);
		matrixDisplay.setModel(matrixModel);
		matrixDisplay.setBorder(BorderFactory.createLineBorder(Color.gray));
		matrixDisplay.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e){
				int row = matrixDisplay.getSelectedRow();
				int col = matrixDisplay.getSelectedColumn();
				if(row<0||col<=0) return;
			}
		});
		matrixDisplay.setDefaultRenderer(java.lang.Object.class, new ConfusionCellRenderer(this));
		describeScroll = new JScrollPane(matrixDisplay);
		add("br hfill vfill", describeScroll);
	}

	@Override
	public abstract void refreshPanel();

	public void refreshPanel(Map<String, Map<String, List<Integer>>> confusion){
		try{
			matrixModel = new DefaultTableModel();
			Collection<String> labels = new TreeSet<String>();
			for(String s : confusion.keySet()){
				labels.add(s);
				for(String p : confusion.get(s).keySet()){
					labels.add(p);
				}
			}
			matrixModel.addColumn("Act \\ Pred");

			for(String s : labels){
				matrixModel.addColumn(s);
			}
			sum = 0;
			List<Object[]> rowsToPass = generateRows(confusion, labels);
			for(Object[] row : rowsToPass){
				matrixModel.addRow(row);
			}
			matrixDisplay.setModel(matrixModel);
		} catch(ArrayIndexOutOfBoundsException e){
			
		}
	}
	
	protected List<Object[]> generateRows(Map<String, Map<String, List<Integer>>> confusion, Collection<String> labels) {
		List<Object[]> rowsToPass = new ArrayList<Object[]>();
		for(String act : labels){
			Object[] row = new Object[labels.size()+1];
			row[0] = act;
			int index = 1;
			for(String pred : labels){
				if(confusion.containsKey(pred) && confusion.get(pred).containsKey(act)){
					List<Integer> cellIndices = confusion.get(pred).get(act);
					sum += confusion.get(pred).get(act).size();
					row[index] = getCellObject(cellIndices.size());			
				}else{
					row[index] = getCellObject(0);
				}
				index++;
			}
			rowsToPass.add(row);
		}
		return rowsToPass;
	}

	public Object getCellObject(Object o){
		return o;
	}
}
