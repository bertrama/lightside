package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JLabel;
import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.dataitem.TrainingResultInterface;
import edu.cmu.side.simple.SimpleTrainingResult;
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
		
		add("hfill", new JLabel("Trained Models:"));
		clear.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e){
				SimpleWorkbench.clearTrainingResults();
			}
		});
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				SimpleWorkbench.removeTrainingResult(list.getSelectedIndex());
			}
		});
		scroll.setPreferredSize(new Dimension(250,150));
		add("br hfill", scroll);
		
		list.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent evt){
				int index = list.locationToIndex(evt.getPoint());
				if(index == -1){ return; }
				clickedModel = ((SimpleTrainingResult)listModel.get(index));
				fireActionEvent();
			}
		});
		list.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				clickedModel = ((SimpleTrainingResult)listModel.get(list.getSelectedIndex()));
				fireActionEvent();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		add("br left", delete);
		add("", clear);
	}
	
	/** This is where the rest of the Learning panels get their information about what model to analyze. */
	public static SimpleTrainingResult getSelectedTrainingResult(){
		return clickedModel;
	}
	
	public void refreshPanel(){
		List<TrainingResultInterface> models = SimpleWorkbench.getTrainingResults();
		boolean set = false;
		if(models.size() != listModel.getSize()){
			set = true;
			listModel.removeAllElements();
			for(TrainingResultInterface model : models){
				listModel.addElement(model);
			}
		}
		super.refreshPanel();
		if(set){
			list.setSelectedIndex(list.getModel().getSize()-1);
		}
		clickedModel = (SimpleTrainingResult)list.getSelectedValue();
	}
}
