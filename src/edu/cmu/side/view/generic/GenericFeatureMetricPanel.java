package edu.cmu.side.view.generic;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.plugin.FeatureMetricPlugin;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.FeatureTableModel;
import edu.cmu.side.view.util.SIDETable;

public abstract class GenericFeatureMetricPanel extends AbstractListPanel {

	SIDETable featureTable = new SIDETable();
	FeatureTableModel model = new FeatureTableModel();
	FeatureTableModel display = new FeatureTableModel();
	JTextField text = new JTextField(20);
	public GenericFeatureMetricPanel(){
		setLayout(new RiverLayout());
		JLabel label = new JLabel("Features in Table:");
		featureTable.setModel(model);
		featureTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		JScrollPane tableScroll = new JScrollPane(featureTable);
		text.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {}

			@Override
			public void keyReleased(KeyEvent arg0) {
				display = filterTable(model, text.getText());
				System.out.println("Keypressed GFMP48");
				featureTable.setModel(display);
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(display);
				featureTable.setRowSorter(sorter);
				featureTable.validate();
			}

			@Override
			public void keyTyped(KeyEvent arg0) {}
			
		});
		add("left", label);
		add("br left", new JLabel("Search:"));
		add("hfill", text);
		add("br hfill vfill", tableScroll);
	}

	public void refreshPanel(Recipe recipe, Map<? extends FeatureMetricPlugin, Map<String, Boolean>> tableEvaluationPlugins, boolean[] mask){
		model = new FeatureTableModel();
		if(recipe != null && recipe.getTrainingTable() != null){

			int countTrues = 0;
			for(FeatureMetricPlugin plug : tableEvaluationPlugins.keySet()){
				for(String s : tableEvaluationPlugins.get(plug).keySet()){
					if(tableEvaluationPlugins.get(plug).get(s)){
						countTrues++;
					}
				}
			}
			if(countTrues+1 != model.getColumnCount() || model.getRowCount() != recipe.getTrainingTable().getFeatureSet().size()){
				model.addColumn("Feature");
				int rowCount = 1;
				Map<FeatureMetricPlugin, Map<String, Map<Feature, Comparable>>> evals = new HashMap<FeatureMetricPlugin, Map<String, Map<Feature, Comparable>>>();
				for(FeatureMetricPlugin plug : tableEvaluationPlugins.keySet()){
					evals.put(plug, new TreeMap<String, Map<Feature, Comparable>>());
					for(String s : tableEvaluationPlugins.get(plug).keySet()){
						if(tableEvaluationPlugins.get(plug).get(s)){
							model.addColumn(s);
							rowCount++;
							Map<Feature, Comparable> values = plug.evaluateFeatures(recipe, mask, s, getTargetAnnotation());
							for(Feature f : values.keySet()){
								if(f.getFeatureName().contains("remove")){
									System.out.println(f.getFeatureName() + ", " + s + ": " + values.get(f) + " GFM93");
								}
							}
							evals.get(plug).put(s, values);
						}
					}
				}
				for(Feature f : recipe.getTrainingTable().getFeatureSet()){
					Object[] row = new Object[rowCount];
					row[0] = f;
					int r = 1;
					for(FeatureMetricPlugin tep : evals.keySet()){
						for(String eval : evals.get(tep).keySet()){
							if(f.getFeatureName().contains("remove")){
								System.out.println(f.getFeatureName() + ", " + eval + ": " + evals.get(tep).get(eval).get(f)+ " GFM107");
							}
							row[r++] = evals.get(tep).get(eval).get(f);
						}
					}
					model.addRow(row);
				}
			}
		}
		display = filterTable(model, text.getText());
		featureTable.setModel(display);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(display);
		featureTable.setRowSorter(sorter);
	}
	
	public FeatureTableModel filterTable(FeatureTableModel ftm, String t){
		FeatureTableModel disp = new FeatureTableModel();
		for(int i = 0; i < ftm.getColumnCount(); i++){
			disp.addColumn(ftm.getColumnName(i));
		}
		if(disp.getColumnCount() > 0){
			for(int i = 0; i < ftm.getRowCount(); i++){
				if(ftm.getValueAt(i, 0).toString().contains(t)){
					Object[] row = new Object[ftm.getColumnCount()];
					for(int j = 0; j < ftm.getColumnCount(); j++){
						row[j] = ftm.getValueAt(i,j);
					}
					disp.addRow(row);
				}
			}			
		}
		return disp;
	}
	public abstract String getTargetAnnotation();
}
