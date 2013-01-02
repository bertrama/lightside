package edu.cmu.side.view.generic;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.view.util.AbstractListPanel;

public abstract class GenericLoadPanel extends AbstractListPanel{

	protected JPanel buttons = new JPanel();

	protected JLabel label;
	protected GenericLoadPanel(){
		setLayout(new RiverLayout());
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(combo.getSelectedItem() != null){
					Recipe r = (Recipe)combo.getSelectedItem();
					setHighlight(r);
				}
				Workbench.update();
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
		buttons.setLayout(new GridLayout(1,3));
		ImageIcon iconDelete = new ImageIcon("toolkits/icons/cross.png");
		ImageIcon iconSave = new ImageIcon("toolkits/icons/disk.png");
		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_table.png");
		delete.setText("");
		delete.setIcon(iconDelete);
		delete.setToolTipText("Delete");
		save.setText("");
		save.setIcon(iconSave);
		save.setToolTipText("Save");
		load.setText("");
		load.setIcon(iconLoad);
		load.setToolTipText("Load");
		buttons.add("left", delete);
		buttons.add("left", save);
		buttons.add("left", load);
		add("hfill", combo);
		add("br hfill vfill", describeScroll);
		add("br hfill", buttons);
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
			Workbench.reloadComboBoxContent(combo, recipes, getHighlight());
		}
		if(getHighlight() == null && combo.getItemCount() > 0){
			Recipe r = (Recipe)combo.getItemAt(combo.getItemCount()-1);
			setHighlight(r);
		}
		if(getHighlight() != null && !RecipeManager.containsRecipe(getHighlight())){
			deleteHighlight();
		}
		if(getHighlight() != null){
			description.setText(getHighlightDescription());
			combo.setSelectedItem(getHighlight());
			save.setEnabled(true);
			combo.setEnabled(true);
			delete.setEnabled(true);
		}else{
			description.setText("");
			combo.setEnabled(false);
			combo.setSelectedIndex(-1);
			save.setEnabled(false);
			delete.setEnabled(false);
		}
	}
}
