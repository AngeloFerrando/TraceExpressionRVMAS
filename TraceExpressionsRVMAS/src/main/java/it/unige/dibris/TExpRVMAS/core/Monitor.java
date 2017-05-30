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
	
	private static ErrorMessageGUI errorMsgGUI = new ErrorMessageGUI();
	
	public static void setErrorMessageGUIVisible(boolean visible){
		errorMsgGUI.setVisible(visible);
	}
	
	public static boolean isErrorMessageGUIVisible(){
		return errorMsgGUI.isVisible();
	}
	
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
	
	public void sendMessageLogToGUI(String msg){
		errorMsgGUI.addMessageLog(this, msg);
	}
	
}
