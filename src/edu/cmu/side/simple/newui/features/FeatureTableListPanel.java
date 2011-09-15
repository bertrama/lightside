package edu.cmu.side.simple.newui.features;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.yerihyo.yeritools.swing.SimpleOKDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.ResultOption;

import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class FeatureTableListPanel extends AbstractListPanel{
	private static final long serialVersionUID = -2125231290325317847L;

	static FeatureTable clickedFeatureTable;

	private JButton saveButton = new JButton("save");
	private JButton loadButton = new JButton("load");
	private JButton exportButton = new JButton("export");
	public FeatureTableListPanel(){
		/** List model initialized and defined in the abstract class */
		super();
		
		add("hfill", new JLabel("feature tables:"));
		scroll.setPreferredSize(new Dimension(250,80));
		add("br hfill", scroll);
		
		list.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt){
				int index = list.locationToIndex(evt.getPoint());
				if(index == -1){ return; }
				clickedFeatureTable = ((FeatureTable)listModel.get(index));
				fireActionEvent();
			}
		});
		loadButton.addActionListener(new SimpleWorkbench.FeatureTableLoadListener());
		add("br left", loadButton);
		saveButton.addActionListener(new SimpleWorkbench.FeatureTableSaveListener());
		add("left", saveButton);
		exportButton.addActionListener(new SimpleWorkbench.FeatureTableExportListener());
		add("left", exportButton);
	}
	
	/**
	 * Finds the feature table currently highlighted in the Swing UI.
	 * @return That feature table.
	 */
	public static FeatureTable getSelectedFeatureTable(){
		return clickedFeatureTable;
	}

	/**
	 * Grab all of the currently active feature tables from the Workbench and refresh the Swing UI to match.
	 */
	public void refreshPanel(){
		List<FeatureTable> featureTables = SimpleWorkbench.getFeatureTables();
		if(featureTables.size() != listModel.getSize()){
			listModel.removeAllElements();
			for(FeatureTable table : featureTables){
				listModel.addElement(table);
			}
		}
		super.refreshPanel();
		if(list.getModel().getSize()>0 && list.getSelectedIndex()==-1){
			list.setSelectedIndex(list.getModel().getSize()-1);	
		}
		clickedFeatureTable = (FeatureTable)list.getSelectedValue();		
	}
}
