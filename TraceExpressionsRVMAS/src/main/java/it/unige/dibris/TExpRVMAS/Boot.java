package it.unige.dibris.TExpRVMAS;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import it.unige.dibris.Exception.EnvironmentVariableNotDefined;
import it.unige.dibris.Exception.JADEAgentInitializationException;
import it.unige.dibris.Exception.JADEContainerInitializationException;
import it.unige.dibris.Exception.JavaLibraryPathException;
import it.unige.dibris.monitor.Sniffer;
import it.unige.dibris.utils.JPL.JPLInitializer;
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

	/**
	 * Executes the TraceExpressionRVMAS
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
		
		/* The arguments must be at least 2, the trace expression file and one (or more) agent(s) */
		if(args.length != 2){
			throw new IllegalArgumentException("Too few arguments: expected <path_to_trace_expression_file> <jade-agent1>;...;<jade-agentN>");
		}
		
		/* Retrieve the SWI_LIB environment variable */
		String swipl = System.getenv("SWI_LIB");
		
		/* If it does not exist an exception is thrown */
		if(swipl == null){
			throw new EnvironmentVariableNotDefined("SWI_LIB environment variable not defined");
		}
		
		/* We need to add the SWI-Prolog Home to the path in order to use the JPL library */
		try{
			addLibraryPath(swipl);
		} catch(Exception e){
			throw new JavaLibraryPathException("An error occured during the user path retrieval information process", e);
		}
		
		/* Now we check only if the file exists. 
		 * TO-DO To check if the file is syntactically and semantically correct */
		String tExpPath = args[0];
		File tExpFile = new File(tExpPath);
		if(!tExpFile.exists()){ 
		    throw new FileNotFoundException(tExpPath + " file not found");
		}
		
		/* SWI-Prolog environment initialization (transition system, DecAMon, current trace expression) */
		JPLInitializer.init(tExpPath);
		
		/* Initialize JADE environment */
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		AgentContainer container = runtime.createMainContainer( profile );
		
		/* List of JADE agents */
		List<AgentController> agents = new ArrayList<>();
		
		/* Parsing JADE agents */
		String[] agentsargs = args[1].split(";");
		/* Partition used for the runtime verification */
		String projectionSet = "[";
		for(String agent : agentsargs){
			String name = agent.split(":")[0];
			projectionSet += name + ",";
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
		projectionSet = projectionSet.substring(0, projectionSet.length()-1) + "]";
		/* Sniffer creation */
		Sniffer s = new Sniffer();
		s.setArguments(new String[]{
				"snifferCentralized.txt",
				projectionSet
		});
		try{
			AgentController ac = container.acceptNewAgent("snifferCentralized", s);
			ac.start();
			for(AgentController agent : agents){
				agent.start();
			}
		} catch(StaleProxyException e){
			throw new JADEContainerInitializationException("Unable to start an agent container", e);
		}
		
		/* Set to close the JVM when JADE environment ends */
		jade.core.Runtime.instance().setCloseVM(true);		
	}
	
	public static void executeMonitors(Partition p){
		
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
