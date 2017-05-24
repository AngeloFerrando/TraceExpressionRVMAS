/*****************************************************************

GNU Lesser General Public License
 *****************************************************************/


package it.unige.dibris.TExpRVMAS.core.monitor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import java.util.Map;
import java.util.TreeMap;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.Logger;

import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;
import jade.domain.FIPAService;
import jade.domain.FIPAAgentManagement.Envelope;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;

import jade.content.lang.sl.SLCodec;

import jade.content.AgentAction;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;

import jade.proto.SimpleAchieveREResponder;
import jade.proto.SimpleAchieveREInitiator;

import jade.tools.ToolAgent;

import jade.util.ExtendedProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.io.*;



public class MsgParser{
		
public static String format_message(long local_epoch, ACLMessage tmp) {
		try {
	// Mandatory field: performative
			// Optional fields: sender, receiver, content, reply-to(...),
			// language(...),
			// encoding(...), ontology(...),
			// protocol(...), conversation-id(...), reply-with(...),
			// in-reply-to(...),
			// reply-by(...)
			String prolog_msg = "msg(performative("
					+ tmp.getPerformative(tmp.getPerformative()).toLowerCase()
					+ ")";

			if (tmp.getSender() != null)
				prolog_msg = prolog_msg + ",sender("
						+ tmp.getSender().getLocalName() + ")";

			if (tmp.getAllReceiver().next() != null)
				prolog_msg = prolog_msg + ",receiver("
						+ ((AID) (tmp.getAllReceiver().next())).getLocalName()
						+ ")";

			if (tmp.getInReplyTo() != null)
				prolog_msg = prolog_msg + ",reply-to('" + tmp.getInReplyTo()
						+ "')";

			if (tmp.getContent() != null)
				prolog_msg = prolog_msg + ",content(" + tmp.getContent() + ")";

			if (tmp.getLanguage() != null)
				prolog_msg = prolog_msg + ",language("
						+ tmp.getLanguage().toLowerCase() + ")";
			if (tmp.getEncoding() != null)
				prolog_msg = prolog_msg + ",encoding("
						+ tmp.getEncoding().toLowerCase() + ")";
			if (tmp.getOntology() != null)
				prolog_msg = prolog_msg + ",ontology("
						+ tmp.getOntology().toLowerCase() + ")";
			if (tmp.getProtocol() != null)
				prolog_msg = prolog_msg + ",protocol("
						+ tmp.getProtocol().toLowerCase() + ")";
			if (tmp.getConversationId() != null)
				prolog_msg = prolog_msg + ",conversation-id("
						+ tmp.getConversationId().toLowerCase() + ")";
			if (tmp.getReplyWith() != null)
				prolog_msg = prolog_msg + ",reply-with('" + tmp.getReplyWith()
						+ "')";

			if (tmp.getInReplyTo() != null)
				prolog_msg = prolog_msg + ",in-reply-to('" + tmp.getInReplyTo()
						+ "')";

			if (tmp.getReplyByDate() != null)
				prolog_msg = prolog_msg + ",reply-by("
						+ (tmp.getReplyByDate().getTime() - local_epoch) + ")";
			if (tmp.getEnvelope() != null)
				prolog_msg = prolog_msg + ",time-stamp("
						+ (tmp.getEnvelope().getDate().getTime() - local_epoch)
						+ ")";

			return prolog_msg + ")";
			} catch (Exception e) {
			System.err.println("ERROR IN FORMATTING THE PROLOG MESSAGE");
			return "ERROR IN FORMATTING THE PROLOG MESSAGE";
		}
	}

}  // End of class MsgParser
