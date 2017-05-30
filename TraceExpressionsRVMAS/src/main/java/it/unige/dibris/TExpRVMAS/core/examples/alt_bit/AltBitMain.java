package it.unige.dibris.TExpRVMAS.core.examples.alt_bit;

import java.io.IOException;

import it.unige.dibris.TExpRVMAS.core.Monitor;
import it.unige.dibris.TExpRVMAS.core.SnifferMonitorFactory;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.core.decentralized.PartitionType;
import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
import it.unige.dibris.TExpRVMAS.exception.DecentralizedPartitionNotFoundException;
import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class AltBitMain {

	public static void main(String[] args) throws StaleProxyException, DecentralizedPartitionNotFoundException, IOException {
		JPLInitializer.init();
		
		TraceExpression tExp = new TraceExpression(args[0]);
		
		/* Initialize JADE environment */
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		AgentContainer container = runtime.createMainContainer( profile );	
		
		/* Centralized monitor */
		//Monitor centralizedM = SnifferMonitorFactory.createAndRunCentralizedMonitor(tExp, container);
		//container.acceptNewAgent("centralizedMonitor", centralizedM);
		
		/* Decentralized monitors (random MMS) */
		//for(Monitor m : SnifferMonitorFactory.createDecentralizedMonitors(tExp, PartitionType.MinimalMonitoringSafe, null)){
		//	container.acceptNewAgent(m.getMonitorName(), m).start();
		//}
		
		/* Decentralized monitors (first MS partition) */
		Partition<String> partition = tExp.getFirstMonitoringSafePartition(null);
		for(Monitor m : SnifferMonitorFactory.createDecentralizedMonitors(tExp, partition)){
			container.acceptNewAgent(m.getMonitorName(), m).start();
		}
		
		Monitor.setErrorMessageGUIVisible(true);
		
		Sender alice = new Sender();
		alice.setArguments(new String[] { "bob", "msg1", "5000" });
		Receiver bob = new Receiver();
		bob.setArguments(new String[] { "alice", "ack1" });

		Sender charlie = new Sender();
		charlie.setArguments(new String[] { "david", "msg2", "10000" });
		Receiver david = new Receiver();
		david.setArguments(new String[] { "charlie", "ack2" });
		
		container.acceptNewAgent("alice", alice).start();
		container.acceptNewAgent("bob", bob).start();
		container.acceptNewAgent("charlie", charlie).start();
		container.acceptNewAgent("david", david).start();
		
		TraceExpression tExp_1 = new TraceExpression(args[1]);
		Monitor centralizedM = SnifferMonitorFactory.createAndRunCentralizedMonitor(tExp_1, container);
		container.acceptNewAgent("centralizedMonitor", centralizedM);
		
		Sender alice1 = new Sender();
		alice1.setArguments(new String[] { "bob1", "msg1", "5000" });
		Receiver bob1 = new Receiver();
		bob1.setArguments(new String[] { "alice1", "ack1" });

		Sender charlie1 = new Sender();
		charlie1.setArguments(new String[] { "david1", "msg2", "10000" });
		Receiver david1 = new Receiver();
		david1.setArguments(new String[] { "charlie1", "ack2" });
		
		container.acceptNewAgent("alice1", alice1).start();
		container.acceptNewAgent("bob1", bob1).start();
		container.acceptNewAgent("charlie1", charlie1).start();
		container.acceptNewAgent("david1", david1).start();
	}

}
