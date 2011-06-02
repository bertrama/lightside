/*
 * MLAPluginConfigPanel.java
 *
 * Created on __DATE__, __TIME__
 */

package edu.cmu.side.ui.configpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.swing.AlertDialog;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;
import com.yerihyo.yeritools.swing.SwingToolkit.ValueCustomizedListCellRenderer;

import edu.cmu.side.SIDEToolkit;
import edu.cmu.side.Workbench;
import edu.cmu.side.dataitem.TextRecipe;
import edu.cmu.side.dataitem.DataItem.SIDEFilterOperatorSetting;
import edu.cmu.side.dataitem.DataItem.SIDEFilterOperatorSetting.OperatorType;
import edu.cmu.side.dataitem.DataItem.SIDEFilterOperatorSetting.SIDEFilterOperator;
import edu.cmu.side.dataitem.TextRecipe.Limit;
import edu.cmu.side.plugin.EMPlugin;
import edu.cmu.side.plugin.PluginWrapper;
import edu.cmu.side.ui.managerpanel.RecipeManagerPanel;
import edu.cmu.side.viewer.TreeBuilder;


/**
 * 
 * @author __USER__
 */
public class TextRecipeConfigPanel extends javax.swing.JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;

	/** Creates new form MLAPluginConfigPanel */
	public TextRecipeConfigPanel() {
		yeriInit();
	}
	
	
	private void createRecipe(){
		List<CharSequence> errorMessageList = new ArrayList<CharSequence>();
		boolean valid = true;
		
		EMPlugin emPlugin = (EMPlugin)this.metricComboBox.getSelectedItem();
		if(emPlugin==null){
			valid = false;
			errorMessageList.add("EMPlugin is null");
		}else{
			emPlugin.uiToMemory();
		}
		
		Limit limit = Limit.create((String)this.limitComboBox.getSelectedItem());
		double number=0;
		try{
			String numberText = this.numberField.getText();
			if(numberText.trim().length()==0){ number = Double.POSITIVE_INFINITY; }
			else{ number = Double.parseDouble(numberText); }
		}catch(NumberFormatException ex){
			valid = false;
			errorMessageList.add("Number in illegal format");
		}
		
		String recipeName = recipeNameTextField.getText().trim();
		if(recipeName.length()==0){
			valid = false;
			errorMessageList.add("Please type in recipe name");
		}
		
		if(!valid){
			AlertDialog.show("Error", errorMessageList, this);
			return;
		}
		
		boolean restoreOrder = restoreOrderCheckBox.isSelected();
		TextRecipe textRecipe = new TextRecipe(this.treeBuilder.getRootNode(), emPlugin, limit, number, restoreOrder);
		textRecipe.setName(recipeName);
		SIDEToolkit.FileType.recipe.addItemToList(textRecipe);
	}
	
//	private List<EMPlugin> getEMPluginList(){
//		List<EMPlugin> emPluginList = new ArrayList<EMPlugin>();
//		DefaultMutableTreeNode rootNode = this.treeBuilder.getRootNode();
//		
//		for(Enumeration<?> enumeration = rootNode.depthFirstEnumeration(); enumeration.hasMoreElements(); ){
//			DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumeration.nextElement();
//			SIDEFilterOperator operator = (SIDEFilterOperator)node.getUserObject();
//			if(operator.getOperatorType()!=OperatorType.IS){
//				continue;
//			}
//			
//			SIDEFilter sideFilter = operator.getSideFilter();
//			EMPlugin thisEMPlugin = sideFilter.getEmPlugin();
//			emPluginList.add(thisEMPlugin);
//		}
//		return emPluginList;
//	}

	private void yeriInit() {
		
		JPanel leftPanel = new JPanel();
		
		leftPanel.setLayout(new RiverLayout());
		
		leftPanel.add("br hfill", new JLabel("Expression Builder:"));
		
		SIDEFilterOperator operator = new SIDEFilterOperator(OperatorType.AND);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(operator);
		treeBuilder = new TreeBuilder(new SIDEFilterOperatorSetting(), root);
		
		treeBuilder.setSize(new Dimension(400,800));
		treeBuilder.addActionListener(this);
		leftPanel.add("br hfill", treeBuilder);
		
		leftPanel.add("br hfill", new JLabel("Evaluation Metric"));
		
		metricComboBox = new JComboBox();
		
		List<PluginWrapper> pluginWrapperList = Workbench.current.pluginManager.getPluginWrapperCollectionByType(EMPlugin.type);
		for(PluginWrapper pluginWrapper : pluginWrapperList){
			metricComboBox.addItem(pluginWrapper.getSIDEPlugin());
		}
		
		metricComboBox.setRenderer(new ValueCustomizedListCellRenderer(metricComboBox.getRenderer()){
			@Override
			protected String getText(Object value) {
				if(value==null){ return "No EMPlugin"; }
				else{ return value.getClass().getName(); }
			}
		});
		leftPanel.add("br hfill", metricComboBox);
		
		limitComboBox = new JComboBox();
		for(Limit limit : Limit.values()){
			limitComboBox.addItem(limit.getComboBoxText());
		}
		limitComboBox.setSelectedIndex(0);
		leftPanel.add("p hfill", new JLabel("Order: "));
		leftPanel.add("br", limitComboBox);
		
		numberField = new JTextField();
//		leftPanel.add("p hfill", new JLabel("Limit:"));
		leftPanel.add("tab", new JLabel("where n="));
		leftPanel.add("hfill", numberField);
		leftPanel.add("", SwingToolkit.InfoButton.create(new String[]{
			"To choose all the valid items, keep the field for 'n', blank.",
			"For percentage, type in proportion. ex) 0.35 for 35%",
		}));
		
		restoreOrderCheckBox = new JCheckBox();
		restoreOrderCheckBox.setText("restore original order?");
		restoreOrderCheckBox.setSelected(true);
		leftPanel.add("br hfill", restoreOrderCheckBox);
		
		recipeNameTextField = new JTextField();
		leftPanel.add("p", new JLabel("recipe name:"));
		leftPanel.add("hfill", recipeNameTextField);
		
		createRecipeButton = new JButton("create recipe");
		createRecipeButton.setMnemonic(KeyEvent.VK_C);
		createRecipeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				createRecipe();
			}
		});
		
		leftPanel.add("br hfill", createRecipeButton);
		
		recipeManagerPanel = new RecipeManagerPanel();
		leftPanel.add("p hfill", recipeManagerPanel);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(400);
		
		JScrollPane leftScrollPane = new JScrollPane(leftPanel);
		SwingToolkit.adjustScrollBar(leftScrollPane, JScrollBar.VERTICAL);
		splitPane.setLeftComponent(leftScrollPane);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new RiverLayout());
		
		rightPanel.add("br hfill", new JLabel("description:"));
		
		descriptionTextArea = new JTextArea();
		rightPanel.add("br hfill vfill", new JScrollPane(descriptionTextArea));
		splitPane.setRightComponent(rightPanel);
		
		this.setLayout(new BorderLayout());
		this.add(splitPane);
		
		this.refreshPanel();
	}
	
	public void refreshPanel() {
	}

	public static void main(String[] args) {
		test01();
	}
	protected static void test01(){
		SIDEToolkit.FileType.loadAll();
		
		TextRecipeConfigPanel textRecipeConfigPanel = new TextRecipeConfigPanel();
//		SIDEToolkit.FileType.sideFilter.loadFileArray(SIDEToolkit.FileType.sideFilter.getFileArray());

		TestFrame testFrame = new TestFrame(textRecipeConfigPanel);
		testFrame.setSize(new Dimension(800, 600));
		testFrame.showFrame();
	}
	
	private TreeBuilder treeBuilder;
	private JComboBox metricComboBox;
	private JComboBox limitComboBox;
	private JTextField numberField;
	
	private JTextField recipeNameTextField;
	private JButton createRecipeButton;
	private RecipeManagerPanel recipeManagerPanel;
	
	private JTextArea descriptionTextArea;
	private JCheckBox restoreOrderCheckBox;

	@Override
	public void actionPerformed(ActionEvent e) {
		this.refreshPanel();
	}

}