package com.att.scef.mme;

import java.io.FileInputStream;

import org.jdiameter.api.Answer;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
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

import com.att.scef.interfaces.T6aAbstractClient;


public class T6aClient extends T6aAbstractClient {
  protected final Logger logger = LoggerFactory.getLogger(T6aClient.class);
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
  public Answer processRequest(Request request) {
    int code = request.getCommandCode();
    switch (code) {
    case JConfigurationInformationRequest.code:
      if (request.isRequest()) {
        if (logger.isInfoEnabled()) {
          logger.info("GOT CIR from SCEF");
        }
      }
      else {
        logger.info("GOT CIA from SCEF");
      }
      logger.error(new StringBuilder("processRequest - T6a : Configuration-Information-Request (CIR): Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
    case JReportingInformationRequest.code:
      logger.error(new StringBuilder("processRequest - T6a : Reporting-Information-Answer (RIA): Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
    case JMO_DataAnswer.code:
      logger.error(new StringBuilder("processRequest - T6a : MO=Data-Answe (ODR): Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
    case JMT_DataRequest.code:
      logger.error(new StringBuilder("processRequest - T6a : MO-Data-Request (TDR): Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
    case JConnectionManagementRequest.code:
      if (request.isRequest()) {
        if (logger.isInfoEnabled()) {
          logger.info("GOT CMR from SCEF");
        }
      }
      else {
        logger.info("GOT CMA from SCEF");
      }
      logger.error(new StringBuilder("processRequest - T6a : Connection-Management-Request (CMR): Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
    default:
      logger.error(new StringBuilder("processRequest - T6a - Not Supported message: ").append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      return null;
    }
    return null;
  }

  @Override
  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleT6aOtherEvent(session, request, answer);
  }

  @Override
  public void doConfigurationInformationAnswerEvent(ClientT6aSession session, JConfigurationInformationRequest request,
      JConfigurationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleConfigurationInformationAnswerEvent(session, request, answer);
  }

  @Override
  public void doConfigurationInformationRequestEvent(ClientT6aSession session, JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleConfigurationInformationRequestEvent(session, request);
  }

  @Override
  public void doReportingInformationAnswerEvent(ClientT6aSession session, JReportingInformationRequest request,
      JReportingInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleReportingInformationAnswerEvent(session, request, answer);
  }

  @Override
  public void doMO_DataAnswerEvent(ClientT6aSession session, JMO_DataRequest request, JMO_DataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleMO_DataAnswerEvent(session, request, answer);
  }

  @Override
  public void doMT_DataRequestEvent(ClientT6aSession session, JMT_DataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleMT_DataRequestEvent(session, request);
  }

  @Override
  public void doConnectionManagementAnswerEvent(ClientT6aSession session, JConnectionManagementRequest request,
      JConnectionManagementAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleConnectionManagementAnswerEvent(session, request, answer);
  }

  @Override
  public void doConnectionManagementRequestEvent(ClientT6aSession session, JConnectionManagementRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleConnectionManagementRequestEvent(session, request);
  }

}
