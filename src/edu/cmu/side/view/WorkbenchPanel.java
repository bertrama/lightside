package edu.cmu.side.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;

import edu.cmu.side.view.build.BuildModelPane;
import edu.cmu.side.view.compare.CompareModelsPane;
import edu.cmu.side.view.explore.ExploreResultsPane;
import edu.cmu.side.view.extract.ExtractFeaturesPane;
import edu.cmu.side.view.modify.ModifyFeaturesPane;
import edu.cmu.side.view.predict.PredictLabelsPane;

public class WorkbenchPanel extends JTabbedPane implements ActionListener{
	
	ExtractFeaturesPane extractFeatures = new ExtractFeaturesPane();
	ModifyFeaturesPane modifyFeatures = new ModifyFeaturesPane();
	BuildModelPane buildModel = new BuildModelPane();
	ExploreResultsPane exploreResults = new ExploreResultsPane();
	CompareModelsPane compareModels = new CompareModelsPane();
	PredictLabelsPane predictLabels = new PredictLabelsPane();
	
	boolean updating = false;
	public WorkbenchPanel(){
		addTab("Extract Features", extractFeatures);
		addTab("Modify Tables", modifyFeatures);
		addTab("Build Models", buildModel);
		addTab("Explore Results", exploreResults);
		addTab("Compare Models", compareModels);
		addTab("Predict Labels", predictLabels);
	}

	public static int refreshCount = 0;
	@Override
	public void actionPerformed(ActionEvent ae){
		if(!updating){
			refreshCount++;
			updating = true;
			extractFeatures.refreshPanel();
			modifyFeatures.refreshPanel();
			buildModel.refreshPanel();
			exploreResults.refreshPanel();
			compareModels.refreshPanel();
			predictLabels.refreshPanel();
			updating = false;			
		}
	}
}
