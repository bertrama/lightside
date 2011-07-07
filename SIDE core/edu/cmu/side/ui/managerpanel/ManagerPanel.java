package edu.cmu.side.ui.managerpanel;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.swing.JEasyList;
import com.yerihyo.yeritools.swing.SwingToolkit;

import edu.cmu.side.Workbench.ListManager;
import edu.cmu.side.dataitem.DataItem;

public abstract class ManagerPanel<T extends DataItem> extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	
	private List<ActionListener> doubleClickListenerList = new ArrayList<ActionListener>();
	public void addDoubleClickListener(ActionListener al){ this.doubleClickListenerList.add(al); }
	public void removeDoubleClickListener(ActionListener al){ this.doubleClickListenerList.remove(al); }
	
	private static int coubleClickActionEventID = 1;
	protected void fireDoubleClickEvent(T t){
		ActionEvent actionEvent = new ActionEvent(t, coubleClickActionEventID++, "double click");
		for(ActionListener al : doubleClickListenerList){
			al.actionPerformed(actionEvent);
		}
	}
	
	protected abstract JPopupMenu getPopupMenu(T t);
	protected abstract String getLabelString();
	
	protected abstract ListManager<T> getListManager();
	protected void addSelfAsListener(){
		this.getListManager().addActionListener(this);
	}
	protected Iterator<T> iterator(){
		return this.getListManager().iterator();
	}
	
	public T getSelectedItem(){
		int selectedIndex = this.easyList.getSelectedIndex();
		if(selectedIndex<0){ return null; }
		
		return (T)this.listModel.get(selectedIndex);
	} 
	
	public Object[] getSelectedItems(){
		return this.easyList.getSelectedValues();
	}
	
	public ManagerPanel(){
		yeriInit();
		addSelfAsListener();
	}
	
	public void setSelectionMode(int selectionMode){
		this.easyList.setSelectionMode(selectionMode);
	}
	
	// ActionListener
	private int actionEventID = 0;
	private List<ActionListener> actionListenerList = new ArrayList<ActionListener>();
	public void addActionListener(ActionListener actionListener){
		actionListenerList.add(actionListener);
	}
	public void removeActionListener(ActionListener actionListener){
		actionListenerList.remove(actionListener);
	}
	protected void fireActionEvent(){
		for(ActionListener actionListener : actionListenerList){
			actionListener.actionPerformed(new ActionEvent(ManagerPanel.this, actionEventID++, "action!"));
		}
	}
	
	private void yeriInit(){
		this.setLayout(new RiverLayout());
		
		listModel = new DefaultListModel();
		easyList = new JEasyList(); 
		easyList.setModel(listModel);
		
		JButton clearButton = new JButton("clear");
		clearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				getListManager().removeAll();
				refreshPanel();
			}
		});
		ListManager l = this.getListManager();
		this.add("", new JLabel(l.createImageIcon()));
		this.add("tab hfill", new JLabel(getLabelString()+":"));
		this.add("", clearButton);
		this.add("br hfill vfill", new JScrollPane(easyList) );
		
		easyList.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt){
				int button = evt.getButton();
				
				int index = easyList.locationToIndex(evt.getPoint());
				T t = index<0?null:(T)listModel.get(index);
				
				if(button==MouseEvent.BUTTON1 && evt.getClickCount()>=2){
					ManagerPanel.this.fireDoubleClickEvent(t);
					return;
				}
				else if(button==MouseEvent.BUTTON3){ 
					Point popupLocation = easyList.getPopupLocation(evt);
					popupLocation = (popupLocation==null)?evt.getPoint():popupLocation;
					
					JPopupMenu exportMenu = getPopupMenu(t);
					exportMenu.show(ManagerPanel.this, popupLocation.x, popupLocation.y);
				}
			}
		});
		
		easyListSelectionListener = new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				fireActionEvent();
			}
		};
		easyList.addListSelectionListener(easyListSelectionListener);
		easyList.setCellRenderer(new SwingToolkit.ValueCustomizedListCellRenderer(easyList.getCellRenderer()){
			private static final long serialVersionUID = 1L;

			@Override
			protected String getText(Object value) {
				T t = (T)value;
				return t.getDisplayText();
			}
		});
		
		refreshPanel();
	}
	
	protected DefaultListModel listModel;
	protected JEasyList easyList;
	
	protected void removeInternalListeners(){
		easyList.removeListSelectionListener(easyListSelectionListener);
	}
	protected void addInternalListeners(){
		easyList.addListSelectionListener(easyListSelectionListener);
	}
	
	public void refreshPanel(){
		Object value = easyList.getSelectedValue();
		
		removeInternalListeners();
		listModel.removeAllElements();
		for(Iterator<T> iterator = iterator(); iterator.hasNext();){
			listModel.addElement(iterator.next());
		}
		
		if(value!=null){
			easyList.setSelectedValue(value, true);
		}
		if(easyList.getSelectedIndex()<0){
			int index = listModel.size()-1;
			easyList.setSelectedIndex(index);
		}
		
		addInternalListeners();
		this.fireActionEvent();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		this.refreshPanel();
	}
	
	private ListSelectionListener easyListSelectionListener;
	
}