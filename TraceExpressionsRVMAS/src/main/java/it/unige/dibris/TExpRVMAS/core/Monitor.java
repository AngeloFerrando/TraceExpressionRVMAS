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
	 * GUI flag
	 */
	private boolean errorMessageGUI;
	
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
	
	/**
	 * Setter method to enable the message GUI for the errors thrown by the monitor 
	 * @param gui true to enable the GUI, false otherwise 
	 */
	public void setErrorMessageGuiFlag(boolean gui){
		this.errorMessageGUI = gui;
	}

	/**
	 * Getter method for the GUI flag
	 * @return the errorMessageGUI
	 */
	public boolean isErrorMessageGUI() {
		return errorMessageGUI;
	}
	
}
