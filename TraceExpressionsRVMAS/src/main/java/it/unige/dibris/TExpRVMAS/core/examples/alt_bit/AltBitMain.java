package it.unige.dibris.TExpRVMAS.core.examples.alt_bit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unige.dibris.TExpRVMAS.core.Monitor;
import it.unige.dibris.TExpRVMAS.core.SnifferMonitorFactory;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
import it.unige.dibris.TExpRVMAS.exception.DecentralizedPartitionNotFoundException;
import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class AltBitMain {

	public static void main(String[] args) throws StaleProxyException, DecentralizedPartitionNotFoundException, IOException {
		JPLInitializer.init();
		
		TraceExpression tExp = new TraceExpression(args[0]);
		
		/* Initialize JADE environment */
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		AgentContainer container = runtime.createMainContainer( profile );	
		
		/* Decentralized monitors (random MMS) */
		//for(Monitor m : SnifferMonitorFactory.createDecentralizedMonitors(tExp, PartitionType.MinimalMonitoringSafe, null)){
		//	container.acceptNewAgent(m.getMonitorName(), m).start();
		//}
		
		List<AgentController> agents = new ArrayList<>();
		
		Sender alice = new Sender();
		alice.setArguments(new String[] { "bob", "msg1", "5000" });
		AgentController aliceC = container.acceptNewAgent("alice", alice);
		agents.add(aliceC);
		
		Receiver bob = new Receiver();
		bob.setArguments(new String[] { "alice", "ack1" });
		AgentController bobC = container.acceptNewAgent("bob", bob);
		agents.add(bobC);
		
		Sender charlie = new Sender();
		charlie.setArguments(new String[] { "david", "msg2", "10000" });
		AgentController charlieC = container.acceptNewAgent("charlie", charlie);
		agents.add(charlieC);
		
		Receiver david = new Receiver();
		david.setArguments(new String[] { "charlie", "ack2" });
		AgentController davidC = container.acceptNewAgent("david", david);
		agents.add(davidC);
		
		/* Centralized monitor */
		//SnifferMonitorFactory.createAndRunCentralizedMonitor(tExp, container, agents);
		//container.acceptNewAgent("centralizedMonitor", centralizedM);
		
		/* Decentralized monitors (first MS partition) */
		
		//ConditionsFactory.createMustBeSplitCondition("alice", "bob");		
		Partition<String> partition = tExp.getFirstMonitoringSafePartition(null);
		for(Monitor m : SnifferMonitorFactory.createDecentralizedMonitors(tExp, partition, agents)){
			container.acceptNewAgent(m.getMonitorName(), m).start();
		}
		
		Monitor.setErrorMessageGUIVisible(true);
		
		aliceC.start();
		bobC.start();
		charlieC.start();
		davidC.start();
		
		/*TraceExpression tExp_1 = new TraceExpression(args[1]);
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
		container.acceptNewAgent("david1", david1).start();*/
	}

}
