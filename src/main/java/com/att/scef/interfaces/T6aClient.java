package com.att.scef.interfaces;

import java.io.FileInputStream;

import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.t6a.ClientT6aSession;
import org.jdiameter.api.t6a.events.JConfigurationInformationAnswer;
import org.jdiameter.api.t6a.events.JConfigurationInformationRequest;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataAnswer;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.jdiameter.api.t6a.events.JReportingInformationAnswer;
import org.jdiameter.api.t6a.events.JReportingInformationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.mme.MME;


public class T6aClient extends T6aAbstractClient {
  protected final Logger logger = LoggerFactory.getLogger(S6tClient.class);
  private MME mme;
  //private String remoteRealm = null;
  
  private String configFile;
  
  public T6aClient(MME mmeCtx, String configFile) {
      this.mme = mmeCtx;
      this.configFile = configFile;
  }
  
  public void init(String clientID) throws Exception {
      this.init(new FileInputStream(configFile), clientID);
  }

  @Override
  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a Other\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doConfigurationInformationAnswerEvent(ClientT6aSession session, JConfigurationInformationRequest request,
      JConfigurationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CIA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doConfigurationInformationRequestEvent(ClientT6aSession session, JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CIR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doReportingInformationAnswerEvent(ClientT6aSession session, JReportingInformationRequest request,
      JReportingInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a RIA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doMO_DataAnswerEvent(ClientT6aSession session, JMO_DataRequest request, JMO_DataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a ODA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doMT_DataRequestEvent(ClientT6aSession session, JMT_DataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a TDR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doConnectionManagementAnswerEvent(ClientT6aSession session, JConnectionManagementRequest request,
      JConnectionManagementAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CMA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doConnectionManagementRequestEvent(ClientT6aSession session, JConnectionManagementRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CMR\" event, request[" + request + "], on session[" + session + "]");
  }

}
