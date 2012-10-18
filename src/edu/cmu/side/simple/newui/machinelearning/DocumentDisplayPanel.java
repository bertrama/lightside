package edu.cmu.side.simple.newui.machinelearning;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.export.CSVExporter;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.Feature.Type;
import edu.cmu.side.simple.feature.LocalFeatureHit;
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
	
	JButton exportButton = new JButton("Export to CSV...");

	JTextPane highlight = new JTextPane();
	private boolean necessary = false;
	/** Retrieved from ConfusionMatrixPanel */
	private SimpleTrainingResult model = null;
	private Integer[] localCell = {-1, -1};
	private Feature localFeat = null;
	public DocumentDisplayPanel(){
		highlight.setEditable(false);
		StyledDocument style = highlight.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		style.addStyle("regular", def);
		Style s = style.addStyle("highlight", def);
		StyleConstants.setBackground(s, Color.yellow);
		scroll = new JScrollPane(display);
		button.add(allDisplay);
		button.add(cellDisplay);
		button.add(featDisplay);
		ActionListener update = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
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
			public void mouseReleased(MouseEvent e){
				StyledDocument doc = highlight.getStyledDocument();
				try{
					doc.remove(0, highlight.getStyledDocument().getLength());
					if(display.getSelectedRow() >= 0){
						SimpleTrainingResult clicked = ModelListPanel.getSelectedTrainingResult();
						Feature clickedFeature = MiniErrorAnalysisPanel.getSelectedFeature();
						updateHighlight(clicked, clickedFeature, doc);
					}
				}catch(Exception e2){e2.printStackTrace();}
			}
		});
		setLayout(new RiverLayout());
		JSplitPane modelZoom = new JSplitPane();
		JPanel groupList = new JPanel(new RiverLayout());
		groupList.add("left", new JLabel("Display: "));
		groupList.add("left", allDisplay);
		groupList.add("left", cellDisplay);
		groupList.add("left", featDisplay);
		groupList.add("left", exportButton);
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(BorderLayout.NORTH, groupList);
		leftPanel.add(BorderLayout.CENTER, scroll);
		scroll.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getID()==KeyEvent.VK_UP || arg0.getID()==KeyEvent.VK_DOWN){
					StyledDocument doc = highlight.getStyledDocument();
					try{
						doc.remove(0, highlight.getStyledDocument().getLength());
						if(display.getSelectedRow() >= 0){
							SimpleTrainingResult clicked = ModelListPanel.getSelectedTrainingResult();
							Feature clickedFeature = MiniErrorAnalysisPanel.getSelectedFeature();
							updateHighlight(clicked, clickedFeature, doc);
						}
					}catch(Exception e){e.printStackTrace();}
				}
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		modelZoom.setLeftComponent(leftPanel);
		modelZoom.setRightComponent(new JScrollPane(highlight));
		modelZoom.setBorder(null);
		add("hfill vfill", modelZoom);
		
		exportButton.addActionListener(new ActionListener()
		{
			JFileChooser chooser = new JFileChooser(".");
			
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int choice = chooser.showSaveDialog(DocumentDisplayPanel.this);
				if(choice == JFileChooser.APPROVE_OPTION)
					CSVExporter.exportToCSV(displayModel, chooser.getSelectedFile());
				
			}
		});
		exportButton.setEnabled(false);
		
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
			displayModel.addColumn("id");
			displayModel.addColumn("text");
			displayModel.addColumn("predicted");
			displayModel.addColumn("actual");
			SimpleDocumentList evalTable = model.getEvaluationTable().getDocumentList();
			boolean textExists = evalTable.getTextColumn()!= null;
			if(model != null){
				if(allDisplay.isSelected() || (localFeat != null && featDisplay.isSelected() && localFeat.getFeatureType().equals(Type.NUMERIC))){
					for(int i = 0; i < evalTable.getSize(); i++){
						Object[] row = populateRow(i, textExists);
						displayModel.addRow(row);
					}					
				}else if(cellDisplay.isSelected() && localCell[0] >= 0 && localCell[1] >= 0){
					String act = ""; String pred = "";
					switch(model.getEvaluationTable().getClassValueType()){
					case NOMINAL:
					case BOOLEAN:
						act = evalTable.getLabelArray()[localCell[0]];
						pred = evalTable.getLabelArray()[localCell[1]];
						break;
					case NUMERIC:
						act = "Q"+(localCell[0]+1);
						pred = "Q"+(localCell[1]+1);
					}
					List<Integer> cell = model.getConfusionMatrixCell(pred, act);
					for(int i : cell){
						Object[] row = populateRow(i, textExists);
						displayModel.addRow(row);
					}			
				}else if(featDisplay.isSelected() && localFeat != null){
					Collection<FeatureHit> hits = model.getEvaluationTable().getHitsForFeature(localFeat);
					Set<Integer> cell = new TreeSet<Integer>();
					if(hits != null){
						for(FeatureHit hit : hits){ cell.add(hit.getDocumentIndex()); }
						for(int i : cell){
							Object[] row = populateRow(i, textExists);
							displayModel.addRow(row);
						}			
					}
				}
			}
			display.setModel(displayModel);
			TableColumnModel columns = display.getColumnModel();
			columns.getColumn(0).setPreferredWidth(30);
			columns.getColumn(1).setPreferredWidth(245);
			columns.getColumn(2).setPreferredWidth(50);
			columns.getColumn(3).setPreferredWidth(50);
			display.setColumnModel(columns);
			display.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(displayModel);
			display.setRowSorter(sorter);
			display.changeSelection(rowAtBottom, 0, false,false);
			try{
				StyledDocument doc = highlight.getStyledDocument();
				doc.remove(0, highlight.getStyledDocument().getLength());
				if(display.getSelectedRow() >= 0){
					updateHighlight(clicked, clickedFeature, doc);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			repaint();
			necessary = false;
			
			exportButton.setEnabled(displayModel.getRowCount() > 0);
		}
	}

	private void updateHighlight(SimpleTrainingResult clicked,
			Feature clickedFeature, StyledDocument doc)
					throws BadLocationException {
		Object sort = display.getSortedValue(display.getSelectedRow(), 0);
		int id = -1;
		if(sort != null){
			id = Integer.parseInt(sort.toString());						
		}
		Object text = display.getSortedValue(display.getSelectedRow(),1);
		if(text != null){
			String dispText = text.toString();

			String space = " ";
			dispText = dispText.replaceAll("<", space + "<lessthan" + space);
			dispText = dispText.replaceAll(">", space + "<greaterthan>" + space);
			dispText = dispText.replaceAll(space + "<lessthan", space + "<lessthan>" + space);
			dispText = dispText.replaceAll(",", space + "<comma>" + space);
			dispText = dispText.replaceAll("\\.", space + "<period>" + space);
			dispText = dispText.replaceAll(";", space + "<semicolon>" + space);
			dispText = dispText.replaceAll(":", space + "<colon>" + space);
			dispText = dispText.replaceAll("-", space + "<dash>" + space);
			dispText = dispText.replaceAll("\\?", space + "<questionmark>" + space);
			dispText = dispText.replaceAll("!", space + "<exclamationmark>" + space);
			dispText = dispText.replaceAll("\\*", space + "<asterisk>" + space);
			dispText = dispText.replaceAll("&", space + "<ampersand>" + space);
			dispText = dispText.replaceAll("@", space + "<atsign>" + space);
			dispText = dispText.replaceAll("#", space + "<numbersign>" + space);
			dispText = dispText.replaceAll("%", space + "<percentsign>" + space);
			dispText = dispText.replaceAll("\\(", space + "<openparen>" + space);
			dispText = dispText.replaceAll("\\)", space + "<closeparen>" + space);
			dispText = dispText.replaceAll("\\{", space + "<opencurly>" + space);
			dispText = dispText.replaceAll("\\}", space + "<closecurly>" + space);
			dispText = dispText.replaceAll("\"", space + "<doublequote>" + space);
			dispText = dispText.replaceAll("\\$", space + "<dollar>" + space);
			dispText = dispText.replaceAll("\\\\", space + "<backslash>" + space);
			dispText = dispText.replaceAll("\\/", space + "<forwardslash>" + space);
			dispText = dispText.replaceAll("\\+", space + "<plussign>" + space);
			dispText = dispText.replaceAll("\\~", space + "<tilde>" + space);
			dispText = dispText.replaceAll("\\^", space + "<carat>" + space);
			dispText = dispText.replaceAll("\\[", space + "<leftsquareparen>" + space);
			dispText = dispText.replaceAll("\\]", space + "<rightsquareparen>" + space);
			dispText = dispText.replaceAll("\\s+", " ");
			if(clickedFeature != null && id != -1){
				Collection<FeatureHit> hits = clicked.getEvaluationTable().getHitsForDocument(id);
				//making sure things stay sorted
				TreeMap<Integer, int[]> breakpoints = new TreeMap<Integer, int[]>();
				for(FeatureHit hit : hits){
					if(hit instanceof LocalFeatureHit && hit.getFeature().equals(clickedFeature)){
						LocalFeatureHit lfh = (LocalFeatureHit)hit;
						for(int[] bound : lfh.getHits()){
							breakpoints.put(bound[0], bound);
						}
					}
				}
				try{
					int offset = 0;
					for(Integer start : breakpoints.keySet()){
						Integer end = Math.min(dispText.length(), breakpoints.get(start)[1]);
						doc.insertString(offset, dispText.substring(offset, start), doc.getStyle("regular"));
						offset = start;
						doc.insertString(offset, dispText.substring(start, end), doc.getStyle("highlight"));
						offset = end;
					}
					doc.insertString(offset, dispText.substring(offset, dispText.length()), doc.getStyle("regular"));								
				}catch(Exception e){
					System.out.println("DDP251");
					e.printStackTrace();
				}
			}else{
				doc.insertString(0,dispText,doc.getStyle("regular"));
			}
		}
		highlight.setStyledDocument(doc);
	}

	private Object[] populateRow(int i, boolean textExists) {
		Object[] row = new Object[4];
		row[0] = i;
		row[1] = textExists?(model.getEvaluationTable().getDocumentList().getCoveredTextList().size()==0?"":model.getEvaluationTable().getDocumentList().getCoveredTextList().get(i)):"";
		row[2] = model.getPredictions().get(i);
		row[3] = model.getEvaluationTable().getDocumentList().getAnnotationArray().get(i);
		return row;
	}
}
