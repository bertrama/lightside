package edu.cmu.side.ui.configpanel;

import java.awt.BorderLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.OnPanelSwingTask;
import edu.cmu.side.Workbench;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTableKey;
import edu.cmu.side.plugin.FEPlugin;
import edu.cmu.side.ui.managerpanel.FeatureTableManagerPanel;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;

public class FeatureTableConfigPanel extends JPanel{
	private static final long serialVersionUID = 1L;

	public FeatureTableConfigPanel(){
		yeriInit();
	}
	
	private DocumentListConfigPanel annotationPanel;
	private FEPluginConfigPanel featurePanel;
	private FeatureTableManagerPanel featureTableManagerPanel;
	private JTextField featureTableNameTextField;
	private JButton createButton;
	private JProgressBar progressBar;
	
	private void yeriInit() {
		// leftPanel
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new RiverLayout());
		
		annotationPanel = new DocumentListConfigPanel();
		annotationPanel.setAnnotationLabelText("annotation:");
		leftPanel.add("hfill", annotationPanel);
		leftPanel.add("p hfill", new JSeparator());
		
		featurePanel = new FEPluginConfigPanel();
		featurePanel.updateFEPluginArray(null);
		annotationPanel.addActionListner(featurePanel);
		featurePanel.setDocumentList(annotationPanel.getDocumentList());

		leftPanel.add("p hfill", featurePanel);
		
		leftPanel.add("p hfill", new JSeparator());
		
		featureTableNameTextField = new JTextField();
		featureTableNameTextField.setText("feature_table");
		leftPanel.add("p ", new JLabel("new feature table name:"));
		leftPanel.add("hfill", featureTableNameTextField);
		
		progressBar = new JProgressBar();
		createButton = new JButton("create feature table");
		createButton.setMnemonic(KeyEvent.VK_C);
		createButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				double time1 = System.currentTimeMillis();
				(new BuildTask(progressBar)).execute();
				double time2 = System.currentTimeMillis();
				System.err.println("Feature table creation done, took " + (time2-time1)/1000.0 + " seconds");
			}
		});
		leftPanel.add("br hfill", createButton);

		leftPanel.add("br hfill", progressBar);
		
		leftPanel.add("p hfill", new JSeparator());
		final JSplitPane splitPane = new JSplitPane();

		featureTableManagerPanel = new FeatureTableManagerPanel();
		leftPanel.add("p hfill", featureTableManagerPanel);
		featureTableManagerPanel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				final FeatureTable featureTable = featureTableManagerPanel.getSelectedFeatureTable();
				JPanel rightPanel = new JPanel();
				rightPanel.setLayout(new RiverLayout());
				rightPanel.add("left", new JLabel("feature table manager"));
				rightPanel.add("br left", new JLabel("use filtered list below as table:"));
				final JTextField tf = new JTextField();
				rightPanel.add("hfill", tf);
				final JPanel checklistPanel = new JPanel(new RiverLayout());
				JButton updatedTableButton = new JButton("create");
				updatedTableButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						FeatureTable newFeatureTable = featureTable.clone();
						newFeatureTable.setName(tf.getText());
						List<FeatureTableKey> newftk = newFeatureTable.getAllFeatureTableKeyList();
						Set<String> filter = new HashSet<String>();
						for(Component c : checklistPanel.getComponents()){
							if(c instanceof JCheckBox && !((JCheckBox)c).isSelected()){
								for(FeatureTableKey f : featureTable.getAllFeatureTableKeyList()){
									if(f.getFeatureName().equals(((JCheckBox)c).getText())){
										filter.add(f.getFeatureIdentifyingString());
									}
								}
							}
						}
						newFeatureTable.setFeatureFilterer(filter);
						newFeatureTable.rebuild(null, true);
					}
				});
				rightPanel.add("right", updatedTableButton);

				JLabel sizeLabel = new JLabel("Feature table size: " + (featureTable == null ? "":featureTable.getAllFeatureTableKeyList().size()));
				rightPanel.add("br left", sizeLabel);
				JButton uncheckButton = new JButton("uncheck all");
				uncheckButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						for(Component c : checklistPanel.getComponents()){
							if(c instanceof JCheckBox){
								((JCheckBox) c).setSelected(false);
							}
						}
					}
				});
				JButton checkButton = new JButton("check all");
				checkButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						for(Component c : checklistPanel.getComponents()){
							if(c instanceof JCheckBox){
								((JCheckBox) c).setSelected(true);
							}
						}
					}
				});
				rightPanel.add("right", checkButton); rightPanel.add("right", uncheckButton);
				if(featureTable!=null){
					List<FeatureTableKey> ftkList = featureTable.getAllFeatureTableKeyList();
					for(FeatureTableKey ftk : ftkList){
						if(ftk.isMeta()) continue;
						JCheckBox featureCheckbox = new JCheckBox(ftk.getFeatureName());
						featureCheckbox.setSelected(true);
						checklistPanel.add("br left", featureCheckbox);
					}
				}
				rightPanel.add("br hfill vfill", new JScrollPane(checklistPanel));
				splitPane.setRightComponent(rightPanel);
			}
		});
		
		
		
		featureTableManagerPanel.addDoubleClickListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				FeatureTable featureTable = (FeatureTable)e.getSource();
				if(featureTable==null){ return; }
				
				int result = JOptionPane.showConfirmDialog(featureTableManagerPanel, "load settings of this feature table?", "load", JOptionPane.YES_NO_OPTION);
				if(result!=JOptionPane.YES_OPTION){ return; }
				
				FeatureTableConfigPanel configPanel = FeatureTableConfigPanel.this;
				configPanel.loadFeatureTable(featureTable);
			}
		});
		
		// right Panel
		
		JScrollPane leftPane = new JScrollPane(leftPanel);
		leftPane.setPreferredSize(new Dimension(400, splitPane.getHeight()));
		splitPane.setLeftComponent(leftPane);
		featureTableManagerPanel.actionPerformed(null);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
	}
	

	private class BuildTask extends OnPanelSwingTask{
		public BuildTask(JProgressBar progressBar){
			this.addProgressBar(progressBar);
		}

		@Override
		protected Void doInBackground(){
			try{
				FeatureTableConfigPanel.this.createFeatureTable();
				featureTableManagerPanel.actionPerformed(null);				
			}catch(Exception e){
				e.printStackTrace();
			}
            return null;
		}
	}
	
	protected void loadFeatureTable(FeatureTable featureTable){
		
		if(featureTable==null){ return; }
		
		DocumentList documentList = featureTable.getDocumentList();
		this.annotationPanel.setDocumentList(documentList);

		this.featurePanel.updateFEPluginArray( featureTable.getFEPluginList().toArray(new FEPlugin[0]) );
		this.featureTableNameTextField.setText(featureTable.getName());
	}
	
	
//	protected void a(){
//	}
	
	protected void createFeatureTable() {
		// DocumentList
		DocumentList documentList = annotationPanel.getDocumentList();
		if(documentList==null){
			printTrainError("No annotation scheme has been selected.");
			return;
		}
		
		// FEPlugin[]
		FEPlugin[] fePluginArray = this.featurePanel.getCheckedFEPlugin();
		if(fePluginArray.length==0){
			printTrainError("No feature extraction plugins have been selected.");
			return;
		}
		// name
		String featureTableName = featureTableNameTextField.getText().trim();
		if(featureTableName.length()==0){
			printTrainError("Please type in feature table name");
			return;
		}
		
		FeatureTable featureTable = FeatureTable.createAndBuild(documentList, fePluginArray, featurePanel.rareFilterThreshold(), null);
		
		featureTable.setName(featureTableName);
		Workbench.current.featureTableListManager.add(featureTable);
	}

	private void printTrainError(String string) {
		AlertDialog.show("Error!", string, this);
	}

}
