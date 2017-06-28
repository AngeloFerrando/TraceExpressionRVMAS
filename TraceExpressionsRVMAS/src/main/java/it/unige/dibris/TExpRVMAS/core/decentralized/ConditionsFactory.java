package it.unige.dibris.TExpRVMAS.core.decentralized;

import java.util.HashSet;
import java.util.Set;

/**
 * Factory to create the conditions that will be used to filter the partitions during the decentralization process
 *
 * @author angeloferrando
 *
 */
public class ConditionsFactory {

	/**
	 * Create a condition on the number of singletons present inside the partition
	 * @param minNumberSingletons is the minimum number of singletons allowed
	 * @param maxNumberSingletons is the maximum number of singletons allowed
	 * @return the corresponding condition
	 *
	 * @throws IllegalArgumentException if <code>minNumberSingletons</code> > <code>maxNumberSingletons</code>
	 */
	public static Condition<String> createNumberSingletonsCondition(int minNumberSingletons, int maxNumberSingletons){
		if(minNumberSingletons > maxNumberSingletons){
			throw new IllegalArgumentException("the minimum number of singletons must be less than the maximum number");
		}
		return p -> {
			int singletons = p.getNumberSingletons();
			return singletons >= minNumberSingletons && singletons <= maxNumberSingletons;
		};
	}

	/**
	 * Create a condition on the minimum number of singletons present inside the partition
	 * @param minNumberSingletons is the minimum number of singletons allowed
	 * @return the corresponding condition
	 */
	public static Condition<String> createAtLeastNumberSingletonsCondition(int minNumberSingletons){
		return p -> {
			int singletons = p.getNumberSingletons();
			return singletons >= minNumberSingletons;
		};
	}

	/**
	 * Create a condition on the maximum number of singletons present inside the partition
	 * @param maxNumberSingletons is the maximum number of singletons allowed
	 * @return the corresponding condition
	 */
	public static Condition<String> createAtMostNumberSingletonsCondition(int maxNumberSingletons){
		return p -> {
			int singletons = p.getNumberSingletons();
			return singletons <= maxNumberSingletons;
		};
	}

	/**
	 * Create a condition on the number of agents for single constraint
	 * @param minNumberAgentsForConstraint is the minimum number of agents for single constraint allowed
	 * @param maxNumberAgentsForConstraint is the maximum number of agents for single constraint allowed
	 * @return the corresponding condition
	 *
	 * @throws IllegalArgumentException if <code>minNumberAgentsForConstraint</code> > <code>maxNumberAgentsForConstraint</code>
	 */
	public static Condition<String> createNumberAgentsForConstraintCondition(int minNumberAgentsForConstraint, int maxNumberAgentsForConstraint){
		if(minNumberAgentsForConstraint > maxNumberAgentsForConstraint){
			throw new IllegalArgumentException("the minimum number of agents for constraint must be less than the maximum number");
		}
		return p -> {
			return p.getNumberConstraintsBetweenSize(minNumberAgentsForConstraint, maxNumberAgentsForConstraint) == p.getNumberConstraints();
		};
	}

	/**
	 * Create a condition on the minimum number of agents for single constraint
	 * @param minNumberAgentsForConstraint is the minimum number of agents for single constraint allowed
	 * @return the corresponding condition
	 */
	public static Condition<String> createAtLeastNumberAgentsForConstraintCondition(int minNumberAgentsForConstraint){
		return p -> {
			return p.getNumberConstraintsAtLeastSize(minNumberAgentsForConstraint) == p.getNumberConstraints();
		};
	}

	/**
	 * Create a condition on the maximum number of agents for single constraint
	 * @param maxNumberAgentsForConstraint is the maximum number of agents for single constraint allowed
	 * @return the corresponding condition
	 */
	public static Condition<String> createAtMostNumberAgentsForConstraintCondition(int maxNumberAgentsForConstraint){
		return p -> {
			return p.getNumberConstraintsAtMostSize(maxNumberAgentsForConstraint) == p.getNumberConstraints();
		};
	}

	/**
	 * Create a condition on number of constraints present inside the partition
	 * @param minNumberCostraints is the minimum number of constraints inside the partition allowed
	 * @param maxNumberCostraints is the maximum number of constraints inside the partition allowed
	 * @return the corresponding condition
	 *
	 * @throws IllegalArgumentException if <code>minNumberCostraints</code> > <code>maxNumberCostraints</code>
	 */
	public static Condition<String> createNumberOfConstraintsCondition(int minNumberCostraints, int maxNumberCostraints){
		if(minNumberCostraints > maxNumberCostraints){
			throw new IllegalArgumentException("the minimum number constraints must be less than the maximum number");
		}
		return p -> {
			int count = p.getNumberConstraints();
			return count >= minNumberCostraints && count <= maxNumberCostraints;
		};
	}

	/**
	 * Create a condition on the minimum number of constraints present inside the partition
	 * @param minNumberCostraints is the minimum number of constraints inside the partition allowed
	 * @return the corresponding condition
	 */
	public static Condition<String> createAtLeastNumberOfConstraintsCondition(int minNumberCostraints){
		return p -> {
			int count = p.getNumberConstraints();
			return count >= minNumberCostraints;
		};
	}

	/**
	 * Create a condition on the maximum number of constraints present inside the partition
	 * @param maxNumberCostraints is the maximum number of constraints inside the partition allowed
	 * @return the corresponding condition
	 */
	public static Condition<String> createAtMostNumberOfConstraintsCondition(int maxNumberCostraints){
		return p -> {
			int count = p.getNumberConstraints();
			return count <= maxNumberCostraints;
		};
	}

	/**
	 * Create a condition on the presence of two agents in the same constraint
	 * @param agent1 is the first agent
	 * @param agent2 is the second agent
	 * @return the corresponding condition
	 *
	 * @throws NullPointerException if <code>agent1</code> or <code>agent2</code> are null
	 */
	public static Condition<String> createMustBeTogetherCondition(String agent1, String agent2){
		if(agent1 == null || agent2 == null){
			throw new NullPointerException("agent1 and agent2 must not be null");
		}
		return p -> {
			Set<String> set1 = new HashSet<>();
			set1.add(agent1);
			Set<String> set2 = new HashSet<>();
			set2.add(agent2);
			return p.areMonitoredTogether(set1, set2);
		};
	}

	/**
	 * Create a condition on the absence of two agents in the same constraint
	 * @param agent1 is the first agent
	 * @param agent2 is the second agent
	 * @return the corresponding condition
	 *
	 * @throws NullPointerException if <code>agent1</code> or <code>agent2</code> are null
	 */
	public static Condition<String> createMustBeSplitCondition(String agent1, String agent2){
		if(agent1 == null || agent2 == null){
			throw new NullPointerException("agent1 and agent2 must not be null");
		}
		return p -> {
			Set<String> set1 = new HashSet<>();
			set1.add(agent1);
			Set<String> set2 = new HashSet<>();
			set2.add(agent2);
			return !p.areMonitoredTogether(set1, set2);
		};
	}

}
