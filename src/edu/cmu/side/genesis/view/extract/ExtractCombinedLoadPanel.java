package edu.cmu.side.genesis.view.extract;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import se.datadosen.component.RiverLayout;

import edu.cmu.side.genesis.GenesisWorkbench;
import edu.cmu.side.genesis.control.ExtractFeaturesControl;
import edu.cmu.side.genesis.model.GenesisRecipe;
import edu.cmu.side.genesis.view.CheckBoxList;
import edu.cmu.side.genesis.view.CheckBoxListEntry;
import edu.cmu.side.genesis.view.SwingUpdaterLabel;
import edu.cmu.side.genesis.view.generic.GenericLoadPanel;
import edu.cmu.side.simple.SimpleDocumentList;
import edu.cmu.side.simple.newui.AbstractListPanel;
import edu.cmu.side.simple.newui.FastListModel;
import edu.cmu.side.simple.newui.features.FeatureFileManagerPanel;
import edu.cmu.side.simple.newui.features.FeaturePluginPanel;

public class ExtractCombinedLoadPanel extends AbstractListPanel{
	
	JComboBox annotationField = new JComboBox();
	ExtractLoadPanel files = new ExtractLoadPanel("CSV Files:");
	CheckBoxList textColumnsList = new CheckBoxList();
	JScrollPane textColumnsScroll = new JScrollPane(textColumnsList);
	
	public ExtractCombinedLoadPanel(String s){
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, files);
		JPanel pan = new JPanel(new RiverLayout());
		annotationField.addActionListener(new ExtractFeaturesControl.AnnotationComboListener(this));
		pan.add("left", new JLabel("Class:"));
		pan.add("hfill", annotationField);
		pan.add("br left", new JLabel("Text Fields:"));
		pan.add("br hfill", textColumnsScroll);
		add(BorderLayout.SOUTH, pan);
	}
	
	@Override
	public void refreshPanel(){
		files.refreshPanel();
		if(files.getHighlight() != null){
			SimpleDocumentList sdl = ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList();
			GenesisWorkbench.reloadComboBoxContent(annotationField, sdl.allAnnotations().keySet(), sdl.getCurrentAnnotation());
			Map<String, Boolean> columns = new TreeMap<String, Boolean>();
			for(String s : sdl.allAnnotations().keySet()){
				if(sdl.getCurrentAnnotation() == null || !sdl.getCurrentAnnotation().equals(s)) columns.put(s, false);
			}
			for(String s : sdl.getTextColumns()){
				columns.put(s,  true);
			}
			reloadCheckBoxList(columns);
		}else{
			GenesisWorkbench.reloadComboBoxContent(annotationField, new ArrayList<Object>(), null);
			reloadCheckBoxList(new TreeMap<String, Boolean>());
		}
		
	}

	public void reloadCheckBoxList(Map<String, Boolean> labels){
		FastListModel model = new FastListModel();
		CheckBoxListEntry[] array = new CheckBoxListEntry[labels.size()];
		int i = 0;
		for(String key : labels.keySet()){
			array[i] = new CheckBoxListEntry(key, labels.get(key));
			array[i].addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent ie) {
					SimpleDocumentList sdl = ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList();
					sdl.setTextColumn(((CheckBoxListEntry)ie.getItem()).getValue().toString(), ie.getStateChange()==ie.SELECTED);
					GenesisWorkbench.reloadComboBoxContent(annotationField, sdl.allAnnotations().keySet(), sdl.getCurrentAnnotation());
					GenesisWorkbench.update();						
				}
			});
			i++;
		}
		model.addAll(array);
		textColumnsList.setModel(model);
	}

	
	public JComboBox getAnnotationField(){
		return annotationField;
	}
}
