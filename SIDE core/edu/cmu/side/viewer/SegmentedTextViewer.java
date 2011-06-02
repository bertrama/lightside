package edu.cmu.side.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;

import se.datadosen.component.ControlPanel;

import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.math.StatisticsToolkit;
import com.yerihyo.yeritools.swing.QuietCaret;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.MultipleSelection;
import com.yerihyo.yeritools.swing.SwingToolkit.ResultOption;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;
import com.yerihyo.yeritools.swing.SwingToolkit.TinyBar;

import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.type.SIDEAnnotation;
import edu.cmu.side.uima.type.SIDEAnnotationSetting;
import edu.cmu.side.uima.type.SIDEPredictionAnnotation;
import edu.cmu.side.uima.type.SIDESegment;

public class SegmentedTextViewer extends ControlPanel {
	private static final long serialVersionUID = 1L;
	private JCas jCas = null;
	private String subtypeName = null;
//	SegmentList source = null;
	protected SegmentTextArea lastSelected = null;
	
	private List<ActionListener> alList = new ArrayList<ActionListener>();
	public void addActionListener(ActionListener al){
		alList.add(al);
	}
	public void removeActionListener(ActionListener al){
		alList.remove(al);
	}
	protected void fireActionEvent(){
		ActionEvent ae = new ActionEvent(this, 0, "changed");
		for(ActionListener al : alList){
			al.actionPerformed(ae);
		}
	}
	
	protected MouseAdapter callback = new MouseAdapter() {
		public void mousePressed(java.awt.event.MouseEvent evt) {
			Object source = evt.getSource();
			if (!(source instanceof SegmentTextArea)) {
				return;
			}
			SegmentTextArea sta = (SegmentTextArea) source;
//			SIDEAnnotation sideAnnotation= sta.getSIDEAnnotation();
			
			// can optimize but didn't for easy understanding of code
			switch (evt.getButton()) {
			case MouseEvent.BUTTON1:
				if (evt.isShiftDown()) {
					selectBetween(lastSelected, sta);
					return;
				} else {
					adjustSegmentTextAreaSelection(sta, evt.isControlDown());
					lastSelected = sta;
					return;
				}
			case MouseEvent.BUTTON3:
				if (!sta.isSelected()) {
					adjustSegmentTextAreaSelection(sta, evt.isControlDown());
					lastSelected = sta;
				}
				ShowLabelMenu(evt);
				return;
			}
		}
	};

	// 1. Manage selection

	public List<SegmentTextArea> getSelectedSegmentTextAreas() {
		Component cList[] = this.getComponents();
		List<SegmentTextArea> list = new ArrayList<SegmentTextArea>();
		for (Component c : cList) {
			if (!(c instanceof SegmentTextArea)) {
				continue;
			}
			SegmentTextArea sta = (SegmentTextArea) c;
			if (sta.isSelected()) {
				list.add(sta);
			}
		}
		return list;
	}
	
//	public boolean[] getSelectionMask() {
//		Component cList[] = this.getComponents();
//		List<Boolean> list = new ArrayList<Boolean>();
//		for (Component c : cList) {
//			if (!(c instanceof SegmentTextArea)) {
//				continue;
//			}
//			SegmentTextArea sta = (SegmentTextArea) c;
//			list.add(sta.isSelected());
//		}
//		return CollectionsToolkit.toBooleanArray(list);
//	}

	protected List<SegmentTextArea> getSegmentTextAreas() {
		List<SegmentTextArea> returnList = new ArrayList<SegmentTextArea>();
		Component cList[] = this.getComponents();
		for (Component c : cList) {
			if (c instanceof SegmentTextArea) {
				returnList.add((SegmentTextArea) c);
			}
		}
		return returnList;
	}

	protected SegmentTextArea getSegmentTextArea(int index) {
		int currentIndex = 0;
		Component cList[] = this.getComponents();
		for (Component c : cList) {
			if (c instanceof SegmentTextArea) {
				if (index == currentIndex) {
					return (SegmentTextArea) c;
				}
				currentIndex++;
			}
		}
		return null;
	}

	protected boolean isInOrder(SegmentTextArea first, SegmentTextArea second)
			throws Exception {
		if (first == null || second == null) {
			throw new NullPointerException();
		}
		if (first == second) {
			return true;
		}

		boolean isInOrder = false;
		boolean hasAppeared = false;
		for (SegmentTextArea sta : getSegmentTextAreas()) {
			if (!hasAppeared) {
				if (sta == first) {
					isInOrder = true;
					hasAppeared = true;
				} else if (sta == second) {
					isInOrder = false;
					hasAppeared = true;
				} else {
					continue;
				}
			} else {
				if (sta == first || sta == second) {
					return isInOrder;
				}
			}
		}
		throw new Exception("Not both are in SegmentTextArea Set");
	}

	public void selectBetween(SegmentTextArea lastSelected,
			SegmentTextArea currentSelected) {
		boolean isInOrder = true;
		try {
			isInOrder = isInOrder(lastSelected, currentSelected);
		} catch (Exception ex) {
			selectSegmentTextArea(currentSelected, false);
			return;
		}

		this.deselectAllSegmentTextAreas();

		boolean hasStarted = false;
		SegmentTextArea first = isInOrder ? lastSelected : currentSelected;
		SegmentTextArea second = isInOrder ? currentSelected : lastSelected;

		for (SegmentTextArea sta : getSegmentTextAreas()) {
			if (!hasStarted) {
				if (!(hasStarted = (first == sta))) {
					continue;
				}
				// this cannot go to the beginning of for
				// because first==second is possible
				if (second == sta) {
					break;
				}
			}
			this.selectSegmentTextArea(sta, true);
			if (second == sta) {
				break;
			}
		}
	}

	private void ShowLabelMenu(java.awt.event.MouseEvent e) {
		RightClickMenu rightClickMenu = new RightClickMenu();
		rightClickMenu.init();
		rightClickMenu.show((Component) e.getSource(), e.getX() - 5,
				e.getY() - 5);
	}

	private void selectSegmentTextArea(SegmentTextArea source,
			boolean isCtrlDown) {
		if (source == null) {
			return;
		}
		if (!isCtrlDown) {
			deselectAllSegmentTextAreas();
		}
		source.setSelected(true);
	}

	private void adjustSegmentTextAreaSelection(SegmentTextArea source,
			boolean isCtrlDown) {
		if (source == null) {
			return;
		}

		// can optimize but didn't for easy understanding of code
		if (isCtrlDown) {
			source.toggleSelection();
		} else {
			selectSegmentTextArea(source, false);
		}
	}

	private void selectAllSegmentTextAreas() {
		for (SegmentTextArea sta : getSegmentTextAreas()) {
			sta.setSelected(true);
		}
	}
	
	private void selectUnannotatedSegmentTextAreas(){
		for(SegmentTextArea sta : getSegmentTextAreas()){
			sta.setSelected(!sta.isAnnotated());
		}
	}

	private void deselectAllSegmentTextAreas() {
		for (SegmentTextArea sta : getSegmentTextAreas()) {
			sta.deselect();
		}
	}

//	public class LabelMenuItem extends JMenuItem {
//		private static final long serialVersionUID = 1L;
//
//		public LabelMenuItem(String labelString) {
//			super(labelString);
//			setActionCommand("setLabel");
//		}
//	}

	protected class RightClickMenu extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = 1L;
//		private SegmentedTextViewer annoEditor = SegmentedTextViewer.this;

		public void init() {
			JMenuItem jTemp = null;

			SIDEAnnotationSetting setting = UIMAToolkit.getSIDEAnnotationSetting(jCas, subtypeName);
			Map<String,Color> labelColorMap = UIMAToolkit.getLabelColorMap(setting);
			
			if(labelColorMap!=null && labelColorMap.size()>0){
				jTemp = new JMenuItem("set label");
				jTemp.setActionCommand("setLabelDialog");
				jTemp.addActionListener(this);
				add(jTemp);
				add(new JSeparator());
			}
			
//			if(labelColorMap!=null && labelColorMap.size()>0){
//				for (String label : labelColorMap.keySet()) {
//					jTemp = new JMenuItem(label);
//					jTemp.setActionCommand("setLabelMenu");
//					jTemp.addActionListener(this);
//					add(jTemp);
//				}
//				add(new JSeparator());
//			}
			
			jTemp = new JMenuItem("select segments with same annotation");
			jTemp.setActionCommand("selectIdenticalAnnotation");
			jTemp.addActionListener(this);
//			add(jTemp);
			
			jTemp = new JMenuItem("Select All Segments");
			jTemp.setActionCommand("selectAll");
			jTemp.addActionListener(this);
			add(jTemp);
			
			jTemp = new JMenuItem("Select Unannotated Segments");
			jTemp.setActionCommand("selectUnannotated");
			jTemp.addActionListener(this);
			add(jTemp);

			add(new JSeparator());
			
			jTemp = new JMenuItem("Clear Annotation");
			jTemp.setActionCommand("clear");
			jTemp.addActionListener(this);
			add(jTemp);
			
			jTemp = new JMenuItem("Clear All Annotations");
			jTemp.setActionCommand("clearAll");
			jTemp.addActionListener(this);
			add(jTemp);
			
			
//			MetadataSet mds = selectedSeg.getMetadataSet();
//			if ((mds != null) && (mds.size() > 0))
//			{
//				jTemp = new JMenuItem("View Metadata");
//				jTemp.setActionCommand("viewMD");
//				jTemp.addActionListener(this);
//				add(jTemp);
//			}
			
			//sourish
//			setSelectedSegment(selectedSeg);
		}

		public void actionPerformed(ActionEvent e) {
			System.out.println("Action Performed: " + e.getActionCommand());
			String cmd = e.getActionCommand();
			
			SegmentedTextViewer stv = SegmentedTextViewer.this;

			// special case - no change in content
			if (cmd.equalsIgnoreCase("selectIdenticalAnnotation")) {
				selectAllSegmentTextAreas();
				return;
			}else if (cmd.equalsIgnoreCase("selectAll")) {
				selectAllSegmentTextAreas();
				return;
			}
			else if (cmd.equalsIgnoreCase("selectUnannotated")){
				selectUnannotatedSegmentTextAreas();
				return;
			}
//			else if(cmd.equalsIgnoreCase("configLabelSetting")){
//				SIDEAnnotationSetting setting = UIMAToolkit.getSIDEAnnotationSetting(source, subtypeName);
//				Map<String,Color> labelColorMap = UIMAToolkit.getLabelColorMap(setting);
//				
//				Map<String,Color> result = 
//					ColorMapperUI.showColorMapUI(labelColorMap, annoEditor);
//				String labelColorMapString = UIMAToolkit.toLabelColorString(result).toString();
//				setting.setLabelColorMapString(labelColorMapString);
//				refreshColors();
//			}

			//showing metadata
//			if(cmd.equalsIgnoreCase("viewmd"))
//			{
//				MetadataSet mds = selectedSegment.getMetadataSet();
//				MetadataViewer.ViewMetadata(mds);
//			}
			else if (cmd.equalsIgnoreCase("setLabelDialog")) {
				SIDEAnnotationSetting setting = UIMAToolkit.getSIDEAnnotationSetting(jCas, subtypeName);
				Map<String,Color> labelColorMap = UIMAToolkit.getLabelColorMap(setting);
				
				MultipleSelection multipleSelection = MultipleSelection.createSingle(labelColorMap.keySet().toArray(new String[0]), null);
				
				if(multipleSelection.showDialog(SegmentedTextViewer.this)!=ResultOption.APPROVE_OPTION){
					return;
				}
				
				String selectedItem = multipleSelection.getSelectedValue();
				if(selectedItem==null){ return; }
				
				for (SegmentTextArea sta : getSelectedSegmentTextAreas()) {
					SIDEAnnotation sideAnnotation = sta.getSIDEAnnotation();
					sideAnnotation.setLabelString(selectedItem);
					sta.refresh();
				}
				stv.fireActionEvent();
				
			}
			else if (cmd.equalsIgnoreCase("setLabelMenu")) {
				JMenuItem mi = (JMenuItem) e.getSource();
				// Segment sTemp = mi.seg; // get the origin
				for (SegmentTextArea sta : getSelectedSegmentTextAreas()) {
					SIDEAnnotation sideAnnotation = sta.getSIDEAnnotation();
					sideAnnotation.setLabelString(mi.getText());
					sta.refresh();
				}
				stv.fireActionEvent();
				
			} else if (cmd.equalsIgnoreCase("clear")) {
//				JMenuItem mi = (JMenuItem) e.getSource();
				// Segment sTemp = mi.seg;
				clearAnnotations(getSelectedSegmentTextAreas());
				stv.fireActionEvent();
			} else if (cmd.equalsIgnoreCase("clearAll")) {
				clearAnnotations(getSegmentTextAreas());
				stv.fireActionEvent();
			}
		}
	}

	// - ends

	public SegmentedTextViewer(JCas jCas, String subtypeName)
			throws Exception {
		this.jCas = jCas;
		this.subtypeName = subtypeName;
		BuildInterface();
	}

	public static String sizeToString(Dimension d) {
		return "[" + d.getWidth() + ',' + d.getHeight() + ']';
	}

	public static void printSize(String title, java.awt.Component container) {
		StringBuffer returnValue = new StringBuffer();
		Dimension size = container.getSize();
		Dimension maxSize = container.getMaximumSize();
		Dimension preSize = container.getPreferredSize();
		Dimension minSize = container.getMinimumSize();

		returnValue.append("## ").append(title).append(" ##");
		returnValue.append(" CUR").append(sizeToString(size));
		returnValue.append(" MAX").append(sizeToString(maxSize));
		returnValue.append(" PRE").append(sizeToString(preSize));
		returnValue.append(" MIN").append(sizeToString(minSize));
		System.out.println(returnValue);
	}

	/** break here and look when problem with SegmentedTextViewer layout * */
	public void OptimizeLayout() {
		// for(int i=0; i<2; i++){
		optimizeLayoutItem();
		// scrollPane.revalidate();
		// scrollPane.repaint();
		// }
		for (JTextComponent sta : this.getSegmentTextAreas()) {
			QuietCaret.makeCaretQuiet(sta);
		}
	}

	private void optimizeLayoutItem() {
		// (new Exception()).printStackTrace();
		// Need to have this code to force the panel to get smaller
		// when the user constricts the window. Otherwise it would
		// expand and never contract again.
		Dimension d = this.getSize();
//		Component cList[] = this.getComponents();
		JViewport viewPort = (JViewport) this.getParent();
		// viewPort.getViewSize();
		Dimension vp = viewPort.getSize();

		// printSize("ViewPort", viewPort);
		// printSize("SegmentedTextViewer BEFORE: ", this);

		Dimension tmpDimension = new Dimension(vp.width, d.height);
		// this.setPreferredSize (tmpDimension);

		// force this panel to be this width and validate its children
		// components to resize
		this.setSize(tmpDimension);
		this.validate();

		int totalHeight = 0;
		for (SegmentTextArea sta : this.getSegmentTextAreas()) {
			Dimension cd = sta.getPreferredSize();
			totalHeight += cd.height + 7;
		}
		Dimension newPreferredSize = new Dimension(vp.width, totalHeight);
		// System.out.println("New preferred
		// size:"+sizeToString(newPreferredSize));

		this.setPreferredSize(newPreferredSize);
		// printSize("SegmentedTextViewer AFTER: ", this);
	}

	private void OnPanelResized(java.awt.event.HierarchyEvent evt) {
		OptimizeLayout();
	}

	
	public void refreshColors(){
		for(SegmentTextArea sta : this.getSegmentTextAreas()){
			sta.refresh();
		}
	}

	public void clearAnnotations(Collection<? extends SegmentTextArea> staArray) {
		for (SegmentTextArea sta : staArray) {
			SIDEAnnotation sideAnnotation = sta.getSIDEAnnotation();
			sideAnnotation.setLabelString(null);
			sta.refresh();
		}
	}
	
	// end of ### Lable and Segments ###

	
	
	public static class SegmentTextArea extends JTextArea {
		private static final long serialVersionUID = 1L;
		private SIDEAnnotation sideAnnotation;
		protected boolean isSelected = false;

		private TinyBar tinyBar;
		
		public String getCoveredText() {
			return sideAnnotation.getCoveredText();
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			if(tinyBar!=null){
				tinyBar.paintBar(this, g);
			}
		}

		private void setSelected(boolean selected) {
			this.isSelected = selected;
			setBorder(createBorder());
		}

		private void toggleSelection() {
			setSelected(!this.isSelected());
		}

		public void deselect() {
			setSelected(false);
		}

		public boolean isSelected() {
			return this.isSelected;
		}


		public void refresh() {
			Color color = new Color(0xffffffff);
			try {
				color = UIMAToolkit.getLabelColor(sideAnnotation);
			} catch (Exception e) {
				YeriDebug.die(e);
			}
			
			this.setBackground(color);
			this.setToolTipText(sideAnnotation.getLabelString());
			
			invalidate();
			repaint();
		}

		protected Border createBorder() {
			return this.isSelected() ? createBorderWhenSelected()
					: createDefaultBorder();
		}

		public static Border createDefaultBorder() {
			return BorderFactory.createEtchedBorder();
		}

		public static Border createBorderWhenSelected() {
			return BorderFactory.createEtchedBorder(new Color(0x333399),
					new Color(0x6666ff));
		}
		
		public boolean isAnnotated(){ return this.getSIDEAnnotation().getLabelString()!=null; }

		public SegmentTextArea(SIDEAnnotation sideAnnotation) {
			super();
			this.sideAnnotation = sideAnnotation;
			if(sideAnnotation instanceof SIDEPredictionAnnotation){
				SIDEPredictionAnnotation prediction = (SIDEPredictionAnnotation)sideAnnotation;
				DoubleArray doubleArray = prediction.getPredictionArray();
				if(doubleArray!=null){
					double[] predictionArray = prediction.getPredictionArray().toArray();
					double[] minmax = StatisticsToolkit.getMinMax(predictionArray);
					tinyBar = new TinyBar(minmax[1]);
				}
			}

			this.setColumns(10);
			this.setLineWrap(true);
			this.setRows(1);
			this.setFont(new java.awt.Font("Verdana", 0, 11));
			this.setEditable(false);
			this.setWrapStyleWord(true);
			ToolTipManager.sharedInstance().registerComponent(this);

			try {
				String text = this.getSIDEAnnotation().getCoveredText();
				if (text != null){
					this.setText(text.trim());
				}
			} catch (Exception e) {
			}
			this.setBorder(createBorder());
			refresh();
		}

		public SIDEAnnotation getSIDEAnnotation() {
			return sideAnnotation;
		}
	}

	private void BuildInterface() {
		this.setBackground(new java.awt.Color(240, 240, 240));
		HierarchyBoundsListener boundListener = new HierarchyBoundsListener() {
			public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
			}

			public void ancestorResized(java.awt.event.HierarchyEvent evt) {
				OnPanelResized(evt);
			}
		};

		List<SIDESegment> sideSegmentList = UIMAToolkit.getSIDESegmentList(jCas,
				SIDEAnnotation.type, subtypeName);

		for (SIDESegment sideSegment : sideSegmentList) {
			SIDEAnnotation sideAnnotation = (SIDEAnnotation)sideSegment;
			SegmentTextArea sta = new SegmentTextArea(sideAnnotation);
			if (callback != null) {
				sta.addMouseListener(this.callback);
			}
			add("br hfill", sta);
		}
		// this.addMouseListener(this.callback);

		// OptimizeLayout();
		this.addHierarchyBoundsListener(boundListener);
	}
	
	public static void main(String[] args) throws Exception{
		File xmiFile = new File("C:/yeri/projects/summarization/SIDE project/program/SIDE/side workspace/xmi/361-Gold.csv.xmi");
		
		CAS cas = UIMAToolkit.createSIDECAS(xmiFile);
		
		SegmentedTextViewer annoEditor = new SegmentedTextViewer(cas.getJCas(), "csv_Who");
		JScrollPane scrollPane = new JScrollPane();
		
		scrollPane.setViewportView(annoEditor);
		TestFrame testFrame = new TestFrame(scrollPane);
		
		
		SwingToolkit.setSize(testFrame, new Dimension(800,600), SwingToolkit.SizeType.values());
		SwingToolkit.adjustScrollBar(scrollPane, JScrollBar.VERTICAL);
		
		testFrame.showFrame();
	}
}