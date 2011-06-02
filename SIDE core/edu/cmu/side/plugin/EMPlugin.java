package edu.cmu.side.plugin;

import java.io.File;

import edu.cmu.side.uima.UIMAToolkit.DocumentList;

public abstract class EMPlugin extends SIDEPlugin {
	public EMPlugin () {super();}
	
	public EMPlugin (File rootFolder)
	{
		super (rootFolder);
	}
	
	public static String type = "evaluation_metric";
	public String getType () {return type;}
	
//	public boolean equals(Object o){
//		if(!(o instanceof EMPlugin)){
//			return false;
//		}
//		
//		return this.toXML().equals( ((EMPlugin)o).toXML() );
//	}
	
	public int[] getOrderIndex(DocumentList documentList) throws Exception{
		this.uiToMemory();
		return this.getOrderIndexForSubclass(documentList);
	}
	
	protected abstract int[] getOrderIndexForSubclass(DocumentList documentList);
}
