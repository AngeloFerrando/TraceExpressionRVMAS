package it.unige.dibris.TExpRVMAS;

public class FactoryConditions {

	public Condition createNumberSingletonsCondition(int minNumberSingletons, int maxNumberSingletons){
		return p -> {
			int singletons = p.getNumberSingletons();
			return singletons >= minNumberSingletons && singletons <= maxNumberSingletons;
		};
	}
	
	public Condition createNumberAgentsForConstraintCondition(int minNumberAgentsForConstraint, int maxNumberAgentsForConstraint){
		return p -> {
			return p.getNumberConstraints(minNumberAgentsForConstraint, maxNumberAgentsForConstraint) == p.getNumberConstraints();
		};
	}
	
	public Condition createNumberOfConstraintsCondition(int minNumberCostraints, int maxNumberCostraints){
		return p -> {
			int count = p.getNumberConstraints();
			return count >= minNumberCostraints && count <= maxNumberCostraints;
		};
	}

}
