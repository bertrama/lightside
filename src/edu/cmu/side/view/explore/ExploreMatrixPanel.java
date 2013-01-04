package edu.cmu.side.view.explore;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import edu.cmu.side.control.ExploreResultsControl;
import edu.cmu.side.view.generic.GenericMatrixPanel;
import edu.cmu.side.view.util.RadioButtonListEntry;
import edu.cmu.side.view.util.SIDETable;
import edu.cmu.side.view.util.ToggleButtonTableEntry;

public class ExploreMatrixPanel extends GenericMatrixPanel{

	private ButtonGroup toggleButtons;

	public ExploreMatrixPanel(){
		super();
		label.setText("Cell Highlight:");
		this.getDisplayTable().setCellSelectionEnabled(false);
		this.getDisplayTable().addMouseListener(new MatrixMouseAdapter(this.getDisplayTable()));
	}
	@Override
	public Object getCellObject(Object o){
		RadioButtonListEntry tb = new RadioButtonListEntry(o, false);
		toggleButtons.add(tb);
		return tb;
	}


	@Override
	public void refreshPanel() {
		toggleButtons = new ButtonGroup();
		if(ExploreResultsControl.hasHighlightedTrainedModelRecipe()){
			refreshPanel(ExploreResultsControl.getHighlightedTrainedModelRecipe().getTrainingResult().getConfusionMatrix());				
		}else{
			refreshPanel(new TreeMap<String, Map<String, List<Integer>>>());
		}
	}

}

class MatrixMouseAdapter extends MouseAdapter{
	SIDETable panel;
	public MatrixMouseAdapter(SIDETable p){
		super();
		panel = p;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int col = panel.columnAtPoint(e.getPoint());
		int row = panel.rowAtPoint(e.getPoint());
		if (col > 0 && row != -1) {
			Object obj = panel.getValueAt(row, col);
			if (obj instanceof JCheckBox) {
				JCheckBox checkbox = (JCheckBox) obj;

				checkbox.setSelected(!checkbox.isSelected());
				panel.repaint();
			}
			if(obj instanceof JRadioButton){
				JRadioButton radio = (JRadioButton) obj;
				radio.setSelected(!radio.isSelected());
				panel.repaint();
			}

			if(obj instanceof JToggleButton){
				JToggleButton radio = (JToggleButton) obj;
				radio.setSelected(!radio.isSelected());
				panel.repaint();
			}
		}
	}
}
