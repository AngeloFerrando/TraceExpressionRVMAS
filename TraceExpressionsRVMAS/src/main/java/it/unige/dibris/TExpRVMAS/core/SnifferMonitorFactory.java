package it.unige.dibris.TExpRVMAS.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jpl7.Query;
import org.jpl7.Term;

import it.unige.dibris.TExpRVMAS.Exception.DecentralizedPartitionNotFoundException;
import it.unige.dibris.TExpRVMAS.Exception.JADEContainerInitializationException;
import it.unige.dibris.TExpRVMAS.Exception.NoMinimalMonitoringSafePartitionFoundException;
import it.unige.dibris.TExpRVMAS.Exception.NoMonitoringSafePartitionFoundException;
import it.unige.dibris.TExpRVMAS.Exception.NotMonitoringSafePartitionException;
import it.unige.dibris.TExpRVMAS.core.decentralized.Condition;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.core.decentralized.PartitionType;
import it.unige.dibris.TExpRVMAS.core.monitor.Sniffer;
import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class SnifferMonitorFactory {
	
	public static Monitor createCentralizedMonitor(TraceExpression tExp){
		tExp.load();
		Sniffer s = new Sniffer("sniffer_monitor_centralized");
		Query query = new Query("involved(InvolvedAgents)");
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
		}
		s.setArguments(new String[]{
				"sniffer_centralized" + ".txt",
				agents
		});
		return s;
	}
	
	public static Monitor createAndRunCentralizedMonitor(TraceExpression tExp, AgentContainer container){
		tExp.load();
		Sniffer s = new Sniffer("sniffer_monitor_centralized");
		Query query = new Query("involved(InvolvedAgents)");
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
		}
		s.setArguments(new String[]{
				"sniffer_centralized" + ".txt",
				agents
		});
		try{
			AgentController ac = container.acceptNewAgent("sniffer_monitor", s);
			ac.start();
		} catch(StaleProxyException e){
			throw new JADEContainerInitializationException("Unable to start an agent container", e);
		}
		return s;
	}
	
	public static List<Monitor> createDecentralizedMonitor(TraceExpression tExp, PartitionType pType, List<Condition> conditions) throws DecentralizedPartitionNotFoundException{
		tExp.load();
		if(pType == PartitionType.MinimalMonitoringSafe){
			List<Partition<String>> mmsPartitions = tExp.getMinimalMonitoringSafePartitions(conditions);
			int random = new Random().nextInt(mmsPartitions.size());
			if(mmsPartitions.size() == 0){
				throw new NoMinimalMonitoringSafePartitionFoundException();
			} else if(mmsPartitions.size() == 1 && mmsPartitions.get(0).getNumberConstraints() == 1){
				throw new DecentralizedPartitionNotFoundException();
			} else{
				return createMonitors(mmsPartitions.get(random));
			}
		} else{
			Partition<String> msPartition = tExp.getRandomMonitoringSafePartition(conditions);
			if(msPartition.getNumberConstraints() == 1){
				throw new DecentralizedPartitionNotFoundException();
			}	
			return createMonitors(msPartition);
		}
	}
	
	public static List<Monitor> createDecentralizedMonitor(TraceExpression tExp, Partition<String> partition) {
		if(!tExp.isMonitoringSafe(partition)){
			throw new NotMonitoringSafePartitionException();
		}
		return createMonitors(partition);
	}
	
	private static List<Monitor> createMonitors(Partition<String> partition){
		List<Monitor> monitors = new ArrayList<>();
		for(Set<String> constraint : partition){
			/* Sniffer creation */
			String constraintAux = constraint.toString().replace("[", "").replace("]", "").replace(",", "_").replace(" ", "");
			Sniffer s = new Sniffer("sniffer_monitor_decentralized_on_" + constraintAux);
			s.setArguments(new String[]{
					"sniffer_monitor_decentralized_on_" + constraintAux + ".txt",
					constraint.toString()
			});
			monitors.add(s);
		}
		return monitors;
	}
	
}
