package it.unige.dibris.TExpRVMAS.core;

import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
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
	 * Trace expression guiding this monitor
	 */
	private TraceExpression tExp;
	
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
	 * @param tExp is the Trace Expression which will be used to generate and guide this monitor
	 * 
	 */
	public Monitor(String name, TraceExpression tExp){
		this.name = name;
		this.tExp = tExp;
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

	/**
	 * @return the tExp
	 */
	public TraceExpression getTraceExpression() {
		return tExp;
	}
	
}
