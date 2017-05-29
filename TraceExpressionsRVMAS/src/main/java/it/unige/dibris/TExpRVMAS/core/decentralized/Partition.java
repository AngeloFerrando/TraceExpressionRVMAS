package it.unige.dibris.TExpRVMAS.core.decentralized;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jpl7.Term;

import it.unige.dibris.TExpRVMAS.utils.JPL.JPLInitializer;

/**
 * Class representing a partition of agents used to obtain the decentralized runtime verification of a JADE MAS
 * @author angeloferrando
 *
 */
public class Partition<T> implements Iterable<Set<T>>{
	/**
	 * The constraints contained inside the partition
	 */
	private Set<Set<T>> constraints = new HashSet<>();
	
	/**
	 * Default constructor (empty partition)
	 */
	public Partition(){}
	
	/**
	 * Constructor which set the partition starting from a given list of lists
	 * @param partition is the partition represented as list of lists
	 * 
	 * @throws NullPointerException if partition is null
	 */
	public Partition(List<List<T>> partition){
		if(partition == null){
			throw new NullPointerException("partition must not be null");
		}
		for(List<? extends T> constraint : partition){
			if(constraint != null && constraint.size() > 0){
				HashSet<T> newConstraint = new HashSet<>();
				for(T elem: constraint){
					newConstraint.add(elem);
				}
				constraints.add(newConstraint);
			}
		}
	}
	
	/**
	 * Add a new element to the partition (inside a new constraint)
	 * @param elem is the element to add
	 * 
	 * @throws NullPointerException if elem is null
	 */
	public <E extends T> void addElement(E elem){
		if(elem == null){
			throw new NullPointerException("elem must be not null");
		}
		for(Set<T> set : constraints){
			if(set.contains(elem)){
				return;
			}
		}
		Set<T> newConstraint = new HashSet<T>();
		newConstraint.add(elem);
		constraints.add(newConstraint);
	}
	
	/**
	 * Get the number of singletons constraint contained in the partition
	 * @return the number of singletons found
	 */
	public int getNumberSingletons(){
		return getNumberConstraintsSize(1);
	}
	
	/**
	 * Get the number of total constraints contained in the partition
	 * @return the number of constraints found
	 */
	public int getNumberConstraints() {
		return constraints.size();
	}
	
	/**
	 * Get the number of constraints inside the partition of a particular size
	 * @param size is the chosen size
	 * @return the number of constraints of this <code>size</code>
	 */
	public int getNumberConstraintsSize(int size){
		int count = 0;
		for(Set<T> set : constraints){
			if(set.size() == size){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Get the number of constraints containing at least <code>size</code> elements
	 * @param size is the minimum number of elements to check
	 * @return the number of constraints with at least <code>size</code> elements 
	 */
	public int getNumberConstraintsAtLeastSize(int size){
		int count = 0;
		for(Set<T> set : constraints){
			if(set.size() >= size){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Get the number of constraints containing at most <code>size</code> elements
	 * @param size is the maximum number of elements to check
	 * @return the number of constraints with at most <code>size</code> elements
	 */
	public int getNumberConstraintsAtMostSize(int size){
		int count = 0;
		for(Set<T> set : constraints){
			if(set.size() <= size){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Get the number of constraints containing at least <code>minSize</code> and at most <code>maxSize</code> elements
	 * @param minSize is the minimum number of elements in the constraints
	 * @param maxSize is the maximum number of elements in the constraints
	 * @return the number of constraint with at least <code>minSize</code> and at most <code>maxSize</code> elements
	 * 
	 * @throws IllegalArgumentException if <code>minSize</code> > <code>maxSize</code>
	 */
	public int getNumberConstraintsBetweenSize(int minSize, int maxSize){
		if(minSize > maxSize){
			throw new IllegalArgumentException("minSize must be less than maxSize");
		}
		int count = 0;
		for(Set<T> set : constraints){
			if(set.size() >= minSize && set.size() <= maxSize){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Check if the two set of elements are in the same constraint
	 * @param elem1 first set of elements
	 * @param elem2 second set of elements
	 * @return true if the two sets are in the same constraint, false otherwise
	 */
	public boolean areMonitoredTogether(Set<? extends T> elem1, Set<? extends T> elem2){
		if(elem1 == null || elem2 == null){
			return false;
		}
		for(Set<T> set : constraints){
			if(set.containsAll(elem1) && set.containsAll(elem2)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add a new constraint inside the partition.
	 * If one of (or both) element belongs already to a constraint it will be done a fusion of constraints.
	 * @param elem1 first element
	 * @param elem2 second element
	 * 
	 * @throws NullPointerException if <code>elem1</code> or <code>elem2</code> are null
	 */
	public <E extends T> void addConstraint(E elem1, E elem2){
		if(elem1 == null || elem2 == null){
			throw new NullPointerException("elem1 and elem2 must not be null");
		}
		Set<T> set1 = null;
		Set<T> set2 = null;
		for(Set<T> set : constraints){
			if(set1 != null && set2 != null){
				break;
			}
			if(set.contains(elem1)){
				set1 = set;
			}
			if(set.contains(elem2)){
				set2 = set;
			}
		}
		if(set1 != null){
			if(set2 != null){
				for(T elem : set2){
					set1.add(elem);
				}
				constraints.remove(set2);
			} else{
				set1.add(elem2);
			}
		} else{
			if(set2 != null){
				set2.add(elem1);
			}else{
				Set<T> newConstraint = new HashSet<>();
				newConstraint.add(elem1);
				newConstraint.add(elem2);
				constraints.add(newConstraint);
			}
		}
	}
	
	/**
	 * If <code>elem</code> belongs to the partition, separate it from the other and add it to a single new constraint.
	 * @param elem
	 * 
	 * @throws NullPointerException if <code>elem</code> is null 
	 */
	public <E extends T> void makeIndependent(E elem){
		if(elem == null){
			throw new NullPointerException("elem must not be null");
		}
		for(Set<T> set : constraints){
			if(set.contains(elem)){
				if(set.size() > 1){
					set.remove(elem);
					Set<T> newConstraint = new HashSet<>();
					newConstraint.add(elem);
					constraints.add(newConstraint);
					break;
				}
			}
		}
	}
	
	/**
	 * Convert a term in the corresponding partition object
	 * @param term representing the partition to convert
	 * @return the corresponding partition
	 * 
	 * @throws NullPointerException if <code>term</code> is null
	 */
	public static Partition<String> extractOnePartitionFromTerm(Term term){
		if(term == null){
			throw new NullPointerException("term must not be null");
		}
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
	
	@Override
	public String toString(){
		String res = "[";
		for(Set<T> constraint : constraints){
			res += "[";
			for(T elem : constraint){
				res += elem + ",";
			}
			res = res.substring(0, res.length() - 1) + "],";
		}
		return res.substring(0, res.length() - 1) + "]";
	}

	@Override
	public Iterator<Set<T>> iterator() {
		return constraints.iterator();
	}
	
}
