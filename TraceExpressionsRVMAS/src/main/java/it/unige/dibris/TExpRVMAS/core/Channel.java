package it.unige.dibris.TExpRVMAS.core;

import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public abstract class Channel {
	
	private String name;
	
	private static Map<String, Channel> channels = new HashMap<>();
	
	public Channel(String name){
		this.name = name;
	}
	
	public static void addChannel(Channel channel){
		channels.putIfAbsent(channel.name, channel);
	}
	
	public static Channel getChannel(String name){
		return channels.get(name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	public void sent(ACLMessage msg){
		Monitor m = Monitor.getMyMonitor(msg.getSender().getName());
		if(m != null){
			m.addMessage(this, msg, true);
		}
	}
	
	public void received(ACLMessage msg){
		AID ag = (AID) msg.getAllReceiver().next();
		Monitor m = Monitor.getMyMonitor(ag.getName());
		if(m != null){
			m.addMessage(this, msg, false);
		}
	}
}
