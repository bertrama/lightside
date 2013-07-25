package edu.cmu.side.view.generic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.Workbench;
import edu.cmu.side.control.GenesisControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager.Stage;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.plugin.control.ImportController;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.RecipeExporter;

public abstract class GenericLoadPanel extends AbstractListPanel
{


	protected JPanel describePanel;
	protected JLabel label;
	protected JButton warn = new JButton("");
	protected JFileChooser chooser;
	protected JPanel buttons = new JPanel(new RiverLayout(0, 0));

	public static FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV", "csv", "CSV");
	public static FileNameExtensionFilter arffFilter = new FileNameExtensionFilter("ARFF (Weka)", "arff");
	public static FileNameExtensionFilter sideFilter = new FileNameExtensionFilter("LightSIDE", "side", "model.side");
	public static FileNameExtensionFilter trainedFilter = new FileNameExtensionFilter("Predict-Only", "side", "predict.side");

	protected GenericLoadPanel()
	{

		setLayout(new RiverLayout());
		combo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if (combo.getSelectedItem() != null)
				{
					Recipe r = (Recipe) combo.getSelectedItem();
					setHighlight(r);

					Component recipeTree = GenesisControl.getRecipeTree(getHighlight());
					describeScroll = new JScrollPane(recipeTree);
					if(describePanel != null)
					{
						describePanel.removeAll();
						describePanel.add(BorderLayout.CENTER, describeScroll);
	
						describePanel.revalidate();
					}
				}
				Workbench.update(GenericLoadPanel.this);

			}
		});
	}

	public GenericLoadPanel(String l)
	{
		this(l, true, true, true);
	}

	public GenericLoadPanel(String l, boolean showLoad, boolean showDelete, boolean showSave)
	{
		this(l, showLoad, showDelete, showSave, true);
	}

	public GenericLoadPanel(String l, boolean showLoad, boolean showDelete, boolean showSave, boolean showDescription)
	{
		this();

		label = new JLabel(l);
		ImageIcon iconDelete = new ImageIcon("toolkits/icons/cross.png");
		ImageIcon iconSave = new ImageIcon("toolkits/icons/disk.png");
		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_table.png");
		ImageIcon iconWarn = new ImageIcon("toolkits/icons/error.png");
		//TODO: replace with WarningButton utility class
		warn.setIcon(iconWarn);
		warn.setBorderPainted(false);
		warn.setContentAreaFilled(false);
		warn.setOpaque(false);
		warn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(GenericLoadPanel.this, warn.getToolTipText(), "Warning", JOptionPane.WARNING_MESSAGE);
			}
		});
		warn.setVisible(false);

		delete.setText("");
		delete.setIcon(iconDelete);
		delete.setToolTipText("Delete");
		delete.setBorderPainted(true);
		delete.setEnabled(false);
		save.setText("");
		save.setIcon(iconSave);
		save.setToolTipText("Save");
		save.setBorderPainted(true);
		save.setEnabled(false);
		load.setText("");
		load.setIcon(iconLoad);
		load.setToolTipText("Load");
		load.setBorderPainted(true);

		buttons.setBorder(BorderFactory.createEmptyBorder());
		
		add("hfill", label);
		buttons.add("right", warn);
		if (showLoad) buttons.add("right", load);
		add("right", buttons);
		add("br hfill", combo);
		if (showSave) add("right", save);
		if (showDelete) add("right", delete);

		if (showDescription)
		{
			describePanel = new JPanel(new BorderLayout());
			describeScroll = new JScrollPane();
			describePanel.add(BorderLayout.CENTER, describeScroll);
			add("br hfill vfill", describePanel);
		}
		// add("br left hfill", buttons);

		connectButtonListeners();
		// GenesisControl.addListenerToMap(this, this);
	}

	public abstract void setHighlight(Recipe r);

	public abstract Recipe getHighlight();

	public void deleteHighlight()
	{
		describeScroll = new JScrollPane();
		setHighlight(null);
	}

	@Override
	public abstract void refreshPanel();

	public void refreshPanel(Collection<Recipe> recipes)
	{
		if (combo.getItemCount() != recipes.size())
		{
			Workbench.reloadComboBoxContent(combo, recipes, getHighlight());
		}
		if (getHighlight() == null && combo.getItemCount() > 0)
		{
			Recipe r = (Recipe) combo.getItemAt(combo.getItemCount() - 1);
			setHighlight(r);
		}
		if (getHighlight() != null && !Workbench.getRecipeManager().containsRecipe(getHighlight()))
		{
			deleteHighlight();
		}
		if (getHighlight() != null)
		{
			combo.setSelectedItem(getHighlight());
			save.setEnabled(true);
			combo.setEnabled(true);
			delete.setEnabled(true);
		}
		else
		{
			combo.setEnabled(false);
			combo.setSelectedIndex(-1);
			save.setEnabled(false);
			delete.setEnabled(false);
			describeScroll = new JScrollPane();
			if (describePanel != null)
			{
				describePanel.removeAll();
				describePanel.add(BorderLayout.CENTER, describeScroll);
			}
		}
	}

	/** load/save/delete button listeners */
	private void connectButtonListeners()
	{
		save.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				save.setEnabled(false);
				if (combo.getSelectedIndex() >= 0)
				{
					SwingWorker saver = new SwingWorker()
					{

						@Override
						protected Object doInBackground() throws Exception
						{
							saveSelectedItem();
							return null;
						}
						
						@Override
						public void done()
						{
							save.setEnabled(true);
						}
						
					};
					
					saver.execute();
				}
			}

		});

		delete.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (combo.getSelectedIndex() >= 0)
				{
					deleteSelectedItem();
				}
			}
		});

		load.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadNewItem();
			}

		});
	}

	public void saveSelectedItem()
	{
		checkChooser();
		
		Recipe recipe = (Recipe) combo.getSelectedItem();

		if (recipe.getStage() == Stage.FEATURE_TABLE || recipe.getStage() == Stage.MODIFIED_TABLE)
		{
			RecipeExporter.exportFeatures(recipe);
		}
		else if (recipe.getStage() == Stage.TRAINED_MODEL || recipe.getStage() == Stage.PREDICTION_ONLY)
		{
			RecipeExporter.exportTrainedModel(recipe);
		}
		else
		{
			chooser.setFileFilter(sideFilter);
			chooser.setSelectedFile(new File("saved/" + recipe.getRecipeName()));
			int response = chooser.showSaveDialog(this);
			if (response == JFileChooser.APPROVE_OPTION)
			{
				File target = chooser.getSelectedFile();
				if (target.exists())
				{
					response = JOptionPane.showConfirmDialog(this, target.getName() + " already exists in this folder.\nDo you want to overwrite it?");
					if (response != JOptionPane.YES_OPTION) return;
				}

				try
				{
					FileOutputStream fout = new FileOutputStream(target);
					ObjectOutputStream oos = new ObjectOutputStream(fout);
					oos.writeObject(recipe);

				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(this, "Error while saving:\n" + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}

			}
		}
	}

	public void deleteSelectedItem()
	{
		Recipe recipe = (Recipe) combo.getSelectedItem();// TODO: should this be
															// more generic?
		setHighlight(null);
		Workbench.getRecipeManager().deleteRecipe(recipe);
		Workbench.update(this);
	};

	public void loadNewItem()
	{
		checkChooser();
		
		chooser.setFileFilter(sideFilter);
		int response = chooser.showOpenDialog(this);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			File target = chooser.getSelectedFile();
			if (!target.exists())
			{
				JOptionPane.showMessageDialog(this, "The selected file does not exist. Where did it go?", "No Such File", JOptionPane.ERROR_MESSAGE);
			}

			try
			{
				FileInputStream fout = new FileInputStream(target);
				ObjectInputStream in = new ObjectInputStream(fout);
				Recipe recipe = (Recipe) in.readObject(); // TODO: should this
															// be more generic?
				Workbench.getRecipeManager().addRecipe(recipe);
				setHighlight(recipe);
				Workbench.update(this);
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "Error while loading file:\n" + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

		}
	}

	protected void loadNewDocumentsFromCSV()
	{
		checkChooser();
		
		chooser.setFileFilter(csvFilter);
		chooser.setMultiSelectionEnabled(true);
		int result = chooser.showOpenDialog(GenericLoadPanel.this);
		if (result != JFileChooser.APPROVE_OPTION) { return; }

		File[] selectedFiles = chooser.getSelectedFiles();
		TreeSet<String> docNames = new TreeSet<String>();

		for (File f : selectedFiles)
		{
			docNames.add(f.getPath());
		}
		try{
		DocumentList testDocs = ImportController.makeDocumentList(docNames);
		testDocs.guessTextAndAnnotationColumns();
		Recipe r = Workbench.getRecipeManager().fetchDocumentListRecipe(testDocs);
		setHighlight(r);
		} catch(Exception e){
			JOptionPane.showMessageDialog(this, e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		refreshPanel();
		Workbench.update(this);
	}

	protected void checkChooser()
	{
		if(chooser == null)
		{
			chooser = new JFileChooser(new File("saved"));
		}
	}

	public void setWarning(String warnText)
	{
		warn.setVisible(true);
		warn.setToolTipText(warnText);
	}

	public void clearWarning()
	{
		warn.setVisible(false);
	}
}
