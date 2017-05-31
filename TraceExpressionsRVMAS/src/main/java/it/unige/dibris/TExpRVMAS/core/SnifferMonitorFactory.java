package it.unige.dibris.TExpRVMAS.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jpl7.PrologException;

import it.unige.dibris.TExpRVMAS.core.decentralized.Condition;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.core.decentralized.PartitionType;
import it.unige.dibris.TExpRVMAS.core.monitor.Sniffer;
import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
import it.unige.dibris.TExpRVMAS.exception.DecentralizedPartitionNotFoundException;
import it.unige.dibris.TExpRVMAS.exception.JADEContainerInitializationException;
import it.unige.dibris.TExpRVMAS.exception.NoMinimalMonitoringSafePartitionFoundException;
import it.unige.dibris.TExpRVMAS.exception.NotMonitoringSafePartitionException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Factory class to create the monitors to do the Runtime Verification of our JADE MAS
 * 
 * @author angeloferrando
 *
 */
public class SnifferMonitorFactory {
	
	/**
	 * Create a centralized monitor guided by a trace expression
	 * @param tExp is the trace expression used to generate the centralized monitor
	 * @return the centralized monitor (ready to be executed by the user)
	 * @throws StaleProxyException 
	 * 
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 * @throws NullPointerException if arguments are null
	 */
	public static Monitor createCentralizedMonitor(TraceExpression tExp, List<AgentController> agents) throws StaleProxyException{
		if(tExp == null || agents == null){
			throw new NullPointerException("tExp and agents must not be null");
		}
		Sniffer s = new Sniffer("sniffer_monitor_centralized", tExp, agents);
		/*Query query = new Query("involved(InvolvedAgents)");
		String agents;
		if(!query.hasSolution()){
			agents = "[]";
		} else{
			Term invAgentsTerm = query.oneSolution().get("InvolvedAgents");
			List<Term> invAgentsTermList = JPLInitializer.fromCompoundToList(invAgentsTerm);
			agents = "[";
			for(Term agent : invAgentsTermList){
				agents += agent + ",";
			}
			agents = agents.substring(0, agents.length() - 1) + "]";
		}*/
		String agentsStr = "[";
		for(AgentController agent : agents){
			agentsStr += agent.getName() + ",";
		}
		agentsStr = agentsStr.substring(0, agentsStr.length() - 1) + "]";
		s.setArguments(new String[]{
				"sniffer_centralized" + ".txt",
				agentsStr
		});
		return s;
	}
	
	/**
	 * Create and Run a centralized monitor guided by a trace expression
	 * @param tExp is the trace expression used to generate the centralized monitor
	 * @param container where to execute the monitor
	 * @return the centralized monitor (ready to be executed by the user)
	 * @throws StaleProxyException 
	 * 
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 * @throws NullPointerException if arguments are null
	 */
	public static Monitor createAndRunCentralizedMonitor(TraceExpression tExp, AgentContainer container, List<AgentController> agents) throws StaleProxyException{
		if(tExp == null || container == null || agents == null){
			throw new NullPointerException("tExp, container and agents must not be null");
		}
		Sniffer s = new Sniffer("sniffer_monitor_centralized", tExp, agents);
		/*Query query = new Query("involved(InvolvedAgents, " + tExp.getProtocolName() + ")");
		String agents;
		if(!query.hasSolution()){
			agents = "[]";
		} else{
			Term invAgentsTerm = query.oneSolution().get("InvolvedAgents");
			List<Term> invAgentsTermList = JPLInitializer.fromCompoundToList(invAgentsTerm);
			agents = "[";
			for(Term agent : invAgentsTermList){
				agents += agent + ",";
			}
			agents = agents.substring(0, agents.length() - 1) + "]";
		}*/
		String agentsStr = "[";
		for(AgentController agent : agents){
			agentsStr += agent.getName() + ",";
		}
		agentsStr = agentsStr.substring(0, agentsStr.length() - 1) + "]";
		s.setArguments(new String[]{
				"sniffer_centralized" + ".txt",
				agentsStr
		});
		try{
			AgentController ac = container.acceptNewAgent("sniffer_monitor", s);
			ac.start();
		} catch(StaleProxyException e){
			throw new JADEContainerInitializationException("Unable to start an agent container", e);
		}
		return s;
	}
	
	/**
	 * Create a set of decentralized monitors guided by a trace expression
	 * @param tExp is the trace expression used to generate the decentralzed monitors 
	 * @param pType is the type of partition we want the algorithm uses to decentralize the moitors
	 * @param conditions that must be satisfied by the partition selected internally
	 * @return the set of monitors generated (ready to be executed by the user) 
	 * 
	 * @throws DecentralizedPartitionNotFoundException if no partition can be found to decentralize to generate the monitors (only a centralized monitor can be used)
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 * @throws NullPointerException if arguments are null
	 */
	public static List<Monitor> createDecentralizedMonitors(TraceExpression tExp, PartitionType pType, List<Condition<String>> conditions, List<AgentController> agents) throws DecentralizedPartitionNotFoundException{
		if(tExp == null || pType == null || agents == null){
			throw new NullPointerException("tExp, pType and agents must not be null");
		}
		if(pType == PartitionType.MinimalMonitoringSafe){
			List<Partition<String>> mmsPartitions = tExp.getMinimalMonitoringSafePartitions(conditions);
			int random = new Random().nextInt(mmsPartitions.size());
			if(mmsPartitions.size() == 0){
				throw new NoMinimalMonitoringSafePartitionFoundException();
			} else if(mmsPartitions.size() == 1 && mmsPartitions.get(0).getNumberConstraints() == 1){
				throw new DecentralizedPartitionNotFoundException();
			} else{
				return createMonitors(mmsPartitions.get(random), tExp, agents);
			}
		} else{
			Partition<String> msPartition = tExp.getRandomMonitoringSafePartition(conditions);
			if(msPartition.getNumberConstraints() == 1){
				throw new DecentralizedPartitionNotFoundException();
			}	
			return createMonitors(msPartition, tExp, agents);
		}
	}
	
	/**
	 * Create a set of decentralized monitors guided by a trace expression using a fixed partition
	 * @param tExp is the trace expression used to generate the decentralzed monitors 
	 * @param partition is the partition to use to guide the decentralization process
	 * @return the set of monitors generated using the partition (ready to be executed by the user) 
	 * 
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 * @throws NullPointerException if arguments are null
	 */
	public static List<Monitor> createDecentralizedMonitors(TraceExpression tExp, Partition<String> partition, List<AgentController> agents) {
		if(tExp == null || partition == null || agents == null){
			throw new NullPointerException("tExp, partition and agents must not be null");
		}
		if(!tExp.isMonitoringSafe(partition)){
			throw new NotMonitoringSafePartitionException();
		}
		return createMonitors(partition, tExp, agents);
	}
	
	/**
	 * Auxiliary method used to generate a set of decentralized monitors starting from a given partition
	 * @param partition to use to generate the decentralized monitors
	 * @param tExp the Trace Expression to use to generate the monitors
	 * @return the set of monitors generated
	 * 
	 * @throws NullPointerException if arguments are null
	 */
	private static List<Monitor> createMonitors(Partition<String> partition, TraceExpression tExp, List<AgentController> agents){
		if(partition == null || tExp == null || agents == null){
			throw new NullPointerException("partition, tExp and agents must not be null");
		}
		List<Monitor> monitors = new ArrayList<>();
		for(Set<String> constraint : partition){
			/* Sniffer creation */
			String constraintAux = constraint.toString().replace("[", "").replace("]", "").replace(",", "_").replace(" ", "");
			Sniffer s = new Sniffer("sniffer_monitor_decentralized_on_" + constraintAux, tExp, agents);
			s.setArguments(new String[]{
					"sniffer_monitor_decentralized_on_" + constraintAux + ".txt",
					constraint.toString()
			});
			monitors.add(s);
		}
		return monitors;
	}
	
}
