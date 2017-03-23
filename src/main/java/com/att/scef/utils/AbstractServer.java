package com.att.scef.utils;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.DisconnectCause;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Message;
import org.jdiameter.api.MetaData;
import org.jdiameter.api.Network;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.Request;
import org.jdiameter.api.SessionFactory;
import org.jdiameter.api.Stack;
import org.jdiameter.api.StackType;
import org.jdiameter.client.impl.helpers.XMLConfiguration;
import org.jdiameter.client.impl.parser.MessageParser;
import org.jdiameter.server.impl.StackImpl;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.diameter.dictionary.AvpRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Adi Enzel on 3/23/17.
 *
 * @author <a href="mailto:aa7133@att.com"> Adi Enzel </a>
 *
 *         allow initialization of stack and base configuration as helper for
 *         the real running servers
 *
 */
public abstract class AbstractServer implements NetworkReqListener, EventListener<Request, Answer> {
	// protected final Logger logger =
	// LoggerFactory.getLogger(AbstractServer.class);
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ApplicationId t6aAppId = ApplicationId.createByAuthAppId(10415L, 16777346L);
	private ApplicationId s6tAppId = ApplicationId.createByAuthAppId(10415L, 16777345L);

	protected MessageParser parser = new MessageParser();
	protected Stack stack;
	protected SessionFactory factory;
	protected InputStream configFile;
	protected AvpDictionary dictionary = AvpDictionary.INSTANCE;
	private boolean run = true;

	public AbstractServer() {

	}

	public void configure(InputStream f) throws Exception {
		this.configFile = f;
		// add more
		try {
			dictionary.parseDictionary(this.getClass().getClassLoader().getResourceAsStream("dictionary.xml"));
			logger.info("AVP Dictionary successfully parsed.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		initStack();
	}

	private void initStack() {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing Stack...");
		}

		try {

			this.stack = new StackImpl();
		} catch (Exception e) {
			e.printStackTrace();
			stack.destroy();
			return;
		}

		try {
			InputStream is;
			if (configFile != null) {
				is = this.configFile;
			} else {
				String configFile = "jdiameter-config.xml";
				is = this.getClass().getClassLoader().getResourceAsStream(configFile);
			}
			Configuration config = new XMLConfiguration(is);
			factory = stack.init(config);
			if (logger.isInfoEnabled()) {
				logger.info("Stack Configuration successfully loaded.");
			}
			Network network = stack.unwrap(Network.class);

			Set<org.jdiameter.api.ApplicationId> appIds = stack.getMetaData().getLocalPeer().getCommonApplications();

			logger.info("Diameter Stack  :: Supporting " + appIds.size() + " applications.");

			// network.addNetworkReqListener(this,
			// ApplicationId.createByAccAppId( 193, 19302 ));

			for (org.jdiameter.api.ApplicationId appId : appIds) {
				logger.info("Diameter Stack Mux :: Adding Listener for [" + appId + "].");
				network.addNetworkReqListener(this, appId);
			}

		} catch (Exception e) {
			e.printStackTrace();
			stack.destroy();
			return;
		}

		MetaData metaData = stack.getMetaData();
		if (metaData.getStackType() != StackType.TYPE_SERVER || metaData.getMinorVersion() <= 0) {
			stack.destroy();
			if (logger.isErrorEnabled()) {
				logger.error("Incorrect driver");
			}
			return;
		}

		try {
			if (logger.isInfoEnabled()) {
				logger.info("Starting stack");
			}
			stack.start();
			if (logger.isInfoEnabled()) {
				logger.info("Stack is running.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			stack.destroy();
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info("Stack initialization successfully completed.");
		}
	}

	/**
	 * @return the run
	 */
	public boolean isRun() {
		return run;
	}

	/**
	 * @param run
	 *            the run to set
	 */
	public void setRun(boolean run) {
		this.run = run;
	}


	/**
	 * 
	 */
	protected void clean() {
		if (stack != null) {
			try {
				stack.stop(10, TimeUnit.SECONDS, DisconnectCause.REBOOTING);

				stack = null;
				factory = null;
			} catch (IllegalDiameterStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InternalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void dumpMessage(Message message, boolean sending) {
		if (logger.isInfoEnabled()) {
			logger.info(new StringBuilder(sending ? "Sending " : "Received ")
					.append(message.isRequest() ? "Request: " : "Answer: ")
			        .append(message.getCommandCode())
			        .append("\nE2E: ").append(message.getEndToEndIdentifier())
			        .append("\nHBH: ").append(message.getHopByHopIdentifier())
			        .append("\nAppID: ").append(message.getApplicationId())
			        .append("\nRequest AVPS: \n").toString());
			try {
				printAvps(message.getAvps());
			} catch (AvpDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Message createAnswer(Request request, int answerCode, ApplicationId appId)
			throws InternalException, IllegalDiameterStateException {

		int commandCode = 0;
		long endToEndId = 0;
		long hopByHopId = 0;
		commandCode = request.getCommandCode();
		endToEndId = request.getEndToEndIdentifier();
		hopByHopId = request.getHopByHopIdentifier();

		Message raw = stack.getSessionFactory().getNewRawSession()
				 .createMessage(commandCode, appId, hopByHopId, endToEndId);
		AvpSet avps = raw.getAvps();

		// inser session iD
		avps.insertAvp(0, 263, request.getSessionId(), false);
		// add result //asUnsignedInt32
		avps.addAvp(268, 2001L, true);
		// origin host
		avps.addAvp(264, serverHost, true);
		// origin realm
		avps.addAvp(296, realmName, true);
		raw.setProxiable(true);
		raw.setRequest(false);
		// ((MessageImpl) raw).setPeer(((MessageImpl) request).getPeer());
		return raw;

	}

	protected void printAvps(AvpSet avpSet) throws AvpDataException {
		printAvpsAux(avpSet, 0);
	}

	/**
	 * Prints the AVPs present in an AvpSet with a specified 'tab' level
	 *
	 * @param avpSet
	 *            the AvpSet containing the AVPs to be printed
	 * @param level
	 *            an int representing the number of 'tabs' to make a pretty
	 *            print
	 * @throws AvpDataException
	 */
	private void printAvpsAux(AvpSet avpSet, int level) throws AvpDataException {
		String prefix = "                      ".substring(0, level * 2);

		for (Avp avp : avpSet) {
			AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(avp.getCode(), avp.getVendorId());

			if (avpRep != null && avpRep.getType().equals("Grouped")) {
				logger.info(new StringBuilder().append(prefix)
						.append("<avp name=\"").append(avpRep.getName())
						.append("\" code=\"").append(avp.getCode())
						.append("\" vendor=\"").append(avp.getVendorId())
						.append("\">").toString());
				printAvpsAux(avp.getGrouped(), level + 1);
				logger.info(prefix + "</avp>");
			} else if (avpRep != null) {
				String value = "";

				if (avpRep.getType().equals("Integer32")) {
					value = String.valueOf(avp.getInteger32());
				} else if (avpRep.getType().equals("Integer64") || avpRep.getType().equals("Unsigned64")) {
					value = String.valueOf(avp.getInteger64());
				} else if (avpRep.getType().equals("Unsigned32")) {
					value = String.valueOf(avp.getUnsigned32());
				} else if (avpRep.getType().equals("Float32")) {
					value = String.valueOf(avp.getFloat32());
				} else {
					value = new String(avp.getOctetString());
				}

				logger.info(new StringBuilder().append(prefix)
						.append("<avp name=\"").append(avpRep.getName())
						.append("\" code=\"").append(avp.getCode())
						.append("\" vendor=\"").append(avp.getVendorId())
						.append("\" value=\"").append(value)
						.append("\" />").toString());
			}
		}
	}

}
