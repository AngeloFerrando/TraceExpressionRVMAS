package it.unige.dibris.TExpRVMAS.core.decentralized;

/**
 * Condition that must be satisfied by a partition
 * 
 * @author angeloferrando
 *
 */
public interface Condition<T> {
	/**
	 * Check is the partition satisfies the condition
	 * @param partition to check
	 * @return true if the partition satisfies the condition, false otherwise
	 */
	public boolean isConsistent(Partition<T> partition);
}
