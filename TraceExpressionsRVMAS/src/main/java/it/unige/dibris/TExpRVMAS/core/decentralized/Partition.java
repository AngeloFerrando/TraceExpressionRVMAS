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
	private Set<Set<T>> constraints = new HashSet<>();
	
	public Partition(){}
	
	public Partition(List<List<? extends T>> partition){
		if(partition == null) return;
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
	
	public <E extends T> void addElement(E elem){
		for(Set<T> set : constraints){
			if(set.contains(elem)){
				return;
			}
		}
		Set<T> newConstraint = new HashSet<T>();
		newConstraint.add(elem);
		constraints.add(newConstraint);
	}
	
	public int getNumberSingletons(){
		return getNumberConstraintsSize(1);
	}
	

	public int getNumberConstraints() {
		return constraints.size();
	}
	
	public int getNumberConstraintsSize(int size){
		int count = 0;
		for(Set<T> set : constraints){
			if(set.size() == size){
				count++;
			}
		}
		return count;
	}
	
	public int getNumberConstraintsAtLeastSize(int size){
		int count = 0;
		for(Set<T> set : constraints){
			if(set.size() >= size){
				count++;
			}
		}
		return count;
	}
	
	public int getNumberConstraintsAtMostSize(int size){
		int count = 0;
		for(Set<T> set : constraints){
			if(set.size() <= size){
				count++;
			}
		}
		return count;
	}
	
	public int getNumberConstraintsBetweenSize(int minSize, int maxSize){
		int count = 0;
		for(Set<T> set : constraints){
			if(set.size() >= minSize && set.size() <= maxSize){
				count++;
			}
		}
		return count;
	}
	
	public boolean areMonitoredTogether(Set<? extends T> elem1, Set<? extends T> elem2){
		for(Set<T> set : constraints){
			if(set.containsAll(elem1) && set.containsAll(elem2)){
				return true;
			}
		}
		return false;
	}
	
	public <E extends T> void addConstraint(E elem1, E elem2){
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
	
	public <E extends T> void makeIndependent(E elem){
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
	 */
	public static Partition<String> extractOnePartitionFromTerm(Term term){
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
