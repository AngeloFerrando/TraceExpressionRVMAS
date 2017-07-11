package it.unige.dibris.TExpRVMAS.core;

import java.util.Random;

import jade.lang.acl.ACLMessage;

public class SimulatedChannel extends Channel{
	
	private double reliability;

	public SimulatedChannel(String name, double reliability) {
		super(name);
		if(reliability < 0 || reliability > 1){
			throw new IllegalArgumentException("reliability must be between 0 and 1");
		}
		this.reliability = reliability;
	}
	
	@Override
	public void sent(ACLMessage msg) {
		if(reliability == 1){
			super.sent(msg);
		} else if(reliability > 0){
			Random r = new Random();
			if(r.nextDouble() <= reliability){
				super.sent(msg);
			}
		}
	}

	@Override
	public void received(ACLMessage msg) {
		if(reliability == 1){
			super.received(msg);
		} else if(reliability > 0){
			Random r = new Random();
			if(r.nextDouble() <= reliability){
				super.received(msg);
			}
		}
	}
}
