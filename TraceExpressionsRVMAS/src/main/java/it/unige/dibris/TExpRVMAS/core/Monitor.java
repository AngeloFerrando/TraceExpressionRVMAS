package it.unige.dibris.TExpRVMAS.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
import it.unige.dibris.TExpRVMAS.exception.JADEAgentInitializationException;
import jade.tools.ToolAgent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

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
	
	/**
	 * Agents checked by this monitor
	 */
	private List<AgentController> agents;
	
	/**
	 * Message GUI used by monitors to print all log information
	 */
	private static ErrorMessageGUI errorMsgGUI = new ErrorMessageGUI();
	
	/**
	 * Map used to maintain the link between agents and monitors
	 */
	private static ConcurrentHashMap<String, Monitor> mapToMonitor = new ConcurrentHashMap<>();
	
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
	 * @throws StaleProxyException 
	 * 
	 * @throws NullPointerException if arguments are null
	 */
	public Monitor(String name, TraceExpression tExp, List<AgentController> agents) {
		if(name == null || tExp == null || agents == null){
			throw new NullPointerException();
		}
		this.name = name;
		this.tExp = tExp;
		this.agents = new ArrayList<>(agents);
		for(AgentController agent : agents){
			try {
				Monitor.mapToMonitor.putIfAbsent(agent.getName(), this);
			} catch (StaleProxyException e) {
				throw new JADEAgentInitializationException(e);
			}
		}
	}

	/**
	 * Getter method for the monitor name
	 * @return the monitor name
	 */
	public String getMonitorName() {
		return name;
	}
	
	protected void sendMessageLogToGUI(String msg){
		errorMsgGUI.addMessageLog(this, msg);
	}

	/**
	 * @return the tExp
	 */
	public TraceExpression getTraceExpression() {
		return tExp;
	}
	
	/**
	 * Method handling the addition of a new perception
	 * @param perception to add
	 */
	public abstract void addPerception(Perception perception);

	/**
	 * Return the monitor which are checking the agent
	 * @param agentName the agent checked by the monitor returned
	 * @return the monitor checking the agent (null if the agent is not checked by any monitor)
	 * @throws StaleProxyException 
	 */
	public static Monitor getMyMonitor(AgentController agent) {
		try {
			return Monitor.mapToMonitor.get(agent.getName());
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			throw new JADEAgentInitializationException(e);
		}
	}
	
	/**
	 * Return the monitor which are checking the agent
	 * @param agentName the agent checked by the monitor returned
	 * @return the monitor checking the agent (null if the agent is not checked by any monitor)
	 */
	public static Monitor getMyMonitor(String name) {
		return Monitor.mapToMonitor.get(name);
	}

	/**
	 * @return the agentsNames
	 */
	public List<AgentController> getAgentsNames() {
		return agents;
	}
	
	
	
}
