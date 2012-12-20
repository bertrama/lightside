package edu.cmu.side.genesis.view.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ExploreResultsControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.simple.newui.AbstractListPanel;

public abstract class GenericLoadPanel extends AbstractListPanel{

	protected JPanel buttons = new JPanel(new RiverLayout());

	protected GenericLoadPanel(){
		setLayout(new RiverLayout());		
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					GenesisRecipe r = (GenesisRecipe)combo.getSelectedItem();
					setHighlight(r);
				}
				GenesisWorkbench.update();
			}
		});
	}

	public GenericLoadPanel(String label){
		this();
		add("left", new JLabel(label));
		buttons.add("left", delete);
		add("left", delete);
		add("br hfill", combo);
		add("br hfill vfill", describeScroll);
	}

	public abstract void setHighlight(GenesisRecipe r);

	public abstract GenesisRecipe getHighlight();

	public abstract String getHighlightDescription();

	@Override
	public abstract void refreshPanel();

	public void refreshPanel(Collection<GenesisRecipe> plugins){
		if(combo.getItemCount() != plugins.size()){
			GenesisWorkbench.reloadComboBoxContent(combo, plugins, getHighlight());
		}
		if(getHighlight() != null){
			description.setText(getHighlightDescription());
			combo.setSelectedItem(getHighlight());
		}else{
			description.setText("");
			combo.setSelectedIndex(-1);
		}
	}
}
