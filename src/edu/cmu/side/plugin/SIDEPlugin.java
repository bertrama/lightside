package edu.cmu.side.plugin;

import java.awt.Component;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.mysterion.xml.XMLBoss;
import com.yerihyo.yeritools.xml.XMLable;

import edu.cmu.side.SimpleWorkbench;


public abstract class SIDEPlugin implements Cloneable{
	private transient File rootFolder;

	private Map<String,String> aboutMap = new HashMap<String,String>();
	public Map<String, String> getAboutMap() {
		return aboutMap;
	}

	public void configureFromFile(String filename){
		
	}

	
	public static String classnameXMLKey = "classname";

	protected StringBuilder wrapSIDEPluginOption(CharSequence cs){
		StringBuilder builder = new StringBuilder();
		builder.append("<").append(this.getType()).append(" "+classnameXMLKey+"=\"").append(this.getClass().getName()).append("\">");
		builder.append(cs);
		builder.append("</").append(this.getType()).append(">");
		return builder;
	}
	
	public SIDEPlugin() {
	}

	public SIDEPlugin(File rootFolder) {
		this.rootFolder = rootFolder;
	}

	public void setRootFolder(File rootFolder) {
		this.rootFolder = rootFolder;
	}

	public File PluginFolder() {
		return this.rootFolder;
	}

	/**
	 * return a string which indicates the type of functionality
	 * provided by the pluginWrapper; pluginManager are grouped by type
	 * for example: "segmenter", "summarization", etc.
	 * 
	 * @return
	 */
	public abstract String getType();

	/**
	 * This method is provided to give you a way to make sure that the pluginWrapper
	 * has been installed properly. The default implementation prints the name
	 * and version of the pluginWrapper and a 'success' message. If your implementation
	 * requires more sophisitcated error checking, you should overload this
	 * method. For example, if there are any external resources which you need
	 * access to, this is the time to check and make sure you can get to them.
	 * If you encounter any error or 'warning' conditions, append the relevant
	 * messages to the 'msg' buffer which was passed in.
	 */
	public boolean validatePlugin(StringBuffer msg)
	
	{
		boolean result = true;
		msg.append(this.aboutMap.get("title") + " " + this.aboutMap.get("version") + "\n");
		result = doValidation(msg);
		return result;
	}

	/*--------------------------------------------------------------------------------- */
	/*--------------------------------- OVERLOADABLE METHODS -------------------------- */
	/*--------------------------------------------------------------------------------- */

	public abstract boolean doValidation(StringBuffer msg);

	// Perform whatever pluginWrapper-specific validation is necessary
	// to ensure that the pluginWrapper can run

	public boolean isConfigurable()
	// return TRUE if the user can customize the
	// behavior of the pluginWrapper, FALSE otherwise
	{
		return false;
	}

	public Component getConfigurationUI(){
		this.memoryToUI();
		return this.getConfigurationUIForSubclass();
	}
	
	protected abstract Component getConfigurationUIForSubclass();
	public abstract void memoryToUI();
	public abstract void uiToMemory();
}
