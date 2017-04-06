package com.att.scef.scef;

import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.api.s6t.ServerS6tSession;
import org.jdiameter.api.s6t.ServerS6tSessionListener;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
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
import org.jdiameter.common.impl.app.s6a.S6aSessionFactoryImpl;
import org.jdiameter.common.impl.app.s6t.S6tSessionFactoryImpl;

import com.att.scef.data.ConnectorImpl;
import com.att.scef.utils.AbstractServer;

import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.RedisStringCommands;

public class SCEF extends AbstractServer implements ServerT6aSessionListener, ServerS6tSessionListener,
                                             StateChangeListener<AppSession> {
	
	private ApplicationId t6aAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777346);
	private ApplicationId s6tAuthApplicationId = ApplicationId.createByAuthAppId(10415, 16777345);

	private S6aSessionFactoryImpl t6aSessionFactory;
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
		
	}
	
	
	@Override
	public void doConfigurationInformationRequestEvent(ServerS6tSession session,
			JConfigurationInformationRequest request)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doReportingInformationAnswerEvent(ServerS6tSession session, JReportingInformationRequest request,
			JReportingInformationAnswer answer)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doNIDDInformationRequestEvent(ServerS6tSession session, JNIDDInformationRequest request)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSendConfigurationInformationAnswerEvent(ServerT6aSession session,
			org.jdiameter.api.t6a.events.JConfigurationInformationRequest request,
			org.jdiameter.api.t6a.events.JConfigurationInformationAnswer answer)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSendConfigurationInformationRequestEvent(ServerT6aSession session,
			org.jdiameter.api.t6a.events.JConfigurationInformationRequest request)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSendReportingInformationRequestEvent(ServerT6aSession session,
			org.jdiameter.api.t6a.events.JReportingInformationRequest request)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSendMO_DataRequestEvent(ServerT6aSession session, JMO_DataRequest request)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSendMT_DataAnswertEvent(ServerT6aSession session, JMT_DataRequest request, JMT_DataAnswer answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSendConnectionManagementAnswertEvent(ServerT6aSession session, JConnectionManagementRequest request,
			JConnectionManagementAnswer answer)
					throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSendConnectionManagementRequestEvent(ServerT6aSession session, JConnectionManagementRequest request)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		// TODO Auto-generated method stub
		
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

	public S6aSessionFactoryImpl getT6aSessionFactory() {
		return t6aSessionFactory;
	}

	public void setT6aSessionFactory(S6aSessionFactoryImpl t6aSessionFactory) {
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
