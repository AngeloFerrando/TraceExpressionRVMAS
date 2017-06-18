package it.unige.dibris.TExpRVMAS.core.protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;

import it.unige.dibris.TExpRVMAS.core.decentralized.Condition;
import it.unige.dibris.TExpRVMAS.core.decentralized.Partition;
import it.unige.dibris.TExpRVMAS.exception.NoMonitoringSafePartitionFoundException;
import it.unige.dibris.TExpRVMAS.exception.TraceExpressionFileFormatException;
import it.unige.dibris.TExpRVMAS.exception.TraceExpressionNeitherAtomicNorAsyncEventTypesException;
import it.unige.dibris.TExpRVMAS.exception.TraceExpressionNotContractiveException;
import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;

/**
 * Class representing the Trace Expression used to model the protocols which guide the
 * runtime verification of the MAS.
 *
 * @author angeloferrando
 *
 */
public class TraceExpression {

	/**
	 * File containing the trace expression definition
	 */
	//private File tExpFile;

	private String protocolName;

	/**
	 * Constructor of the TraceExpression class
	 * @param pathToFile is the path to the file containing the trace expression definition
	 *
	 * @throws IOException if the file is not found or if there are problems in the generation of the file deriving from the preprocessing phase
	 * @throws NullPointerException if pathToFile is null
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 * @throws TraceExpressionNotContractiveException if the trace expression is not contractive
	 * @throws TraceExpressionNeitherAtomicNorAsyncEventTypesException if the trace expression does not contain only atomic event types (or only async event types)
	 */
	public TraceExpression(String pathToFile) throws IOException{
		if(pathToFile == null){
			throw new NullPointerException("pathToFile must not be null");
		}
		File tExpFile = new File(pathToFile);
		if(!tExpFile.exists()){
		    throw new FileNotFoundException(pathToFile + " file not found");
		}

		BufferedReader tExpFileReader = new BufferedReader(new FileReader(tExpFile));
		Optional<String> traceExpressionLine = tExpFileReader.lines().filter(s -> s.startsWith("trace_expression(")).findFirst();
		if(!traceExpressionLine.isPresent() || !traceExpressionLine.get().matches("trace_expression((.+)(\\s*),(\\s*)(.+))")){
			tExpFileReader.close();
			throw new TraceExpressionFileFormatException("The trace expression file must contain the trace_expression(protocol_name, T) predicate");
		}

		String aux = traceExpressionLine.get();
		tExpFileReader.close();
		protocolName = aux.substring(aux.indexOf('(') + 1, aux.indexOf(','));
		FileWriter fw = new FileWriter(pathToFile + ".tmp");
		try{
			tExpFileReader = new BufferedReader(new FileReader(tExpFile));
			Iterator<String> it = tExpFileReader.lines().map(s -> {
				return s.contains("match(") ? s.replace("match(", "match(" + protocolName + ", ") : s;
			}).iterator();

			while(it.hasNext()){
				fw.write(it.next() + "\n");
			}
		} catch(IOException e){
			throw e;
		} finally{
			fw.close();
			tExpFileReader.close();
		}

		load(pathToFile + ".tmp");
	}

	/**
	 * Load the trace expression inside the SWI-Prolog environment.
	 *
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 * @throws TraceExpressionNotContractiveException if the trace expression is not contractive
	 * @throws TraceExpressionNeitherAtomicNorAsyncEventTypesException if the trace expression does not contain only atomic event types (or only async event types)
	 */
	public void load(String tExpFilePath){
		//JPLInitializer.createAndCheck("retractall(match(" + protocolName + ", _, _))");
		JPLInitializer.createAndCheck("retractall(trace_expression(" + protocolName + ", _))");
		JPLInitializer.createAndCheck("consult", new Atom(tExpFilePath));
		if(!isContractive()){
			throw new TraceExpressionNotContractiveException();
		}
		if(!(areEventsAtomic() || areEventsAsync())){
			throw new TraceExpressionNeitherAtomicNorAsyncEventTypesException();
		}
	}

	/**
	 * Get the set of Minimal Monitoring Safe partitions (MMS)
	 * @param conditions that must be satisfied by the partitions returned
	 * @return the set of Minimal Monitoring Safe partitions satisfying the conditions
	 *
	 * @throws NullPointerException if conditions is null
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 */
	/*@SuppressWarnings("unchecked")
	public List<Partition<String>> getMinimalMonitoringSafePartitions(Condition<String>... conditions){
		if(conditions == null){
			throw new NullPointerException("conditions must not be null");
		}
		List<Partition<String>> mmsPartitions = new ArrayList<>();
		Query query = JPLInitializer.createAndCheck("decAMonJADE(MMsPartitions)");
		Compound mmsPartitionsTerm = (Compound) query.oneSolution().get("MMsPartitions");
		for(Term t0 : JPLInitializer.fromCompoundToList(mmsPartitionsTerm)){
			Partition<String> partition = Partition.extractOnePartitionFromTerm(t0);
			boolean toAdd = true;
			if(conditions != null){
				for(Condition<String> cond : conditions){
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
	}*/

	/**
	 * Get the set of Minimal Monitoring Safe partitions (MMS)
	 * @param conditions that must be satisfied by the partitions returned
	 * @return the set of Minimal Monitoring Safe partitions satisfying the conditions
	 *
	 * @throws NullPointerException if conditions is null
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 */
	public List<Partition<String>> getMinimalMonitoringSafePartitions(List<Condition<String>> conditions){
		/*if(conditions == null){
			throw new NullPointerException("conditions must not be null");
		}*/
		List<Partition<String>> mmsPartitions = new ArrayList<>();
		Query query = JPLInitializer.createAndCheck("decAMonJADE(MMsPartitions, " + protocolName + ")");
		Term mmsPartitionsTerm = query.oneSolution().get("MMsPartitions");
		for(Term t0 : JPLInitializer.fromCompoundToList(mmsPartitionsTerm)){
			Partition<String> partition = Partition.extractOnePartitionFromTerm(t0);
			boolean toAdd = true;
			if(conditions != null){
				for(Condition<String> cond : conditions){
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

	/**
	 * Check if the partition passed as argument is Monitoring Safe for the trace expression
	 * @param partition to check if is Monitoring Safe
	 * @return true if the partition is Monitoring Safe, false otherwise
	 *
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 */
	public boolean isMonitoringSafe(Partition<String> partition){
		Query query = new Query("is_monitoring_safe(" + partition + ", " + protocolName + ")");
		boolean res = query.hasSolution();
		query.close();
		return res;
	}

	/**
	 * Check if the trace expression is contractive
	 * @return true if the trace expression is contractive, false otherwise
	 *
	 * @throws PrologException if an error occurred during the communication with SWI-Prolog
	 */
	private boolean isContractive(){
		Query query = new Query("is_contractive_aux(" + protocolName + ")");
		boolean res = query.hasSolution();
		query.close();
		return res;
	}

	/**
	 * Get a random Monitoring Safe partition
	 * @param conditions that must be satisfied by the partition returned
	 * @return the monitoring safe partition selected randomly
	 *
	 * @throws NoMonitoringSafePartitionFoundException if no monitoring safe partition can be retrieved
	 * @throws NullPointerException if conditions is null
	 */
	/*@SuppressWarnings("unchecked")
	public Partition<String> getRandomMonitoringSafePartition(Condition<String>... conditions) throws NoMonitoringSafePartitionFoundException{
		if(conditions == null){
			throw new NullPointerException("conditions must not be null");
		}
		int random = new Random().nextInt();
		List<Partition<String>> msPartitions = new ArrayList<>();
		int count = 0;
		for(Partition<String> p : getMonitoringSafePartitions(conditions)){
			if(count == random){
				return p;
			}
			msPartitions.add(p);
		}
		if(msPartitions.size() > 0){
			return msPartitions.get(random % msPartitions.size());
		} else{
			throw new NoMonitoringSafePartitionFoundException();
		}
	}*/

	/**
	 * Get a random Monitoring Safe partition
	 * @param conditions that must be satisfied by the partition returned
	 * @return the monitoring safe partition selected randomly
	 *
	 * @throws NoMonitoringSafePartitionFoundException if no monitoring safe partition can be retrieved
	 */
	public Partition<String> getRandomMonitoringSafePartition(List<Condition<String>> conditions) throws NoMonitoringSafePartitionFoundException{
		/*if(conditions == null){
			throw new NullPointerException("conditions must not be null");
		}*/
		int random = new Random().nextInt();
		List<Partition<String>> msPartitions = new ArrayList<>();
		int count = 0;
		for(Partition<String> p : getMonitoringSafePartitions(conditions)){
			if(count == random){
				return p;
			}
			msPartitions.add(p);
		}
		if(msPartitions.size() > 0){
			return msPartitions.get(random % msPartitions.size());
		} else{
			throw new NoMonitoringSafePartitionFoundException();
		}
	}

	/**
	 * Get the first Monitoring Safe partition generated
	 * @param conditions that must be satisfied by the partition returned
	 * @return the monitoring safe partition selected
	 *
	 * @throws NoMonitoringSafePartitionFoundException if no monitoring safe partition can be retrieved
	 * @throws NullPointerException if conditions is null
	 */
	/*@SuppressWarnings("unchecked")
	public Partition<String> getFirstMonitoringSafePartition(Condition<String>... conditions) throws NoMonitoringSafePartitionFoundException{
		if(conditions == null){
			throw new NullPointerException("conditions must not be null");
		}
		Iterator<Partition<String>> itPartitions = getMonitoringSafePartitions(conditions).iterator();
		if(itPartitions.hasNext()){
			return itPartitions.next();
		} else{
			throw new NoMonitoringSafePartitionFoundException();
		}
	}*/

	/**
	 * Get the first Monitoring Safe partition generated
	 * @param conditions that must be satisfied by the partition returned
	 * @return the monitoring safe partition selected
	 *
	 * @throws NoMonitoringSafePartitionFoundException if no monitoring safe partition can be retrieved
	 */
	public Partition<String> getFirstMonitoringSafePartition(List<Condition<String>> conditions) throws NoMonitoringSafePartitionFoundException{
		/*if(conditions == null){
			throw new NullPointerException("conditions must not be null");
		}*/
		Iterator<Partition<String>> itPartitions = getMonitoringSafePartitions(conditions).iterator();
		if(itPartitions.hasNext()){
			return itPartitions.next();
		} else{
			throw new NoMonitoringSafePartitionFoundException();
		}
	}

	/**
	 * Get the set of Monitoring Safe partitions (MS)
	 * @param conditions that must be satisfied by the partitions returned
	 * @return the set of Monitoring Safe partitions satisfying the conditions
	 *
	 * @throws NullPointerException if conditions is null
	 */
	/*@SuppressWarnings("unchecked")
	public Iterable<Partition<String>> getMonitoringSafePartitions(Condition<String>... conditions){
		if(conditions == null){
			throw new NullPointerException("conditions must not be null");
		}
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
									partitionAux = Partition.extractOnePartitionFromTerm(partitionTerm);
									if(conditions != null){
										for(Condition<String> cond : conditions){
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
									lastPartition = Partition.extractOnePartitionFromTerm(partitionTerm);
									if(conditions != null){
										for(Condition<String> cond : conditions){
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
	}*/

	/**
	 * Get the set of Monitoring Safe partitions (MS)
	 * @param conditions that must be satisfied by the partitions returned
	 * @return the set of Monitoring Safe partitions satisfying the conditions
	 *
	 */
	public Iterable<Partition<String>> getMonitoringSafePartitions(List<Condition<String>> conditions){
		/*if(conditions == null){
			throw new NullPointerException("conditions must not be null");
		}*/

		Query query = new Query("decOne(MSPartition, " + protocolName + ")");
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
									partitionAux = Partition.extractOnePartitionFromTerm(partitionTerm);
									if(conditions != null){
										for(Condition<String> cond : conditions){
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
									lastPartition = Partition.extractOnePartitionFromTerm(partitionTerm);
									if(conditions != null){
										for(Condition<String> cond : conditions){
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

	/**
	 * @return the protocolName
	 */
	public String getProtocolName() {
		return protocolName;
	}

	/**
	 * Get the information about the events atomicity
	 * (The events must be either all atomic or all async)
	 * @return true if all events are atomic, false otherwise
	 */
	public boolean areEventsAtomic() {
		Query query1 = new Query("are_all_events_atomic_aux(" + protocolName + ")");
		boolean res1 = query1.hasSolution();
		Query query2 = new Query("are_all_events_async_aux(" + protocolName + ")");
		boolean res2 = query2.hasSolution();
		query1.close();
		query2.close();
		return res1 && !res2;
	}

	/**
	 * Get the information about the events concurrency
	 * (The events must be either all atomic or all async)
	 * @return true if all events are async, false otherwise
	 */
	public boolean areEventsAsync() {
		Query query1 = new Query("are_all_events_atomic_aux(" + protocolName + ")");
		boolean res1 = query1.hasSolution();
		Query query2 = new Query("are_all_events_async_aux(" + protocolName + ")");
		boolean res2 = query2.hasSolution();
		query1.close();
		query2.close();
		return !res1 && res2;
	}

}
