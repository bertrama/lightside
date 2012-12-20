package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.view.generic.GenericTableDisplayPanel;
import edu.cmu.side.genesis.view.generic.GenericLoadPanel;
import edu.cmu.side.simple.newui.features.FeatureTableListPanel;
import edu.cmu.side.simple.newui.features.FeatureTablePanel;

public class ExtractBottomPanel extends JPanel{
        
        GenericLoadPanel control = new GenericLoadPanel("Highlighted Feature Table:") {	
			@Override
			public void setHighlight(GenesisRecipe r) {
				ExtractFeaturesControl.setHighlightedFeatureTableRecipe(r);
			}
			
			@Override
			public void refreshPanel() {
				refreshPanel(ExtractFeaturesControl.getFeatureTables());
			}
			
			@Override
			public String getHighlightDescription() {
				return getHighlight().getFeatureTable().getDescriptionString();
			}
			
			@Override
			public GenesisRecipe getHighlight() {
				return ExtractFeaturesControl.getHighlightedFeatureTableRecipe();
			}
		};
		
        ExtractTableChecklistPanel checklist = new ExtractTableChecklistPanel();
        GenericTableDisplayPanel display = new GenericTableDisplayPanel();
        
        public ExtractBottomPanel(){
                setLayout(new BorderLayout());
                JSplitPane split = new JSplitPane();
                split.setLeftComponent(control);
                
                JSplitPane displaySplit = new JSplitPane();
                displaySplit.setLeftComponent(checklist);
                displaySplit.setRightComponent(display);
                displaySplit.setPreferredSize(new Dimension(650,200));
                checklist.setPreferredSize(new Dimension(275,200));
                display.setPreferredSize(new Dimension(350, 200));
                split.setRightComponent(displaySplit);
                control.setPreferredSize(new Dimension(275,200));
                add(BorderLayout.CENTER, split);
        }
        
        public void refreshPanel(){
                control.refreshPanel();
                checklist.refreshPanel();
                if(ExtractFeaturesControl.hasHighlightedFeatureTable()){
                	display.refreshPanel(ExtractFeaturesControl.getHighlightedFeatureTableRecipe().getFeatureTable(), ExtractFeaturesControl.getTableEvaluationPlugins());
                }
        }
}