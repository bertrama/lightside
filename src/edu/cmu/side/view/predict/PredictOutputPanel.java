package edu.cmu.side.view.predict;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.DefaultRowSorter;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.DocumentListTableModel;
import edu.cmu.side.view.util.SIDETable;

public class PredictOutputPanel extends AbstractListPanel
{

	SIDETable docTable = new SIDETable();
	DocumentListTableModel model = new DocumentListTableModel(null);
	JLabel label = new JLabel("Selected Dataset");

	public void setLabel(String l)
	{
		label.setText(l);
	}

	public PredictOutputPanel()
	{
		setLayout(new RiverLayout());
		add("left", label);
		docTable.setModel(model);
		docTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		docTable.setRowSorter(new TableRowSorter<TableModel>(model));
		JScrollPane tableScroll = new JScrollPane(docTable);
		add("br hfill vfill", tableScroll);
	}

	public void refreshPanel(Recipe recipe)
	{
		if(recipe == null)
			model.setDocumentList(null);
		else
			model.setDocumentList(recipe.getDocumentList());
	}

}
