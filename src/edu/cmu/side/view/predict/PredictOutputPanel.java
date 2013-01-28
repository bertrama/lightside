package edu.cmu.side.view.predict;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.control.PredictLabelsControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.CSVExporter;
import edu.cmu.side.view.util.DocumentListTableModel;
import edu.cmu.side.view.util.SIDETable;

public class PredictOutputPanel extends AbstractListPanel
{

	SIDETable docTable = new SIDETable();
	DocumentListTableModel model = new DocumentListTableModel(null);
	JLabel label = new JLabel("Selected Dataset");
	JButton export = new JButton("");
	JScrollPane tableScroll;

	public void setLabel(String l)
	{
		label.setText(l);
	}

	public PredictOutputPanel()
	{
		export.setIcon(new ImageIcon("toolkits/icons/note_go.png"));
		export.setToolTipText("Export to CSV...");
		export.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				CSVExporter.exportToCSV(model);
			}});
		
		setLayout(new RiverLayout());
		add("left", label);
		add("hfill", new JPanel());
		add("right", export);
		docTable.setModel(model);
		docTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		docTable.setRowSorter(new TableRowSorter<TableModel>(model));
		docTable.setAutoCreateColumnsFromModel(true);
		tableScroll = new JScrollPane(docTable);
		add("br hfill vfill", tableScroll);
	}
	
	public void refreshPanel()
	{
		refreshPanel(PredictLabelsControl.getHighlightedUnlabeledData());
	}

	public void refreshPanel(Recipe recipe)
	{
		model.setDocumentList(null);
		
		if(recipe == null)
			model.setDocumentList(null);
		else
			model.setDocumentList(recipe.getDocumentList());
		
		//docTable.setModel(new DocumentListTableModel(recipe.getDocumentList()));
	}

}
