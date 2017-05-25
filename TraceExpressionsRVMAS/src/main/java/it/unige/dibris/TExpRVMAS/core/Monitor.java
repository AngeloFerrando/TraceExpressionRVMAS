package it.unige.dibris.TExpRVMAS.core;

import jade.tools.ToolAgent;

public abstract class Monitor extends ToolAgent {

	private String name;
	
	public Monitor(String name){
		this.name = name;
	}

	public String getMonitorName() {
		return name;
	}
	
}
