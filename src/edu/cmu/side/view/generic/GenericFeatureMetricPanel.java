package edu.cmu.side.view.generic;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.plugin.FeatureMetricPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.CSVExporter;
import edu.cmu.side.view.util.FeatureTableModel;
import edu.cmu.side.view.util.SIDETable;

public abstract class GenericFeatureMetricPanel extends AbstractListPanel {

	protected SIDETable featureTable = new SIDETable();
	FeatureTableModel model = new FeatureTableModel();
	FeatureTableModel display = new FeatureTableModel();
	JTextField text = new JTextField(20);
	JButton export = new JButton("");

	protected static boolean evaluating = false;


	public static void setEvaluating(boolean e){
		evaluating = e;
	}

	public static boolean isEvaluating(){
		return evaluating;
	}

	protected FeatureTable localTable;

	public GenericFeatureMetricPanel(){
		setLayout(new RiverLayout());
		JLabel label = new JLabel("Features in Table:");
		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		featureTable.setAutoCreateColumnsFromModel(false);
		

		export.setIcon(new ImageIcon("toolkits/icons/note_go.png"));
		export.setToolTipText("Export to CSV...");
		export.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				CSVExporter.exportToCSV(model);
			}});
		export.setEnabled(false);

		JScrollPane tableScroll = new JScrollPane(featureTable);
		text.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {}

			@Override
			public void keyReleased(KeyEvent arg0) {
				display = filterTable(model, text.getText());
				featureTable.setModel(display);
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(display);
				featureTable.setRowSorter(sorter);
				featureTable.revalidate();
			}

			@Override
			public void keyTyped(KeyEvent arg0) {}

		});
		add("hfill", label);
		add("right", export);
		add("br left", new JLabel("Search:"));
		add("hfill", text);
		add("br hfill vfill", tableScroll);
	}

	public void refreshPanel(Recipe recipe, Map<? extends FeatureMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins, boolean[] mask){
		int countTrues = 0;
		for(FeatureMetricPlugin plug : tableEvaluationPlugins.keySet()){
			for(String s : tableEvaluationPlugins.get(plug).keySet()){
				if(tableEvaluationPlugins.get(plug).get(s)){
					countTrues++;
				}
			}
		}
		FeatureTable newTable = (recipe == null ? null : recipe.getTrainingTable());
		if(!isEvaluating()){
			localTable = newTable;
			EvaluateFeaturesTask task = new EvaluateFeaturesTask(getActionBar(), recipe, tableEvaluationPlugins, mask, getTargetAnnotation());
			task.execute();
		}
		
		export.setEnabled(recipe != null);
	}

	public FeatureTableModel filterTable(FeatureTableModel ftm, String t){
		Vector<Object> header = new Vector<Object>();
		Vector<Vector<Object>> rows = new Vector<Vector<Object>>();

		for(int i = 0; i < ftm.getColumnCount(); i++){
			header.add(ftm.getColumnName(i));
		}
		if(header.size() > 0){
			for(int i = 0; i < ftm.getRowCount(); i++){
				try{
					if(ftm.getValueAt(i, 0) != null && ftm.getValueAt(i, 0).toString().contains(t)){
						Vector<Object> row = new Vector<Object>();
						for(int j = 0; j < ftm.getColumnCount(); j++){
							row.add(ftm.getValueAt(i,j));
						}
						rows.add(row);
					}					
				}catch(Exception e){}
			}			
		}
		FeatureTableModel disp = new FeatureTableModel(rows, header);

		return disp;
	}


	public class EvaluateFeaturesTask extends ActionBarTask
	{
		Recipe recipe;
		Map<? extends FeatureMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins;
		boolean[] mask;
		String target;
		FeatureMetricPlugin plugin;

		public EvaluateFeaturesTask(ActionBar action, Recipe r, Map<? extends FeatureMetricPlugin, Map<String, Boolean>> plugins, boolean[] m, String t)
		{
			super(action);
			recipe = r;
			target = t;
			tableEvaluationPlugins = plugins;
			mask = m;
		}
		
		@Override
		protected void beginTask(){
			super.beginTask();
			GenericFeatureMetricPanel.setEvaluating(true);
			combo.setEnabled(false);
		}
		
		@Override
		protected void finishTask(){
			super.finishTask();
			GenericFeatureMetricPanel.setEvaluating(false);
			combo.setEnabled(true);
		}

		@Override
		protected void doTask(){
			try
			{

				List<SortKey> sortKeysToPass = new ArrayList<SortKey>();
				if(featureTable.getRowSorter() != null){
					List<? extends SortKey> sortKeys = featureTable.getRowSorter().getSortKeys();
					for(SortKey key : sortKeys){
						String colName = featureTable.getColumnName(key.getColumn());
						SortOrder order = key.getSortOrder();
						for(int i = 0; i < model.getColumnCount(); i++){
							if(model.getColumnName(i).equals(colName)){
								sortKeysToPass.add(new SortKey(i, order));
							}
						}
					}
				}
				
				Vector<Object> header = new Vector<Object>();
				Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
				header.add("Feature");
				int rowCount = 1;

				Map<FeatureMetricPlugin, Map<String, Map<Feature, Comparable>>> evals = new HashMap<FeatureMetricPlugin, Map<String, Map<Feature, Comparable>>>();

				if(localTable != null){
					for(FeatureMetricPlugin plug : tableEvaluationPlugins.keySet()){
						plugin = plug;
						evals.put(plug, new TreeMap<String, Map<Feature, Comparable>>());
						for(String s : tableEvaluationPlugins.get(plug).keySet()){
							if(tableEvaluationPlugins.get(plug).get(s) && !halt){
								header.add(s);
								rowCount++;
								Map<Feature, Comparable> values = plug.evaluateFeatures(recipe, mask, s, target, actionBar.update);
								evals.get(plug).put(s, values);
							}
						}
					}
					for(Feature f : localTable.getFeatureSet()){
						Vector<Object> row = new Vector<Object>();
						row.add(getCellObject(f));
						for(FeatureMetricPlugin plug : tableEvaluationPlugins.keySet()){
							for(String s : tableEvaluationPlugins.get(plug).keySet()){
								if(tableEvaluationPlugins.get(plug).get(s)){
									Object value = "";
									if(evals.get(plug).containsKey(s) && evals.get(plug).get(s) != null){
										Object tryVal = evals.get(plug).get(s).get(f);
										if(tryVal != null){
											value = tryVal;
										}
									}
									row.add(value);
								}
							}
						}
						rows.add(row);
					}						
				}
				if(!halt)
				{
					model = new FeatureTableModel(rows, header);
	
					display = filterTable(model, text.getText());
					TableColumnModel columns = new DefaultTableColumnModel();
					for(int i = 0; i < display.getColumnCount(); i++){
						TableColumn col = new TableColumn(i);
						col.setHeaderValue(display.getColumnName(i));
						columns.addColumn(col);
					}
					featureTable.setColumnModel(columns);
					featureTable.setModel(display);
					TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(display);
					sorter.setSortKeys(sortKeysToPass);
					sorter.setSortsOnUpdates(true);
					featureTable.setRowSorter(sorter);
					featureTable.sorterChanged(new RowSorterEvent(sorter));
				}

				GenericFeatureMetricPanel.setEvaluating(false);

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void requestCancel()
		{
			System.out.println("cancelling...");
			plugin.stopWhenPossible();
		}
	}

	public Object getCellObject(Object o){
		return o;
	}

	public abstract String getTargetAnnotation();

	public abstract ActionBar getActionBar();
}
