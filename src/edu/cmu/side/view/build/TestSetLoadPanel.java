package edu.cmu.side.view.build;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.yerihyo.yeritools.io.FileToolkit;

import edu.cmu.side.Workbench;
import edu.cmu.side.control.BuildModelControl;
import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.model.Recipe;
import edu.cmu.side.model.RecipeManager;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.view.generic.GenericLoadPanel;

public class TestSetLoadPanel extends GenericLoadPanel{

	public TestSetLoadPanel(String s){
		super();		
		ImageIcon iconDelete = new ImageIcon("toolkits/icons/cross.png");
		ImageIcon iconLoad = new ImageIcon("toolkits/icons/folder_table.png");
		delete.setText("");
		delete.setIcon(iconDelete);
		delete.setToolTipText("Delete");
		delete.setBorderPainted(true);
		load.setText("");
		load.setToolTipText("Open");
		load.setIcon(iconLoad);
		load.addActionListener(new ActionListener() //TODO: this is duplicated from ExtractFeaturesControl. should be generic/util, to allow for later extension to ARFF?
		{
			private JFileChooser chooser = new JFileChooser(Workbench.csvFolder);
			

			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setFileFilter(FileToolkit
						.createExtensionListFileFilter(new String[] { "csv" }, true));
				chooser.setMultiSelectionEnabled(true);
				int result = chooser.showOpenDialog(TestSetLoadPanel.this);
				if (result != JFileChooser.APPROVE_OPTION) {
					return;
				}
				
				File[] selectedFiles = chooser.getSelectedFiles();
				HashSet<String> docNames = new HashSet<String>();
				
				String description = "";
				for(File f : selectedFiles)
				{
					docNames.add(f.getPath());
					description += f.getName()+"\n";
				}	

				DocumentList testDocs = new DocumentList(docNames);
				Recipe r = RecipeManager.fetchDocumentListRecipe(testDocs);
				setHighlight(r);
				
				Workbench.update();
			}
			
		});
		//load.addActionListener(new ExtractFeaturesControl.AddFilesListener(this));
		load.setBorderPainted(true);
		label = new JLabel(s);
		add("hfill", label);
		add("right", load);
		add("br hfill", combo);
		add("left", delete);
		describeScroll = new JScrollPane();
		describePanel.add(BorderLayout.CENTER, describeScroll);
		add("br hfill vfill", describePanel);
	}

	@Override
	public void setHighlight(Recipe r) {
		BuildModelControl.updateValidationSetting("testRecipe", r);
		BuildModelControl.updateValidationSetting("testSet", r.getDocumentList());
	}

	@Override
	public Recipe getHighlight() {
		return (Recipe) BuildModelControl.getValidationSettings().get("testRecipe");
	}

	@Override
	public void refreshPanel() {		
		refreshPanel(BuildModelControl.getDocumentLists());
	}
}
