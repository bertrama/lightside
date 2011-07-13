package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JLabel;
import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.simple.SimpleTrainingResult;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;

/**
 * Stores a list of all the models that have been built so far.
 * @author emayfiel
 *
 */
public class ModelListPanel extends AbstractListPanel{
	private static final long serialVersionUID = -2125231290325317847L;

	static SimpleTrainingResult clickedModel;

	public ModelListPanel(){
		/** List model initialized and defined in the abstract class */
		super();
		
		add("hfill", new JLabel("trained models:"));
		scroll.setPreferredSize(new Dimension(250,150));
		add("br hfill", scroll);
		
		list.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt){
				int index = list.locationToIndex(evt.getPoint());
				if(index == -1){ return; }
				clickedModel = ((SimpleTrainingResult)listModel.get(index));
				fireActionEvent();
			}
		});
	}
	
	/** This is where the rest of the Learning panels get their information about what model to analyze. */
	public static SimpleTrainingResult getSelectedTrainingResult(){
		return clickedModel;
	}
	
	public void refreshPanel(){
		List<TrainingResultInterface> models = SimpleWorkbench.getTrainingResults();
		if(models.size() != listModel.getSize()){
			list.setSelectedIndex(-1);
			listModel.removeAllElements();
			for(TrainingResultInterface model : models){
				listModel.addElement(model);
			}
		}
		super.refreshPanel();
		if(list.getModel().getSize()>0 && list.getSelectedIndex()==-1){
			list.setSelectedIndex(list.getModel().getSize()-1);
		}
		clickedModel = (SimpleTrainingResult)list.getSelectedValue();
	}
}
