package edu.cmu.side.viewer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.apache.uima.jcas.JCas;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.MultipleSelectionOption;
import com.yerihyo.yeritools.swing.SwingToolkit.ValueCustomizedListCellRenderer;

import edu.cmu.side.Workbench;
import edu.cmu.side.plugin.PluginWrapper;
import edu.cmu.side.plugin.SegmenterPlugin;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.type.SIDESegment;

public class SegmentationEditor extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private JCas jCas;
	private AnnotationEditor ae;
	public SegmentationEditor(JCas jCas, AnnotationEditor ae){
		this.jCas = jCas;
		this.ae = ae;
		yeriInit();
	}

//	private Map<String, SIDEPlugin> pluginMap = new HashMap<String,SIDEPlugin>();
	
	private static class ComboBoxRenderer extends ValueCustomizedListCellRenderer{
		public ComboBoxRenderer(ListCellRenderer listCellRenderer){
			super(listCellRenderer);
		}
		
		@Override
		protected String getText(Object value) {
			if(value instanceof SegmenterPlugin){
				SegmenterPlugin plugin = (SegmenterPlugin)value;
//				return "segment using '"+plugin.getClass().getName()+"'";
				return plugin.getClass().getName();
			}else if(value instanceof String){
				return (String)value;
			}else{
				throw new UnsupportedOperationException();
			}
		}
	}
	private void yeriInit() {
		this.setLayout(new RiverLayout());
		this.add("br ", new JLabel("segmentation:"));
		
		segmentationComboBox = new JComboBox();
		segmentationComboBox.setRenderer(new ComboBoxRenderer(segmentationComboBox.getRenderer()));
		
		Set<String> baseSubtypeSet = UIMAToolkit.getBaseSubtypeNameSet(jCas);
		for(String baseSubtype : baseSubtypeSet){
			segmentationComboBox.addItem(baseSubtype);
		}
		
		List<PluginWrapper> pluginWrapperList = Workbench.current.pluginManager.getPluginWrapperCollectionByType(SegmenterPlugin.type);
		for(PluginWrapper pluginWrapper : pluginWrapperList){
			SegmenterPlugin segmenterPlugin = (SegmenterPlugin)pluginWrapper.getSIDEPlugin();
			String segmenterBaseSubtypeName = UIMAToolkit.getSegmenterBaseSubtypeName(segmenterPlugin);
			if(baseSubtypeSet.contains(segmenterBaseSubtypeName)){ continue; }
			segmentationComboBox.addItem(segmenterPlugin);
		}
		
		
		
		this.add("br hfill", segmentationComboBox);
		
		this.add("br ",new JSeparator());
		
		subtypeField = new JTextField();
//		subtypeField.setEditable(false);
		this.add("br ",new JLabel("subtype name:"));
		this.add("br ", new JLabel(""));
		this.add("hfill",subtypeField);
		
		this.add("br ",new JSeparator());
		
		segmentButton = new JButton("segment");
		this.add("br hfill", segmentButton);
		segmentButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selectedItem = segmentationComboBox.getSelectedItem();
				if(selectedItem==null){
					AlertDialog.show("error!", "plugin must be selected", SegmentationEditor.this);
					return;
				}
				
				String subtypeName = subtypeField.getText();
				if(subtypeName==null){
					AlertDialog.show("error!", "subtype name must be selected", SegmentationEditor.this);
					return;
				}
				
				MultipleSelectionOption thisOption = UIMAToolkit.overrideAndContinueSubtype(jCas, SIDESegment.type, subtypeName, null, null);
				if(thisOption==MultipleSelectionOption.NO_ALL_OPTION){
					return;
				}
				
				
				String segmentationSubtypeName;
				String annotationSubtypeName = ""+subtypeName;
				
				if(selectedItem instanceof SegmenterPlugin){
					SegmenterPlugin plugin = (SegmenterPlugin)selectedItem;
					segmentationSubtypeName = UIMAToolkit.getSegmenterBaseSubtypeName(plugin);
					plugin.uiToMemory();
					try {
						UIMAToolkit.segmentJCas(jCas, plugin);
					} catch (Exception ex) {
						AlertDialog.show("error", ex, SegmentationEditor.this);
					}
				}else if(selectedItem instanceof String){
					segmentationSubtypeName = (String)selectedItem;
				}else{ throw new UnsupportedOperationException(); }
				
				UIMAToolkit.addAnnotationLayer(jCas, segmentationSubtypeName, annotationSubtypeName);
				
				try {
					UIMAToolkit.saveJCasToSource(jCas);
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
				ae.refreshPanel();
				ae.setSubtypeName(annotationSubtypeName);
				ae.refreshPanel();
				
				Window window = SwingToolkit.getParentWindow(SegmentationEditor.this);
				window.dispose();
			}
		});
	}
	
	private JComboBox segmentationComboBox;
	private JTextField subtypeField;
	private JButton segmentButton;
}
