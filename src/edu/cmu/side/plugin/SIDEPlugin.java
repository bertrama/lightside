package edu.cmu.side.plugin;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.side.Workbench;

public abstract class SIDEPlugin implements Cloneable{
	private transient File rootFolder;

	public static boolean halt;
	
	private Map<String,String> aboutMap = new HashMap<String,String>();
	public Map<String, String> getAboutMap() {
		return aboutMap;
	}
	
	public abstract String getOutputName();

	
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
	 * LightSIDE Genesis will remind the user, at many places, what options they selected.
	 * This method will be called to get that string.
	 * @return
	 */
	public String getDescription(){
		return "No description available.";
	}
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

	public void stopWhenPossible(){
		halt = true;
	}

	/*--------------------------------------------------------------------------------- */
	/*--------------------------------- OVERLOADABLE METHODS -------------------------- */
	/*--------------------------------------------------------------------------------- */

	public boolean doValidation(StringBuffer msg){
		return true;
	}
	// Perform whatever pluginWrapper-specific validation is necessary
	// to ensure that the pluginWrapper can run

	public boolean isConfigurable()
	// return TRUE if the user can customize the
	// behavior of the pluginWrapper, FALSE otherwise
	{
		return false;
	}

	public Component getConfigurationUI(){
		return this.getConfigurationUIForSubclass();
	}
	
	protected abstract Component getConfigurationUIForSubclass();
	public abstract Map<String, String> generateConfigurationSettings();
	public abstract void configureFromSettings(Map<String, String> settings);
	
	public static SIDEPlugin fromSerializable(Serializable pug)
	{
		if(pug == null) 
			return null;
		
		SIDEPlugin plugin = Workbench.pluginManager.getPluginWrapperByPluginClassName((String) pug).getSIDEPlugin();
		return plugin;
	}
	
	public Serializable toSerializable() throws IOException
	{
		return this.getClass().getName();
	}
}
