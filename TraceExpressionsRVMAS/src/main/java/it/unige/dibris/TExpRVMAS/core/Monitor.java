package it.unige.dibris.TExpRVMAS.core;

import jade.tools.ToolAgent;

/**
 * Abstract class used to generalize the monitor structure
 * 
 * @author angeloferrando
 *
 */
@SuppressWarnings("serial")
public abstract class Monitor extends ToolAgent {

	/**
	 * The monitor name
	 */
	private String name;
	
	/**
	 * Constructor
	 * @param name of the monitor
	 */
	public Monitor(String name){
		this.name = name;
	}

	/**
	 * Getter method for the monitor name
	 * @return the monitor name
	 */
	public String getMonitorName() {
		return name;
	}
	
}
