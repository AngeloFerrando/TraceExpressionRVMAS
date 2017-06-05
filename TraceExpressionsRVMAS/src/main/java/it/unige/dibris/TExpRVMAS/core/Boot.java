package it.unige.dibris.TExpRVMAS.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jpl7.PrologException;

import it.unige.dibris.TExpRVMAS.core.decentralized.Condition;
import it.unige.dibris.TExpRVMAS.core.decentralized.ConditionsFactory;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.core.protocol.TraceExpression;
import it.unige.dibris.TExpRVMAS.exception.EnvironmentVariableNotDefinedException;
import it.unige.dibris.TExpRVMAS.exception.JADEAgentInitializationException;
import it.unige.dibris.TExpRVMAS.exception.JADEContainerInitializationException;
import it.unige.dibris.TExpRVMAS.exception.JavaLibraryPathException;
import it.unige.dibris.TExpRVMAS.exception.NoMinimalMonitoringSafePartitionFoundException;
import it.unige.dibris.TExpRVMAS.exception.NoMonitoringSafePartitionFoundException;
import it.unige.dibris.TExpRVMAS.exception.TraceExpressionNotContractiveException;
import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Class used to run the TraceExpressionRVMAS system.
 * Boots the TraceExpressionsRVMAS system, parsing command line arguments.
 * 
 * @author angeloferrando
 * 
 */

public class Boot {
	
	/** 
	 * Trace expression used by the main to guide the runtime verification process 
	 */
	private TraceExpression tExp;
	private ArrayList<AgentController> agents;
	private Partition<String> partition;
	private boolean decentralize;
	private boolean gui;
	private ArrayList<Condition<String>> conditions;
	private boolean minimal;
	
	/**
	 * Default constructor
	 */
	private Boot(){}

	/**
	 * It executes the TraceExpressionRVMAS
	 * 
	 * @param args to setup the TraceExpressionRVMAS environment
	 * @throws NoMonitoringSafePartitionFoundException if no monitoring safe partition can be found to decentralize the RV process (consistently with the conditions)
	 * @throws NoMinimalMonitoringSafePartitionFoundException if no minimal monitoring safe partition can be found to decentralize the RV process (consistently with the conditions)
	 * @throws IOException if the trace expression file is not found or if there are problems in the generation of the file deriving from the preprocessing phase
	 * @throws StaleProxyException if an error occurred communicating with JADE
	 * @throws TraceExpressionNotContractiveException if the trace expression is not contractive
	 */
	public static void main(String[] args) throws NoMonitoringSafePartitionFoundException, NoMinimalMonitoringSafePartitionFoundException, IOException, StaleProxyException {
		/* Initialize JADE environment */
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		Profile profile = new ProfileImpl();
		AgentContainer container = runtime.createMainContainer( profile );		
		
		/* Parsing arguments */
		Boot boot = Boot.parseArguments(args, container);
		
		if(boot.decentralize || boot.conditions != null){
			Partition<String> partition = null;
			if(boot.partition == null){
				if(boot.minimal){
					List<Partition<String>> mmsPartitions = null;
					if(boot.conditions != null){
						mmsPartitions = boot.tExp.getMinimalMonitoringSafePartitions(boot.conditions);
					} else{
						mmsPartitions = boot.tExp.getMinimalMonitoringSafePartitions(null);
					}
					if(mmsPartitions.size() == 0){
						throw new NoMinimalMonitoringSafePartitionFoundException();
					}
					int random = new Random().nextInt(mmsPartitions.size());
					
					if(mmsPartitions.size() > 0){
						partition = mmsPartitions.get(random);
					}
				} else{
					if(boot.conditions != null){
						partition = boot.tExp.getRandomMonitoringSafePartition(boot.conditions);
					} else{
						partition = boot.tExp.getRandomMonitoringSafePartition(null);
					}
				}
			} else{
				partition = boot.partition;
			}
			
			/*Decentralized monitors creation */
			for(Monitor m : SnifferMonitorFactory.createDecentralizedMonitors(boot.tExp, partition, boot.agents)){
				try{
					AgentController ac = container.acceptNewAgent(m.getMonitorName(), m);
					ac.start();
				} catch(StaleProxyException e){
					throw new JADEContainerInitializationException("Unable to start a monitor container", e);
				}
			}
		} else{
			/* Centralized monitor creation */
			SnifferMonitorFactory.createAndRunCentralizedMonitor(boot.tExp, container, boot.agents);
		}
		
		/* Set (not) visible the Message Logging GUI */
		Monitor.setErrorMessageGUIVisible(boot.gui);

		runAgents(container, boot.agents);
		
		/* Set to close the JVM when JADE environment ends */
		jade.core.Runtime.instance().setCloseVM(true);		
	}
	
	/**
	 * Parse the arguments passed to the Boot main method
	 * @param args are the arguments passed to the Boot class main that we want to parse
	 * @return a new Boot object generated starting from the passed arguments
	 * 
	 * @throws IOException if the trace expression file is not found or if there are problems in the generation of the file deriving from the preprocessing phase
	 * @throws NullPointerException if <code>args</code> is null
	 * @throws IllegalArgumentException if arguments do not follow these structure:
	 * 		-texp path_to_trace_expression_file
	 * 		-agents name1:type1(args);...;nameN:typeN(args)
	 * 		-gui (optional)
	 * 		-decentralized
	 * 		-decentralized [[agent1, agent2], [agent3], [agent4, agent5, agent6], ... , [agentN]] (optional)
	 * 		-conditions together(agent1,agent2);...;split(agent1,agent2);...;number_of_singletons(min,max);...;
	 * 					 constraint_size(min,max);...;number_of_constraints(min,max);... 
	 * 					 [min (max) might be '_' to mean free bound]
	 * 		-minimal
	 * @throws EnvironmentVariableNotDefinedException if SWI_LIB environment variable is not defined
	 * @throws JavaLibraryPathException if an error occurred adding the SWI_LIB folder to the java library path
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 * @throws TraceExpressionNotContractiveException if the trace expression is not contractive
	 */
	private static Boot parseArguments(String[] args, AgentContainer container) throws IOException{
		if(args == null){
			throw new NullPointerException("args must not be null");
		}
		
		String tExpPath = null, agentsString = null;
		
		Boot boot = new Boot();
		
		for(Option opt : parseOptions(args)){
			switch(opt.flag){
				case "-texp":
					tExpPath = opt.opt;
					break;
				case "-agents":
					agentsString = opt.opt;
					break;
				case "-gui":
					boot.gui = true;
					break;
				case "-decentralized":
					boot.decentralize = true;
					if(opt.opt != null){
						boot.partition = new Partition<String>(fromStringToListOfLists(opt.opt));
					}
					break;
				case "-minimal":
					boot.minimal = true;
					break;
				case "-conditions":
					boot.conditions = new ArrayList<>();
					for(String cond : opt.opt.split(";")){
						if(cond.indexOf('(') == -1 || cond.length() < cond.indexOf('(') + 1 || !cond.contains(",") || cond.charAt(cond.length() - 1) != ')'){
							throw new IllegalArgumentException("Syntax error: condition " + cond);
						}
						if(cond.startsWith("together(")){
							String[] agents = cond.substring(cond.indexOf('(') + 1, cond.length() - 1).split(",");
							if(agents.length != 2){
								throw new IllegalArgumentException("together supports only two arguments");
							}
							boot.conditions.add(ConditionsFactory.createMustBeTogetherCondition(agents[0], agents[1]));
						} else if(cond.startsWith("split(")){
							String[] agents = cond.substring(cond.indexOf('(') + 1, cond.length() - 1).split(",");
							if(agents.length != 2){
								throw new IllegalArgumentException("split supports only two arguments");
							}
							boot.conditions.add(ConditionsFactory.createMustBeSplitCondition(agents[0], agents[1]));
						} else if(cond.startsWith("number_of_singletons(")){
							String[] bounds = cond.substring(cond.indexOf('(') + 1, cond.length() - 1).split(",");
							if(bounds.length != 2){
								throw new IllegalArgumentException("number_of_singletons supports only two arguments");
							}
							Integer min = null, max = null;
							try{
								if(!bounds[0].equals("_")){
									min = Integer.valueOf(bounds[0]);
								}
								if(!bounds[1].equals("_")){
									max = Integer.valueOf(bounds[1]);
								}
							} catch(NumberFormatException e){
								throw new IllegalArgumentException("minimum and maximum inside number_of_singletons must be integers", e);
							}
							if(min == null){
								if(max == null){
									throw new IllegalArgumentException("number_of_singletons cannot have both arguments free");
								} else{
									boot.conditions.add(ConditionsFactory.createAtMostNumberSingletonsCondition(max));
								}
							} else{
								if(max == null){
									boot.conditions.add(ConditionsFactory.createAtLeastNumberSingletonsCondition(min));
								} else{
									boot.conditions.add(ConditionsFactory.createNumberSingletonsCondition(min, max));
								}
							}
						} else if(cond.startsWith("constraint_size(")){
							String[] bounds = cond.substring(cond.indexOf('(') + 1, cond.length() - 1).split(",");
							if(bounds.length != 2){
								throw new IllegalArgumentException("constraint_size supports only two arguments");
							}
							Integer min = null, max = null;
							try{
								if(!bounds[0].equals("_")){
									min = Integer.valueOf(bounds[0]);
								}
								if(!bounds[1].equals("_")){
									max = Integer.valueOf(bounds[1]);
								}
							} catch(NumberFormatException e){
								throw new IllegalArgumentException("minimum and maximum inside constraint_size must be integers", e);
							}
							if(min == null){
								if(max == null){
									throw new IllegalArgumentException("constraint_size cannot have both arguments free");
								} else{
									boot.conditions.add(ConditionsFactory.createAtMostNumberAgentsForConstraintCondition(max));
								}
							} else{
								if(max == null){
									boot.conditions.add(ConditionsFactory.createAtLeastNumberAgentsForConstraintCondition(min));
								} else{
									boot.conditions.add(ConditionsFactory.createNumberAgentsForConstraintCondition(min, max));
								}
							}
						} else if(cond.startsWith("number_of_constraints(")){
							String[] bounds = cond.substring(cond.indexOf('(') + 1, cond.length() - 1).split(",");
							if(bounds.length != 2){
								throw new IllegalArgumentException("number_of_constraints supports only two arguments");
							}
							Integer min = null, max = null;
							try{
								if(!bounds[0].equals("_")){
									min = Integer.valueOf(bounds[0]);
								}
								if(!bounds[1].equals("_")){
									max = Integer.valueOf(bounds[1]);
								}
							} catch(NumberFormatException e){
								throw new IllegalArgumentException("minimum and maximum inside number_of_constraints must be integers", e);
							}
							if(min == null){
								if(max == null){
									throw new IllegalArgumentException("number_of_constraints cannot have both arguments free");
								} else{
									boot.conditions.add(ConditionsFactory.createAtMostNumberOfConstraintsCondition(max));
								}
							} else{
								if(max == null){
									boot.conditions.add(ConditionsFactory.createAtLeastNumberOfConstraintsCondition(min));
								} else{
									boot.conditions.add(ConditionsFactory.createNumberOfConstraintsCondition(min, max));
								}
							}
						} else{
							throw new IllegalArgumentException("Condition " + cond + " not recognized");
						}
					}
					break;
				default:
					throw new IllegalArgumentException("Flag " + opt.flag + " not recognized");
			}
		}
		
		/* List of JADE agents */
		boot.agents = new ArrayList<>();
		
		/* The arguments must be at least 2, the trace expression file and one (or more) agent(s) */
		if(tExpPath == null || agentsString == null){
			throw new IllegalArgumentException("You have to pass at least the trace expression file and the list of JADE agents");
		}
		
		/* SWI-Prolog environment initialization (transition system, DecAMon, current trace expression) */
		JPLInitializer.init();
		
		/* Now we check only if the file exists. 
		 * TO-DO To check if the file is syntactically and semantically correct */
		boot.tExp = new TraceExpression(tExpPath);
		
		/* Parsing JADE agents */
		String[] agentsargs = agentsString.split(";");
		if(agentsargs.length == 0){
			throw new IllegalArgumentException("JADE agents must be separated by ';'");
		}
		for(String agent : agentsargs){
			if(!agent.contains(":")){
				throw new IllegalArgumentException("JADE agents must follow the syntax name:type(arg1,...,argN)");
			}
			String name = agent.split(":")[0];
			String constructor = agent.split(":")[1];
			if(!constructor.contains("(") || !constructor.contains(")")){
				throw new IllegalArgumentException("JADE agents must follow the syntax: name:type(arg1,...,argN)");
			}
			String type = constructor.split("\\(")[0];
			Object[] arguments = constructor.split("\\(")[1].replace(")", "").split(",");
			try {
				boot.agents.add(container.createNewAgent(name, type, arguments));
			} catch (StaleProxyException e) {
				throw new JADEAgentInitializationException("Unable to create the agent " + name + " of type " + type, e);
			}
		}
		
		return boot;
	}
	
	private static List<List<String>> fromStringToListOfLists(String partition){
		if(partition == null){
			return new ArrayList<>();
		}
		if(partition.charAt(0) != '[' || partition.charAt(partition.length() - 1) != ']'){
			throw new IllegalArgumentException("partition must follow the syntax: [[elem1, elem2], [elem3], ...]");
		}
		partition = partition.substring(1, partition.length() - 1).replace("],", "]");
		List<List<String>> partitionList = new ArrayList<>();
		for(String constraint : partition.split("]")){
			List<String> constraintList = new ArrayList<>();
			partitionList.add(constraintList);
			if(constraint.length() == 0 || constraint.charAt(0) != '['){
				throw new IllegalArgumentException("partition must follow the syntax: [[elem1, elem2], [elem3], ...]");
			}
			for(String elem : constraint.replace("[", "").split(",")){
				constraintList.add(elem);
			}
		}
		return partitionList;
	}
	
	private static class Option{
		 private String flag;
		 private String opt;
	     public Option(String flag, String opt) { this.flag = flag; this.opt = opt; }
	}
	
	private static List<Option> parseOptions(String[] args){
	    List<String> argsList = new ArrayList<String>();  
	    List<Option> optsList = new ArrayList<Option>();

	    for (int i = 0; i < args.length; i++) {
	        switch (args[i].charAt(0)) {
	        case '-':
	            if (args[i].length() < 2)
	                throw new IllegalArgumentException("Not a valid argument: "+args[i]);
	            if (!(args[i].charAt(1) == '-')) {
	                //if (args.length-1 == i)
	                	//break;
	                  //  throw new IllegalArgumentException("Expected arg after: "+args[i]);
	                // -opt
	                if(args.length-1 == i || args[i+1].contains("-")){
	                	optsList.add(new Option(args[i], null));
	                } else{
	                	optsList.add(new Option(args[i], args[i+1]));
	                	i++;
	                }
	            }
	            break;
	        default:
	            // arg
	            argsList.add(args[i]);
	            break;
	        }
	    }
	    return optsList;
	}
	
//	public static void runMonitors(AgentContainer container, Partition<String> p){
//		for(Set<String> constraint : p){
//			/* Sniffer creation */
//			Sniffer s = new Sniffer();
//			String constraintAux = constraint.toString().replace("[", "").replace("]", "").replace(",", "_").replace(" ", "");
//			s.setArguments(new String[]{
//					"sniffer" + constraintAux + ".txt",
//					constraint.toString()
//			});
//			try{
//				AgentController ac = container.acceptNewAgent("sniffer" + constraintAux, s);
//				ac.start();
//			} catch(StaleProxyException e){
//				throw new JADEContainerInitializationException("Unable to start an agent container", e);
//			}
//		}
//	}
	
	/**
	 * Run all the JADE agents of the MAS that we are monitoring
	 * @param container is the AgentContainer where the agents are executed
	 * @param agents is the set of agents we want to execute on the container
	 * 
	 * @throws JADEAgentInitializationException if the Agent cannot be executed on the container
	 */
	private static void runAgents(AgentContainer container, List<AgentController> agents){
		try{
			for(AgentController agent : agents){
				agent.start();
			}
		} catch(StaleProxyException e){
			throw new JADEAgentInitializationException("Unable to start an agent container", e);
		}
	}

}
