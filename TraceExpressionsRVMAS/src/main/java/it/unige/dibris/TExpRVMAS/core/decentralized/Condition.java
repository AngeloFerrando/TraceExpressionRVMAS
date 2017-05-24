package it.unige.dibris.TExpRVMAS.core.decentralized;

public interface Condition {
	public boolean isConsistent(Partition<?> p);
}
