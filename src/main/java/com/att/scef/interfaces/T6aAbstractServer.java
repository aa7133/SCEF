package com.att.scef.interfaces;

import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Mode;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.t6a.ClientT6aSession;
import org.jdiameter.api.t6a.ServerT6aSession;
import org.jdiameter.api.t6a.ServerT6aSessionListener;
import org.jdiameter.api.t6a.events.JConfigurationInformationAnswer;
import org.jdiameter.api.t6a.events.JConfigurationInformationRequest;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataAnswer;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.jdiameter.api.t6a.events.JReportingInformationRequest;
import org.jdiameter.common.impl.app.t6a.T6aSessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.utils.TBase;

public abstract class T6aAbstractServer extends TBase implements ServerT6aSessionListener {
  private final Logger logger = LoggerFactory.getLogger(T6aAbstractServer.class);
  protected ServerT6aSession serverT6aSession;
  protected T6aSessionFactoryImpl t6aSessionFactory;
  private static final long VENDOR_ID = 10415;
  private static final long T6A_AUTH_APPLICATION_ID = 16777346;

  public void init(FileInputStream configStream, String serverId) {
    try {
      super.init(configStream, serverId, ApplicationId.createByAuthAppId(VENDOR_ID, T6A_AUTH_APPLICATION_ID));
      t6aSessionFactory = new T6aSessionFactoryImpl(this.sessionFactory);
      this.sessionFactory.registerAppFacory(ServerT6aSession.class, t6aSessionFactory);
      this.sessionFactory.registerAppFacory(ClientT6aSession.class, t6aSessionFactory);
      t6aSessionFactory.setServerSessionListener(this);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        configStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void start() throws IllegalDiameterStateException, InternalException {
    stack.start();
  }

  public void start(Mode mode, long timeOut, TimeUnit timeUnit) throws IllegalDiameterStateException, InternalException {
    stack.start(mode, timeOut, timeUnit);
  }

  public void setServerT6aSession(ServerT6aSession serverT6aSession) {
    this.serverT6aSession = serverT6aSession;
  }
  
  @Override
  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a Other\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  @Override
  public void doSendConfigurationInformationAnswerEvent(ServerT6aSession session,
      JConfigurationInformationRequest request, JConfigurationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CIA\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  @Override
  public void doSendConfigurationInformationRequestEvent(ServerT6aSession session,
      JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CIR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doSendReportingInformationRequestEvent(ServerT6aSession session, JReportingInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a RIR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doSendMO_DataRequestEvent(ServerT6aSession session, JMO_DataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a ODR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doSendMT_DataAnswertEvent(ServerT6aSession session, JMT_DataRequest request, JMT_DataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a TDA\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  @Override
  public void doSendConnectionManagementAnswertEvent(ServerT6aSession session, JConnectionManagementRequest request,
      JConnectionManagementAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CMA\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  @Override
  public void doSendConnectionManagementRequestEvent(ServerT6aSession session, JConnectionManagementRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CMR\" event, request[" + request + "], on session[" + session + "]");
  }

}
