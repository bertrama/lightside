package edu.cmu.side.view.extract;

import java.awt.BorderLayout;
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
import edu.cmu.side.Workbench;
import edu.cmu.side.control.ExtractFeaturesControl;
import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.view.util.AbbreviatedComboBoxCellRenderer;
import edu.cmu.side.view.util.AbstractListPanel;
import edu.cmu.side.view.util.SelectPluginList;
import edu.cmu.side.view.util.CheckBoxListEntry;
import edu.cmu.side.view.util.FastListModel;

public class ExtractCombinedLoadPanel extends AbstractListPanel{
	
	JComboBox annotationField = new JComboBox();
	ExtractLoadPanel files = new ExtractLoadPanel("CSV Files:");
	SelectPluginList textColumnsList = new SelectPluginList();
	JScrollPane textColumnsScroll = new JScrollPane(textColumnsList);
	
	public ExtractCombinedLoadPanel(String s){
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, files);
		JPanel pan = new JPanel(new RiverLayout());
		annotationField.addActionListener(new ExtractFeaturesControl.AnnotationComboListener(this));
		annotationField.setRenderer(new AbbreviatedComboBoxCellRenderer(25));
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
			DocumentList sdl = ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList();
			Workbench.reloadComboBoxContent(annotationField, sdl.allAnnotations().keySet(), sdl.getCurrentAnnotation());
			Map<String, Boolean> columns = new TreeMap<String, Boolean>();
			for(String s : sdl.allAnnotations().keySet()){
				if(sdl.getCurrentAnnotation() == null || !sdl.getCurrentAnnotation().equals(s)) columns.put(s, false);
			}
			for(String s : sdl.getTextColumns()){
				columns.put(s,  true);
			}
			reloadCheckBoxList(columns);
		}else{
			Workbench.reloadComboBoxContent(annotationField, new ArrayList<Object>(), null);
			reloadCheckBoxList(new TreeMap<String, Boolean>());
		}
		annotationField.setEnabled(ExtractFeaturesControl.hasHighlightedDocumentList());
		
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
					DocumentList sdl = ExtractFeaturesControl.getHighlightedDocumentListRecipe().getDocumentList();
					sdl.setTextColumn(((CheckBoxListEntry)ie.getItem()).getValue().toString(), ie.getStateChange()==ItemEvent.SELECTED);
					Workbench.reloadComboBoxContent(annotationField, sdl.allAnnotations().keySet(), sdl.getCurrentAnnotation());
					Workbench.update();						
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
