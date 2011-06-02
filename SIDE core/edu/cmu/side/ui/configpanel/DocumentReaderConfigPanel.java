package edu.cmu.side.ui.configpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;

import se.datadosen.component.RiverLayout;

import com.yerihyo.yeritools.swing.SwingToolkit.TestFrame;

import edu.cmu.side.Workbench;
import edu.cmu.side.plugin.DocumentReaderPlugin;
import edu.cmu.side.plugin.PluginWrapper;
import edu.cmu.side.plugin.SIDEPlugin;

public class DocumentReaderConfigPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args){
		test01();
	}
	protected static void test01(){
		DocumentReaderConfigPanel drp = new DocumentReaderConfigPanel();
		TestFrame testFrame = new TestFrame(drp);
		testFrame.setSize(new Dimension(800,800));
		testFrame.showFrame();
	}
	
	public DocumentReaderConfigPanel(){
		yeriInit();
	}

	private void yeriInit() {
		this.setLayout(new RiverLayout());
		
		List<PluginWrapper> pluginWrapperList = Workbench.current.pluginManager.getPluginWrapperCollectionByType(DocumentReaderPlugin.type);
		pluginComboBox = new JComboBox();
		for(PluginWrapper pluginWrapper: pluginWrapperList){
			pluginComboBox.addItem(pluginWrapper.getSIDEPlugin().getClass().getName());
		}
		if(pluginComboBox.getItemCount()>0){ pluginComboBox.setSelectedIndex(0); }
		pluginComboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshPanel();
			}
		});
		this.add("left", new JLabel("document reader plugins:"));
		this.add("br hfill", pluginComboBox);
		
		this.add("br hfill", new JSeparator());
		
		configPanel = new JPanel();
		configPanel.setLayout(new BorderLayout());
		this.add("br hfill vfill", configPanel);
		
		this.add("br hfill", new JSeparator());
		
		convertFilesButton = new JButton("convert files");
		convertFilesButton.setMnemonic(KeyEvent.VK_V);
		this.add("br hfill", convertFilesButton);
		convertFilesButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				DocumentReaderPlugin documentReaderPlugin = getDocumentReaderPlugin();
				if(documentReaderPlugin==null){ throw new RuntimeException("DocumentReaderPlugin is null"); }
				
				documentReaderPlugin.uiToMemory();
				try {
					documentReaderPlugin.readDocuments();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(DocumentReaderConfigPanel.this, "convert failed.", "failure", JOptionPane.PLAIN_MESSAGE);
					ex.printStackTrace();
					return;
				}
				JOptionPane.showMessageDialog(DocumentReaderConfigPanel.this, "convert succeeded.", "success", JOptionPane.PLAIN_MESSAGE);
//				
			}
		});
		progressBar = new JProgressBar();
		this.add("br hfill", progressBar);
		
		refreshPanel();
	}
	
	private Map<String,SIDEPlugin> pluginCacheMap = new HashMap<String,SIDEPlugin>();
	protected DocumentReaderPlugin getDocumentReaderPlugin(){
		String pluginClassName = (String)this.pluginComboBox.getSelectedItem();
		
		if(pluginClassName==null){ return null; }
		
		SIDEPlugin plugin = pluginCacheMap.get(pluginClassName);
		if(plugin==null){
			PluginWrapper pluginWrapper = Workbench.current.pluginManager.getPluginWrapperByPluginClassName(pluginClassName);
			plugin = pluginWrapper.getSIDEPlugin();
		}
		DocumentReaderPlugin documentReaderPlugin = (DocumentReaderPlugin)plugin;
		pluginCacheMap.put(pluginClassName, documentReaderPlugin);
		return documentReaderPlugin;
	}
	public void refreshPanel() {
		DocumentReaderPlugin documentReaderPlugin = this.getDocumentReaderPlugin();
		
		if(documentReaderPlugin==null){ return; }
		
		this.configPanel.removeAll();
		this.configPanel.add(documentReaderPlugin.getConfigurationUI());
		this.configPanel.revalidate();
	}

	private transient JComboBox pluginComboBox;
	private transient JPanel configPanel;
	private transient JButton convertFilesButton;
	private transient JProgressBar progressBar;
}
