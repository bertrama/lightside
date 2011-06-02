package edu.cmu.side.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;

import org.apache.uima.jcas.JCas;
import org.w3c.dom.Element;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.MapBasedListCellRenderer;

import edu.cmu.side.dataitem.TrainingResult;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;
import edu.cmu.side.uima.type.SIDEAnnotation;

public class VisualizationToolkit {
	public static abstract class TrainingResultVisualizationPlugin extends SIDEPlugin{
		
		protected Map<String,String> columnNameMap;
		protected Set<? extends CharSequence> visibleSubtypeSet;
		
		public static String type = "visualization_tr";
		public abstract Component buildVisualizationPanel(TrainingResult trainingResult);
		
		public String getDisplayNameOfColumn(String columnName){
			if(columnNameMap==null){ return columnName; }
			
			String displayName = columnNameMap.get(columnName);
			return displayName==null?columnName:displayName;
		}
		
		public ListCellRenderer getColumnCellRenderer(){
			return MapBasedListCellRenderer.create(columnNameMap);
		}
		
		@Override
		public void memoryToUI() {}

		@Override
		public void uiToMemory() {}

		@Override
		public void fromXML(Element element) throws Exception {
			throw new UnsupportedOperationException();		
		}

		@Override
		public String toXML() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean doValidation(StringBuffer msg) { return true; }

		@Override
		public String getType() { return type; }


		public Set<? extends CharSequence> getVisibleSubtypeSet() {
			return visibleSubtypeSet;
		}

		public void setVisibleSubtypeSet(Set<? extends CharSequence> visibleSubtypeSet) {
			this.visibleSubtypeSet = visibleSubtypeSet;
		}

		public Map<String, String> getColumnNameMap() {
			return columnNameMap;
		}

		public void setColumnNameMap(Map<String, String> columnNameMap) {
			this.columnNameMap = columnNameMap;
		}
	}
	

	public static String visualizationSubtypeName = "(visualize_from_model)";
	public static abstract class JCasListVisualizationPlugin extends TrainingResultVisualizationPlugin {
		public static final String type = "visualisation_tr_jcas";
		
		public String getType() { return type; }

		/**
		 * visualizationSubtypeName must be refreshed beforehand by SIDE!!
		 */
		public Component buildVisualizationPanel(TrainingResult trainingResult) {
//			UIMAToolkit.addSelfPredictionAnnotation(trainingResult, visualizationSubtypeName);

			List<JCas> jCasList = trainingResult.getDocumentList().getJCasList();
			
			return buildVisualizationPanel(jCasList.toArray(new JCas[0]));
		}
		
		public abstract Component buildVisualizationPanel(
				JCas[] jCasArray);


		public abstract void setSubtypeName(String subtype);
		public abstract void setSubtypeChangeEnabled(boolean b);
	}
	
	public static abstract class DocumentListVisualizationPlugin extends JCasListVisualizationPlugin {

		public static final String type = "visualisation_tr_jcas_doclist";
		public String getType() { return type; }

//		private String subtypeName = null;
		private DocumentListViewerPanel dlvp = new DocumentListViewerPanel();
		
		
		public Component buildVisualizationPanel(JCas[] jCasArray){
			dlvp.setJCasArray(jCasArray);
//			dlvp.subtypeComboBox.setSelectedItem(subtypeName);
			
			return dlvp;
		}
		public abstract Component buildVisualizationPanel(DocumentList documentList);
	
		private class DocumentListViewerPanel extends JPanel implements ActionListener{
			private static final long serialVersionUID = 1L;
			
			private JCas[] jCasArray;
			private DocumentListViewerPanel(){
				yeriInit();
			}
			
			public void actionPerformed(ActionEvent evt){
				this.refreshPanel();
			}

			private void yeriInit(){
				this.setLayout(new BorderLayout());
				
				JPanel leftPanel = new JPanel();
				leftPanel.setLayout(new RiverLayout());
				
//				Set<String> commonSubtypeSet = UIMAToolkit.getCommonSubtypeNameSet(jCasCollection, SIDEAnnotation.type);
				subtypeComboBox = new JComboBox();
				subtypeComboBox.addActionListener(this);
				leftPanel.add("", new JLabel("target column:"));
				leftPanel.add("br hfill", subtypeComboBox);
//				SwingToolkit.reloadComboBoxContent(subtypeComboBox,
//						commonSubtypeSet.toArray(new String[0]),
//						new ActionListener[]{this},
//						false);
				
				leftPanel.add("p", new JSeparator());
				configPanelWrapper = new JPanel();
				configPanelWrapper.setLayout(new BorderLayout());
				leftPanel.add("p hfill", configPanelWrapper);
				
				rightPanel = new JPanel();
				rightPanel.setLayout(new BorderLayout());
				
				JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftPanel, rightPanel);
				this.add(splitPane, BorderLayout.CENTER);
			}
			
			public void refreshPanel(){
				configPanelWrapper.removeAll();
				Component configUI = DocumentListVisualizationPlugin.this.getConfigurationUI();
				if(configUI!=null){
					configPanelWrapper.add(configUI);
				}
				
				rightPanel.removeAll();
				
				// subtype
				Set<String> commonSubtypeSet = UIMAToolkit.getCommonSubtypeNameSet(jCasArray, SIDEAnnotation.type);
				Set<? extends CharSequence> availSubtypeSet = CollectionsToolkit.intersection(commonSubtypeSet, visibleSubtypeSet, null);
				SwingToolkit.reloadComboBoxContent(subtypeComboBox, availSubtypeSet.toArray(new CharSequence[0]), new ActionListener[]{this}, false);
				subtypeComboBox.setRenderer(getColumnCellRenderer());
				
				String selectedSubtypeName = (String)subtypeComboBox.getSelectedItem();
//				DocumentListVisualizationPlugin.this.subtypeName = selectedSubtypeName;
				if(selectedSubtypeName==null){ return; }
				
				DocumentList documentList = DocumentList.create(jCasArray, selectedSubtypeName);
				Component c = DocumentListVisualizationPlugin.this.buildVisualizationPanel(documentList);
				rightPanel.add(c, BorderLayout.CENTER);
			}
			
			private JPanel configPanelWrapper;
			private JComboBox subtypeComboBox;
			private JPanel rightPanel;
//			public void setJCasCollection(Collection<? extends JCas> casCollection) {
//				jCasCollection = casCollection;
//				this.refreshPanel();
//			}
//			
			public void setJCasArray(JCas[] jCasArray) {
				this.jCasArray = jCasArray;
				this.refreshPanel();
			}
			
		}
		
		public void setSubtypeChangeEnabled(boolean b){
			dlvp.subtypeComboBox.setEnabled(b);
		}

		public void setSubtypeName(String subtypeName) {
			this.dlvp.subtypeComboBox.setSelectedItem(subtypeName);
		}
		
		@Override
		protected final Component getConfigurationUIForSubclass() {
			return null;
		}
	}
}
