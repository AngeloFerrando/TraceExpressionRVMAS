package it.unige.dibris.TExpRVMAS.core;

import java.util.ArrayList;
import java.util.List;

import jade.core.Agent;

public class PerceptionFactory {

	public static Perception createSimpleAction(Agent agent, String action){
		return new Perception() {
			
			@Override
			public String toPrologRepresentation() {
				return "act(agent(" + agent.getLocalName() + "), content(" + action + "))";
			}
			
			@Override
			public List<Agent> getAgentsInvolved() {
				List<Agent> res = new ArrayList<>();
				res.add(agent);
				return res;
			}
			
		};
	}
	
}
