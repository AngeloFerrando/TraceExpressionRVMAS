package it.unige.dibris.TExpRVMAS.core;

public class FactoryConditions {

	public static Condition createNumberSingletonsCondition(int minNumberSingletons, int maxNumberSingletons){
		if(minNumberSingletons > maxNumberSingletons){
			throw new IllegalArgumentException("the minimum number of singletons must be less than the maximum number");
		}
		return p -> {
			int singletons = p.getNumberSingletons();
			return singletons >= minNumberSingletons && singletons <= maxNumberSingletons;
		};
	}
	
	public static Condition createAtLeastNumberSingletonsCondition(int minNumberSingletons){
		return p -> {
			int singletons = p.getNumberSingletons();
			return singletons >= minNumberSingletons;
		};
	}
	
	public static Condition createAtMostNumberSingletonsCondition(int maxNumberSingletons){
		return p -> {
			int singletons = p.getNumberSingletons();
			return singletons <= maxNumberSingletons;
		};
	}
	
	public static Condition createNumberAgentsForConstraintCondition(int minNumberAgentsForConstraint, int maxNumberAgentsForConstraint){
		if(minNumberAgentsForConstraint > maxNumberAgentsForConstraint){
			throw new IllegalArgumentException("the minimum number of agents for constraint must be less than the maximum number");
		}
		return p -> {
			return p.getNumberConstraintsBetweenSize(minNumberAgentsForConstraint, maxNumberAgentsForConstraint) == p.getNumberConstraints();
		};
	}
	
	public static Condition createAtLeastNumberAgentsForConstraintCondition(int minNumberAgentsForConstraint){
		return p -> {
			return p.getNumberConstraintsAtLeastSize(minNumberAgentsForConstraint) == p.getNumberConstraints();
		};
	}
	
	public static Condition createAtMostNumberAgentsForConstraintCondition(int maxNumberAgentsForConstraint){
		return p -> {
			return p.getNumberConstraintsAtMostSize(maxNumberAgentsForConstraint) == p.getNumberConstraints();
		};
	}
	
	public static Condition createNumberOfConstraintsCondition(int minNumberCostraints, int maxNumberCostraints){
		if(minNumberCostraints > maxNumberCostraints){
			throw new IllegalArgumentException("the minimum number constraints must be less than the maximum number");
		}
		return p -> {
			int count = p.getNumberConstraints();
			return count >= minNumberCostraints && count <= maxNumberCostraints;
		};
	}
	
	public static Condition createAtLeastNumberOfConstraintsCondition(int minNumberCostraints){
		return p -> {
			int count = p.getNumberConstraints();
			return count >= minNumberCostraints;
		};
	}
	
	public static Condition createAtMostNumberOfConstraintsCondition(int maxNumberCostraints){
		return p -> {
			int count = p.getNumberConstraints();
			return count <= maxNumberCostraints;
		};
	}

}
