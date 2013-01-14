package edu.cmu.side.view.explore;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.view.generic.GenericFeatureMetricPanel;
import edu.cmu.side.view.util.RadioButtonListEntry;
import edu.cmu.side.view.util.ToggleMouseAdapter;

public class ExploreFeatureMetricPanel extends GenericFeatureMetricPanel{

	ButtonGroup toggleButtons = new ButtonGroup();

	public ExploreFeatureMetricPanel(){
		super();
		featureTable.addMouseListener(new ToggleMouseAdapter(featureTable){

			@Override
			public void setHighlight(Object row, String col) {
				System.out.println("Highlighting " + row + " EFMP22");
				if(row instanceof RadioButtonListEntry){
					ExploreResultsControl.setHighlightedFeature((Feature)((RadioButtonListEntry)row).getValue());
				}
				Workbench.update();
			}
		});
		featureTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){

			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
				Component rend = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
				rend.setBackground(Color.white);

				if(value instanceof RadioButtonListEntry){
					RadioButtonListEntry radioButton = (RadioButtonListEntry) value;
					radioButton.setEnabled(isEnabled());
					radioButton.setFont(getFont());
					radioButton.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
					rend = radioButton;
				}
				return rend;
			}
		});
	}
	@Override
	public Object getCellObject(Object o){
		RadioButtonListEntry tb = new RadioButtonListEntry(o, false);
		toggleButtons.add(tb);
		return tb;
	}

	@Override
	public String getTargetAnnotation() { return null; }
}
