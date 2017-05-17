package com.att.scef.scef;

import static org.jdiameter.client.impl.helpers.Parameters.OwnDiameterURI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Mode;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.ResultCode;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6t.ClientS6tSession;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.api.t6a.ServerT6aSession;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataAnswer;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.data.AsyncDataConnector;
import com.att.scef.data.AsyncPubSubConnector;
import com.att.scef.data.ConnectorImpl;
import com.att.scef.data.PubSubConnectorImpl;
import com.att.scef.data.SyncDataConnector;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.gson.GSCEFUserProfile;
import com.att.scef.gson.GUserIdentifier;
import com.att.scef.utils.MonitoringType;
import com.att.scef.utils.UserIdentifier;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.SetArgs;
import com.lambdaworks.redis.api.async.RedisStringAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.lambdaworks.redis.pubsub.api.async.RedisPubSubAsyncCommands;
import com.lambdaworks.redis.pubsub.api.sync.RedisPubSubCommands;


public class SCEF {

	protected final Logger logger = LoggerFactory.getLogger(SCEF.class); //this.getClass());
 
	private ConnectorImpl syncDataConnector;
	private ConnectorImpl asyncDataConnector;
	private RedisStringAsyncCommands<String, String> asyncHandler;
	private RedisStringCommands<String, String> syncHandler;

	private PubSubConnectorImpl syncPubSubConnector;
	private PubSubConnectorImpl asyncPubSubConnector;
	private RedisPubSubAsyncCommands<String, String> asyncPubSubHandler;
	private RedisPubSubCommands<String, String> syncPubSubHandler;

	
	private String scefId = null;
	
	private final static String DEFAULT_S6T_CLIENT_NAME = "S6T-CLIENT";
	private final static String DEFAULT_T6A_SERVER_NAME = "T6A-SERVER";
	
	private final static String DEFAULT_SCEF_ID = "aaa://127.0.0.1:2300";
    private final static String DEFAULT_S6T_CONFIG_FILE = "/home/odldev/scef/src/main/resources/scef/config-scef-s6t.xml";
    private final static String DEFAULT_T6A_CONFIG_FILE = "/home/odldev/scef/src/main/resources/scef/config-scef-t6a.xml";
    private final static String DEFAULT_DICTIONARY_FILE = "/home/odldev/scef/src/main/resources/dictionary.xml";

    private S6tClient s6tClient;
    private T6aServer t6aServer;
    
    private final static String SCEF_REF_ID_PREFIX = "SCEF-REF-ID-";
    private final static String SCEF_MSISDN_PREFIX = "SCEF-MSISDN-";
	
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
		this.t6aServer = new T6aServer(this, t6aConfigFile);
		try {
			this.s6tClient.init(DEFAULT_S6T_CLIENT_NAME);
		    this.s6tClient.start(Mode.ANY_PEER, 10, TimeUnit.SECONDS);
		    
		    this.t6aServer.init(DEFAULT_T6A_SERVER_NAME);
		    this.t6aServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.scefId = this.s6tClient.getStack().getMetaData().getConfiguration().getStringValue(OwnDiameterURI.ordinal(), DEFAULT_SCEF_ID);
		if (logger.isInfoEnabled()) {
	        logger.info("=================================== S6t ==============================");
		  this.s6tClient.checkConfig();
	        logger.info("=================================== T6a ==============================");
		  this.t6aServer.checkConfig();
		}

		
		logger.info("=================================== SCEF started ==============================");
	}
	
	/**
	 * 
	 * @param msg
	 */
	public void sendDiamterMessages(String msg) {
	    logger.info("sendDiamterMessages");
		GSCEFUserProfile newMsg = getFormatedMessage(msg);
		RedisFuture<String> setScefExternId = null;
        RedisFuture<String> setScefMsisdn = null;
        RedisFuture<String> setScefUserName = null;
        List<RedisFuture<String>> setScefRefId = new ArrayList<RedisFuture<String>>();
		
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
			msisdn = newMsg.getMsisdn();
			
			// translate to monitoring type list all requested monitoring flags
			List<Integer> ml = MonitoringType.getMonitorinTypeList(newMsg.getMonitoringFlags());
			GMonitoringEventConfig[] mc = new GMonitoringEventConfig[ml.size()];


			for (int i = 0; i < ml.size(); i++) {
				mc[i] = new GMonitoringEventConfig();
				mc[i].setMonitoringType(ml.get(i));
				int scefRefId = SCEF_Reference_ID_Generator.getSCEFRefID();
				mc[i].setScefRefId(scefRefId);
                setScefRefId.add(this.getAsyncHandler().set(SCEF_REF_ID_PREFIX + scefRefId, msisdn));
                if (logger.isInfoEnabled()) {
                  logger.info("SCEF ref id = " + scefRefId + " MSISDN = " + msisdn);
                }
				//get the SCEF address and fill the parameter from the configuration 
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
			setScefMsisdn = this.getAsyncHandler().set(SCEF_MSISDN_PREFIX + newMsg.getMsisdn(), new Gson().toJson(newMsg));
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
			String data = this.getSyncHandler().get(SCEF_MSISDN_PREFIX + newMsg.getMsisdn());
			if (logger.isInfoEnabled()) {
				logger.info(new StringBuilder("got a message for user : ")
						.append(msisdn).append(" message = ")
						.append(msg).append("Data = ").append(data)
						.toString());
			}
			
			GSCEFUserProfile userProfile = new Gson().fromJson(new JsonParser().parse(data), GSCEFUserProfile.class);
			
			List<Integer> mapToAdd = MonitoringType.getNewMonitoringTypeList(userProfile.getMonitoringFlags(), newMsg.getMonitoringFlags());
			List<Integer> mapToDelete = MonitoringType.getDeletedMonitoringTypeList(userProfile.getMonitoringFlags(), newMsg.getMonitoringFlags());
			
			userProfile.setMonitoringFlags(MonitoringType.getnextMonitoringMap(userProfile.getMonitoringFlags(), newMsg.getMonitoringFlags()));

			GMonitoringEventConfig[] mc = new GMonitoringEventConfig[mapToAdd.size() + mapToDelete.size()];
			
			int i = 0;
			for (; i < mapToAdd.size(); i++) {
				mc[i] = new GMonitoringEventConfig();
				mc[i].setMonitoringType(mapToAdd.get(i));
                int scefRefId = SCEF_Reference_ID_Generator.getSCEFRefID();
                mc[i].setScefRefId(scefRefId);
                setScefRefId.add(this.getAsyncHandler().set(SCEF_REF_ID_PREFIX + scefRefId, msisdn));
                if (logger.isInfoEnabled()) {
                  logger.info("SCEF ref id = " + scefRefId);
                }
				mc[i].setScefId(this.scefId);
			}
			//current support is just for 1 SCEF ref ID so the delete will be the same.
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
			
            logger.info("Sending NIDD-Information-Request (NIR)");
            this.s6tClient.sendNirRequest(newMsg);

            if (userProfile.getDataQueueAddress() == null || (userProfile.getDataQueueAddress()).length() == 0) {
				if (newMsg.getDataQueueAddress() != null && newMsg.getDataQueueAddress().length() != 0) {
					// send NIR
	                logger.info("Sending NIDD-Information-Request (NIR)");
					this.s6tClient.sendNirRequest(newMsg);
				}
			}
			
			GMonitoringEventConfig[] nextMap = userProfile.getMc();
			List<GMonitoringEventConfig> l = new ArrayList<GMonitoringEventConfig>();
			for (int j = 0; j < nextMap.length; j++) {
				if (mapToDelete.contains(nextMap[j].getMonitoringType())) {
				  SetArgs args = new SetArgs();
				  args.ex(3);
				  this.getSyncHandler().set(SCEF_REF_ID_PREFIX + nextMap[j].getScefRefId(), "", args);
				  if (logger.isInfoEnabled()) {
				    logger.info("Removing SCEF Referancre ID : " + SCEF_REF_ID_PREFIX + nextMap[j].getScefRefId());
				  }
                  nextMap[j] = null;
				}
				else {
					l.add(nextMap[j]);
				}
			}
			for (int j : mapToAdd) {
				GMonitoringEventConfig m = new GMonitoringEventConfig();
				m.setMonitoringType(j);
                int scefRefId = SCEF_Reference_ID_Generator.getSCEFRefID();
                m.setScefRefId(scefRefId);
                setScefRefId.add(this.getAsyncHandler().set(SCEF_REF_ID_PREFIX + scefRefId, String.valueOf(msisdn)));
                if (logger.isInfoEnabled()) {
                  logger.info("SCEF ref id = " + scefRefId);
                }
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

			
			setScefMsisdn = this.getAsyncHandler().set(SCEF_MSISDN_PREFIX + newMsg.getMsisdn(), new Gson().toJson(userProfile));
			setScefMsisdn.thenRun(listener);

			if (setScefRefId.size() > 0) {
			  for (RedisFuture<String> f : setScefRefId) {
			    f.thenRun(listener);
			  }
            }
		}
	}
	
	public void closedataBases() {
		syncDataConnector.closeDataBase();
		asyncDataConnector.closeDataBase();
		logger.info("=================================== closing data ==============================");
	}
	
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

	// setters and getters 


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
	
  public void handleOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error(
        "Received \"S6t Other\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  public void handleConfigurationInformationAnswerEvent(ClientS6tSession session,
      JConfigurationInformationRequest request, JConfigurationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    StringBuffer str = new StringBuffer("");
    boolean session_id = false;
    boolean auth_sessin_state = false;
    boolean orig_host = false;
    boolean orig_relm = false;
    try {
      for (Avp a : answer.getMessage().getAvps()) {
        switch (a.getCode()) {
        case Avp.SESSION_ID:
          session_id = true;
          str.append("SESSION_ID : ").append(a.getUTF8String()).append("\n");
          break;
        case Avp.DRMP:
          str.append("\tDRMP : ").append(a.getUTF8String()).append("\n");
          break;
        case Avp.RESULT_CODE:
          str.append("\tRESULT_CODE : ").append(a.getInteger32()).append("\n");
          break;
        case Avp.EXPERIMENTAL_RESULT:
          str.append("\tEXPERIMENTAL_RESULT : ").append(a.getInteger32()).append("\n");
          break;
        case Avp.AUTH_SESSION_STATE:
          auth_sessin_state = true;
          break;

        case Avp.ORIGIN_HOST:
          orig_host = true;
          break;
        case Avp.ORIGIN_REALM:
          orig_relm = true;
          break;
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
    if (!session_id || !auth_sessin_state || !orig_host || !orig_relm) {
      logger.error("Configuration-Information-Answer (CIA) - mandatory paramters are missing");
    }
    logger.info(str.toString());
  }

  public void handleReportingInformationRequestEvent(ClientS6tSession session, JReportingInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    if (logger.isInfoEnabled()) {
      logger.info("Got RIR from HSS");
    }
    this.s6tClient.sendRIA(session, request, ResultCode.SUCCESS);
  }

  public void handleNIDDInformationAnswerEvent(ClientS6tSession session, JNIDDInformationRequest request,
      JNIDDInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    boolean session_id = false;
    boolean auth_sessin_state = false;
    boolean orig_host = false;
    boolean orig_relm = false;

    StringBuffer str = new StringBuffer("");
    try {
      for (Avp a : answer.getMessage().getAvps()) {
        switch (a.getCode()) {
        case Avp.SESSION_ID:
          session_id = true;
          str.append("SESSION_ID : ").append(a.getUTF8String()).append("\n");
          break;
        case Avp.DRMP:
          str.append("\tDRMP : ").append(a.getUTF8String()).append("\n");
          break;
        case Avp.RESULT_CODE:
          str.append("\tRESULT_CODE : ").append(a.getInteger32()).append("\n");
          break;
        case Avp.EXPERIMENTAL_RESULT:
          str.append("\tEXPERIMENTAL_RESULT : ").append(a.getInteger32()).append("\n");
          break;
        case Avp.AUTH_SESSION_STATE:
          auth_sessin_state = true;
          break;

        case Avp.ORIGIN_HOST:
          orig_host = true;
          break;
        case Avp.ORIGIN_REALM:
          orig_relm = true;
          break;
        case Avp.OC_SUPPORTED_FEATURES:
          break;
        case Avp.OC_OLR:
          break;
        case Avp.SUPPORTED_FEATURES: // grouped
          break;
        case Avp.USER_IDENTIFIER:
          break;
        case Avp.NIDD_AUTHORIZATION_RESPONSE: // grouped
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
    if (!session_id || !auth_sessin_state || !orig_host || !orig_relm) {
      logger.error("NIDD-Information-Answer (NIA) - mandatory paramters are missing");
    }
    logger.info(str.toString());
  }

  public void handleT6aOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a Other\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  public void handleT6aConfigurationInformationAnswerEvent(ServerT6aSession session,
      org.jdiameter.api.t6a.events.JConfigurationInformationRequest request, org.jdiameter.api.t6a.events.JConfigurationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("not yet implemented \"T6a CIA\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  public void handleT6aConfigurationInformationRequestEvent(ServerT6aSession session,
      org.jdiameter.api.t6a.events.JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("not yet implemented \"T6a CIR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void handleT6aReportingInformationRequestEvent(ServerT6aSession session,
      org.jdiameter.api.t6a.events.JReportingInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    if (logger.isInfoEnabled()) {
      logger.info("Got Reporting Information Request (RIR) from MME");
    }
    try {
      AvpSet reqSet = request.getMessage().getAvps();
      AvpSet monReport = reqSet.getAvps(Avp.MONITORING_EVENT_REPORT);
      if (monReport == null) {
        this.t6aServer.sendRIA(session, request, ResultCode.DIAMETER_ERROR_OPERATION_NOT_ALLOWED);
      }
      
      for (Avp m : monReport) {
        int scefRefId = 0;
        int monitoringType = -1;
        String scefID = null;
        int reachabilityInfo = -1;
        AvpSet epsLocation = null;
        AvpSet commuFail = null;
        List<AvpSet> numOfUEPerLocation = new ArrayList<AvpSet>();
        for (Avp a : m.getGrouped()) {
          
          if (a.getCode() == Avp.SCEF_REFERENCE_ID) {
            scefRefId = (int)a.getUnsigned32();
            logger.info("SCEF_REFERENCE_ID = " + scefRefId);
          }
          else if (a.getCode() == Avp.SCEF_ID) {
            scefID = a.getDiameterIdentity();
            logger.info("SCEF_ID = " + scefID);
          }
          else if (a.getCode() == Avp.MONITORING_TYPE) {
            monitoringType = a.getInteger32();
            logger.info("Monitoring Type (" + monitoringType + ") " + MonitoringType.getMonitoringText(monitoringType));
          }
          else if (a.getCode() == Avp.REACHABILITY_INFORMATION) {
            reachabilityInfo = (int)a.getUnsigned32();
            logger.info("REACHABILITY_INFORMATION = " + reachabilityInfo);
          }
          else if (a.getCode() == Avp.EPS_LOCATION_INFORMATION) {
            epsLocation = a.getGrouped();
            Avp mmeLoc = epsLocation.getAvp(Avp.MME_LOCATION_INFORMATION);
            Avp sgsnLoc = epsLocation.getAvp(Avp.SGSN_LOCATION_INFORMATION);
            
          }
          else if (a.getCode() == Avp.COMMUNICATION_FAILURE_INFORMATION) {
            commuFail = a.getGrouped();
            Avp causeType = commuFail.getAvp(Avp.CAUSE_TYPE);
          }
          else if (a.getCode() == Avp.NUMBER_OF_UE_PER_LOCATION_REPORT) {
            numOfUEPerLocation.add(a.getGrouped());
          }
        }
        
        String key = SCEF_REF_ID_PREFIX + scefRefId;
        String msisdn = this.getSyncHandler().get(key);
        if (msisdn == null || msisdn.length() == 0) {
          logger.error("SCEF_REFERENCE_ID for user not found in DB for Key " + key);
          this.t6aServer.sendRIA(session, request, ResultCode.INVALID_AVP_VALUE);
          return;
        }
        // get the monitoring queue for the user
        if (logger.isInfoEnabled()) {
          logger.info("monitoring event for user : " + msisdn);
        }
        String data = this.getSyncHandler().get(SCEF_MSISDN_PREFIX+ msisdn);
        if (data == null || data.length() == 0) {
          logger.error("data not found for user : " + msisdn);
          this.t6aServer.sendRIA(session, request, ResultCode.INVALID_AVP_VALUE);
          return;
        }
        GSCEFUserProfile userProfile = new Gson().fromJson(new JsonParser().parse(data), GSCEFUserProfile.class);
        
        String channel = userProfile.getMonitoroingQueue();
        
        if (logger.isInfoEnabled()) {
          logger.info("Data queue for user : " + msisdn + " is : " + channel);
        }
       

        
      }

    } catch (AvpDataException e) {
      e.printStackTrace();
    }
    
  }

  public void handleT6aMO_DataRequestEvent(ServerT6aSession session, JMO_DataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    if (logger.isInfoEnabled()) {
      logger.info("Got MO Data Request (ODR) from MME");
    }
    AvpSet reqSet = request.getMessage().getAvps();
    
    try {
      Avp userIdentifier = reqSet.getAvp(Avp.USER_IDENTIFIER);
      if (userIdentifier == null) {
          if (logger.isInfoEnabled()) {
              logger.info("MO-Data-Request (ODR) missing the mandatory \"User-Identifier\" parameter");
          }
          this.t6aServer.sendODA(session, request, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
          return;
      }
      
      Avp bearer = reqSet.getAvp(Avp.BEARER_IDENTIFIER);
      if (bearer == null) {
        if (logger.isInfoEnabled()) {
            logger.info("MO-Data-Request (ODR) missing the mandatory \"Bearer-Identifier\" parameter");
        }
        this.t6aServer.sendODA(session, request, ResultCode.DIAMETER_ERROR_INVALID_EPS_BEARER);
        return;
      }

      byte[] bearearIdentifier = bearer.getOctetString();
      String bearerString = new String(bearearIdentifier);
      /*
      StringBuffer sb = new StringBuffer();
      for (byte b : bearearIdentifier) {
        sb.append((char)b);
      }
      */
      logger.info("Bearer id = " + bearerString);
      
      //TODO  need to send data to application
      Avp niddData = reqSet.getAvp(Avp.NON_IP_DATA);
      String msg = niddData.getUTF8String();
      logger.info("got message : \"" + msg + "\"");

      GUserIdentifier uid;
      uid = UserIdentifier.extractFromAvpSingle(userIdentifier);
      String msisdn = uid.getMsisdn();

      String data = this.getSyncHandler().get(SCEF_MSISDN_PREFIX + msisdn);

      GSCEFUserProfile userProfile = null;
      
      if (data == null || data.length() == 0) {
        logger.error("User not found in scef data base");
        this.t6aServer.sendODA(session, request, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
        return;
      }
      userProfile =  new Gson().fromJson(new JsonParser().parse(data), GSCEFUserProfile.class);
      this.getAsyncPubSubHandler().publish(userProfile.getDataQueueAddress(), msg);
      userProfile.getDataQueueAddress();
      
    } catch (AvpDataException e) {
      e.printStackTrace();
    }
    
    this.t6aServer.sendODA(session, request, ResultCode.SUCCESS);
  }

  public void handleT6aMT_DataAnswertEvent(ServerT6aSession session, JMT_DataRequest request, JMT_DataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("not yet implemented \"T6a TDA\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  public void handleT6aConnectionManagementAnswertEvent(ServerT6aSession session, JConnectionManagementRequest request,
      JConnectionManagementAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("not yet implemented \"T6a CMA\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  public void handleT6aConnectionManagementRequestEvent(ServerT6aSession session, JConnectionManagementRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("not yet implemented \"T6a CMR\" event, request[" + request + "], on session[" + session + "]");
  }

  
  
}
