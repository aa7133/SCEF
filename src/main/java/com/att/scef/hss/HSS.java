package com.att.scef.hss;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

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
import org.jdiameter.common.api.app.s6a.IS6aMessageFactory;
import org.jdiameter.common.api.app.s6t.IS6tMessageFactory;
import org.jdiameter.common.impl.app.s6a.S6aSessionFactoryImpl;
import org.jdiameter.common.impl.app.s6t.JConfigurationInformationAnswerImpl;
import org.jdiameter.common.impl.app.s6t.S6tSessionFactoryImpl;
import org.jdiameter.server.impl.app.s6a.S6aServerSessionImpl;
import org.jdiameter.server.impl.app.s6t.S6tServerSessionImpl;

import com.att.scef.data.ConnectorImpl;
import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GCommunicationPatternSet;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.gson.GUserIdentifier;
import com.att.scef.utils.AESE_CommunicationPattern;
import com.att.scef.utils.AbstractServer;
import com.att.scef.utils.BCDStringConverter;
import com.att.scef.utils.ExtractTool;
import com.att.scef.utils.MonitoringEventConfig;
import com.att.scef.utils.UserIdentifier;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.RedisStringCommands;

public class HSS extends AbstractServer implements ServerS6aSessionListener, ServerS6tSessionListener, 
StateChangeListener<AppSession>, IS6aMessageFactory, IS6tMessageFactory{

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
	}
	
	@SuppressWarnings("unchecked")
	public HSS(String host, int port) {
		super();
		asyncDataConnector = new ConnectorImpl();
		asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(RedisStringAsyncCommands.class, host, port);

		syncDataConnector = new ConnectorImpl();
		syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(RedisStringCommands.class, host, port);
	}

	public void closedataBases() {
		syncDataConnector.closeDataBase();
		asyncDataConnector.closeDataBase();
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
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
		//TODO remove this
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
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
		
		//TODO remove this
		return null;
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
			// we need to return error
			JConfigurationInformationAnswer cia = new JConfigurationInformationAnswerImpl((Request) cir,
					ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			cia.getMessage().setError(true);
			cia.getMessage().setRequest(false);
			cia.getMessage().setProxiable(true);
			AvpSet set = cia.getMessage().getAvps();

			if (set.getAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID) == null) {
				AvpSet vendorSpecificApplicationId = set.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, false,
						false);
				vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, getS6tAuthApplicationId().getVendorId(), true);
				vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, getS6tAuthApplicationId().getAuthAppId(),
						true);/**
				 * Created by Adi Enzel on 3/23/17.
				 *
				 * @author <a href="mailto:aa7133@att.com"> Adi Enzel </a>
				 */

			}
			// [ Experimental-Result ]
			if (set.getAvp(Avp.EXPERIMENTAL_RESULT) == null) {
				AvpSet experimentalResult = set.addGroupedAvp(Avp.EXPERIMENTAL_RESULT, 0, true, false);
				experimentalResult.addAvp(Avp.VENDOR_ID, 0);
				experimentalResult.addAvp(Avp.EXPERIMENTAL_RESULT_CODE, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			}
			// { Auth-Session-State }
			if (set.getAvp(Avp.AUTH_SESSION_STATE) == null) {
				set.addAvp(Avp.AUTH_SESSION_STATE, 1);
			}
			session.sendConfigurationInformationAnswer(cia);
			return;
		}
		try {
			RedisFuture<String> future = null;

			GUserIdentifier uid = UserIdentifier.extractFromAvpSingle(userIdentifier);

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
			
			GAESE_CommunicationPattern[] aeseComPattern;


			Avp AESECommPatternAvp = reqSet.getAvp(Avp.AESE_COMMUNICATION_PATTERN);
			if (AESECommPatternAvp != null)  {
				aeseComPattern = AESE_CommunicationPattern.extractFromAvp(AESECommPatternAvp);	
			}
			
			GMonitoringEventConfig[] monitoringEvent;
			Avp monitoringEventConfig = reqSet.getAvp(Avp.MONITORING_EVENT_CONFIGURATION);
			if (monitoringEventConfig != null) {
				monitoringEvent = MonitoringEventConfig.extractFromAvp(monitoringEventConfig);
			}

			String userData = future.get();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JConfigurationInformationRequest createConfigurationInformationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JNIDDInformationAnswer createNIDDInformationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JNIDDInformationRequest createNIDDInformationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JReportingInformationAnswer createReportingInformationAnswer(Answer answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JReportingInformationRequest createReportingInformationRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
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
