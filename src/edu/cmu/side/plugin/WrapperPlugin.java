package edu.cmu.side.plugin;

import java.awt.Color;

import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.model.StatusUpdater;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;

public abstract class WrapperPlugin extends SIDEPlugin{


	protected JPanel panel = new JPanel(new RiverLayout());
	
	public WrapperPlugin(){
		panel.setBackground(Color.white);
	}
	@Override
	public String getType() {
		return "learning_wrapper";
	}
	
	public abstract void learnFromTrainingData(FeatureTable train, boolean[] mask, StatusUpdater update);

	public FeatureTable wrapTableBefore(FeatureTable table, boolean[] mask, StatusUpdater update){
		return wrapTableForSubclass(table, mask, update);
	}
	
	public PredictionResult wrapResultAfter(PredictionResult predict, boolean[] mask, StatusUpdater update){
		return wrapResultForSubclass(predict, mask, update);
	}
	
	public abstract FeatureTable wrapTableForSubclass(FeatureTable table, boolean[] mask, StatusUpdater update);
	
	public abstract PredictionResult wrapResultForSubclass(PredictionResult result, boolean[] mask, StatusUpdater update);
}
