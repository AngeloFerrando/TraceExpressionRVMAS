package it.unige.dibris.TExpRVMAS.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import it.unige.dibris.TExpRVMAS.Exception.EnvironmentVariableNotDefined;
import it.unige.dibris.TExpRVMAS.Exception.JADEAgentInitializationException;
import it.unige.dibris.TExpRVMAS.Exception.JADEContainerInitializationException;
import it.unige.dibris.TExpRVMAS.Exception.JavaLibraryPathException;
import it.unige.dibris.TExpRVMAS.core.decentralized.Condition;
import it.unige.dibris.TExpRVMAS.core.decentralized.FactoryConditions;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.core.monitor.Sniffer;
import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

/**
 * Boots the TraceExpressionsRVMAS system, parsing command line arguments.
 * 
 * @author angeloferrando
 * 
 */

public class Boot {

	private String swipl;
	private TraceExpression tExp;
	
	private Boot(){}

	/**
	 * It executes the TraceExpressionRVMAS
	 * 
	 * @param args 
	 * trace expression file containing the protocol to verify, and a list of JADE agents to execute/monitor 
	 * <path_to_trace_expression_file> <jade-agent1> ... <jade-agentN>
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		args = new String[2];
		args[0] = "/Users/angeloferrando/Desktop/trace_expression.texp";
		args[1] = "alice:alt_bit.Sender(bob,msg1,5000);bob:alt_bit.Receiver(alice,ack1);charlie:alt_bit.Sender(david,msg2,10000);david:alt_bit.Receiver(charlie,ack2)";
		
		/* Parsing arguments */
		Boot boot = Boot.parseArguments(args);
		
		/* Initialize JADE environment */
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		AgentContainer container = runtime.createMainContainer( profile );
		
		/* List of JADE agents */
		List<AgentController> agents = new ArrayList<>();
		
		/* Parsing JADE agents */
		String[] agentsargs = args[1].split(";");
		/* Partition used for the runtime verification */
		List<List<? extends String>> projectionSet = new ArrayList<>();
		List<String> centralizedP = new ArrayList<>();
		projectionSet.add(centralizedP);
		for(String agent : agentsargs){
			String name = agent.split(":")[0];
			centralizedP.add(name);
			String constructor = agent.split(":")[1];
			if(!constructor.contains("(") || !constructor.contains(")")){
				throw new IllegalArgumentException("JADE agents must follow the syntax: name:type(arg1,...,argN)");
			}
			String type = constructor.split("\\(")[0];
			Object[] arguments = constructor.split("\\(")[1].replace(")", "").split(",");
			try {
				agents.add(container.createNewAgent(name, type, arguments));
			} catch (StaleProxyException e) {
				throw new JADEAgentInitializationException("Unable to create the agent " + name + " of type " + type, e);
			}
		}
		//projectionSet = projectionSet.substring(0, projectionSet.length()-1) + "]";
		Partition<String> partition = new Partition<String>(projectionSet);
				
		for(Partition<String> p : boot.tExp.getMinimalMonitoringSafePartitions(null)){
			System.out.println(p);
		}
		System.out.println();
		List<Condition> conditions = new ArrayList<>();
		conditions.add(FactoryConditions.createAtLeastNumberSingletonsCondition(2));
		conditions.add(FactoryConditions.createAtLeastNumberAgentsForConstraintCondition(1));
		conditions.add(FactoryConditions.createAtLeastNumberOfConstraintsCondition(3));

		List<Partition<String>> mmsPartitions = boot.tExp.getMinimalMonitoringSafePartitions(conditions);
		int random = new Random().nextInt(mmsPartitions.size());
		
		System.out.println("Partitions found:");
		for(Partition<String> p : mmsPartitions){
			System.out.println(p);
		}
		
		/*// Test of getMonitoringSafePartitions method
		Iterator<Partition<String>> itMSPartitions = boot.tExp.getMonitoringSafePartitions(conditions).iterator();
		if(itMSPartitions.hasNext()){
			partition = itMSPartitions.next();
		}
		*/
		
		if(mmsPartitions.size() > 0){
			partition = mmsPartitions.get(random);
		}
		
		/* Sniffer creation */
		runMonitors(container, partition);
		runAgents(container, agents);
		
		/* Set to close the JVM when JADE environment ends */
		jade.core.Runtime.instance().setCloseVM(true);		
	}
	
	private static Boot parseArguments(String[] args) throws FileNotFoundException{
		Boot boot = new Boot();
		/* The arguments must be at least 2, the trace expression file and one (or more) agent(s) */
		if(args.length != 2){
			throw new IllegalArgumentException("Too few arguments: expected <path_to_trace_expression_file> <jade-agent1>;...;<jade-agentN>");
		}
		
		/* Retrieve the SWI_LIB environment variable */
		boot.swipl = System.getenv("SWI_LIB");
		
		/* If it does not exist an exception is thrown */
		if(boot.swipl == null){
			throw new EnvironmentVariableNotDefined("SWI_LIB environment variable not defined");
		}		
		
		/* We need to add the SWI-Prolog Home to the path in order to use the JPL library */
		try{
			addLibraryPath(boot.swipl);
		} catch(Exception e){
			throw new JavaLibraryPathException("An error occured during the user path retrieval information process", e);
		}
		
		/* SWI-Prolog environment initialization (transition system, DecAMon, current trace expression) */
		JPLInitializer.init();
		
		/* Now we check only if the file exists. 
		 * TO-DO To check if the file is syntactically and semantically correct */
		boot.tExp = new TraceExpression(args[0]);
		
		return boot;
	}
	
	public static void runMonitors(AgentContainer container, Partition<String> p){
		for(Set<String> constraint : p){
			/* Sniffer creation */
			Sniffer s = new Sniffer();
			String constraintAux = constraint.toString().replace("[", "").replace("]", "").replace(",", "_").replace(" ", "");
			s.setArguments(new String[]{
					"sniffer" + constraintAux + ".txt",
					constraint.toString()
			});
			try{
				AgentController ac = container.acceptNewAgent("sniffer" + constraintAux, s);
				ac.start();
			} catch(StaleProxyException e){
				throw new JADEContainerInitializationException("Unable to start an agent container", e);
			}
		}
	}
	
	private static void runAgents(AgentContainer container, List<AgentController> agents){
		try{
			for(AgentController agent : agents){
				agent.start();
			}
		} catch(StaleProxyException e){
			throw new JADEContainerInitializationException("Unable to start an agent container", e);
		}
	}
	
	/**
	* Adds the specified path to the java library path
	*
	* @param pathToAdd the path to add
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	*/
	private static void addLibraryPath(String pathToAdd) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
	    final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
	    usrPathsField.setAccessible(true);

	    //get array of paths
	    final String[] paths = (String[])usrPathsField.get(null);

	    //check if the path to add is already present
	    for(String path : paths) {
	        if(path.equals(pathToAdd)) {
	            return;
	        }
	    }

	    //add the new path
	    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
	    newPaths[newPaths.length-1] = pathToAdd;
	    usrPathsField.set(null, newPaths);
	}

}
