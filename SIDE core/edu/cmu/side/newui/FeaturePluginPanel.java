package edu.cmu.side.newui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.swing.JCheckBoxList;
import com.yerihyo.yeritools.swing.SimpleOKDialog;
import com.yerihyo.yeritools.swing.SwingToolkit.ResultOption;

import edu.cmu.side.viewer.FastListModel;
import edu.cmu.side.feature.FeaturePlugin;

public class FeaturePluginPanel extends JPanel{

	FastListModel pluginsModel = new FastListModel();
	JCheckBoxList pluginsList = new JCheckBoxList();
	JTextField tableName = new JTextField();
	JTextField threshold = new JTextField();
	JButton newButton = new JButton("Create New FT");
	JButton addButton = new JButton("Add to Current FT");
	JProgressBar progressBar = new JProgressBar();
	FeaturePlugin[] plugins;

	public FeaturePluginPanel(){
		setLayout(new RiverLayout());
		pluginsList.setModel(pluginsModel);

		add("hfill", new JLabel("feature extractor plugins:"));
		add("br hfill", new JScrollPane(pluginsList));

		pluginsList.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt){
				int index = pluginsList.locationToIndex(evt.getPoint());
				if(index == -1){ return; }
				if(evt.getButton()!=MouseEvent.BUTTON3){ return; }
				Component configUI = plugins[index].getConfigurationUI();
				ResultOption resultOption = SimpleOKDialog.show(pluginsList, "config", configUI);
				if(resultOption!=ResultOption.APPROVE_OPTION){
					return;
				}
				plugins[index].uiToMemory();
			}
		});

		add("br left", new JLabel("name:"));
		tableName.setText("feature_table");
		add("hfill", tableName);

		add("right", new JLabel("threshold: "));

		threshold.setText("5");
		add("right", threshold);

		add("br center", newButton);
		add("center", addButton);
		add("br hfill", progressBar);
	}
}
