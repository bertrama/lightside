/*
 * ExpressionBuilder.java
 *
 * This object provides a user interface for modifying the definition
 * of an Expression object (part of the Recipe class).
 */

package edu.cmu.side.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.tree.TreeNodeToolkit;

import edu.cmu.side.SIDEToolkit;

/**
 * 
 * @author __USER__
 */
public class TreeBuilder extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;

	private static final int INDENT_WIDTH = 16;
	private static final int GAP_WIDTH = 5;
	
	public TreeBuilder(TreeSetting treeSetting, DefaultMutableTreeNode root){
		
		yeriInit();
		
		treeSetting.setTreeBuilder(this);
		this.treeSetting = treeSetting;
		this.setRootNode(root);
	}

	private TreeSetting treeSetting = null;
	public static interface TreeSetting{
		void setTreeBuilder(TreeBuilder treeBuilder);
		Component getComponent(Object userObject);
		JPopupMenu getPopupMenu(DefaultMutableTreeNode node, boolean addition);
	}
	
	private JPanel expressionPanel;
	private void yeriInit() {
		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		
		expressionPanel = new JPanel();
		expressionPanel.setBackground(Color.white);
		expressionPanel.setLayout(new RiverLayout());
		this.add(new JScrollPane(expressionPanel), BorderLayout.CENTER);
		
		this.refreshAll();
	}
	
	private DefaultMutableTreeNode root;
	public DefaultMutableTreeNode getRootNode(){ return this.root; }
	public void setRootNode(DefaultMutableTreeNode root){
		this.root = root;
		refreshAll();
	}
	
	private class DeleteMouseAdapter extends MouseAdapter{
		private DefaultMutableTreeNode node;
		
		public DeleteMouseAdapter(DefaultMutableTreeNode node){
			this.node = node;
		}
		public void mouseClicked(MouseEvent evt){
			TreeNodeToolkit.removeSelf(node);
			refreshAll();
		}
	}
	private class DropdownMouseAdapter extends MouseAdapter{
		private DefaultMutableTreeNode node;
		private boolean addition;
		public DropdownMouseAdapter(DefaultMutableTreeNode node, boolean addition){
			this.node = node;
			this.addition = addition;
		}
		public void mouseClicked(MouseEvent evt){
			JPopupMenu popupMenu = treeSetting.getPopupMenu(node, addition);
			popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}
	protected Component getOnelinePanel(DefaultMutableTreeNode node) throws IOException{
		JPanel onelinePanel = new JPanel();
		onelinePanel.setOpaque(false);
		
		onelinePanel.setLayout(new BoxLayout(onelinePanel, BoxLayout.LINE_AXIS));
		
		// delete
		int level = node.getLevel();
		if(level==0){
			onelinePanel.add(Box.createHorizontalStrut(INDENT_WIDTH));
		}else{
			JLabel deleteLabel = new JLabel(new ImageIcon(SIDEToolkit.deleteImageFile.getCanonicalPath()));
			deleteLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			deleteLabel.addMouseListener( new DeleteMouseAdapter(node) );
			onelinePanel.add(deleteLabel);
			onelinePanel.add(Box.createHorizontalStrut(INDENT_WIDTH*level));
		}
		onelinePanel.add(Box.createHorizontalStrut(GAP_WIDTH));
		
		// dropdown
		JLabel dropdownLabel = new JLabel(new ImageIcon(SIDEToolkit.dropdownImageFile.getCanonicalPath()));
		dropdownLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		dropdownLabel.addMouseListener( new DropdownMouseAdapter(node, false) );
		onelinePanel.add(dropdownLabel);
		onelinePanel.add(Box.createHorizontalStrut(GAP_WIDTH));
		
		// content
		onelinePanel.add(treeSetting.getComponent(node.getUserObject()));
		onelinePanel.add(Box.createHorizontalStrut(GAP_WIDTH));
		
		// add
		JLabel addLabel = new JLabel(new ImageIcon(SIDEToolkit.addImageFile.getCanonicalPath()));
		addLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addLabel.addMouseListener( new DropdownMouseAdapter(node, true) );
		onelinePanel.add(addLabel);
		onelinePanel.add(Box.createHorizontalStrut(GAP_WIDTH));
		
		return onelinePanel;
	}
	
	public void refreshAll(){
		if(expressionPanel==null || root==null){ return; }
		
		expressionPanel.removeAll();
		
		for(Enumeration<?> enumeration = this.root.preorderEnumeration(); enumeration.hasMoreElements(); ){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumeration.nextElement();
			try {
				Component c = getOnelinePanel(node);
				expressionPanel.add("br hfill", c);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}		
		expressionPanel.revalidate();
		expressionPanel.repaint();
		
		this.fireActionEvent();
	}
	
	public static void main(String[] args){
//		test02();
	}
	

//	protected static void test01(){
//		TreeBuilder builder = new TreeBuilder();
//		
//		BooleanOperator root = new BooleanOperator(OperatorType.AND);
//		BooleanOperator or = new BooleanOperator(OperatorType.OR);
//		root.add(or);
//		BooleanOperator is = new BooleanOperator(OperatorType.IS);
//		or.add(is);
//		BooleanOperator and = new BooleanOperator(OperatorType.AND);
//		root.add(and);
//		BooleanOperator and2 = new BooleanOperator(OperatorType.AND);
//		and.add(and2);
//		builder.setRootOperator(root);
//		
//		TestFrame testFrame = new TestFrame(builder);
//		testFrame.setSize(new Dimension(600,800));
//		testFrame.showFrame();
//	}
	
	
	private transient List<ActionListener> actionListenerList = new ArrayList<ActionListener>();
	public void addActionListener(ActionListener actionListener){
		this.actionListenerList.add(actionListener);
	}
	public void removeActionListener(ActionListener actionListener){
		this.actionListenerList.remove(actionListener);
	}
	private int actionEventID = 1;
	protected void fireActionEvent() {
		ActionEvent evt = new ActionEvent(this, actionEventID++, "hi!");
		for(ActionListener listener : actionListenerList){
			listener.actionPerformed(evt);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		this.refreshAll();
	}
}