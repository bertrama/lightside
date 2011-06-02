package edu.cmu.side.plugin;

import org.apache.uima.cas.CAS;


public abstract class UIMACasConsumerPlugin extends SIDEPlugin{
	public static final String type = "cas_consumer";

	@Override
	public String getType() { return type; }
	
	public abstract void comsume(CAS cas);
}
