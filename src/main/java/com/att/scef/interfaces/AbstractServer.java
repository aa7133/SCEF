package com.att.scef.interfaces;


import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
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
import org.jdiameter.api.Mode;
import org.jdiameter.api.Network;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.Peer;
import org.jdiameter.api.PeerTable;
import org.jdiameter.api.Realm;
import org.jdiameter.api.RealmTable;
import org.jdiameter.api.Request;
import org.jdiameter.api.ResultCode;
import org.jdiameter.api.Session;
import org.jdiameter.api.SessionFactory;
import org.jdiameter.api.Stack;
import org.jdiameter.api.StackType;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.client.impl.parser.MessageParser;
//import org.jdiameter.client.impl.parser.MessageParser;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.Parameters;
import org.jdiameter.server.impl.helpers.XMLConfiguration;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.diameter.dictionary.AvpRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Adi Enzel on 3/23/17.
 *this.getClass()
 * @author <a href="mailto:aa7133@att.com"> Adi Enzel </a>
 *
 *         allow initialization of stack and base configuration as helper for
 *         the real running servers
 *
 */
public abstract class AbstractServer implements NetworkReqListener, EventListener<Request, Answer>, StateChangeListener<AppSession>  {
	// protected final Logger logger =
	// LoggerFactory.getLogger(AbstractServer.class);
	protected final Logger logger = LoggerFactory.getLogger(AbstractServer.class);

	protected MessageParser parser = new MessageParser();
	protected Stack stack;
	protected SessionFactory factory;
	protected AvpDictionary dictionary = AvpDictionary.INSTANCE;
	private boolean run = true;
	private String DEFAULT_CONFIG_FILE = "jdiameter-config.xml";
	
	private Configuration config;
	
	public static final String HSS_REALM = "hss.att.com"; 
	public static final String MME_REALM = "mme.att.com"; 
	public static final String SCEF_REALM = "scef.att.com"; 

	public AbstractServer() {
	}

	public void configure(String configFile, String dictionaryFile) throws Exception {
		try {
			dictionary.parseDictionary(this.getClass().getClassLoader().getResourceAsStream(dictionaryFile));
			logger.info("AVP Dictionary File" + dictionaryFile+ " successfully parsed.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (configFile == null || configFile.length() == 0){
			initStack(DEFAULT_CONFIG_FILE);
		}
		else {
			initStack(configFile);
		}
	}

	private void initStack(String configurationFile) {
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
			config = new XMLConfiguration(new FileInputStream(configurationFile));
			
			factory = stack.init(config);
			if (logger.isInfoEnabled()) {
				logger.info("Stack Configuration successfully loaded.");
			}
			
			Network network = stack.unwrap(Network.class);
			Set<ApplicationId> ids = new HashSet<ApplicationId>();
			for (Configuration r : config.getChildren(Parameters.RealmTable.ordinal())) {
				Configuration[] rEntry = r.getChildren(Parameters.RealmEntry.ordinal()); 
				for (Configuration e : rEntry) {
					logger.info("Entry = \n" + e + "\nRelm Name = " + e.getStringValue(Parameters.RealmName.ordinal(), "no value"));
					
					Configuration[] app = e.getChildren(Parameters.ApplicationId.ordinal());
					for (Configuration a : app) {
						ids.add(ApplicationId.createByAuthAppId(
								a.getLongValue(Parameters.VendorId.ordinal(), -1),
								a.getLongValue(Parameters.AuthApplId.ordinal(), -1)));
					}
				}
			}

			
			//List<Peer> peers = stack.unwrap(PeerTable.class).getPeerTable();

			logger.info("Diameter Stack  :: Supporting " + ids.size() + " applications.");

			for (ApplicationId appId : ids) {
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
			logger.error("Incorrect driver");
			return;
		}

		try {
			if (logger.isInfoEnabled()) {
				logger.info("Starting stack");
			}
			//stack.start();
			stack.start(Mode.ALL_PEERS, 60000, TimeUnit.MILLISECONDS);
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
	
	public Configuration getConfiguration() {
		return this.config;
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

	protected Message createAnswer(Request request, int answerCode, ApplicationId appId)
			throws InternalException, IllegalDiameterStateException {

		int commandCode = request.getCommandCode();
		long endToEndId = request.getEndToEndIdentifier();
		long hopByHopId = request.getHopByHopIdentifier();

		AvpSet reqSet = request.getAvps();
		
		Avp originHost = reqSet.getAvp(Avp.ORIGIN_HOST);
	    Avp originRealm = reqSet.getAvp(Avp.ORIGIN_REALM);

		Message raw = stack.getSessionFactory().getNewRawSession().createMessage(commandCode, appId, hopByHopId, endToEndId);
		AvpSet avps = raw.getAvps();

		
		avps.insertAvp(0, Avp.SESSION_ID, request.getSessionId(), false);

		avps.addAvp(Avp.RESULT_CODE, answerCode, true);
		
		avps.addAvp(originHost);
		avps.addAvp(originRealm);
		
		avps.addAvp(Avp.AUTH_SESSION_STATE, 1);
		
		raw.setProxiable(true);
		raw.setRequest(false);
		if (answerCode != ResultCode.SUCCESS) {
			raw.setError(true);
			AvpSet experimentalResult = avps.addGroupedAvp(Avp.EXPERIMENTAL_RESULT, 0, true, false);
			experimentalResult.addAvp(Avp.VENDOR_ID, 0);
			experimentalResult.addAvp(Avp.EXPERIMENTAL_RESULT_CODE, answerCode);
		}
		AvpSet vendorSpecificApplicationId = avps.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, false, false);
		vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, appId.getVendorId(), true);
		vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, appId.getAuthAppId(), true);
		
		return raw;
	}
	
	/**
	 * 
	 * @param session application session
	 * @param applicationId  the application id of the designated app
	 * @param code    command to send 
	 * @param realmName the realm of the target
	 * @param originHost from where
	 * @return request message
	 */
	protected Request createRequest(AppSession session, ApplicationId applicationId, int code,  String originRealmName, String originHost) {
		Request r  = null;
		try {
			Message raw = stack.getSessionFactory().getNewRawSession().createMessage(code, applicationId);

			List<Session> sessions = session.getSessions();
			Session s = sessions.get(0);
			r = s.createRequest(code, applicationId, originRealmName);

			// Request r = session.getSessions().get(0).createRequest(code,
			// applicationId, originRealmName);

			AvpSet reqSet = r.getAvps();
			AvpSet vendorSpecificApplicationId = reqSet.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, false,
					false);
			vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, applicationId.getVendorId(), true);
			vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, applicationId.getAuthAppId(), true);
			reqSet.addAvp(Avp.AUTH_SESSION_STATE, 1);

			//reqSet.removeAvp(Avp.ORIGIN_HOST);
			//reqSet.removeAvp(Avp.ORIGIN_REALM);

			reqSet.addAvp(Avp.ORIGIN_HOST, originHost, true);
			reqSet.addAvp(Avp.ORIGIN_REALM, originRealmName, true);
			return r;
		} catch (InternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalDiameterStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
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


	
	protected void printAvps(AvpSet avpSet) throws AvpDataException {
		printAvpsAux(avpSet, 0);
	}

	/**
	 * Prints the AVPs present in an AvpSet with a specified 'tab' level
	 *
	 * @param avpSet the AvpSet containing the AVPs to be printed
	 * @param level  an int representing the number of 'tabs' to make a pretty  print
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

	public Stack getStack() {
		return this.stack;
	}

	@Override
	public void receivedSuccessMessage(Request request, Answer answer) {
		logger.error("Received \"SuccessMessage\" event, request[" + request + "], answer[" + answer + "]");
	}

	@Override
	public void timeoutExpired(Request request) {
		logger.error("Received \"Timoeout\" event, request[" + request + "]");
	}

	@Override
	public Answer processRequest(Request request) {
		logger.error("Received \"Request\" event, request[" + request + "]");
		return null;
	}

	// --- State Changes
	// --------------------------------------------------------
	@SuppressWarnings("rawtypes")
	@Override
	public void stateChanged(Enum oldState, Enum newState) {
		// NOP
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void stateChanged(AppSession source, Enum oldState, Enum newState) {
		// NOP
	}
}
