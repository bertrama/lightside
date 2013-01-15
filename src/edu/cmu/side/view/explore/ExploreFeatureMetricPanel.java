package edu.cmu.side.view.explore;

import javax.swing.ButtonGroup;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.model.feature.Feature;
import edu.cmu.side.view.generic.GenericFeatureMetricPanel;
import edu.cmu.side.view.util.CheckboxTableCellRenderer;
import edu.cmu.side.view.util.RadioButtonListEntry;
import edu.cmu.side.view.util.ToggleMouseAdapter;

public class ExploreFeatureMetricPanel extends GenericFeatureMetricPanel{

	ButtonGroup toggleButtons = new ButtonGroup();

	public ExploreFeatureMetricPanel(){
		super();
		featureTable.addMouseListener(new ToggleMouseAdapter(featureTable){

			@Override
			public void setHighlight(Object row, String col) {
				if(row instanceof RadioButtonListEntry){
					ExploreResultsControl.setHighlightedFeature((Feature)((RadioButtonListEntry)row).getValue());
				}
				Workbench.update();
			}
		});
		featureTable.setDefaultRenderer(Object.class, new CheckboxTableCellRenderer());
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
