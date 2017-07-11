package it.unige.dibris.TExpRVMAS.core.examples.alt_bit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unige.dibris.TExpRVMAS.core.Channel;
import it.unige.dibris.TExpRVMAS.core.Monitor;
import it.unige.dibris.TExpRVMAS.core.SimulatedChannel;
import it.unige.dibris.TExpRVMAS.core.SnifferMonitorFactory;
import it.unige.dibris.TExpRVMAS.core.decentralized.Condition;
import it.unige.dibris.TExpRVMAS.core.decentralized.ConditionsFactory;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
import it.unige.dibris.TExpRVMAS.exception.DecentralizedPartitionNotFoundException;
import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Alt_bit_4 {
	
	public static void main(String[] args) throws StaleProxyException, DecentralizedPartitionNotFoundException, IOException {
		JPLInitializer.init();
		
		TraceExpression tExp = new TraceExpression("/Users/angeloferrando/Documents/runtime-EclipseApplication/test/src-gen/alt_bit_4.pl");
		
		/* Initialize JADE environment */
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		AgentContainer container = runtime.createMainContainer(profile);	
		
		List<AgentController> agents = new ArrayList<>();
		
		Sender alice = new Sender();
		alice.setArguments(new String[] {
			"bob"
						, 
			"msg1"
						, 
			"5000"
		});
		AgentController aliceC = container.acceptNewAgent("alice", alice);
		agents.add(aliceC);
		Receiver bob = new Receiver();
		bob.setArguments(new String[] {
			"alice"
						, 
			"ack1"
		});
		AgentController bobC = container.acceptNewAgent("bob", bob);
		agents.add(bobC);
		Sender charlie = new Sender();
		charlie.setArguments(new String[] {
			"dave"
						, 
			"msg2"
						, 
			"10000"
		});
		AgentController charlieC = container.acceptNewAgent("charlie", charlie);
		agents.add(charlieC);
		Receiver dave = new Receiver();
		dave.setArguments(new String[] {
			"charlie"
						, 
			"ack2"
		});
		AgentController daveC = container.acceptNewAgent("dave", dave);
		agents.add(daveC);
		/* Create and Set the partition */
		List<List<String>> groups = new ArrayList<>();
		List<String> group;
		group = new ArrayList<>();
		groups.add(group);
		group.add("alice");
		group.add("charlie");
		group = new ArrayList<>();
		groups.add(group);
		group.add("bob");
		group = new ArrayList<>();
		groups.add(group);
		group.add("dave");
		Partition<String> partition = new Partition<>(groups);
		
		/* Decentralized monitors */
		
		for(Monitor m : SnifferMonitorFactory.createDecentralizedMonitors(tExp, partition, agents)){
			container.acceptNewAgent(m.getMonitorName(), m).start();
		}
		
		Monitor.setErrorMessageGUIVisible(false);
		
		/* Channels creation */
		Channel.addChannel(new SimulatedChannel("email", 0.8));
		Channel.addChannel(new SimulatedChannel("sms", 1));
		
		/* Run the agents */
		aliceC.start();
		bobC.start();
		charlieC.start();
		daveC.start();
	}
}
