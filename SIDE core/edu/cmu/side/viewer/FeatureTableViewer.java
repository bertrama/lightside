package edu.cmu.side.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTable;
import edu.cmu.side.ml.FeatureExtractionToolkit.FeatureTableKey;
import edu.cmu.side.uima.DocumentListInterface;

public class FeatureTableViewer extends JPanel{
	private static final long serialVersionUID = 1L;

	private FeatureTable featureTable;
	
	private JTable table;
	private DefaultTableModel model;
	private JScrollPane scrollPane;
	
	public FeatureTableViewer(){
		yeriInit();
	}

	
	private static String[][] tmpData = new String[][]{
			new String[]{"a", "b"},
			new String[]{"c", "d"},
	};
	private static String[] tmpColumn = new String[]{"x", "y"};
	
	private void yeriInit() {
		this.setLayout(new BorderLayout());
		
		model = new DefaultTableModel();
		table = new JTable(model);
		scrollPane = new JScrollPane(table);
		SwingToolkit.adjustScrollBar(scrollPane, JScrollBar.VERTICAL);
		
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	protected void refreshPanel(){
		DocumentListInterface documentList = featureTable.getDocumentList();
		List<FeatureTableKey> keyList = featureTable.getAllFeatureTableKeyList();
		int keyListSize = 10;
		
		Object[] columnIdentifiers = new Object[keyListSize];
		List<Object[]> valueTable = new ArrayList<Object[]>();
		
		Iterator<Object> iterator=documentList.iterator();
		for(int i=0; iterator.hasNext(); i++){
			iterator.next();
			
			Object[] valueArray = new Object[keyListSize];
			for(int j=0; j<keyListSize; j++){
				FeatureTableKey key = keyList.get(j);
				if(i==0){
					columnIdentifiers[j] = key.getFeatureName();
				}
				valueArray[j] = featureTable.getValue(key, i).doubleValue();
			}
			valueTable.add(valueArray);
		}
		Object[][] dataVector = valueTable.toArray(new Object[0][]);
		model.setDataVector(dataVector, columnIdentifiers);
	}

	public static void main(String[] args){
		test01();
	}
	protected static void test02(){
		JTable table = new JTable(tmpData, tmpColumn);
		
		TestFrame testFrame = new TestFrame(new JScrollPane(table));
		testFrame.setSize(new Dimension(600,400));
		testFrame.showFrame();
	}
	protected static void test01(){
		SIDEToolkit.FileType.loadAll();
		Iterator<FeatureTable> iterator = Workbench.current.featureTableListManager.iterator();
		if(!iterator.hasNext()){ return; }
		
		FeatureTable featureTable = iterator.next();
		
		FeatureTableViewer ftv = new FeatureTableViewer();
		ftv.setFeatureTable(featureTable);
		
		TestFrame testFrame = new TestFrame(ftv);
		testFrame.setSize(new Dimension(800,600));
		testFrame.showFrame();
	}


	public void setFeatureTable(FeatureTable featureTable) {
		this.featureTable = featureTable;
		this.refreshPanel();
	}
}
