package com.att.scef.scef;

import java.io.FileInputStream;

import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.t6a.ServerT6aSession;
import org.jdiameter.api.t6a.events.JConfigurationInformationAnswer;
import org.jdiameter.api.t6a.events.JConfigurationInformationRequest;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataAnswer;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.jdiameter.api.t6a.events.JReportingInformationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.hss.S6tServer;
import com.att.scef.interfaces.T6aAbstractServer;

public class T6aServer extends T6aAbstractServer {
  protected final Logger logger = LoggerFactory.getLogger(S6tServer.class);
  private SCEF scef;

  private String configFile;

  public T6aServer(SCEF scefCtx, String configFile) {
    this.scef = scefCtx;
    this.configFile = configFile;
  }

  public void init(String serverID) throws Exception {
    if (logger.isInfoEnabled()) {
      logger.info("Reading config file T6aSessionFactoryImpl: " + this.configFile);
    }
    this.init(new FileInputStream(this.configFile), serverID);
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
