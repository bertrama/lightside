package edu.cmu.side.simple.newui.features;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import com.yerihyo.yeritools.swing.SimpleOKDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.ResultOption;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.simple.feature.Feature;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.feature.Feature.Type;
import edu.cmu.side.simple.feature.lab.BooleanFeature;
import edu.cmu.side.simple.feature.lab.InequalityCriterionPanel;
import edu.cmu.side.simple.feature.lab.InequalityFeature;
import edu.cmu.side.simple.feature.lab.SequenceFeature;
import edu.cmu.side.simple.feature.lab.SequencingCriterionPanel;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FeatureTableModel;
import edu.cmu.side.simple.newui.SIDETable;

/**
 * Panel for creating combinations of features, replacing the DefinedFeatureExtractor of old SIDE. Data is stored in the lab's own private
 * feature table, which corresponds to the current highlighted feature table.
 * @author emayfiel
 *
 */
public class FeatureLabPanel extends AbstractListPanel{
	private static final long serialVersionUID = -8819472346457812501L;

	private SIDETable displayTable = new SIDETable();
	private FeatureTableModel tableModel;

	private static FeatureTable labTable;

	/**
	 * Builds a combination tree feature based on the boolean buttons in the Feature lab, 
	 * then collects the feature hits for that new feature.
	 * @param source
	 * @param highlighted
	 * @return
	 */
	public Collection<FeatureHit> createComboFeature(String source, Collection<Feature> highlighted){
		BooleanFeature bool = new BooleanFeature(source, highlighted);
		Map<Integer, Set<Feature>> relevantHits = new TreeMap<Integer, Set<Feature>>();
		for(Feature child : highlighted){
			for(FeatureHit fh : labTable.getHitsForFeature(child)){
				if(!relevantHits.containsKey(fh.getDocumentIndex())){
					relevantHits.put(fh.getDocumentIndex(), new HashSet<Feature>());
				}
				relevantHits.get(fh.getDocumentIndex()).add(child);
			}
		}
		Collection<FeatureHit> hits = new HashSet<FeatureHit>();
		if("NOT".equals(source)){
			for(int i = 0; i < labTable.getDocumentList().getSize(); i++){
				if(!relevantHits.containsKey(i)){
					hits.add(new FeatureHit(bool, true, i));
				}
			}
		}else{
			for(Integer doc : relevantHits.keySet()){	
				if(("OR".equals(source) && relevantHits.get(doc).size() > 0) ||
						("AND".equals(source) && relevantHits.get(doc).size() == highlighted.size()) ||
						("XOR".equals(source) && relevantHits.get(doc).size() == 1)){
					hits.add(new FeatureHit(bool, true, doc));
				}
			}			
		}
		return hits;
	}

	/**
	 * Define one listener that will work on all the boolean buttons
	 */
	private class ComboButtonListener implements ActionListener{
		private String source;
		public ComboButtonListener(JButton button){
			source = button.getText();
		}
		public void actionPerformed(ActionEvent ae){
			FeatureLabPanel.labTable.addAllHits(createComboFeature(source, getHighlightedFeatures()));
			refreshPanel();
		}
	}

	/**
	 * Java layout nonsense, for the most part.
	 */
	public FeatureLabPanel(){
		setLayout(new BorderLayout());
		scroll = new JScrollPane(displayTable);
		scroll.setPreferredSize(new Dimension(550, 125));
		displayTable.setBorder(BorderFactory.createLineBorder(Color.gray));
		displayTable.setShowHorizontalLines(true);
		displayTable.setShowVerticalLines(true);
		add(scroll, BorderLayout.CENTER);
		JButton orButton = new JButton("OR");
		orButton.addActionListener(new ComboButtonListener(orButton));
		JButton andButton = new JButton("AND");
		andButton.addActionListener(new ComboButtonListener(andButton));
		JButton xorButton = new JButton("XOR");
		xorButton.addActionListener(new ComboButtonListener(xorButton));
		JButton notButton = new JButton("NOT");
		notButton.addActionListener(new ComboButtonListener(notButton));
		JButton seqButton = new JButton("Seq.");
		seqButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				Feature[] featureArray = getHighlightedFeatures().toArray(new Feature[0]);
				if(featureArray.length == 2){
					Feature a = (Feature)featureArray[0];
					Feature b = (Feature)featureArray[1];
					SequencingCriterionPanel task = new SequencingCriterionPanel(a,b);
					SequencingCriterionPanel.refreshSettings();
					ResultOption resultOption = SimpleOKDialog.show(FeatureLabPanel.this, "config", task);
					if(resultOption==ResultOption.APPROVE_OPTION){
						createSequenceFeature(a, b, task);
						refreshPanel();
					}					
				}
			}
		});
		JButton ineqButton = new JButton("Ineq.");
		ineqButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				Feature[] featureArray = getHighlightedFeatures().toArray(new Feature[0]);
				if(featureArray.length == 1 && featureArray[0].getFeatureType() == Type.NUMERIC){
					InequalityCriterionPanel task = new InequalityCriterionPanel(featureArray[0]);
					task.refreshSettings();
					ResultOption resultOption = SimpleOKDialog.show(FeatureLabPanel.this, "config", task);
					if(resultOption == ResultOption.APPROVE_OPTION){
						createInequalityFeature(featureArray, task);
						refreshPanel();
					}
				}
			}
		});
		ineqButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				Feature[] featureArray = getHighlightedFeatures().toArray(new Feature[0]);
				if(featureArray.length == 1){
					
				}
			}
		});
		JPanel buttonsPanel = new JPanel(new RiverLayout());
		buttonsPanel.add("left", new JLabel("Combine With:"));
		buttonsPanel.add("br left", orButton);
		buttonsPanel.add("left", andButton);
		buttonsPanel.add("left", xorButton);
		buttonsPanel.add("left", notButton);
		buttonsPanel.add("left", seqButton);
		buttonsPanel.add("left", ineqButton);
		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Collection<Feature> features = getHighlightedFeatures();
				for(Feature f : features){
					labTable.deleteFeature(f);
				}
				refreshPanel();
			}
		});
		JButton moveUpButton = new JButton("Move to Table");
		moveUpButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				Collection<Feature> features = getHighlightedFeatures();
				for(Feature f : features){
					FeatureTableListPanel.getSelectedFeatureTable().addAllHits(labTable.getHitsForFeature(f));
				}
				FeatureTablePanel.activationsChanged();
				fireActionEvent();
			}
		});
		buttonsPanel.add("br left", deleteButton);
		buttonsPanel.add("left", moveUpButton);
		add(buttonsPanel, BorderLayout.SOUTH);
		refreshPanel();
	}

	/**
	 * Triggered by the "Ineq." button, this creates a boolean feature based on whether a numeric feature is > or < than a threshold.
	 */
	private void createInequalityFeature(Feature[] featureArray,
			InequalityCriterionPanel task) {
		Collection<FeatureHit> hits = labTable.getHitsForFeature(featureArray[0]);
		InequalityFeature ineqFeat = new InequalityFeature(task.getHighlightedFeature(), task.getInequality(), task.getComparisonValue());
		Collection<FeatureHit> newHits = new HashSet<FeatureHit>();
		for(FeatureHit hit : hits){
			double val = (Double)hit.getValue();
			if((">".equals(task.getInequality()) && val > task.getComparisonValue()) ||
			   (">=".equals(task.getInequality()) && val >= task.getComparisonValue()) ||
			   ("=".equals(task.getInequality()) && val == task.getComparisonValue()) ||
			   ("<=".equals(task.getInequality()) && val <= task.getComparisonValue()) ||
			   ("<".equals(task.getInequality()) && val < task.getComparisonValue())){
				newHits.add(new FeatureHit(ineqFeat, true, hit.getDocumentIndex()));
			}
		}
		labTable.addAllHits(newHits);
	}
	
	/**
	 * Triggered by the "Seq." button, this method creates a feature that looks across instances, assuming that your data is subdivisions
	 * of a single series of instance (like lines of a conversation). If your instances are independent, this button is useless.
	 */
	private void createSequenceFeature(Feature a, Feature b,
			SequencingCriterionPanel task) {
		SequenceFeature seq = new SequenceFeature();
		Collection<FeatureHit> seqHits = new TreeSet<FeatureHit>();
		Collection<FeatureHit> aHits = labTable.getHitsForFeature(a);
		Collection<FeatureHit> bHits = labTable.getHitsForFeature(b);
		for(FeatureHit aHit : aHits){
			for(FeatureHit bHit : bHits){
				int aInd = aHit.getDocumentIndex();
				int bInd = bHit.getDocumentIndex();
				if((SequencingCriterionPanel.direction.contains("before") && bInd > aInd && bInd - aInd <= SequencingCriterionPanel.turn) || 
						(SequencingCriterionPanel.direction.contains("after") && aInd > bInd && aInd - bInd <= SequencingCriterionPanel.turn)){
					seqHits.add(new FeatureHit(seq, true, aInd));
				}
			}
		}
		labTable.addAllHits(seqHits);
	}
	
	/**
	 * Accommodates the sorted nature of the table to give a collection of features currently highlighted in the GUI.
	 */
	public Collection<Feature> getHighlightedFeatures(){
		Set<Feature> features = new HashSet<Feature>();
		for(int i : displayTable.getSelectedRows()){
			features.add((Feature)displayTable.getSortedValue(i, 1));
		}	
		return features;
	}

	/**
	 * Adds a feature to the lab's feature table.
	 */
	public static void addFeatureToLab(Feature f){
		labTable.addAllHits(FeatureTableListPanel.getSelectedFeatureTable().getHitsForFeature(f));
	}
	
	/**
	 * Returns a count of how many features are in the lab, used for refreshing the GUI.
	 */
	public int getVisibleFeatures(){
		if(tableModel == null) return 0; else
		return tableModel.getRowCount();
	}

	public void refreshPanel(){
		if(labTable == null && FeatureFileManagerPanel.getDocumentList() != null){
			labTable = new FeatureTable(FeatureFileManagerPanel.getDocumentList());
		}
		if(labTable != null){
			tableModel = new FeatureTableModel();
			int numColumns = FeatureTablePanel.getTableModel().getColumnCount();
			for(int i = 0; i < numColumns; i++){
				tableModel.addColumn(FeatureTablePanel.getTableModel().getColumnName(i));
			}
			for(Feature f : labTable.getFeatureSet()){
				tableModel.addRow(FeatureTablePanel.getFeatureDisplayRow(labTable, null, f));
			}
			displayTable.setModel(tableModel);
			displayTable.setRowSorter(new TableRowSorter<FeatureTableModel>(tableModel));

			TableColumnModel columnModel = displayTable.getColumnModel();
			if(columnModel.getColumnCount() > 2){
				columnModel.getColumn(0).setPreferredWidth(30);
				columnModel.getColumn(1).setPreferredWidth(240);
				for(int i = 2; i < columnModel.getColumnCount(); i++){
					columnModel.getColumn(i).setPreferredWidth(60);
				}				
			}
			displayTable.setColumnModel(columnModel);
			displayTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


		}
	}

}
