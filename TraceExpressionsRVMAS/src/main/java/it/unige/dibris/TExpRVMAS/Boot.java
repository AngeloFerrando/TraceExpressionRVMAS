package it.unige.dibris.TExpRVMAS;

import java.lang.reflect.Field;
import java.util.Arrays;

import it.unige.dibris.Exception.EnvironmentVariableNotDefined;
import it.unige.dibris.Exception.JavaLibraryPathException;

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
	 * @throws Exception
	 */
	public static void main(String[] args) {
		String swipl = System.getenv("SWI_PROLOG_HOME");
		
		if(swipl == null){
			throw new EnvironmentVariableNotDefined("SWI_PROLOG_HOME environment variable not defined");
		}
		
		/* We need to add the SWI-Prolog Home to the path in order to use the JPL library */
		try{
			addLibraryPath(swipl);
		} catch(Exception e){
			throw new JavaLibraryPathException("An error occured during the user path retrieval information process", e);
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
