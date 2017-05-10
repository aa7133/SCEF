package com.att.scef.hss;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

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
import org.jdiameter.api.ResultCode;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.api.s6a.ServerS6aSession;
import org.jdiameter.api.s6a.ServerS6aSessionListener;
import org.jdiameter.api.s6a.events.JAuthenticationInformationRequest;
import org.jdiameter.api.s6a.events.JCancelLocationAnswer;
import org.jdiameter.api.s6a.events.JCancelLocationRequest;
import org.jdiameter.api.s6a.events.JDeleteSubscriberDataAnswer;
import org.jdiameter.api.s6a.events.JDeleteSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JInsertSubscriberDataAnswer;
import org.jdiameter.api.s6a.events.JInsertSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JNotifyRequest;
import org.jdiameter.api.s6a.events.JPurgeUERequest;
import org.jdiameter.api.s6a.events.JResetAnswer;
import org.jdiameter.api.s6a.events.JResetRequest;
import org.jdiameter.api.s6a.events.JUpdateLocationRequest;
import org.jdiameter.api.s6t.ServerS6tSession;
import org.jdiameter.api.s6t.ServerS6tSessionListener;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationAnswer;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.common.impl.app.s6a.S6aSessionFactoryImpl;
import org.jdiameter.common.impl.app.s6t.S6tSessionFactoryImpl;
import org.jdiameter.server.impl.app.s6a.S6aServerSessionImpl;
import org.jdiameter.server.impl.app.s6t.S6tServerSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jdiameter.client.impl.helpers.Parameters.OwnDiameterURI;

import com.att.scef.data.AsyncDataConnector;
import com.att.scef.data.ConnectorImpl;
import com.att.scef.data.SyncDataConnector;
import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GCommunicationPatternSet;
import com.att.scef.gson.GHSSUserProfile;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.gson.GUserIdentifier;
import com.att.scef.interfaces.AbstractServer;
import com.att.scef.interfaces.S6aServer;
import com.att.scef.interfaces.S6tServer;
import com.att.scef.utils.AESE_CommunicationPattern;
import com.att.scef.utils.MonitoringEventConfig;
import com.att.scef.utils.UserIdentifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.async.RedisStringAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;

//public class HSS extends AbstractServer implements ServerS6aSessionListener, ServerS6tSessionListener,
//StateChangeListener<AppSession>{

public class HSS {


	protected final Logger logger = LoggerFactory.getLogger(HSS.class);

	private ConnectorImpl syncDataConnector;
	private ConnectorImpl asyncDataConnector;
	private RedisStringAsyncCommands<String, String> asyncHandler;
	private RedisStringCommands<String, String> syncHandler;
	
	
    private S6tServer s6tServer;
    private S6aServer s6aServer;
    
	private final static String DEFAULT_S6T_SERVER_NAME = "S6t-SERVER";
	private final static String DEFAULT_S6A_SERVER_NAME = "S6a-Server";

 
	private final static String DEFAULT_S6A_CONFIG_FILE = "/home/odldev/scef/src/main/resources/hss/config-hss-s6a.xml";
    private final static String DEFAULT_S6T_CONFIG_FILE = "/home/odldev/scef/src/main/resources/hss/config-hss-s6t.xml";
    private final static String DEFAULT_DICTIONARY_FILE = "/home/odldev/scef/src/main/resources/dictionary.xml";


	   public static void main(String[] args) {
	    	String s6tConfigFile = DEFAULT_S6T_CONFIG_FILE;
	    	String s6aConfigFile = DEFAULT_S6A_CONFIG_FILE;
	    	String dictionaryFile = DEFAULT_DICTIONARY_FILE;
	    	String host = "127.0.0.1";
	    	int port = 6379;
	    	String channel = "";
	    	
	    	
	    	for (int i = 0; i < args.length; i += 2) {
	    		if (args[i].equals("--s6t-conf")) {
	    			s6tConfigFile = args[i+1];
	    		}
	    		else if (args[i].equals("--s6a-conf")) {
	    			s6aConfigFile = args[i+1];
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
	    	
	    	new HSS(s6tConfigFile, s6aConfigFile, dictionaryFile, host, port, channel);
	    	
	    }

	
	@SuppressWarnings("unchecked")
	public HSS(String s6tConfigFile, String s6aConfigFile, String dictionaryFile, String host, int port, String channel) {
		super();
		asyncDataConnector = new ConnectorImpl();
		asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(AsyncDataConnector.class, host, port);

		syncDataConnector = new ConnectorImpl();
		syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(SyncDataConnector.class, host, port);


        this.s6tServer = new S6tServer(this, s6tConfigFile);
        this.s6aServer = new S6aServer(this, s6aConfigFile);
		

		try {
          this.s6tServer.init(DEFAULT_S6T_SERVER_NAME);
          this.s6tServer.start();

          this.s6aServer.init(DEFAULT_S6A_SERVER_NAME);
          this.s6aServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("=================================== HSS started ==============================");
	}

	public void closedataBases() {
		syncDataConnector.closeDataBase();
		asyncDataConnector.closeDataBase();
		logger.info("=================================== closing data ==============================");
	}
	
	public RedisStringAsyncCommands<String, String> getAsyncHandler() {
		return asyncHandler;
	}

	public RedisStringCommands<String, String> getSyncHandler() {
		return syncHandler;
	}



	private String getImsiFromUid(GUserIdentifier uid) {
		String imsi = null;
		if (uid.getMsisdn() != null && uid.getMsisdn().length() > 0) {
			imsi = this.getSyncHandler().get("HSS-MSISDN" + uid.getMsisdn());
		}
		else if (uid.getUserName() != null && uid.getUserName().length() > 0) {
			imsi = this.getSyncHandler().get("HSS-USER-NAME" + uid.getUserName());
		}
		else if (uid.getExternalId() != null && uid.getExternalId().length() > 0) {
			imsi = this.getSyncHandler().get("HSS-EXT-ID-" + uid.getExternalId());
		}
		return imsi;
	}

    private String setImsiFromUid(String imsi, GUserIdentifier uid) {
      String msisdn = uid.getMsisdn();
      if (imsi == null || imsi.length() == 0) {
        // we set the MSISDN as IMSI
        if (msisdn == null || msisdn.length() == 0) {
          logger.error("no MSISDN");
          return null;
        }
        imsi = msisdn;
      }
      if (msisdn != null && msisdn.length() != 0) {
        this.getSyncHandler().set("HSS-MSISDN" + msisdn, imsi);
      }
      if (uid.getUserName() != null && uid.getUserName().length() > 0) {
        this.getSyncHandler().set("HSS-USER-NAME" + uid.getUserName(), imsi);
      }
      if (uid.getExternalId() != null && uid.getExternalId().length() > 0) {
        this.getSyncHandler().set("HSS-EXT-ID-" + uid.getExternalId(), imsi);
      }
      return imsi;
    }

	public void handleConfigurationInformationRequestEvent(ServerS6tSession session, JConfigurationInformationRequest cir)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		if (logger.isInfoEnabled()) {
			logger.info("Got Configuration Information Request (CIR)");
		}
		AvpSet reqSet = cir.getMessage().getAvps();

		Avp userIdentifier = reqSet.getAvp(Avp.USER_IDENTIFIER);
		if (userIdentifier == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Configuration-Information-Request (CIR) without User-Identifier parameter");
			}
			s6tServer.sendCIAAnswer(session, cir, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			return;
		}
		
		GUserIdentifier uid;
		List<GAESE_CommunicationPattern> aeseComPattern = new ArrayList<GAESE_CommunicationPattern>();
		List<GMonitoringEventConfig> monitoringEvent = new ArrayList<GMonitoringEventConfig>();;

		JsonParser parser = new JsonParser();
		GHSSUserProfile hssData = null;

		boolean newUser = false;
		
		try {
			RedisFuture<String> future = null;
			uid = UserIdentifier.extractFromAvpSingle(userIdentifier);
			String imsi = getImsiFromUid(uid);

			if (imsi != null) {
				future = this.getAsyncHandler().get("HSS-IMSI-" + imsi);
			}
			else {
				logger.error(new StringBuffer("CIR user not found for MSISDN : ")
						.append(uid.getMsisdn() != null ? uid.getMsisdn() : "NULL")
						.append(" User Name : ").append(uid.getUserName() != null ? uid.getUserName() : "NULL")
						.append(" external ID  : ").append(uid.getExternalId() != null ? uid.getExternalId() : "NULL").toString());
				imsi = setImsiFromUid(null, uid);
				newUser = true;
				//s6tServer.sendCIAAnswer(session, cir, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
				//return;
			}

			Avp AESECommPatternAvp = reqSet.getAvp(Avp.AESE_COMMUNICATION_PATTERN);
			if (AESECommPatternAvp != null)  {
				aeseComPattern = AESE_CommunicationPattern.extractFromAvp(AESECommPatternAvp);	
			}
			AvpSet monitoringEventConfig = reqSet.getAvps(Avp.MONITORING_EVENT_CONFIGURATION);
			if (monitoringEventConfig != null) {
				monitoringEvent = MonitoringEventConfig.extractFromAvp(monitoringEventConfig);
			}

			if (newUser) {
              logger.info("CIR 3");
	          hssData = new GHSSUserProfile();
			  hssData.setMonitoringConfig((GMonitoringEventConfig[])monitoringEvent.toArray(new GMonitoringEventConfig[monitoringEvent.size()]));
			  hssData.setAESECommunicationPattern((GAESE_CommunicationPattern[])aeseComPattern.toArray(new GAESE_CommunicationPattern[aeseComPattern.size()]));
			  String value = new Gson().toJson(hssData);
			  String key = "HSS-IMSI-" + imsi;
			  logger.info("Write to redis Key = " + key + ", Value = " + value);
			  this.getSyncHandler().set(key, value);
			}
			else {
	            logger.info("CIR 4");
	            String value = future.get(5, TimeUnit.MILLISECONDS);
	            hssData = new Gson().fromJson(parser.parse(value), GHSSUserProfile.class);

	            hssData.setMonitoringConfig(MonitoringEventConfig.getNewHSSData(hssData, monitoringEvent));

	            //update new data to hss
	            hssData.setAESECommunicationPattern(AESE_CommunicationPattern.getNewHSSData(hssData, aeseComPattern));
	            logger.info("Setting the MME address to : " + this.s6aServer.getRemoteRealm());
	            hssData.setMMEAdress(this.s6aServer.getRemoteRealm());
                logger.info("CIR 4.1");
	 		}
	        
   		
    		GsonBuilder builder = new GsonBuilder();
            // we ignore Private fields
            builder.excludeFieldsWithModifiers(Modifier.PRIVATE);
            Gson gson = builder.create();
    		
    		RedisFuture<String> setHss = this.getAsyncHandler().set("HSS-MSISDN-" + uid.msisdn,gson.toJson(hssData));

    		// send answer to SCEF
    		s6tServer.sendCIAAnswer(session, cir, ResultCode.SUCCESS);
  		
    		// now we will send update to MME
    		String mmeAddress = hssData.getMMEAdress();
    		if (mmeAddress == null) {
    		  mmeAddress = this.s6aServer.getRemoteRealm();
    		}
    		if (mmeAddress == null || mmeAddress.length() == 0) {
    			// device is not connected to mme
    			return;
    		}
            logger.info("CIR 8");
    		
    		// we have MME 
    		//TODO remove renmarks
    		this.s6aServer.sendIDRRequest(session, hssData, mmeAddress);
	   	 
	   	    // finish the asynchronous write
	   	    setHss.get();
            logger.info("CIR 9");
	   	    
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			hssData = null;
			logger.error("Faild to get message from DB after 5 miliseconds or user not exists");
			s6tServer.sendCIAAnswer(session, cir, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			return;
		}
	}
	
	public void handleNIDDInformationRequestEvent(ServerS6tSession session, JNIDDInformationRequest nir)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		if (logger.isInfoEnabled()) {
			logger.info("Got NIDD Information Request (NIR)");
		}
		AvpSet reqSet = nir.getMessage().getAvps();

		Avp userIdentifier = reqSet.getAvp(Avp.USER_IDENTIFIER);
		if (userIdentifier == null) {
			if (logger.isInfoEnabled()) {
				logger.info("NIDD-Information-Request (NIR) without User-Identifier parameter");
			}
			s6tServer.sendNIAAnswer(session, nir, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			return;
		}
 
		GUserIdentifier uid = UserIdentifier.extractFromAvpSingle(userIdentifier);
		String imsi = getImsiFromUid(uid);
		if (imsi == null) {
			logger.error(new StringBuffer("NIDD user not found for MSISDN : ")
					.append(uid.getMsisdn() != null ? uid.getMsisdn() : "NULL")
					.append(" User Name : ").append(uid.getUserName() != null ? uid.getUserName() : "NULL")
					.append(" external ID  : ").append(uid.getExternalId() != null ? uid.getExternalId() : "NULL").toString());
			s6tServer.sendNIAAnswer(session, nir, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			return;
		}
		
		s6tServer.sendNIAAnswer(session, nir, ResultCode.SUCCESS);

		if (logger.isInfoEnabled()) {
			logger.info("NIDD alwayes supported in the current version");
		}
	}

	public void handleReportingInformationAnswerEvent(ServerS6tSession session, JReportingInformationRequest rir,
			JReportingInformationAnswer ria)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	public void handleAuthenticationInformationRequestEvent(ServerS6aSession session, JAuthenticationInformationRequest air)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	public void handleCancelLocationAnswerEvent(ServerS6aSession session, JCancelLocationRequest clr,
			JCancelLocationAnswer cla)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	public void handleDeleteSubscriberDataAnswerEvent(ServerS6aSession session, JDeleteSubscriberDataRequest dsr,
			JDeleteSubscriberDataAnswer dsa)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	public void handleInsertSubscriberDataAnswerEvent(ServerS6aSession session, JInsertSubscriberDataRequest idr,
			JInsertSubscriberDataAnswer ida)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		if (logger.isInfoEnabled()) {
			logger.info("GOT Insert Subscriber Data Answer (IDA) from MME"); 
		}
		StringBuffer sb = new StringBuffer("Insert Subscriber Data Answer (IDA)\n");
		
		try {
			for (Avp avp : ida.getMessage().getAvps()) {
				switch (avp.getCode()) {
				// < Session-Id >
				case Avp.SESSION_ID:
					if (logger.isInfoEnabled()) {
						sb.append("\tSESSION_ID : ").append(avp.getUTF8String());
					}
					break;
				// [ DRMP ]
				case Avp.DRMP:
					if (logger.isInfoEnabled()) {
						sb.append("\tDRMP : ").append(avp.toString());
					}
					break;
				// [ Vendor-Specific-Application-Id ]
				case Avp.VENDOR_SPECIFIC_APPLICATION_ID:
					if (logger.isInfoEnabled()) {
						sb.append("\t VENDOR_SPECIFIC_APPLICATION_ID : ").append(avp.toString());
					}
					break;
				// *[ Supported-Features ]
				case Avp.SUPPORTED_FEATURES:
					if (logger.isInfoEnabled()) {
						sb.append("\t SUPPORTED_FEATURES : ").append(avp.toString());
					}
					break;
				// [ Result-Code ]
				case Avp.RESULT_CODE:
					if (logger.isInfoEnabled()) {
						sb.append("\t RESULT_CODE : ").append(avp.toString());
					}
					break;
				// [ Experimental-Result ]
				case Avp.EXPERIMENTAL_RESULT:
					if (logger.isInfoEnabled()) {
						sb.append("\t EXPERIMENTAL_RESULT : ").append(avp.toString());
					}
					break;
				// { Auth-Session-State }
				case Avp.AUTH_SESSION_STATE:
					if (logger.isInfoEnabled()) {
						sb.append("\t AUTH_SESSION_STATE : ").append(avp.toString());
					}
					break;
				// { Origin-Host }
				case Avp.ORIGIN_HOST:
					if (logger.isInfoEnabled()) {
						sb.append("\t ORIGIN_HOST : ").append(avp.toString());
					}
					break;
				// { Origin-Realm }
				case Avp.ORIGIN_REALM:
					if (logger.isInfoEnabled()) {
						sb.append("\t ORIGIN_REALM : ").append(avp.toString());
					}
					break;
				// [ IMS-Voice-Over-PS-Sessions-Supported ]
				case Avp.IMS_VOICE_OVER_PS_SESSIONS_SUPPORTED:
					if (logger.isInfoEnabled()) {
						sb.append("\t IMS_VOICE_OVER_PS_SESSIONS_SUPPORTED : ").append(avp.toString());
					}
					break;
				// [ Last-UE-Activity-Time ]
				case Avp.LAST_UE_ACTIVITY_TIME:
					if (logger.isInfoEnabled()) {
						sb.append("\t LAST_UE_ACTIVITY_TIME : ").append(avp.toString());
					}
					break;
				// [ RAT-Type ]
				case Avp.RAT_TYPE:
					if (logger.isInfoEnabled()) {
						sb.append("\t RAT_TYPE : ").append(avp.toString());
					}
					break;
				// [ IDA-Flags ]
				case Avp.IDA_FLAGS:
					if (logger.isInfoEnabled()) {
						sb.append("\t IDA_FLAGS : ").append(avp.toString());
					}
					break;
				// [ EPS-User-State ]
				case Avp.EPS_USER_STATE:
					if (logger.isInfoEnabled()) {
						sb.append("\t EPS_USER_STATE : ").append(avp.toString());
					}
					break;
				// [ EPS-Location-Information ]
				case Avp.EPS_LOCATION_INFORMATION:
					if (logger.isInfoEnabled()) {
						sb.append("\t EPS_LOCATION_INFORMATION : ").append(avp.toString());
					}
					break;
				// [Local-Time-Zone ]
				case Avp.LOCAL_TIME_ZONE:
					if (logger.isInfoEnabled()) {
						sb.append("\t LOCAL_TIME_ZONE : ").append(avp.toString());
					}
					break;
				// [ Supported-Services ]
				case Avp.SUPPORTED_SERVICES:
					if (logger.isInfoEnabled()) {
						sb.append("\t SUPPORTED_SERVICES : ").append(avp.toString());
					}
					break;
				// *[ Monitoring-Event-Report ]
				case Avp.MONITORING_EVENT_REPORT:
					if (logger.isInfoEnabled()) {
						sb.append("\t MONITORING_EVENT_REPORT : ").append(avp.toString());
					}
					break;
				// *[ Monitoring-Event-Config-Status ]
				case Avp.MONITORING_EVENT_CONFIG_STATUS:
					if (logger.isInfoEnabled()) {
						sb.append("\t MONITORING_EVENT_CONFIG_STATUS : ").append(avp.toString());
					}
					break;
				// *[ Failed-AVP ]
				case Avp.FAILED_AVP:
					if (logger.isInfoEnabled()) {
						sb.append("\t FAILED_AVP : ").append(avp.toString());
					}
					break;
				// *[ Proxy-Info ]
				case Avp.PROXY_INFO:
					if (logger.isInfoEnabled()) {
						sb.append("\t PROXY_INFO : ").append(avp.toString());
					}
					break;
				// *[ Route-Record ]
				case Avp.RECORD_ROUTE:
					if (logger.isInfoEnabled()) {
						sb.append("\t RECORD_ROUTE : ").append(avp.toString());
					}
					break;
				default:
					if (logger.isInfoEnabled()) {
						sb.append("\t undefined AVP code ").append(avp.getCode()).append(" - ").append(avp.toString());
					}
					break;

				}
			}
			if (logger.isInfoEnabled()) {
				logger.info(sb.toString());
			}
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void handleNotifyRequestEvent(ServerS6aSession session, JNotifyRequest nor)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	public void handleOtherEvent(AppSession appSession, AppRequestEvent applicationRequest, AppAnswerEvent applicationAnswer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	public void handlePurgeUERequestEvent(ServerS6aSession session, JPurgeUERequest pur)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	public void handleResetAnswerEvent(ServerS6aSession session, JResetRequest rsr, JResetAnswer rsa)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	public void handleUpdateLocationRequestEvent(ServerS6aSession session, JUpdateLocationRequest ulr)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

}
