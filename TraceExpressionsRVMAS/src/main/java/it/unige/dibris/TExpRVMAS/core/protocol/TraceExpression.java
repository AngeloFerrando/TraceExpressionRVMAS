package it.unige.dibris.TExpRVMAS.core.protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;

import it.unige.dibris.TExpRVMAS.core.decentralized.Condition;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;

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
		JPLInitializer.createAndCheck("retractall(match(_, _))");
		JPLInitializer.createAndCheck("retractall(trace_expression(_))");
		JPLInitializer.createAndCheck("consult", new Atom(tExpFile.getAbsolutePath()));
	}
	
	public List<Partition<String>> getMinimalMonitoringSafePartitions(List<Condition> conditions){
		List<Partition<String>> mmsPartitions = new ArrayList<>();
		Query query = new Query("decAMonJADE(MMsPartitions)");
		Compound mmsPartitionsTerm = (Compound) query.oneSolution().get("MMsPartitions");
		for(Term t0 : JPLInitializer.fromCompoundToList(mmsPartitionsTerm)){
			Partition<String> partition = extractOnePartitionFromTerm(t0);
			boolean toAdd = true;
			if(conditions != null){
				for(Condition cond : conditions){
					if(!cond.isConsistent(partition)){
						toAdd = false;
						break;
					}
				}
			}
			if(toAdd){
				mmsPartitions.add(partition);
			}
		}
		query.close();
		return mmsPartitions;
	}
	
	private Partition<String> extractOnePartitionFromTerm(Term term){
		Partition<String> partition = new Partition<>();
		for(Term t1 : JPLInitializer.fromCompoundToList(term)){
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
		return partition;
	}
	
	public boolean isMonitoringSafe(Partition<String> partition){
		Query query = new Query("is_monitoring_safe(Partition)");
		boolean res = query.hasSolution();
		query.close();
		return res;
	}
	
	public Iterable<Partition<String>> getMonitoringSafePartitions(List<Condition> conditions){
		Query query = new Query("decOne(MSPartition)");
		return new Iterable<Partition<String>>() {
			
			@Override
			public Iterator<Partition<String>> iterator() {
				return new Iterator<Partition<String>>() {
					
					Partition<String> lastPartition;
					boolean end = false;
					
					@Override
					public Partition<String> next() {
						if(end){
							throw new NoSuchElementException();
						}
						if(lastPartition != null){
							Partition<String> partitionAux = lastPartition;
							lastPartition = null;
							return partitionAux;
						} else{
							Partition<String> partitionAux = null;
							boolean repeat;
							do{
								repeat = false;
								if(query.hasMoreSolutions()){
									Compound partitionTerm = (Compound) query.nextSolution().get("MSPartition");
									partitionAux = extractOnePartitionFromTerm(partitionTerm);
									if(conditions != null){
										for(Condition cond : conditions){
											if(!cond.isConsistent(lastPartition)){
												repeat = true;
												break;
											}
										}
									}
								} else{
									query.close();
									end = true;
									throw new NoSuchElementException();
								}
							} while(repeat);
							return partitionAux;
						}
					}
					
					@Override
					public boolean hasNext() {
						if(end){
							return false;
						}
						if(lastPartition != null){
							return true;
						} else{
							boolean repeat;
							do{
								repeat = false;
								if(query.hasMoreSolutions()){
									Compound partitionTerm = (Compound) query.nextSolution().get("MSPartition");
									lastPartition = extractOnePartitionFromTerm(partitionTerm);
									if(conditions != null){
										for(Condition cond : conditions){
											if(!cond.isConsistent(lastPartition)){
												repeat = true;
												break;
											}
										}
									}
								} else{
									query.close();
									end = true;
									return false;
								}
							} while(repeat);
							return true;
						}
					}
				};
			}
		};
	}
	
	
}
