package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.SIDETable;
import edu.cmu.side.simple.newui.MultilineTableCell;

public class DocumentDisplayPanel extends AbstractListPanel {

	MultilineTableCell wordWrapRenderer = new MultilineTableCell();
	
	SIDETable display = new SIDETable(){
		public TableCellRenderer getCellRenderer(int row, int column){
			if(row == display.getSelectedRow()) {
				return wordWrapRenderer;
			}else{
				return super.getCellRenderer(row, column);
			}
		}
	};
	DefaultTableModel displayModel = new DefaultTableModel();

	JRadioButton allDisplay = new JRadioButton("All");
	JRadioButton cellDisplay = new JRadioButton("Selected Cell");
	ButtonGroup button = new ButtonGroup();

	private boolean necessary = false;
	/** Retrieved from ConfusionMatrixPanel */
	private SimpleTrainingResult model = null;
	private Integer[] localCell = {-1, -1};

	public DocumentDisplayPanel(){
		scroll = new JScrollPane(display);
		scroll.setPreferredSize(new Dimension(725,275));
		button.add(allDisplay);
		button.add(cellDisplay);
		add("left", new JLabel("Display: "));
		ActionListener update = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				necessary = true;
				refreshPanel();
			}
		};
		allDisplay.addActionListener(update);
		cellDisplay.addActionListener(update);
		allDisplay.setSelected(true);
		display.setShowHorizontalLines(true);
		display.setShowVerticalLines(true);
		display.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				necessary = true;
				refreshPanel();
			}
		});
		add("left", allDisplay);
		add("left", cellDisplay);
		add("br hfill", scroll);
	}

	public void refreshPanel(){
		SimpleTrainingResult clicked = ModelListPanel.getSelectedTrainingResult();
		Integer[] clickedCell = ConfusionMatrixPanel.getSelectedCell();
		if(necessary || clicked != model || clickedCell[0] != localCell[0] || clickedCell[1] != localCell[1]){
			model = clicked;
			localCell = clickedCell;
			displayModel = new DefaultTableModel();
			displayModel.addColumn("text");
			displayModel.addColumn("predicted");
			displayModel.addColumn("actual");
			if(model != null){
				if(allDisplay.isSelected()){
					for(int i = 0; i < model.getDocumentList().getSize(); i++){
						Object[] row = new Object[3];
						row[0] = model.getDocumentList().getCoveredTextList().size()==0?"":model.getDocumentList().getCoveredTextList().get(i);
						row[1] = model.getPredictions().get(i);
						row[2] = model.getDocumentList().getAnnotationArray().get(i);
						displayModel.addRow(row);
					}					
				}else if(cellDisplay.isSelected() && localCell[0] >= 0 && localCell[1] >= 0){
					String act = model.getDocumentList().getLabelArray()[localCell[0]];
					String pred = model.getDocumentList().getLabelArray()[localCell[1]];
					List<Integer> cell = model.getConfusionMatrixCell(pred, act);
					for(int i : cell){
						Object[] row = new Object[3];
						row[0] = model.getDocumentList().getCoveredTextList().size()==0?"":model.getDocumentList().getCoveredTextList().get(i);
						row[1] = model.getPredictions().get(i);
						row[2] = model.getDocumentList().getAnnotationArray().get(i);
						displayModel.addRow(row);
					}			
				}
			}
			display.setModel(displayModel);
			TableColumnModel columns = display.getColumnModel();
			columns.getColumn(1).setPreferredWidth(50);
			columns.getColumn(2).setPreferredWidth(50);
			display.setColumnModel(columns);
			TableRowSorter sorter = new TableRowSorter<TableModel>(displayModel);
			display.setRowSorter(sorter);
			System.out.println(display.getSelectedRow() + " Selected.");
			if(display.getSelectedRow()>=0 && display.getSelectedRow() < display.getModel().getRowCount()){				
				display.setRowHeight(display.getSelectedRow(), MultilineTableCell.getRowHeight());
			}
			repaint();
			necessary = false;
		}
	}
}
