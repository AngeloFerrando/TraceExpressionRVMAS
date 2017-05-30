package it.unige.dibris.TExpRVMAS.core.examples.alt_bit;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class Receiver extends Agent {
	@Override
	protected void setup() {
		super.setup();
		String[] args = (String[]) getArguments();
		String sender = args[0];
		String content = args[1];
		
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				ACLMessage msgR= blockingReceive(); 
				if(msgR != null)
		        	System.out.println("[" + getLocalName() + "]: receive " + msgR.getContent() + " from " + sender);
				
				doWait(2000);
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID(sender, AID.ISLOCALNAME));
		        msg.setContent(content);
		        
		        System.out.println("[" + getLocalName() + "]: send " + content + " to " + sender);
		        send(msg);
			}
		});
	}
}
