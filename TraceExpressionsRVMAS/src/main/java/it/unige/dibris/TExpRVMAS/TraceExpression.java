package it.unige.dibris.TExpRVMAS;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.jpl7.Compound;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;

import it.unige.dibris.utils.JPL.JPLInitializer;

/**
 * Class representing the Trace Expression used to model the protocols which guide the 
 * runtime verification of the MAS. 
 * 
 * @author angeloferrando
 *
 */
public class TraceExpression {
	
	private File tExpFile;

	public TraceExpression(String pathToFile) throws FileNotFoundException{
		tExpFile = new File(pathToFile);
		if(!tExpFile.exists()){ 
		    throw new FileNotFoundException(pathToFile + " file not found");
		}
		load();
	}
	
	/**
	 * Load the trace expression inside the SWI-Prolog environment.
	 * @throws PrologException
	 */
	private void load(){
		JPLInitializer.loadTraceExpression(tExpFile.getAbsolutePath());
	}
	
	public List<Partition<String>> distributableOnMMS(List<Condition> conditions){
		List<Partition<String>> mmsPartitions = new ArrayList<>();
		Query query = new Query("decAMonJADE(MMsPartitions)");
		Term mmsPartitionsTerm = (Compound) query.oneSolution().get("MMsPartitions");
		for(Term t0 : JPLInitializer.fromCompoundToList(mmsPartitionsTerm)){
			Partition<String> partition = new Partition<>();
			for(Term t1 : JPLInitializer.fromCompoundToList(t0)){
				String firstAgent = null;
				for(Term t2 : JPLInitializer.fromCompoundToList(t1)){
					if(firstAgent == null){
						firstAgent = t2.toString();
						partition.addElement(t2.toString());
					} else{
						partition.addConstraint(firstAgent, t2.toString());
					}
				}
			}
			boolean toAdd = true;
			for(Condition cond : conditions){
				if(!cond.isConsistent(partition)){
					toAdd = false;
					break;
				}
			}
			if(toAdd){
				mmsPartitions.add(partition);
			}
		}
		query.close();
		return mmsPartitions;
	}
	
}
