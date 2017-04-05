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
import org.jdiameter.api.s6a.events.JAuthenticationInformationAnswer;
import org.jdiameter.api.s6a.events.JAuthenticationInformationRequest;
import org.jdiameter.api.s6a.events.JCancelLocationAnswer;
import org.jdiameter.api.s6a.events.JCancelLocationRequest;
import org.jdiameter.api.s6a.events.JDeleteSubscriberDataAnswer;
import org.jdiameter.api.s6a.events.JDeleteSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JInsertSubscriberDataAnswer;
import org.jdiameter.api.s6a.events.JInsertSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JNotifyAnswer;
import org.jdiameter.api.s6a.events.JNotifyRequest;
import org.jdiameter.api.s6a.events.JPurgeUEAnswer;
import org.jdiameter.api.s6a.events.JPurgeUERequest;
import org.jdiameter.api.s6a.events.JResetAnswer;
import org.jdiameter.api.s6a.events.JResetRequest;
import org.jdiameter.api.s6a.events.JUpdateLocationAnswer;
import org.jdiameter.api.s6a.events.JUpdateLocationRequest;
import org.jdiameter.api.s6t.ServerS6tSession;
import org.jdiameter.api.s6t.ServerS6tSessionListener;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationAnswer;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.client.impl.app.s6a.S6aClientSessionImpl;
import org.jdiameter.common.api.app.s6a.IS6aMessageFactory;
import org.jdiameter.common.api.app.s6t.IS6tMessageFactory;
import org.jdiameter.common.impl.app.s6a.JInsertSubscriberDataRequestImpl;
import org.jdiameter.common.impl.app.s6a.S6aSessionFactoryImpl;
import org.jdiameter.common.impl.app.s6t.JConfigurationInformationAnswerImpl;
import org.jdiameter.common.impl.app.s6t.JNIDDInformationAnswerImpl;
import org.jdiameter.common.impl.app.s6t.JReportingInformationRequestImpl;
import org.jdiameter.common.impl.app.s6t.S6tSessionFactoryImpl;
import org.jdiameter.server.impl.app.s6a.S6aServerSessionImpl;
import org.jdiameter.server.impl.app.s6a.ServerS6aSessionDataLocalImpl;
import org.jdiameter.server.impl.app.s6t.S6tServerSessionImpl;

import static org.jdiameter.client.impl.helpers.Parameters.OwnDiameterURI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                                                   StateChangeListener<AppSession>, IS6aMessageFactory, IS6tMessageFactory{

	//protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ApplicationId s6aAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777308);
	private ApplicationId s6tAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777345);

	private S6aSessionFactoryImpl s6aSessionFactory;
	private S6tSessionFactoryImpl s6tSessionFactory;

	
	private ConnectorImpl syncDataConnector;
	private ConnectorImpl asyncDataConnector;
	private RedisStringAsyncCommands<String, String> asyncHandler;
	private RedisStringCommands<String, String> syncHandler;

	@SuppressWarnings("unchecked")
	public HSS() {
		super();
		asyncDataConnector = new ConnectorImpl();
		asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(RedisStringAsyncCommands.class);

		syncDataConnector = new ConnectorImpl();
		syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(RedisStringCommands.class);
		logger.info("=================================== HSS started ==============================");
	}
	
	@SuppressWarnings("unchecked")
	public HSS(String host, int port) {
		super();
		asyncDataConnector = new ConnectorImpl();
		asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(RedisStringAsyncCommands.class, host, port);

		syncDataConnector = new ConnectorImpl();
		syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(RedisStringCommands.class, host, port);
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
		try {
			switch (commandCode) {
			case JUpdateLocationRequest.code:
				S6aServerSessionImpl session = ((ISessionFactory) super.factory).getNewAppSession(request.getSessionId(),
			            s6aAuthApplicationId, ServerS6aSession.class, (Object)null);
				session.addStateChangeNotification(this);
				session.processRequest(request);
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
		return null;
	}

	private Answer processS6tRequest(Request request, int commandCode) {
		try {
			switch (commandCode) {
			case JConfigurationInformationRequest.code:
			case JNIDDInformationRequest.code:
				S6tServerSessionImpl session = ((ISessionFactory) super.factory).getNewAppSession(request.getSessionId(),
			            s6tAuthApplicationId, ServerS6tSession.class, (Object)null);
				session.addStateChangeNotification(this);
				session.processRequest(request);
				break;
			case JReportingInformationRequest.code:
				logger.error(new StringBuilder("processS6tRequest - : Reporting-Information-Request: Not Supported message in this state: ")
						.append(commandCode)
						.append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
						.append(request.getClass().getName()).toString());
				return null;
			default:
				logger.error(new StringBuilder("processS6tRequest - Not Supported message: ").append(commandCode)
						.append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
						.append(request.getClass().getName()).toString());
				return null;
			}
		} catch (InternalException e) {
	        e.printStackTrace();
	    }
		
		return null;
	}
	
	private void sendCIAAnswer(ServerS6tSession session, JConfigurationInformationRequest cir, int resultCode) {
		try {
			Answer answer = (Answer) super.createAnswer((Request) cir.getMessage(), resultCode, getS6tAuthApplicationId());

			answer.setError(true);
			AvpSet set = answer.getAvps();

			session.sendConfigurationInformationAnswer(this.createConfigurationInformationAnswer(answer));
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
	

	
	@Override
	public void doConfigurationInformationRequestEvent(ServerS6tSession session, JConfigurationInformationRequest cir)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
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

			uid = UserIdentifier.extractFromAvpSingle(userIdentifier);

			if (uid.msisdn != null && uid.msisdn.length() > 0) {
				future = this.getAsyncHandler().get("HSS-MSISDN-" + uid.msisdn);
			}
			else if (uid.userName != null && uid.userName.length() > 0) {
				uid.msisdn = this.getSyncHandler().get("HSS-USER-NAME" + uid.userName);
				future = this.getAsyncHandler().get("HSS-MSISDN-" + uid.msisdn);
			}
			else if (uid.externalId != null && uid.externalId.length() > 0) {
				uid.msisdn = this.getSyncHandler().get("HSS-EXT-ID-" + uid.externalId);
				future = this.getAsyncHandler().get("HSS-MSISDN-" + uid.msisdn);
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
    		// build  IDR message to mme for update user data
  
    		Request idr = this.createRequest(session, s6aAuthApplicationId,
					JInsertSubscriberDataRequest.code, HSS_REALM, 
					this.getConfiguration().getStringValue(OwnDiameterURI.ordinal(),"aaa://127.0.0.1:23000"));
	   	    reqSet = idr.getAvps();
	   	    idr.setRequest(true);
	   	    
	   	    // add data
	   	    //< Insert-Subscriber-Data-Request> ::=		< Diameter Header: 319, REQ, PXY, 16777251 >
	   	    //< Session-Id > by createRequest
	   	    //[ DRMP ]
	   	    //[ Vendor-Specific-Application-Id ] by createRequest
	   	    //{ Auth-Session-State } by createRequest
	   	    //{ Origin-Host } by createRequest
	   	    //{ Origin-Realm } by createRequest
	   	    //{ Destination-Host }
	   	    reqSet.addAvp(Avp.DESTINATION_HOST, mmeAddress, true);
	   	    //{ Destination-Realm }
	   	    reqSet.addAvp(Avp.DESTINATION_REALM, MME_REALM, true);
	   	    //{ User-Name }
	   	    reqSet.addAvp(Avp.USER_NAME, hssData.getIMSI(), true);
	   	    //*[ Supported-Features]
	   	    //{ Subscription-Data}
	   	    long vndorId = s6tAuthApplicationId.getVendorId();
	   	    AvpSet userData = reqSet.addGroupedAvp(Avp.SUBSCRIPTION_DATA, vndorId,true, false);
	   	
	   	    for (GMonitoringEventConfig mo : hssData.getMonitoringConfig()) {
	   	    	boolean isRefId = false;
		   	    AvpSet monitoringEvCon = userData.addGroupedAvp(Avp.MONITORING_EVENT_CONFIGURATION, vndorId,true, false);
		   	    monitoringEvCon.addAvp(Avp.SCEF_ID, mo.getScefId(), vndorId, true, false, true);
		   	    monitoringEvCon.addAvp(Avp.MONITORING_TYPE, mo.getMonitoringType(), vndorId, true, false);

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
		   	    monitoringEvCon.addAvp(Avp.MAXIMUM_NUMBER_OF_REPORTS, mo.getMaximumNumberOfReports(), vndorId, true, false);
	   	    }
	   	    
	   	    for (GAESE_CommunicationPattern ae : hssData.getAESECommunicationPattern()) {
		   	    AvpSet aese = userData.addGroupedAvp(Avp.AESE_COMMUNICATION_PATTERN, vndorId,true, false);
	   	    	boolean isRefId = false;
		   	    aese.addAvp(Avp.SCEF_ID, ae.getScefId(), vndorId, true, false, true);
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
				   	    AvpSet commPattSet = aese.addGroupedAvp(Avp.COMMUNICATION_PATTERN_SET, vndorId,true, false);
		   	    	    commPattSet.addAvp(Avp.PERIODIC_COMMUNICATION_INDICATOR, cp.periodicCommunicationIndicator);
		   	    	    if (cp.periodicCommunicationIndicator == 0) {
		   	    	    	commPattSet.addAvp(Avp.COMMUNICATION_DURATION_TIME, cp.communicationDurationTime);
		   	    	    	commPattSet.addAvp(Avp.PERIODIC_TIME, cp.periodictime);
		   	    	    }
		   	    	}
		   	    }
	   	    }
	   	    
	   	    //[ IDR- Flags ]
	   	    //*[ Reset-ID ]
	   	    //*[ AVP ]
	   	    //*[ Proxy-Info ]
	   	    //*[ Route-Record ]
	   	    
	   	    JInsertSubscriberDataRequest request = s6aSessionFactory.createInsertSubscriberDataRequest((Request)idr);

	   	    //ISessionFactory sessionFactory = ((ISessionFactory)this.stack.getSessionFactory());
	   	    //ServerS6aSession serverS6asession = sessionFactory.getNewAppSession(request.getMessage().getSessionId(),
            //           s6aAuthApplicationId,
            //           ServerS6aSession.class,
            //           (Object)request);
            //serverS6asession.sendInsertSubscriberDataRequest(request);

	   	    ((ServerS6aSession)((ISessionFactory)this.stack.getSessionFactory())
	   	           .getNewAppSession(request.getMessage().getSessionId(),
	   	        		             s6aAuthApplicationId,
	   	        		             ServerS6aSession.class,
	   	        		             (Object)request))
	   	           .sendInsertSubscriberDataRequest(request);
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
		// TODO Auto-generated method stub
		
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

	@Override
	public JConfigurationInformationAnswer createConfigurationInformationAnswer(Answer answer) {
		return new JConfigurationInformationAnswerImpl(answer);
	}

	@Override
	public JConfigurationInformationRequest createConfigurationInformationRequest(Request request) {
		return null;
	}

	@Override
	public JNIDDInformationAnswer createNIDDInformationAnswer(Answer answer) {
		return new JNIDDInformationAnswerImpl(answer);
	}

	@Override
	public JNIDDInformationRequest createNIDDInformationRequest(Request request) {
		return null;
	}

	@Override
	public JReportingInformationAnswer createReportingInformationAnswer(Answer answer) {
		return null;
	}

	@Override
	public JReportingInformationRequest createReportingInformationRequest(Request request) {
		return new JReportingInformationRequestImpl(request);
	}

	@Override
	public JAuthenticationInformationAnswer createAuthenticationInformationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JAuthenticationInformationRequest createAuthenticationInformationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JCancelLocationAnswer createCancelLocationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JCancelLocationRequest createCancelLocationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JDeleteSubscriberDataAnswer createDeleteSubscriberDataAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JDeleteSubscriberDataRequest createDeleteSubscriberDataRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JInsertSubscriberDataAnswer createInsertSubscriberDataAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JInsertSubscriberDataRequest createInsertSubscriberDataRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JNotifyAnswer createNotifyAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JNotifyRequest createNotifyRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPurgeUEAnswer createPurgeUEAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPurgeUERequest createPurgeUERequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JResetAnswer createResetAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JResetRequest createResetRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JUpdateLocationAnswer createUpdateLocationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JUpdateLocationRequest createUpdateLocationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getApplicationId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ApplicationId getS6aAuthApplicationId() {
		return s6aAuthApplicationId;
	}

	public ApplicationId getS6tAuthApplicationId() {
		return s6tAuthApplicationId;
	}

}
