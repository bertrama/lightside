package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.SIDETable;

public class DocumentDisplayPanel extends AbstractListPanel {
	private static final long serialVersionUID = -8293967293059514141L;
	SIDETable display = new SIDETable();
	DefaultTableModel displayModel = new DefaultTableModel();

	JRadioButton allDisplay = new JRadioButton("All");
	JRadioButton cellDisplay = new JRadioButton("By Error Cell");
	JRadioButton featDisplay = new JRadioButton("By Feature");
	ButtonGroup button = new ButtonGroup();

	JTextArea highlight = new JTextArea();
	private boolean necessary = false;
	/** Retrieved from ConfusionMatrixPanel */
	private SimpleTrainingResult model = null;
	private Integer[] localCell = {-1, -1};
	private Feature localFeat = null;
	public DocumentDisplayPanel(){
		scroll = new JScrollPane(display);
		button.add(allDisplay);
		button.add(cellDisplay);
		button.add(featDisplay);
		ActionListener update = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				necessary = true;
				refreshPanel();
			}
		};
		allDisplay.addActionListener(update);
		cellDisplay.addActionListener(update);
		featDisplay.addActionListener(update);
		allDisplay.setSelected(true);
		display.setShowHorizontalLines(true);
		display.setShowVerticalLines(true);
		display.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
				necessary = true;
				refreshPanel();
			}
		});
		setLayout(new RiverLayout());
		JSplitPane modelZoom = new JSplitPane();
		JPanel groupList = new JPanel(new RiverLayout());
		scroll.setPreferredSize(new Dimension(375, 180));
		groupList.setPreferredSize(new Dimension(400, 225));
		groupList.add("left", new JLabel("Display: "));
		groupList.add("left", allDisplay);
		groupList.add("left", cellDisplay);
		groupList.add("left", featDisplay);
		groupList.add("br hfill", scroll);
		modelZoom.setLeftComponent(groupList);
		modelZoom.setRightComponent(new JScrollPane(highlight));
		modelZoom.setBorder(null);
		add("hfill vfill", modelZoom);
	}

	public void refreshPanel(){
		SimpleTrainingResult clicked = ModelListPanel.getSelectedTrainingResult();
		Integer[] clickedCell = ConfusionMatrixPanel.getSelectedCell();
		Feature clickedFeature = MiniErrorAnalysisPanel.getSelectedFeature();
		if(necessary || clicked != model || clickedFeature != localFeat || clickedCell[0] != localCell[0] || clickedCell[1] != localCell[1]){
			localFeat = clickedFeature;
			int rowAtBottom = display.getSelectedRow();
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
					String act = ""; String pred = "";
					switch(model.getFeatureTable().getClassValueType()){
					case NOMINAL:
					case BOOLEAN:
						act = model.getDocumentList().getLabelArray()[localCell[0]];
						pred = model.getDocumentList().getLabelArray()[localCell[1]];
						break;
					case NUMERIC:
						act = "Q"+(localCell[0]+1);
						pred = "Q"+(localCell[1]+1);
					}
					List<Integer> cell = model.getConfusionMatrixCell(pred, act);
					for(int i : cell){
						Object[] row = new Object[3];
						row[0] = model.getDocumentList().getCoveredTextList().size()==0?"":model.getDocumentList().getCoveredTextList().get(i);
						row[1] = model.getPredictions().get(i);
						row[2] = model.getDocumentList().getAnnotationArray().get(i);
						displayModel.addRow(row);
					}			
				}else if(featDisplay.isSelected() && localFeat != null){
					Collection<FeatureHit> hits = model.getFeatureTable().getHitsForFeature(localFeat);
					Set<Integer> cell = new TreeSet<Integer>();
					if(hits != null){
						for(FeatureHit hit : hits){ cell.add(hit.getDocumentIndex()); }
						for(int i : cell){
							Object[] row = new Object[3];
							row[0] = model.getDocumentList().getCoveredTextList().size()==0?"":model.getDocumentList().getCoveredTextList().get(i);
							row[1] = model.getPredictions().get(i);
							row[2] = model.getDocumentList().getAnnotationArray().get(i);
							displayModel.addRow(row);
						}			
					}
				}
			}
			display.setModel(displayModel);
			TableColumnModel columns = display.getColumnModel();
			columns.getColumn(0).setPreferredWidth(275);
			columns.getColumn(1).setPreferredWidth(50);
			columns.getColumn(2).setPreferredWidth(50);
			display.setColumnModel(columns);
			display.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(displayModel);
			display.setRowSorter(sorter);
			display.changeSelection(rowAtBottom, 0, false,false);
			if(display.getSelectedRow() >= 0){
				highlight.setText(display.getSortedValue(display.getSelectedRow(), 0).toString());		
				highlight.setLineWrap(true);
				highlight.setWrapStyleWord(true);
			}else{
				highlight.setText("");
			}
			repaint();
			necessary = false;
		}
	}
}
