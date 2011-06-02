package edu.cmu.side.dataitem;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Element;

import com.yerihyo.yeritools.CalendarToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.MultipleSelection;
import com.yerihyo.yeritools.swing.SwingToolkit.ResultOption;
import com.yerihyo.yeritools.text.StringToolkit;
import com.yerihyo.yeritools.xml.XMLToolkit;

import edu.cmu.side.Workbench;
import edu.cmu.side.SIDEToolkit.FileType;
import edu.cmu.side.viewer.TreeBuilder;
import edu.cmu.side.viewer.TreeBuilder.TreeSetting;

public abstract class DataItem {
	public DataItem(){
		this.timestamp = System.currentTimeMillis();
	}
	
	public static class SIDEFilterOperatorSetting implements TreeSetting{
//		private static final String c = "boolean_expression";
		private static final String labelsTag = "labels";
		
		public static String toXML(DefaultMutableTreeNode node){
			
			SIDEFilterOperator operator = (SIDEFilterOperator)node.getUserObject();
			OperatorType type = operator.getOperatorType();

			if(type==OperatorType.AND || type==OperatorType.OR){
				StringBuilder builder = new StringBuilder();
				for(Enumeration<?> enumeration = node.children(); enumeration.hasMoreElements();){
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumeration.nextElement();
					builder.append(toXML(child));
				}
				return XMLToolkit.wrapContentWithTag(builder, type.name()).toString();
			}else if(type==OperatorType.IS){
				StringBuilder builder = new StringBuilder();
				builder.append(operator.getTrainingResult().toXML());
				builder.append(XMLToolkit.wrapContentWithTag(StringToolkit.toString(operator.getLabelArray(), ","), labelsTag));
				return XMLToolkit.wrapContentWithTag(builder, type.name()).toString();
			}else{
				throw new UnsupportedOperationException();
			}
		}
		private static OperatorType getOperatorType(Element root){
			String rootTagName = root.getTagName();
			
			for(OperatorType type : OperatorType.values()){
				if(rootTagName.equals(type.name())){
					return type;
				}
			}
			throw new UnsupportedOperationException();
		}
		
		public static DefaultMutableTreeNode fromXML(Element root){
			OperatorType type = getOperatorType(root);
			SIDEFilterOperator operator = new SIDEFilterOperator(type);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(operator);
			
			if(type==OperatorType.AND || type==OperatorType.OR ){
				for(Element element: XMLToolkit.getChildElements(root)){
					DefaultMutableTreeNode childNode = fromXML(element);
					node.add(childNode);
				}
				return node;
			}
			else if(type==OperatorType.IS){
				Element trainingResultElement = XMLToolkit.getLastChildElementByName(root, TrainingResult.xmlTagName);
				TrainingResult trainingResult = TrainingResult.create(trainingResultElement);
				operator.setTrainingResult(trainingResult);
				
				Element lablesElement = XMLToolkit.getLastChildElementByName(root, labelsTag);
				operator.setLabelArray(XMLToolkit.getTextContent(lablesElement).toString().split(","));
				return node;
			}else{
				throw new UnsupportedOperationException();
			}
		}
		public static enum OperatorType{
			AND, OR, IS;
		}
		
		public static class SIDEFilterOperator{
			private OperatorType type;
			private TrainingResult trainingResult;
			private String[] labelArray;
			
			public String toString(){
				CharSequence labelOptionString = StringToolkit.toString(labelArray, ",");
				return type.name()+ (type!=OperatorType.IS?"":"["+(labelOptionString==null?"":labelOptionString)+"]");
			}
			
			
			public SIDEFilterOperator(OperatorType type){
				this.type = type;
//				this.sideFilter = sideFilter;
			}
			
			public OperatorType getOperatorType() {
				return type;
			}

			public String[] getLabelArray() {
				return labelArray;
			}

			public void setLabelArray(String[] labelArray) {
				this.labelArray = labelArray;
			}


			public TrainingResult getTrainingResult() {
				return trainingResult;
			}


			public void setTrainingResult(TrainingResult trainingResult) {
				this.trainingResult = trainingResult;
			}
		}

		private TreeBuilder treeBuilder;
		
		@Override
		public Component getComponent(Object userObject) {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.setOpaque(false);
			
			
			SIDEFilterOperator operator = (SIDEFilterOperator)userObject;
			OperatorType type = operator.getOperatorType();
			StringBuilder content = new StringBuilder(type.name().toLowerCase());
			
			if(type==OperatorType.IS){
				class OptionButtonListener implements ActionListener{
					private SIDEFilterOperator operator;
					private Component c;
					public OptionButtonListener(SIDEFilterOperator operator, Component c){
						this.operator = operator;
						this.c = c;
					}
					
					@Override
					public void actionPerformed(ActionEvent e) {
						String[] allLabelArray = operator.getTrainingResult().getLabelArray();
						MultipleSelection multipleSelection = MultipleSelection.createMultiple(allLabelArray, operator.labelArray);
						if(multipleSelection.showDialog(c)!=ResultOption.APPROVE_OPTION){
							return;
						}
						operator.labelArray = multipleSelection.getSelectedValueArray();
						getTreeBuilder().refreshAll();
					}
				}
				CharSequence labelOptionString = StringToolkit.toString(operator.getLabelArray(),",");
				content.append(" ").append(operator.getTrainingResult().getDisplayText());
				content.append("[").append((labelOptionString==null?"":labelOptionString)).append("]");
				JLabel label = new JLabel(content.toString());
				panel.add( label );
				
				panel.add(Box.createHorizontalStrut(3));
				
				JButton optionButton = new JButton("...");
				optionButton.addActionListener(new OptionButtonListener(operator, panel));
				optionButton.setFont(new java.awt.Font("Tahoma", 0, 10));
				optionButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
				
				panel.add(optionButton);
			}else{
				panel.add(new JLabel(content.toString()));
			}
			
			return panel;
		}

		@Override
		public JPopupMenu getPopupMenu(DefaultMutableTreeNode node,
				boolean addition) {
			JPopupMenu popupMenu = new JPopupMenu();
			OperatorActionListener listener = new OperatorActionListener(node, addition);
			
			JMenuItem andMenuItem = new JMenuItem("and");
			popupMenu.add(andMenuItem);
			andMenuItem.addActionListener(listener);
			
			JMenuItem orMenuItem = new JMenuItem("or");
			popupMenu.add(orMenuItem);
			orMenuItem.addActionListener(listener);
			
			JMenu isMenu = new JMenu("is");
			popupMenu.add(isMenu);

			for(Iterator<TrainingResult> trainingResultIterator = Workbench.current.trainingResultListManager.iterator(); trainingResultIterator.hasNext(); ){
				TrainingResult trainingResult = trainingResultIterator.next();
				JMenuItem menuItem = new JMenuItem(trainingResult.getDisplayText());
//				TrainingResult trainingResult = sideFilter.getTrainingResult();
//				String[] labelArray = trainingResult.getLabelArray();
				
				isMenu.add(menuItem);
				menuItem.addActionListener(new IsActionListener(node, trainingResult, addition));
			}
			
			return popupMenu;
		}
		protected class OperatorActionListener implements ActionListener{
			private DefaultMutableTreeNode node;
			private boolean addition;
			
			public OperatorActionListener(DefaultMutableTreeNode node, boolean addition){
				this.node = node;
				this.addition = addition;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode oldNode = this.node;
				SIDEFilterOperator oldOperator = (SIDEFilterOperator)oldNode.getUserObject();
				OperatorType oldOperatorType = oldOperator.getOperatorType();
				
				String label = ((JMenuItem)e.getSource()).getText();
				OperatorType newOperatorType = null;
				
				if(label.equals("and")){ newOperatorType = OperatorType.AND; }
				else if(label.equals("or")){ newOperatorType = OperatorType.OR; }
				else{ throw new UnsupportedOperationException(); }
				SIDEFilterOperator newOperator = new SIDEFilterOperator(newOperatorType);
				
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
				newNode.setUserObject(newOperator);
				
				if(addition){
					oldNode.add(newNode);
				}else if(oldOperatorType==OperatorType.IS){
					if(oldNode.isRoot()){
						getTreeBuilder().setRootNode(newNode);
					}else{
						DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)oldNode.getParent();
						parentNode.remove(oldNode);
						parentNode.add(newNode);
					}
					newNode.add(oldNode);
					System.out.println(oldNode.getLevel());
					
				}else{
					oldNode.setUserObject(newOperator);
				}
				
				getTreeBuilder().refreshAll();
			}
		}
	
		protected class IsActionListener implements ActionListener{
			private DefaultMutableTreeNode node;
			private TrainingResult trainingResult;
			private boolean addition;
			
			public IsActionListener(DefaultMutableTreeNode node, TrainingResult trainingResult, boolean addition){
				this.node = node;
				this.trainingResult = trainingResult;
				this.addition = addition;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
//				String label = ((JMenuItem)e.getSource()).getText();
				SIDEFilterOperator operator = new SIDEFilterOperator(OperatorType.IS);
				operator.setTrainingResult(trainingResult);
				
				if(addition){
					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(operator);
					this.node.add(newNode);
//					System.out.println(NodeToolkit.toString((DefaultMutableTreeNode)this.node.getRoot()));
				}else{
					this.node.removeAllChildren();
					this.node.setUserObject(operator);
				}
				
				getTreeBuilder().refreshAll();
			}
		}

		public void setTreeBuilder(TreeBuilder treeBuilder) {
			this.treeBuilder = treeBuilder;
		}

		public TreeBuilder getTreeBuilder() {
			return treeBuilder;
		}
	};
	
	public String getDisplayText(){
		String timestampString = CalendarToolkit.toString(this.getTimestamp(), CalendarToolkit.YERI_DEFAULT_TIME_FORMAT);
		
		if(this.name==null){ return timestampString; }
		else{ return name + "("+timestampString+")"; }
	}
	
	public boolean equals(DataItem o){
		if(!this.getClass().getName().equals(o.getClass().getName())){ return false; }
		return this.getTimestamp()==o.getTimestamp();
	}
	
	protected static final String timestampXMLTag = "timestamp";
	protected static final String nameXMLTag = "name";
	
	protected void itemsFromXML(Element element){
		String elementName = element.getTagName();
		if(elementName.equalsIgnoreCase(timestampXMLTag)){
			this.timestamp = Long.parseLong(XMLToolkit.getTextContent(element).toString());
		}else if(elementName.equalsIgnoreCase(nameXMLTag)){
			this.name = XMLToolkit.getTextContent(element).toString();
		}else{
			throw new UnsupportedOperationException();
		}
	}
	
	protected CharSequence itemsToXML(){
		StringBuilder builder = new StringBuilder();
		builder.append(XMLToolkit.wrapContentWithTag(Long.toString(timestamp), timestampXMLTag));
		if(this.name!=null){
			builder.append(XMLToolkit.wrapContentWithTag(XMLToolkit.wrapCdata(name), nameXMLTag));
		}
		return builder;
	}
	
	protected long timestamp;
	protected String name = null;
	
	public long getTimestamp() {
		return timestamp;
	}

	public String getName(){
		return this.name;
	}
	
	public String getSavefileName(){
		if(this.name==null){
			return CalendarToolkit.toString(this.getTimestamp(), CalendarToolkit.YERI_DEFAULT_TIME_FORMAT);
		}else{
			return this.name;
		}
	}
	
	public File getDefaultFolder(){
		return this.getFileType().getDefaultFolder();
	}
	
	public abstract FileType getFileType();

	public void setName(String name) {
		this.name = name;
		fireActionEvent();
	}
	
	private List<ActionListener> alList = new ArrayList<ActionListener>();
	public void addActionListener(ActionListener al){ alList.add(al); }
	public void removeActionListener(ActionListener al){ alList.remove(al); }
	
	private static int eventID = 1;
	protected void fireActionEvent(){
		ActionEvent event = new ActionEvent(this, eventID++, "");
		for(ActionListener al : alList){
			al.actionPerformed(event);
		}
	}
}
