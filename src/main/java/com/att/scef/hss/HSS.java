package com.att.scef.hss;

import java.io.InputStream;
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

import static org.jdiameter.client.impl.helpers.Parameters.OwnDiameterURI;


import com.att.scef.data.ConnectorImpl;
import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GCommunicationPatternSet;
import com.att.scef.gson.GHSSUserProfile;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.gson.GUserIdentifier;
import com.att.scef.utils.AESE_CommunicationPattern;
import com.att.scef.utils.AbstractServer;
import com.att.scef.utils.MonitoringEventConfig;
import com.att.scef.utils.UserIdentifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.RedisStringCommands;

public class HSS extends AbstractServer implements ServerS6aSessionListener, ServerS6tSessionListener,
                                                   StateChangeListener<AppSession>{

	//protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ApplicationId s6aAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777308);
	private ApplicationId s6tAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777345);

	private S6aSessionFactoryImpl s6aSessionFactory;
	private S6tSessionFactoryImpl s6tSessionFactory;

	
	private ConnectorImpl syncDataConnector;
	private ConnectorImpl asyncDataConnector;
	private RedisStringAsyncCommands<String, String> asyncHandler;
	private RedisStringCommands<String, String> syncHandler;
	
	private HssS6aMessages s6aMessages;
	private HssS6tMessages s6tMessages;

	@SuppressWarnings("unchecked")
	public HSS() {
		super();
		asyncDataConnector = new ConnectorImpl();
		asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(RedisStringAsyncCommands.class);

		syncDataConnector = new ConnectorImpl();
		syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(RedisStringCommands.class);
		
		this.setS6aMessages(new HssS6aMessages());
		this.setS6tMessages(new HssS6tMessages());
		
		logger.info("=================================== HSS started ==============================");
	}
	
	@SuppressWarnings("unchecked")
	public HSS(String host, int port) {
		super();
		asyncDataConnector = new ConnectorImpl();
		asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(RedisStringAsyncCommands.class, host, port);

		syncDataConnector = new ConnectorImpl();
		syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(RedisStringCommands.class, host, port);

		this.setS6aMessages(new HssS6aMessages());
		this.setS6tMessages(new HssS6tMessages());

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


	
	@Override
	public void configure(InputStream configInputStream, String dictionaryFile) throws Exception {
		super.configure(configInputStream, dictionaryFile);

	    this.s6aSessionFactory = new S6aSessionFactoryImpl(super.factory);
	    this.s6aSessionFactory.setServerSessionListener(this);
	    this.s6tSessionFactory = new S6tSessionFactoryImpl(super.factory);
	    this.s6tSessionFactory.setServerSessionListener(this);
	    
	    
	    //this.serverS6asession =
	    //          this.s6aSessionFactory.getNewAppSession(this.s6aSessionFactory.getSessionId("xxTESTxx"), getApplicationId(), ServerS6aSession.class, null); // true...

	    Network network = stack.unwrap(Network.class);
	    
	    network.addNetworkReqListener(this, s6aAuthApplicationId);
	    ((ISessionFactory) super.factory).registerAppFacory(ServerS6aSession.class, s6aSessionFactory);

	    network.addNetworkReqListener(this, s6tAuthApplicationId);
	    ((ISessionFactory) super.factory).registerAppFacory(ServerS6tSession.class, s6tSessionFactory);
	}
	
    @Override
	public Answer processRequest(Request request) {
		long appId = request.getApplicationId();
	
		int commandCode = request.getCommandCode();
		if (s6aAuthApplicationId.getAuthAppId() == appId) {
			return processS6aRequest(request, commandCode);	
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

	private Answer processS6aRequest(Request request, int commandCode) {
		Answer answer = null;
		try {
			switch (commandCode) {
			case JUpdateLocationRequest.code:
				S6aServerSessionImpl session = ((ISessionFactory) super.factory).getNewAppSession(request.getSessionId(),
			            s6aAuthApplicationId, ServerS6aSession.class, (Object)null);
				session.addStateChangeNotification(this);
				answer = session.processRequest(request);
				break;
			case JCancelLocationRequest.code:
				break;
			case JAuthenticationInformationRequest.code:
				break;
			case JInsertSubscriberDataRequest.code:
				break;
			case JDeleteSubscriberDataRequest.code:
				break;
			case JPurgeUERequest.code:
				break;
			case JResetRequest.code:
				break;
			case JNotifyRequest.code:
				break;

			default:
				logger.error(new StringBuilder("processS6aRequest - Not Supported message: ").append(commandCode)
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
			case JNIDDInformationRequest.code:
				S6tServerSessionImpl session = ((ISessionFactory) super.factory).getNewAppSession(request.getSessionId(),
			            s6tAuthApplicationId, ServerS6tSession.class, (Object)null);
				session.addStateChangeNotification(this);
				answer = session.processRequest(request);
				break;
			case JReportingInformationRequest.code:
				logger.error(new StringBuilder("processS6tRequest - : Reporting-Information-Request: Not Supported message in this state: ")
						.append(commandCode)
						.append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
						.append(request.getClass().getName()).toString());
				return answer;
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
	
	private void sendCIAAnswer(ServerS6tSession session, JConfigurationInformationRequest cir, int resultCode) {
		try {
			Answer answer = (Answer) super.createAnswer((Request) cir.getMessage(), resultCode, getS6tAuthApplicationId());

			// for extra avps
			AvpSet set = answer.getAvps();

			session.sendConfigurationInformationAnswer(this.getS6tMessages().createConfigurationInformationAnswer(answer));
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (IllegalDiameterStateException e) {
			e.printStackTrace();
		} catch (RouteException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}
	}
	
	private void sendIDRRequest(AppSession session, GHSSUserProfile hssData, String mmeAddress) {
		// build  IDR message to mme for update user data
		try {
			Request idr = this.createRequest(session, this.s6aAuthApplicationId, JInsertSubscriberDataRequest.code,
					HSS_REALM,
					this.getConfiguration().getStringValue(OwnDiameterURI.ordinal(), "aaa://127.0.0.1:23000"));
			AvpSet reqSet = idr.getAvps();
			idr.setRequest(true);

			// add data
			// < Insert-Subscriber-Data-Request> ::= < Diameter Header: 319,
			// REQ, PXY, 16777251 >
			// < Session-Id > by createRequest
			// [ DRMP ]
			// [ Vendor-Specific-Application-Id ] by createRequest
			// { Auth-Session-State } by createRequest
			// { Origin-Host } by createRequest
			// { Origin-Realm } by createRequest
			// { Destination-Host }
			reqSet.addAvp(Avp.DESTINATION_HOST, mmeAddress, true);
			// { Destination-Realm }
			reqSet.addAvp(Avp.DESTINATION_REALM, MME_REALM, true);
			// { User-Name }
			reqSet.addAvp(Avp.USER_NAME, hssData.getIMSI(), true);
			// *[ Supported-Features]
			// { Subscription-Data}

			buildUserDataAvp(hssData, reqSet.addGroupedAvp(Avp.SUBSCRIPTION_DATA,
					                 this.s6tAuthApplicationId.getVendorId(), true, false),
					this.s6tAuthApplicationId.getVendorId());

			// [ IDR- Flags ]
			// *[ Reset-ID ]
			// *[ AVP ]
			// *[ Proxy-Info ]
			// *[ Route-Record ]

			JInsertSubscriberDataRequest request = this.s6aSessionFactory.createInsertSubscriberDataRequest((Request) idr);

			// ISessionFactory sessionFactory = ((ISessionFactory)this.stack.getSessionFactory());
			// ServerS6aSession serverS6asession = sessionFactory.getNewAppSession(request.getMessage().getSessionId(),
			//            s6aAuthApplicationId, ServerS6aSession.class, (Object)request);
			// serverS6asession.sendInsertSubscriberDataRequest(request);

			// since this is in S6t context we need to create a session for S6a
			((ServerS6aSession) ((ISessionFactory) this.stack.getSessionFactory()).getNewAppSession(
					request.getMessage().getSessionId(), this.s6aAuthApplicationId, ServerS6aSession.class,
					(Object) request)).sendInsertSubscriberDataRequest(request);
		} catch (InternalException e) {
			e.printStackTrace();
		} catch (IllegalDiameterStateException e) {
			e.printStackTrace();
		} catch (RouteException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}

	}

	private void buildUserDataAvp(GHSSUserProfile hssData, AvpSet userData, long vendorId) {
   	    for (GMonitoringEventConfig mo : hssData.getMonitoringConfig()) {
   	    	boolean isRefId = false;
	   	    AvpSet monitoringEvCon = userData.addGroupedAvp(Avp.MONITORING_EVENT_CONFIGURATION, vendorId,true, false);
	   	    monitoringEvCon.addAvp(Avp.SCEF_ID, mo.getScefId(), vendorId, true, false, true);
	   	    monitoringEvCon.addAvp(Avp.MONITORING_TYPE, mo.getMonitoringType(), vendorId, true, false);

	   	    if (mo.getScefRefId() != 0) {
				isRefId = true;
	   	    	monitoringEvCon.addAvp(Avp.SCEF_REFERENCE_ID, mo.getScefRefId());
	   	    }
   	    
	   	    int[] refFordel = mo.getScefRefIdForDelition();
	   	    if (refFordel != null) {
				for (int i : refFordel) {
					isRefId = true;
					monitoringEvCon.addAvp(Avp.SCEF_REFERENCE_ID_FOR_DELETION, i);
				}
	   	    }

	   	    if (isRefId == false) {
	   	    	logger.error("No SCEF-Reference-ID or SCEF-Reference-ID-For-Delition exists event skiped");
	   	    	userData.removeAvp(Avp.MONITORING_EVENT_REPORT);
	   	    	continue;
	   	    }
	   	    monitoringEvCon.addAvp(Avp.MAXIMUM_NUMBER_OF_REPORTS, mo.getMaximumNumberOfReports(), vendorId, true, false);
   	    }
   	    
   	    for (GAESE_CommunicationPattern ae : hssData.getAESECommunicationPattern()) {
	   	    AvpSet aese = userData.addGroupedAvp(Avp.AESE_COMMUNICATION_PATTERN, vendorId,true, false);
   	    	boolean isRefId = false;
	   	    aese.addAvp(Avp.SCEF_ID, ae.getScefId(), vendorId, true, false, true);
	   	    if (ae.getScefRefId() != 0) {
				isRefId = true;
	   	    	aese.addAvp(Avp.SCEF_REFERENCE_ID, ae.getScefRefId());
	   	    }
	   	    int[] refFordel = ae.getScefRefIdForDelition();
	   	    if (refFordel != null) {
				for (int i : refFordel) {
					isRefId = true;
					aese.addAvp(Avp.SCEF_REFERENCE_ID_FOR_DELETION, i);
				}
	   	    }
	   	    if (isRefId == false) {
	   	    	logger.error("No SCEF-Reference-ID or SCEF-Reference-ID-For-Delition exists event skiped");
	   	    	userData.removeAvp(Avp.AESE_COMMUNICATION_PATTERN);
	   	    	continue;
	   	    }
	   	    
	   	    GCommunicationPatternSet[] cps = ae.getCommunicationPatternSet();
	   	    if (cps != null) {
	   	    	for (GCommunicationPatternSet cp : cps) {
			   	    AvpSet commPattSet = aese.addGroupedAvp(Avp.COMMUNICATION_PATTERN_SET, vendorId,true, false);
	   	    	    commPattSet.addAvp(Avp.PERIODIC_COMMUNICATION_INDICATOR, cp.periodicCommunicationIndicator);
	   	    	    if (cp.periodicCommunicationIndicator == 0) {
	   	    	    	commPattSet.addAvp(Avp.COMMUNICATION_DURATION_TIME, cp.communicationDurationTime);
	   	    	    	commPattSet.addAvp(Avp.PERIODIC_TIME, cp.periodictime);
	   	    	    }
	   	    	}
	   	    }
   	    }
		
	}
	
	@Override
	public void doConfigurationInformationRequestEvent(ServerS6tSession session, JConfigurationInformationRequest cir)
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
			sendCIAAnswer(session, cir, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			return;
		}
		
		GUserIdentifier uid;
		List<GAESE_CommunicationPattern> aeseComPattern = new ArrayList<GAESE_CommunicationPattern>();
		List<GMonitoringEventConfig> monitoringEvent = new ArrayList<GMonitoringEventConfig>();;

		JsonParser parser = new JsonParser();

		try {
			RedisFuture<String> future = null;

			String imsi = null;
			uid = UserIdentifier.extractFromAvpSingle(userIdentifier);

			if (uid.getMsisdn() != null && uid.getMsisdn().length() > 0) {
				imsi = this.getSyncHandler().get("HSS-MSISDN" + uid.getMsisdn());
			}
			else if (uid.getUserName() != null && uid.getUserName().length() > 0) {
				imsi = this.getSyncHandler().get("HSS-USER-NAME" + uid.getUserName());
			}
			else if (uid.getExternalId() != null && uid.getExternalId().length() > 0) {
				imsi = this.getSyncHandler().get("HSS-EXT-ID-" + uid.getExternalId());
			}
			
			if (imsi != null) {
				future = this.getAsyncHandler().get("HSS-IMSI-" + imsi);
			}
			else {		s6aMessages = new HssS6aMessages();
			s6tMessages = new HssS6tMessages();

				logger.error(new StringBuffer("user not found for MSISDN : ")
						.append(uid.getMsisdn() != null ? uid.getMsisdn() : "NULL")
						.append(" User Name : ").append(uid.getUserName() != null ? uid.getUserName() : "NULL")
						.append(" external ID  : ").append(uid.getExternalId() != null ? uid.getExternalId() : "NULL").toString());
				sendCIAAnswer(session, cir, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
				return;
			}

			
			CompletableFuture<GHSSUserProfile> gsonFuture = 
					(CompletableFuture<GHSSUserProfile>)future.thenApply(new Function<String, GHSSUserProfile>() {
			    @Override
			    public GHSSUserProfile apply(String value) {
			        return new Gson().fromJson(parser.parse(value), GHSSUserProfile.class);
			    }
			});


			Avp AESECommPatternAvp = reqSet.getAvp(Avp.AESE_COMMUNICATION_PATTERN);
			if (AESECommPatternAvp != null)  {
				aeseComPattern = AESE_CommunicationPattern.extractFromAvp(AESECommPatternAvp);	
			}

			Avp monitoringEventConfig = reqSet.getAvp(Avp.MONITORING_EVENT_CONFIGURATION);
			if (monitoringEventConfig != null) {
				monitoringEvent = MonitoringEventConfig.extractFromAvp(monitoringEventConfig);
			}

			
			String str;
			GHSSUserProfile hssData;
			try {
				str = future.get(5, TimeUnit.MILLISECONDS);
				hssData = gsonFuture.get();
			} catch (TimeoutException e) {
				hssData = null;
                str = null;				
				logger.error("Faild to get message from DB after 5 miliseconds or user not exists");
				sendCIAAnswer(session, cir, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
				return;
			}
	        
	        //update new data to HSS
	        hssData.setMonitoringConfig(MonitoringEventConfig.getNewHSSData(hssData, monitoringEvent));

	        //update new data to hss
    		hssData.setAESECommunicationPattern(AESE_CommunicationPattern.getNewHSSData(hssData, aeseComPattern));
    		
    		GsonBuilder builder = new GsonBuilder();
            // we ignore Private fields
            builder.excludeFieldsWithModifiers(Modifier.PRIVATE);
            Gson gson = builder.create();
    		
    		RedisFuture<String> setHss = this.getAsyncHandler().set("HSS-MSISDN-" + uid.msisdn,gson.toJson(hssData));

    		// send answer to SCEF
    		sendCIAAnswer(session, cir, ResultCode.SUCCESS);
    		
    		// now we will send update to MME
    		String mmeAddress = hssData.getMMEAdress();
    		if (mmeAddress == null || mmeAddress.length() == 0) {
    			// device is not connected to mme
    			return;
    		}
    		
    		// we have MME 
    		sendIDRRequest(session, hssData, mmeAddress);
    		
	   	 
	   	    // finish the asynchronous write
	   	    setHss.get();
	   	    
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void doNIDDInformationRequestEvent(ServerS6tSession session, JNIDDInformationRequest nir)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doReportingInformationAnswerEvent(ServerS6tSession session, JReportingInformationRequest rir,
			JReportingInformationAnswer ria)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doAuthenticationInformationRequestEvent(ServerS6aSession session, JAuthenticationInformationRequest air)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doCancelLocationAnswerEvent(ServerS6aSession session, JCancelLocationRequest clr,
			JCancelLocationAnswer cla)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doDeleteSubscriberDataAnswerEvent(ServerS6aSession session, JDeleteSubscriberDataRequest dsr,
			JDeleteSubscriberDataAnswer dsa)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doInsertSubscriberDataAnswerEvent(ServerS6aSession session, JInsertSubscriberDataRequest idr,
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

	@Override
	public void doNotifyRequestEvent(ServerS6aSession session, JNotifyRequest nor)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doOtherEvent(AppSession appSession, AppRequestEvent applicationRequest, AppAnswerEvent applicationAnswer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doPurgeUERequestEvent(ServerS6aSession session, JPurgeUERequest pur)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doResetAnswerEvent(ServerS6aSession session, JResetRequest rsr, JResetAnswer rsa)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doUpdateLocationRequestEvent(ServerS6aSession session, JUpdateLocationRequest ulr)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}


	public ApplicationId getS6aAuthApplicationId() {
		return s6aAuthApplicationId;
	}

	public ApplicationId getS6tAuthApplicationId() {
		return s6tAuthApplicationId;
	}

	public HssS6aMessages getS6aMessages() {
		return s6aMessages;
	}

	public void setS6aMessages(HssS6aMessages s6aMessages) {
		this.s6aMessages = s6aMessages;
	}

	public HssS6tMessages getS6tMessages() {
		return s6tMessages;
	}

	public void setS6tMessages(HssS6tMessages s6tMessages) {
		this.s6tMessages = s6tMessages;
	}
	
	

}
