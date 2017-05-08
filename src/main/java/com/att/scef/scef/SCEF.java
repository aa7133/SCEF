package com.att.scef.scef;

import static org.jdiameter.client.impl.helpers.Parameters.OwnDiameterURI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Mode;
import org.jdiameter.api.Network;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.api.s6t.ClientS6tSession;
import org.jdiameter.api.s6t.ClientS6tSessionListener;
import org.jdiameter.api.s6t.ServerS6tSession;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.api.t6a.ServerT6aSession;
import org.jdiameter.api.t6a.ServerT6aSessionListener;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataAnswer;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.common.impl.app.s6t.S6tSessionFactoryImpl;
import org.jdiameter.common.impl.app.t6a.T6aSessionFactoryImpl;
import org.jdiameter.server.impl.app.s6t.S6tServerSessionImpl;
import org.jdiameter.server.impl.app.t6a.T6aServerSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.data.AsyncDataConnector;
import com.att.scef.data.AsyncPubSubConnector;
import com.att.scef.data.ConnectorImpl;
import com.att.scef.data.PubSubConnectorImpl;
import com.att.scef.data.SyncDataConnector;
import com.att.scef.data.SyncPubSubConnector;
import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.gson.GSCEFUserProfile;
import com.att.scef.interfaces.AbstractServer;
import com.att.scef.interfaces.S6tClient;
import com.att.scef.utils.BCDStringConverter;
import com.att.scef.utils.MonitoringType;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.async.RedisStringAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.lambdaworks.redis.pubsub.api.async.RedisPubSubAsyncCommands;
import com.lambdaworks.redis.pubsub.api.sync.RedisPubSubCommands;


public class SCEF {

	protected final Logger logger = LoggerFactory.getLogger(SCEF.class); //this.getClass());

	private ApplicationId t6aAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777346);
	private ApplicationId s6tAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777345);

	private T6aSessionFactoryImpl t6aSessionFactory;

	private S6tSessionFactoryImpl s6tSessionFactory;

 
	private ConnectorImpl syncDataConnector;
	private ConnectorImpl asyncDataConnector;
	private RedisStringAsyncCommands<String, String> asyncHandler;
	private RedisStringCommands<String, String> syncHandler;

	private PubSubConnectorImpl syncPubSubConnector;
	private PubSubConnectorImpl asyncPubSubConnector;
	private RedisPubSubAsyncCommands<String, String> asyncPubSubHandler;
	private RedisPubSubCommands<String, String> syncPubSubHandler;

	
	private String scefId = null;
	
	private final static String DEFAULT_S6T_CLIENT_NAME = "S6T-Client";
	private final static String DEFAULT_T6A_SERVER_NAME = "T6a-Server";
	
	private final static String DEFAULT_SCEF_ID = "aaa://127.0.0.1:2300";
    private final static String DEFAULT_S6T_CONFIG_FILE = "/home/odldev/scef/src/main/resources/scef/config-scef-s6t.xml";
    private final static String DEFAULT_T6A_CONFIG_FILE = "/home/odldev/scef/src/main/resources/scef/config-scef-t6a.xml";
    private final static String DEFAULT_DICTIONARY_FILE = "/home/odldev/scef/src/main/resources/dictionary.xml";

    private S6tClient s6tClient;
    
	
    public static void main(String[] args) {
    	String s6tCconfigFile = DEFAULT_S6T_CONFIG_FILE;
    	String t6aCconfigFile = DEFAULT_T6A_CONFIG_FILE;
    	String dictionaryFile = DEFAULT_DICTIONARY_FILE;
    	//String host = "ILTLV937";
    	String host = "127.0.0.1";
    	int port = 6379;
    	String channel = "DeviceFromApp";
    	
    	
    	for (int i = 0; i < args.length; i += 2) {
    		if (args[i].equals("--s6t-conf")) {
    			s6tCconfigFile = args[i+1];
    		}
    		else if (args[i].equals("--t6a-conf")) {
    			t6aCconfigFile = args[i+1];
    		}
    		else if (args[i].equals("--dir")) {
    			dictionaryFile = args[i+1];
    		}
    		else if (args[i].equals("--redis-host")) {
    			host = args[i+1];
    		}
    		else if (args[i].equals("--redis-port")) {
    			port = Integer.parseInt(args[i+1]);
    		}
    		else if (args[i].equals("--redis-channel")) {
    			channel = args[i+1];
    		}
    	}
    	
    	new SCEF(s6tCconfigFile, t6aCconfigFile, dictionaryFile, host, port, channel);
    	
    }
	
	@SuppressWarnings("unchecked")
	public SCEF(String s6tConfigFile, String t6aConfigFile, String dictionaryFile, String host, int port, String channel) {
		super();
		this.asyncDataConnector = new ConnectorImpl();
		this.asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(AsyncDataConnector.class, host, port);

		this.syncDataConnector = new ConnectorImpl();
		this.syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(SyncDataConnector.class, host, port);

		this.asyncPubSubConnector = new PubSubConnectorImpl();
		this.asyncPubSubHandler = (RedisPubSubAsyncCommands<String, String>)asyncPubSubConnector.createPubSub(AsyncPubSubConnector.class, host, port, this, channel);
		
		//this.syncPubSubConnector = new PubSubConnectorImpl();
		//this.syncPubSubHandler = (RedisPubSubCommands<String, String>)syncPubSubConnector.createPubSub(SyncPubSubConnector.class, host, port, this, channel);


		this.s6tClient = new S6tClient(this, s6tConfigFile);
		try {
			this.s6tClient.init(DEFAULT_S6T_CLIENT_NAME);
		    this.s6tClient.start(Mode.ANY_PEER, 10, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//TODO replace this with S6t and T6a functions
		this.scefId = this.s6tClient.getStack().getMetaData().getConfiguration().getStringValue(OwnDiameterURI.ordinal(), DEFAULT_SCEF_ID);

		logger.info("=================================== SCEF started ==============================");
	}
	
	/**
	 * 
	 * @param msg
	 */
	public void sendDiamterMessages(String msg) {
		GSCEFUserProfile newMsg = getFormatedMessage(msg);
		RedisFuture<String> setScefExternId = null;
        RedisFuture<String> setScefMsisdn = null;
        RedisFuture<String> setScefUserName = null;
		
		Runnable listener = new Runnable() {
		    @Override
		    public void run() {
		      /*
		    	if (logger.isInfoEnabled()) {
		    		logger.info("Update the SCEF data");
		    	}
		    	*/
		    }
		};
		
		// get the MSISDN from the SCEF external id to MSISDN table.
		// if not exists so this is new user and we need to set the tables
		String msisdn =  this.getSyncHandler().get("SCEF-Extern-ID-" + newMsg.getExternalId());
		
		if (msisdn == null || msisdn.length() == 0) {
			if (logger.isInfoEnabled()) {
				logger.info("**** got a message for new user : " + msg);
			}
			
			// translate to monitoring type list all requested monitoring flags
			List<Integer> ml = MonitoringType.getMonitorinTypeList(newMsg.getMonitoringFlags());
			GMonitoringEventConfig[] mc = new GMonitoringEventConfig[ml.size()];
			
			for (int i = 0; i < ml.size(); i++) {
				mc[i] = new GMonitoringEventConfig();
				mc[i].setMonitoringType(ml.get(i));
				mc[i].setScefRefId(ml.get(i));
				//TODO get the SCEF address and fill the parameter from the configuration 
				mc[i].setScefId(this.scefId);
			}
			newMsg.setMc(mc);
			
			// send command to HSS
			if (mc.length != 0) {
	            logger.info("Sending Configuration Information Request (CIR)");
				this.s6tClient.sendCirRequest(newMsg, mc, null);
			}
			if (newMsg.getDataQueueAddress() != null) {
				// send NIR
			  this.s6tClient.sendNirRequest(newMsg);
			}
			
			setScefExternId = this.getAsyncHandler().set("SCEF-Extern-ID-" + newMsg.getExternalId(), newMsg.getMsisdn());
			setScefMsisdn = this.getAsyncHandler().set("SCEF-MSISDN-" + newMsg.getMsisdn(), new Gson().toJson(newMsg));
			if (newMsg.getUserName() != null && newMsg.getUserName().length() >0) {
			  setScefUserName = this.getAsyncHandler().set("SCEF-USER-NAME-" + newMsg.getUserName(), newMsg.getMsisdn());
			}
			
			setScefExternId.thenRun(listener);
			setScefMsisdn.thenRun(listener);
			if (setScefUserName != null) {
			  setScefUserName.thenRun(listener);
			}
			
		}
		else {
			String data = this.getSyncHandler().get("SCEF-MSISDN-" + newMsg.getMsisdn());
			if (logger.isInfoEnabled()) {
				logger.info(new StringBuilder("got a message for user : ")
						.append(msisdn).append(" message = ")
						.append(msg).append("Data = ").append(data)
						.toString());
			}
			
			GSCEFUserProfile userProfile = new Gson().fromJson(new JsonParser().parse(data), GSCEFUserProfile.class);
			
			List<Integer> mapToAdd = MonitoringType.getNewMonitoringTypeList(userProfile.getMonitoringFlags(), newMsg.getMonitoringFlags());
			List<Integer> mapToDelete = MonitoringType.getDeletedMonitoringTypeList(userProfile.getMonitoringFlags(), newMsg.getMonitoringFlags());
			//Integer[] newMap = MonitoringType.getnextMonitoringTypeList(userProfile.getMonitoringFlags(), newMsg.getMonitoringFlags());
			
			userProfile.setMonitoringFlags(MonitoringType.getnextMonitoringMap(userProfile.getMonitoringFlags(), newMsg.getMonitoringFlags()));

			GMonitoringEventConfig[] mc = new GMonitoringEventConfig[mapToAdd.size() + mapToDelete.size()];
			
			int i = 0;
			for (; i < mapToAdd.size(); i++) {
				mc[i] = new GMonitoringEventConfig();
				mc[i].setMonitoringType(mapToAdd.get(i));
				mc[i].setScefRefId(mapToAdd.get(i));
				//TODO get the SCEF address and fill the parameter from the configuration 
				mc[i].setScefId(this.scefId);
			}
			//TODO current support is just for 1 SCEF ref ID so the delete will be the same.
			// in real application it may be several deletion for one monitoring type
			for (int j = 0; j < mapToDelete.size(); j++, i++) {
				mc[i] = new GMonitoringEventConfig();
				mc[i].setMonitoringType(mapToDelete.get(j));
				int[] dscefToDelete = new int[1];
				dscefToDelete[0] = mapToDelete.get(j);
				mc[i].setScefRefIdForDelition(dscefToDelete);
				mc[i].setScefRefId(-1);
			}

			// we need to send CIR here
			if (mc.length != 0) {
			    logger.info("Sending Configuration Information Request (CIR)");
				this.s6tClient.sendCirRequest(newMsg, mc, null);
			}
			
            logger.info("Sending NIDD-Information-Request (CIR)");
            this.s6tClient.sendNirRequest(newMsg);

            if (userProfile.getDataQueueAddress() == null || (userProfile.getDataQueueAddress()).length() == 0) {
				if (newMsg.getDataQueueAddress() != null && newMsg.getDataQueueAddress().length() != 0) {
					// send NIR
	                logger.info("Sending NIDD-Information-Request (CIR)");
					this.s6tClient.sendNirRequest(newMsg);
				}
			}
			
			GMonitoringEventConfig[] nextMap = userProfile.getMc();
			List<GMonitoringEventConfig> l = new ArrayList<GMonitoringEventConfig>();
			for (int j = 0; j < nextMap.length; j++) {
				if (mapToDelete.contains(nextMap[j].getMonitoringType())) {
					nextMap[j] = null;
				}
				else {
					l.add(nextMap[j]);
				}
			}
			for (int j : mapToAdd) {
				GMonitoringEventConfig m = new GMonitoringEventConfig();
				m.setMonitoringType(j);
				m.setScefRefId(j);
				m.setScefId(this.scefId);
				l.add(m);
			}
			
			GMonitoringEventConfig[] lmc = new GMonitoringEventConfig[l.size()];
			int co = 0;
			for (GMonitoringEventConfig c : l) {
			  lmc[co++] = c;
			}
			userProfile.setMc(lmc);
			
			userProfile.setDataQueueAddress(newMsg.getDataQueueAddress());
			userProfile.setMonitoroingQueue(newMsg.getMonitoroingQueue());
			userProfile.setErrorQueue(newMsg.getErrorQueue());

			
			setScefMsisdn = this.getAsyncHandler().set("SCEF-MSISDN-" + newMsg.getMsisdn(), new Gson().toJson(userProfile));
			setScefMsisdn.thenRun(listener);
		}
	}
	
	public void closedataBases() {
		syncDataConnector.closeDataBase();
		asyncDataConnector.closeDataBase();
		logger.info("=================================== closing data ==============================");
	}
/*	
	private int sendCirRequest(GMonitoringEventConfig[] mc, GAESE_CommunicationPattern[] cp) {
		try {
		  logger.info("sendCirRequest");
		    ClientS6tSession session = (ClientS6tSession)this.s6tSessionFactory.getNewSession(
		    		this.stack.getSessionFactory().getSessionId("S6t-"), ClientS6tSession.class, getS6tAuthApplicationId(), null);
			
			Request request = this.createRequest(session, this.s6tAuthApplicationId,
					JConfigurationInformationRequest.code,
					HSS_REALM,
					this.getConfiguration().getStringValue(OwnDiameterURI.ordinal(), "aaa://127.0.0.1:15868"));

			AvpSet reqSet = request.getAvps();
			if (mc != null && mc.length > 0) {
				for (GMonitoringEventConfig m : mc) {
					AvpSet monEvConf = reqSet.addGroupedAvp(Avp.MONITORING_EVENT_CONFIGURATION,
							this.getS6tAuthApplicationId().getVendorId(),true, false);
					monEvConf.addAvp(Avp.MONITORING_TYPE, m.getMonitoringType(), this.getS6tAuthApplicationId().getVendorId(), true, false);
					if (m.getScefRefId() == -1) { // delete
						for (int i : m.getScefRefIdForDelition()) {
							monEvConf.addAvp(Avp.SCEF_REFERENCE_ID_FOR_DELETION, i, this.getS6tAuthApplicationId().getVendorId(), true, false);
						}
					}
					else {
						monEvConf.addAvp(Avp.SCEF_ID, m.getScefId(), this.getS6tAuthApplicationId().getVendorId(), true, false, false);
						monEvConf.addAvp(Avp.SCEF_REFERENCE_ID, m.getScefRefId(), this.getS6tAuthApplicationId().getVendorId(), true, false);
					}
				}
			}

			if (cp != null && cp.length > 0) {
				
			}
			
			// send the message
			JConfigurationInformationRequest cir = s6tSessionFactory.createConfigurationInformationRequest(request);
			session.sendConfigurationInformationRequest(cir);
			
			if (logger.isInfoEnabled()) {
				logger.info("Configuration-Information-Request sent to HSS");
			}
		} catch (IllegalDiameterStateException e) {
			e.printStackTrace();
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (RouteException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}

		return 0;
	}
	
	private void sendNirRequest(GSCEFUserProfile profile) {
		try {
			ISessionFactory sessionFactory = ((ISessionFactory) this.stack.getSessionFactory());
			ClientS6tSession session = sessionFactory.getNewAppSession(this.getS6tAuthApplicationId(),
					ClientS6tSession.class);

			Request request = this.createRequest(session, this.s6tAuthApplicationId,
                    JNIDDInformationRequest.code,
					HSS_REALM,
					this.getConfiguration().getStringValue(OwnDiameterURI.ordinal(), "aaa://127.0.0.1:23000"));

			AvpSet reqSet = request.getAvps();
			
			AvpSet userIdentity = reqSet.addGroupedAvp(Avp.USER_IDENTIFIER, this.getS6tAuthApplicationId().getVendorId(), true, false);
			String userName = profile.getUserName();
			String externalId = profile.getExternalId();
			String msisdn = profile.getMsisdn();
			
			boolean userFlag = false;
			
			if (userName != null && userName.length() != 0) {
				userIdentity.addAvp(Avp.USER_NAME, userName, this.getS6tAuthApplicationId().getVendorId(), true, false, false);
				userFlag = true;
			}

			if (externalId != null && externalId.length() != 0) {
				userIdentity.addAvp(Avp.EXTERNAL_IDENTIFIER, externalId, this.getS6tAuthApplicationId().getVendorId(), true, false, false);
				userFlag = true;
			}

			if (msisdn != null && msisdn.length() != 0) {
				userIdentity.addAvp(Avp.MSISDN, BCDStringConverter.toBCD(msisdn), this.getS6tAuthApplicationId().getVendorId(), true, false);
				userFlag = true;
			}

		    if (userFlag == false) {
		    	if (logger.isErrorEnabled()) {
		    		logger.error("No user name, MSISDN or external-id defined for this profile Can't Send NIDD-Information-Request");
		    	}
		    	return;
		    }
		    
			JNIDDInformationRequest nir = s6tSessionFactory.createNIDDInformationRequest(request);
			session.sendNIDDInformationRequest(nir);
			
			if (logger.isInfoEnabled()) {
				logger.info("NIDD-Information-Request sent to HSS");
			}
		} catch (IllegalDiameterStateException e) {
			e.printStackTrace();
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (RouteException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}		
	}
	*/
	
	private GSCEFUserProfile getFormatedMessage(String msg) {
		GSCEFUserProfile up = new GSCEFUserProfile();
		
		String[] headers = msg.split("\\|");
		if (logger.isTraceEnabled()) {
			logger.trace("message arrived - " + msg);
		}
		
		up.setExternalId(headers[1]);
		String[] lines = headers[2].split(",");
		for (int i = 0; i < lines.length; i++) {
			String[] line = lines[i].split("=");
			if (logger.isTraceEnabled()) {
				logger.trace("new line " + line[0] + " - " + (line.length == 2 ? line[1] : "Null"));
			}
			if (line[0].equals("MSISDN")) {
				up.setMsisdn((line.length == 2 ? line[1] : null));
			}
			else if (line[0].equals("ExtID")) {
				//up.setMsisdn(line[1]);
			} 
			else if (line[0].equals("AppID")) {
				up.setAppId((line.length == 2 ? line[1] : null));
			} 
			else if (line[0].equals("AppRef")) {
				up.setAppRefId((line.length == 2 ? line[1] : null));
			} 
			else if (line[0].equals("MaxNIDD")) {
				up.setNumberOfNiddMessages(Integer.parseInt((line.length == 2 ? line[1] : "-1")));;
			} 
			else if (line[0].equals("NIDDDur")) {
				up.setNiddDuration(Integer.parseInt((line.length == 2 ? line[1] : "-1")));;
			} 
			else if (line[0].equals("mntrgEvent")) {
				up.setMonitoringFlags(Integer.parseInt((line.length == 2 ? line[1] : "0")));;
			} 
			else if (line[0].equals("dataQ")) {
				up.setDataQueueAddress((line.length == 2 ? line[1] : null));
			} 
			else if (line[0].equals("mntrgQ")) {
				up.setMonitoroingQueue((line.length == 2 ? line[1] : null));
			} 
			else if (line[0].equals("errQ")) {
				up.setErrorQueue((line.length == 2 ? line[1] : null));
			} 
		}
		
		up.setMc(null);			
		up.setCp(null);

		return up;
	}
/*	
	@Override
	public void configure(String configFile, String dictionaryFile) throws Exception {
		super.configure(configFile, dictionaryFile);
		if (logger.isInfoEnabled()) {
			logger.info("SCEF start configuration ");
		}

		//this.setT6aSessionFactory(new T6aSessionFactoryImpl(super.factory));
	    //this.getT6aSessionFactory().setServerSessionListener(this);
	    //this.setS6tSessionFactory(new S6tSessionFactoryImpl(super.factory));
	    //this.getS6tSessionFactory().setClientSessionListener(this);
	    
	    Network network = stack.unwrap(Network.class);
	    
	    network.addNetworkReqListener(this, s6tAuthApplicationId);
	    ((ISessionFactory) super.factory).registerAppFacory(ServerS6tSession.class, this.getS6tSessionFactory());

	    network.addNetworkReqListener(this, t6aAuthApplicationId);
	    ((ISessionFactory) super.factory).registerAppFacory(ServerT6aSession.class, this.getT6aSessionFactory());
	    
		if (logger.isInfoEnabled()) {
			logger.info("SCEF End configuration ");
		}
	}
	
    @Override
	public Answer processRequest(Request request) {
		long appId = request.getApplicationId();
	
		int commandCode = request.getCommandCode();
		if (t6aAuthApplicationId.getAuthAppId() == appId) {
			return processT6aRequest(request, commandCode);	
		}
		else if (s6tAuthApplicationId.getAuthAppId() == appId) {
			return processS6tRequest(request, commandCode);	
		}
		else {
			logger.error(new StringBuilder("processRequest - Not Supported message: ").append(commandCode)
					.append(" from interface : ").append(appId)
					.append(" from Class ").append(request.getClass().getName()).toString());
			return null;
		}
	}

	private Answer processT6aRequest(Request request, int commandCode) {
		Answer answer = null;
		T6aServerSessionImpl session = null;
		try {
			switch (commandCode) {
			case org.jdiameter.api.t6a.events.JConfigurationInformationRequest.code:
				// we may get it if there is support for t6ai not common use
			case JConnectionManagementRequest.code:
			case JMO_DataRequest.code:
			case org.jdiameter.api.t6a.events.JReportingInformationRequest.code:
				session = ((ISessionFactory) super.factory).getNewAppSession(request.getSessionId(),
			            t6aAuthApplicationId, ServerT6aSession.class, (Object)null);
				session.addStateChangeNotification(this);
				answer = session.processRequest(request);
				break;
			case JMT_DataRequest.code:
				logger.error(new StringBuilder("processT6aRequest - : Mt-Data-Request: Not Supported message in this state: ")
						.append(commandCode)
						.append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
						.append(request.getClass().getName()).toString());
				return answer;
			default:
				logger.error(new StringBuilder("processT6aRequest - Not Supported message: ").append(commandCode)
						.append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
						.append(request.getClass().getName()).toString());
				return null;
			}
			
		} catch (InternalException e) {
	        e.printStackTrace();
	    }
		return answer;
	}

	private Answer processS6tRequest(Request request, int commandCode) {
		Answer answer = null;
		try {
			switch (commandCode) {
			case JConfigurationInformationRequest.code:
				logger.error(new StringBuilder("processS6tRequest - : Configuration-Information-Request: Not Supported message in this state: ")
						.append(commandCode)
						.append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
						.append(request.getClass().getName()).toString());
				return answer;
			case JNIDDInformationRequest.code:
				logger.error(new StringBuilder("processS6tRequest - : NIDD-Information-Request: Not Supported message in this state: ")
						.append(commandCode)
						.append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
						.append(request.getClass().getName()).toString());
				return answer;
			case JReportingInformationRequest.code:
				S6tServerSessionImpl session = ((ISessionFactory) super.factory).getNewAppSession(request.getSessionId(),
			            s6tAuthApplicationId, ServerS6tSession.class, (Object)null);
				session.addStateChangeNotification(this);
				answer = session.processRequest(request);
				break;
			default:
				logger.error(new StringBuilder("processS6tRequest - Not Supported message: ").append(commandCode)
						.append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
						.append(request.getClass().getName()).toString());
				return null;
			}
		} catch (InternalException e) {
	        e.printStackTrace();
	    }
		
		return answer;
	}
	*/
	
	/** 
	 * 
	 */
	
	/*
	@Override
	public void doConfigurationInformationAnswerEvent(ClientS6tSession session,
			JConfigurationInformationRequest request, JConfigurationInformationAnswer answer)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		if (logger.isInfoEnabled()) {
			logger.info("Got S6t - Configuration Information Answer (CIA) from HSS");
		}
		// TODO Analyze message
		StringBuffer str = new StringBuffer("");
		boolean session_id = false;
		boolean auth_sessin_state = false;
		boolean orig_host = false;
		boolean orig_relm = false;
		AvpSet set = answer.getMessage().getAvps();
		Avp avp;

		try {
			if ((avp = set.getAvp(Avp.RESULT_CODE)) != null) {

			}
			if ((avp = set.getAvp(Avp.EXPERIMENTAL_RESULT)) != null) {
				AvpSet a = avp.getGrouped();
				logger.error(str.append("Got Error : ").append(a.getAvp(Avp.EXPERIMENTAL_RESULT_CODE).getUnsigned32())
				          .append(" from HSS on CIR request session id : ")
				          .append(set.getAvp(Avp.SESSION_ID).getUTF8String()).toString());
				return;
			}
			for (Avp a : set) {
				switch (a.getCode()) {
				case Avp.SESSION_ID:
					session_id = true;
					if (logger.isInfoEnabled()) {
						str.append("SESSION_ID : ").append(a.getUTF8String()).append("\n");
					}
					break;
				case Avp.DRMP:
					if (logger.isInfoEnabled()) {
						str.append("\tDRMP : ").append(a.getUTF8String()).append("\n");
					}
					break;
				case Avp.RESULT_CODE:
					if (logger.isInfoEnabled()) {
						str.append("\tRESULT_CODE : ").append(a.getUTF8String()).append("\n");
					}
					break;
				case Avp.EXPERIMENTAL_RESULT:
					if (logger.isInfoEnabled()) {
						str.append("\tEXPERIMENTAL_RESULT : ").append(a.getUTF8String()).append("\n");
					}
					break;
				case Avp.AUTH_SESSION_STATE:
					auth_sessin_state = true;
					break;

				case Avp.ORIGIN_HOST:
					orig_host = true;
					break;
				case Avp.ORIGIN_REALM:
					orig_relm = true;
				case Avp.OC_SUPPORTED_FEATURES:
					break;
				case Avp.OC_OLR:
					break;
				case Avp.SUPPORTED_FEATURES: // grouped
					break;
				case Avp.USER_IDENTIFIER:
					break;
				case Avp.MONITORING_EVENT_REPORT: // grouped
					break;
				case Avp.MONITORING_EVENT_CONFIG_STATUS: // Grouped
					break;
				case Avp.AESE_COMMUNICATION_PATTERN_CONFIG_STATUS: // Grouped
					break;
				case Avp.SUPPORTED_SERVICES: // Grouped
					break;
				case Avp.S6T_HSS_CAUSE:
					break;
				case Avp.FAILED_AVP: // Grouped
					break;
				case Avp.PROXY_INFO: // Grouped
					break;
				case Avp.ROUTE_RECORD: // Grouped
					break;
				default: // got Extra AVP'S
					break;
				}
			}
		} catch (AvpDataException e) {
			e.printStackTrace();
		}

		if (logger.isInfoEnabled()) {
			logger.info(str.toString());
		}

		// TODO send message to SCS on the user

	}

	@Override
	public void doReportingInformationRequestEvent(ClientS6tSession session, JReportingInformationRequest request)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		if (logger.isInfoEnabled()) {
			logger.info("Got S6t - Reporting Information Request (RIR) from HSS"); 
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doNIDDInformationAnswerEvent(ClientS6tSession session, JNIDDInformationRequest request,
			JNIDDInformationAnswer answer)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		if (logger.isInfoEnabled()) {
			logger.info("Got S6t - NIDD Information Answer (NIA) from HSS"); 
		}
		// TODO get the message
		
		// TODO update SCS/ECS on the registration
		
		
	}
	

	@Override
	public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("Got T6a - unknowen event from  MME"); 
		}
		
	}

	@Override
	public void doSendConfigurationInformationAnswerEvent(ServerT6aSession session,
			org.jdiameter.api.t6a.events.JConfigurationInformationRequest request,
			org.jdiameter.api.t6a.events.JConfigurationInformationAnswer answer)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("Got T6a - Configuration Information Answer (CIA) from MME"); 
		}
		
	}

	@Override
	public void doSendConfigurationInformationRequestEvent(ServerT6aSession session,
			org.jdiameter.api.t6a.events.JConfigurationInformationRequest request)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("Got T6a - Configuration Information Request (CIR) from MME"); 
		}
		
	}

	@Override
	public void doSendReportingInformationRequestEvent(ServerT6aSession session,
			org.jdiameter.api.t6a.events.JReportingInformationRequest request)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("Got T6a - Reporting-Information-Answer (RIR) from MME"); 
		}
		
	}

	@Override
	public void doSendMO_DataRequestEvent(ServerT6aSession session, JMO_DataRequest request)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("Got T6a - MO-Data-Request (ODR) from MME"); 
		}
		
	}

	@Override
	public void doSendMT_DataAnswertEvent(ServerT6aSession session, JMT_DataRequest request, JMT_DataAnswer answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("Got T6a - MT-Data-Answer (TDA) from MME"); 
		}
		
	}

	@Override
	public void doSendConnectionManagementAnswertEvent(ServerT6aSession session, JConnectionManagementRequest request,
			JConnectionManagementAnswer answer)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("Got T6a - Connection-Managemet-Answer (CMA) from MME"); 
		}
		
	}

	@Override
	public void doSendConnectionManagementRequestEvent(ServerT6aSession session, JConnectionManagementRequest request)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		if (logger.isInfoEnabled()) {
			logger.info("Got T6a - Connection-Managemet-Request (CMR) from MME"); 
		}
		
	}
	
	*/

	// setters and getters 
	public ApplicationId getT6aAuthApplicationId() {
		return t6aAuthApplicationId;
	}

	public void setT6aAuthApplicationId(ApplicationId t6aAuthApplicationId) {
		this.t6aAuthApplicationId = t6aAuthApplicationId;
	}

	public ApplicationId getS6tAuthApplicationId() {
		return s6tAuthApplicationId;
	}

	public void setS6tAuthApplicationId(ApplicationId s6tAuthApplicationId) {
		this.s6tAuthApplicationId = s6tAuthApplicationId;
	}

	public T6aSessionFactoryImpl getT6aSessionFactory() {
		return t6aSessionFactory;
	}

	public void setT6aSessionFactory(T6aSessionFactoryImpl t6aSessionFactory) {
		this.t6aSessionFactory = t6aSessionFactory;
	}

	public S6tSessionFactoryImpl getS6tSessionFactory() {
		return s6tSessionFactory;
	}

	public void setS6tSessionFactory(S6tSessionFactoryImpl s6tSessionFactory) {
		this.s6tSessionFactory = s6tSessionFactory;
	}

	public ConnectorImpl getSyncDataConnector() {
		return syncDataConnector;
	}

	public void setSyncDataConnector(ConnectorImpl syncDataConnector) {
		this.syncDataConnector = syncDataConnector;
	}

	public ConnectorImpl getAsyncDataConnector() {
		return asyncDataConnector;
	}

	public void setAsyncDataConnector(ConnectorImpl asyncDataConnector) {
		this.asyncDataConnector = asyncDataConnector;
	}

	public RedisStringAsyncCommands<String, String> getAsyncHandler() {
		return asyncHandler;
	}

	public void setAsyncHandler(RedisStringAsyncCommands<String, String> asyncHandler) {
		this.asyncHandler = asyncHandler;
	}

	public RedisStringCommands<String, String> getSyncHandler() {
		return syncHandler;
	}

	public void setSyncHandler(RedisStringCommands<String, String> syncHandler) {
		this.syncHandler = syncHandler;
	}

	public RedisPubSubAsyncCommands<String, String> getAsyncPubSubHandler() {
		return asyncPubSubHandler;
	}

	public void setAsyncPubSubHandler(RedisPubSubAsyncCommands<String, String> asyncPubSubHandler) {
		this.asyncPubSubHandler = asyncPubSubHandler;
	}

	public RedisPubSubCommands<String, String> getSyncPubSubHandler() {
		return syncPubSubHandler;
	}

	public void setSyncPubSubHandler(RedisPubSubCommands<String, String> syncPubSubHandler) {
		this.syncPubSubHandler = syncPubSubHandler;
	}

	public String getScefId() {
		return scefId;
	}
	
	
}
