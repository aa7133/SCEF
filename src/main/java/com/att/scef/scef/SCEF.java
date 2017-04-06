package com.att.scef.scef;

import java.io.InputStream;

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
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.api.s6t.ClientS6tSession;
import org.jdiameter.api.s6t.ClientS6tSessionListener;
import org.jdiameter.api.s6t.ServerS6tSession;
import org.jdiameter.api.s6t.ServerS6tSessionListener;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationAnswer;
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

import com.att.scef.data.ConnectorImpl;
import com.att.scef.utils.AbstractServer;

import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.RedisStringCommands;

public class SCEF extends AbstractServer implements ServerT6aSessionListener, ClientS6tSessionListener,
                                             StateChangeListener<AppSession> {
	
	private ApplicationId t6aAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777346);
	private ApplicationId s6tAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777345);

	private T6aSessionFactoryImpl t6aSessionFactory;
	private S6tSessionFactoryImpl s6tSessionFactory;

	
	private ConnectorImpl syncDataConnector;
	private ConnectorImpl asyncDataConnector;
	private RedisStringAsyncCommands<String, String> asyncHandler;
	private RedisStringCommands<String, String> syncHandler;

	private SCEFT6aMessages t6aMessages;
	private SCEFS6tMessages s6tMessages;
	

	@SuppressWarnings("unchecked")
	public SCEF() {
		super();
		asyncDataConnector = new ConnectorImpl();
		asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(RedisStringAsyncCommands.class);

		syncDataConnector = new ConnectorImpl();
		syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(RedisStringCommands.class);
		
		this.setT6aMessages(new SCEFT6aMessages());
		this.setS6tMessages(new SCEFS6tMessages());
		
		logger.info("=================================== SCEF started ==============================");
	}
	
	@SuppressWarnings("unchecked")
	public SCEF(String host, int port) {
		super();
		asyncDataConnector = new ConnectorImpl();
		asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(RedisStringAsyncCommands.class, host, port);

		syncDataConnector = new ConnectorImpl();
		syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(RedisStringCommands.class, host, port);

		this.setT6aMessages(new SCEFT6aMessages());
		this.setS6tMessages(new SCEFS6tMessages());
		
		logger.info("=================================== SCEF started ==============================");
	}
	
	public void closedataBases() {
		syncDataConnector.closeDataBase();
		asyncDataConnector.closeDataBase();
		logger.info("=================================== closing data ==============================");
	}
	
	@Override
	public void configure(InputStream configInputStream, String dictionaryFile) throws Exception {
		super.configure(configInputStream, dictionaryFile);

		this.setT6aSessionFactory(new T6aSessionFactoryImpl(super.factory));
	    this.getT6aSessionFactory().setServerSessionListener(this);
	    this.setS6tSessionFactory(new S6tSessionFactoryImpl(super.factory));
	    this.getS6tSessionFactory().setClientSessionListener(this);
	    
	    Network network = stack.unwrap(Network.class);
	    
	    network.addNetworkReqListener(this, s6tAuthApplicationId);
	    ((ISessionFactory) super.factory).registerAppFacory(ServerS6tSession.class, this.getS6tSessionFactory());

	    network.addNetworkReqListener(this, t6aAuthApplicationId);
	    ((ISessionFactory) super.factory).registerAppFacory(ServerT6aSession.class, this.getT6aSessionFactory());
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
	
	
	/** 
	 * 
	 */
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

	public SCEFT6aMessages getT6aMessages() {
		return t6aMessages;
	}

	public void setT6aMessages(SCEFT6aMessages t6aMessages) {
		this.t6aMessages = t6aMessages;
	}

	public SCEFS6tMessages getS6tMessages() {
		return s6tMessages;
	}

	public void setS6tMessages(SCEFS6tMessages s6tMessages) {
		this.s6tMessages = s6tMessages;
	}
}
