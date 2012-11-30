package edu.cmu.side.genesis;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.cmu.side.genesis.view.extract.ExtractLoadPanel;
import edu.cmu.side.genesis.view.extract.ExtractFeaturesPane;
import edu.cmu.side.genesis.view.modify.ModifyFeaturesPane;

public class GenesisWorkbenchPanel extends JTabbedPane implements ActionListener{
	
	ExtractFeaturesPane extractFeatures = new ExtractFeaturesPane();
	ModifyFeaturesPane modifyFeatures = new ModifyFeaturesPane();
	public GenesisWorkbenchPanel(){
		addTab("Extract Features", extractFeatures);
		addTab("Modify Tables", modifyFeatures);
		addTab("Build Models", new JPanel());
		addTab("Explore Models", new JPanel());
		addTab("Predict Labels", new JPanel());
	}

	@Override
	public void actionPerformed(ActionEvent ae){
		extractFeatures.refreshPanel();
		modifyFeatures.refreshPanel();
	}
}
