package edu.cmu.side.view.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.GenesisWorkbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.view.util.AbstractListPanel;

public abstract class GenericLoadPanel extends AbstractListPanel{

	protected JPanel buttons = new JPanel(new RiverLayout());

	protected JLabel label;
	protected GenericLoadPanel(){
		setLayout(new RiverLayout());
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					Recipe r = (Recipe)combo.getSelectedItem();
					setHighlight(r);
				}
				GenesisWorkbench.update();
			}
		});
		delete.addActionListener(new DeleteFilesListener(combo, this));
	}

	public static class DeleteFilesListener implements ActionListener{
		private JComboBox parentComponent;
		private GenericLoadPanel loadPanel;

		public DeleteFilesListener(JComboBox parentComponent, GenericLoadPanel load){
			this.parentComponent = parentComponent;
			loadPanel = load;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RecipeManager.deleteRecipe((Recipe)parentComponent.getSelectedItem());
			loadPanel.deleteHighlight();
		}
	}

	public GenericLoadPanel(String l){
		this();
		label = new JLabel(l);
		add("left", label);
		buttons.add("left", delete);
		add("left", delete);
		add("br hfill", combo);
		add("br hfill vfill", describeScroll);
	}

	public abstract void setHighlight(Recipe r);

	public abstract Recipe getHighlight();

	public abstract String getHighlightDescription();

	public void deleteHighlight(){
		setHighlight(null);
	}

	@Override
	public abstract void refreshPanel();

	public void refreshPanel(Collection<Recipe> recipes){
		if(combo.getItemCount() != recipes.size()){
			GenesisWorkbench.reloadComboBoxContent(combo, recipes, getHighlight());
		}
		if(getHighlight() == null && combo.getItemCount() > 0){
			Recipe r = (Recipe)combo.getItemAt(combo.getItemCount()-1);
			setHighlight(r);
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
