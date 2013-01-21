package edu.cmu.side.view.util;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import edu.cmu.side.model.data.DocumentList;

public class DocumentListTableModel extends AbstractTableModel
{

	private DocumentList docs;

	public DocumentListTableModel(DocumentList docs)
	{
		super();
		this.docs = docs;
	}

	@Override
	public Class<?> getColumnClass(int arg0)
	{
		return String.class;
	}

	@Override
	public int getColumnCount()
	{
		if(docs == null)
			return 0;
		
		if(docs.getTextColumns().isEmpty())
			return docs.getAnnotationNames().length;
		
		else 
			return docs.getAnnotationNames().length + 1;
	}

	@Override
	public String getColumnName(int c)
	{
		if(c == docs.getAnnotationNames().length)
			return "text";
		
		return docs.getAnnotationNames()[c];
	}

	@Override
	public int getRowCount()
	{
		if(docs == null)
			return 0;
		
		return docs.getSize();
	}

	@Override
	public Object getValueAt(int r, int c)
	{
		if(c == docs.getAnnotationNames().length)
			return docs.getPrintableTextAt(r);
		return docs.getAnnotationArray(docs.getAnnotationNames()[c]).get(r);
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1)
	{
		return false;
	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2)
	{
		// TODO Auto-generated method stub
	}

	public DocumentList getDocumentList()
	{
		return docs;
	}

	public void setDocumentList(DocumentList docs)
	{
		this.docs = docs;
		fireTableStructureChanged();
	}

}
