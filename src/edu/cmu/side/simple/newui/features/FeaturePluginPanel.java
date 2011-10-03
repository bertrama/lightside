package edu.cmu.side.simple.newui.features;

import java.awt.Component;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;
import edu.cmu.side.SimpleWorkbench;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.simple.FeaturePlugin;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.feature.FeatureHit;
import edu.cmu.side.simple.feature.FeatureTable;
import edu.cmu.side.simple.newui.AbstractListPanel;

public class FeaturePluginPanel extends AbstractListPanel{
	private static final long serialVersionUID = -1934157714129843426L;

	static JTextField tableName = new JTextField(5);
	static JTextField threshold = new JTextField(2);
	JButton newButton = new JButton("Extract Features (New Table)");
	JButton addButton = new JButton("Extract Features (Same Table)");
	JProgressBar progressBar = new JProgressBar();
	JButton halt = new JButton(new ImageIcon("delete.png"));

	boolean halted = false;
	static JLabel progressLabel = new JLabel();
	/** Keeps track of which feature plugin is being configured or added from right now */
	static FeaturePlugin clickedPlugin = null;

	public FeaturePluginPanel(){
		/** List model initialized and defined in the abstract class */
		super();

		add("hfill", new JLabel("Feature Extractor Plugins:"));
		scroll.setPreferredSize(new Dimension(250,80));
		add("br hfill", scroll);

		list.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent evt){
				int index = list.locationToIndex(evt.getPoint());
				if(index == -1){ return; }
				clickedPlugin = ((FeaturePlugin)listModel.get(index));
				fireActionEvent();
			}
		});

		add("br left", new JLabel("Name:"));
		tableName.setText("features");
		add("hfill", tableName);

		add("right", new JLabel("Threshold: "));
		threshold.setText("5");
		add("right", threshold);

		add("br hfill", newButton);
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimpleDocumentList corpus = FeatureFileManagerPanel.getDocumentList();
				if(clickedPlugin != null && corpus != null){
					BuildFeatureTableTask task = new BuildFeatureTableTask(progressBar, corpus);
					task.execute();
				}
			}
		});
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(clickedPlugin != null && FeatureTableListPanel.getSelectedFeatureTable() != null){
					AppendFeaturesTask task = new AppendFeaturesTask(progressBar);
					task.execute();
				}
			}
		});

		halt.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				halted = true;
				clickedPlugin.stopWhenPossible();
			}
		});
		halt.setEnabled(false);
		add("br hfill", addButton);
		add("br hfill", progressBar);
		add("", halt);
		add("br left", progressLabel);
	}

	private class AppendFeaturesTask extends OnPanelSwingTask{
		public AppendFeaturesTask(JProgressBar progressBar){
			addProgressBar(progressBar);
		}

		@Override
		protected Void doInBackground(){
			halt.setEnabled(true);
			FeatureTable ft = FeatureTableListPanel.getSelectedFeatureTable();
			Collection<FeatureHit> hits = clickedPlugin.extractFeatureHits(ft.getDocumentList(), getProgressLabel());
			ft.addAllHits(hits);
			FeatureTablePanel.activationsChanged();
			fireActionEvent();
			progressLabel.setText("");
			halt.setEnabled(false);
			return null;
		}
	}

	public static JLabel getProgressLabel(){
		return progressLabel;
	}

	/**
	 * Given a corpus, grabs the settings from Swing UI and constructs a feature table.
	 * Called from the "Create New" button in the UI.
	 * Performed in a separate thread.
	 * @param corpus
	 */
	private class BuildFeatureTableTask extends OnPanelSwingTask{
		SimpleDocumentList corpus;

		public BuildFeatureTableTask(JProgressBar progressBar, SimpleDocumentList c){
			this.addProgressBar(progressBar);
			corpus = c;
		}

		@Override
		protected Void doInBackground(){
			try{
				halt.setEnabled(true);
				int thresh = 0;
				try{
					thresh = Integer.parseInt(threshold.getText());
				}catch(Exception ex){
					AlertDialog.show("Error!", "Threshold is not an integer value.", null);
					ex.printStackTrace();
				}
				FeatureTable table = new FeatureTable(clickedPlugin, corpus,thresh);
				if(table.getFeatureSet().size() > 0){
					table.defaultEvaluation();
					table.setTableName(tableName.getText());
					
						SimpleWorkbench.addFeatureTable(table);					
						List<FeatureTable> fts = SimpleWorkbench.getFeatureTables();
						String name = "features";
						boolean available = true;
						for(FeatureTable ft : fts){
							if(name.equals(ft.getTableName())) available = false;
						}
						if(!available){
							int count = 0;
							while(!available){
								count++;
								name = "features" + count;
								available = true;
								for(FeatureTable ft : fts){
									if(name.equals(ft.getTableName())) available = false;
								}	
							}
						}
						tableName.setText(name);
				}
				if(halted){
					halted = false;
				}
				progressLabel.setText("");
				fireActionEvent();
				halt.setEnabled(false);
			}catch(Exception e){
				JOptionPane.showMessageDialog(FeaturePluginPanel.this, "Feature table creation failed. Check the terminal for detail.", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			return null;				
		}
	}

	/**
	 * Tries to get the Swing component associated with the currently selected plugin.
	 * @return That component.
	 */
	public static Component getSelectedPluginComponent(){
		if(clickedPlugin != null){
			return clickedPlugin.getConfigurationUI();
		}else{
			return null;
		}
	}

	/**
	 * Refresh commands propagate down all the way from the FeatureExtractionPanel.
	 */
	public void refreshPanel() {
		SIDEPlugin[] featureExtractors = SimpleWorkbench.getPluginsByType("feature_hit_extractor");
		if(featureExtractors.length != listModel.getSize()){
			listModel.removeAllElements();
			listModel.addAll(featureExtractors);			
		}
		addButton.setEnabled(FeatureTableListPanel.getSelectedFeatureTable() != null);
		super.refreshPanel();
		clickedPlugin = (FeaturePlugin)list.getSelectedValue();
	}
}
