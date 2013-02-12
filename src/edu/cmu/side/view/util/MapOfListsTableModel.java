package edu.cmu.side.view.util;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import edu.cmu.side.model.data.DocumentList;

public class MapOfListsTableModel extends AbstractTableModel
{

	private Map<String, List> map;
	private String[] columns = new String[0];

	public MapOfListsTableModel(Map<String, List> map)
	{
		super();
		this.map = map;
		if (map != null)
		{
			columns = map.keySet().toArray(columns);
		}
	}

	@Override
	public Class<?> getColumnClass(int c)
	{
		if(map != null && !map.get(columns[c]).isEmpty())
		{
			return map.get(columns[c]).get(0).getClass();
		}
		else return Object.class;
	}

	@Override
	public int getColumnCount()
	{
		if (map == null) return 0;

		return columns.length;
	}

	@Override
	public String getColumnName(int c)
	{
		if (c < columns.length)
			return columns[c];
		else
			return "?";
	}

	@Override
	public int getRowCount()
	{
		if (map == null) return 0;

		return map.get(columns[0]).size();
	}

	@Override
	public Object getValueAt(int r, int c)
	{
		if (c < columns.length && r < map.get(columns[c]).size()) return map.get(columns[c]).get(r);
		return "?";
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

	public void setMap(Map<String, List> map)
	{
		if (map != null)
		{
			columns = (String[]) new TreeSet<String>(map.keySet()).toArray(columns);
		}
		else
		{
			columns = new String[0];
		}
		this.map = map;

		this.fireTableStructureChanged();
		this.fireTableDataChanged();
	}

}
