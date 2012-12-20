package edu.cmu.side.genesis;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.cmu.side.genesis.view.build.BuildModelPane;
import edu.cmu.side.genesis.view.compare.CompareModelsPane;
import edu.cmu.side.genesis.view.explore.ExploreResultsPane;
import edu.cmu.side.genesis.view.extract.ExtractCombinedLoadPanel;
import edu.cmu.side.genesis.view.extract.ExtractFeaturesPane;
import edu.cmu.side.genesis.view.modify.ModifyFeaturesPane;

public class GenesisWorkbenchPanel extends JTabbedPane implements ActionListener{
	
	ExtractFeaturesPane extractFeatures = new ExtractFeaturesPane();
	ModifyFeaturesPane modifyFeatures = new ModifyFeaturesPane();
	BuildModelPane buildModel = new BuildModelPane();
	ExploreResultsPane exploreResults = new ExploreResultsPane();
	CompareModelsPane compareModels = new CompareModelsPane();
	
	boolean updating = false;
	public GenesisWorkbenchPanel(){
		addTab("Extract Features", extractFeatures);
		addTab("Modify Tables", modifyFeatures);
		addTab("Build Models", buildModel);
		addTab("Explore Results", exploreResults);
		addTab("Compare Models", compareModels);
		addTab("Predict Labels", new JPanel());
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
			updating = false;			
		}
	}
}
