package com.att.scef.interfaces;

import java.io.FileInputStream;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Message;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
import org.jdiameter.api.ResultCode;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6a.ServerS6aSession;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.hss.HSS;

public class S6aServer extends S6aAbstractServer {
  protected final Logger logger = LoggerFactory.getLogger(S6tServer.class);
  private HSS hss;

  private String configFile;

  public S6aServer(HSS hssCtx, String configFile) {
    this.hss = hssCtx;
    this.configFile = configFile;
  }

  public void init(String serverID) throws Exception {
    if (logger.isInfoEnabled()) {
      logger.info("Reading config file S6aSessionFactoryImpl: " + this.configFile);
    }
    this.init(new FileInputStream(this.configFile), serverID);
  }
  
  @Override
  public Answer processRequest(Request request) {
    int code = request.getCommandCode();
    switch (code) {
    case JAuthenticationInformationRequest.code:
    case JPurgeUERequest.code:
    case JUpdateLocationRequest.code:
    case JNotifyRequest.code:
      try {
        this.serverS6aSession = (ServerS6aSession) this.s6aSessionFactory.getNewSession(request.getSessionId(), ServerS6aSession.class, this.getApplicationId(), (Object[])null);
        ((NetworkReqListener) this.serverS6aSession).processRequest(request);
      } catch (Exception e) {
        logger.error(e.toString());
        e.printStackTrace();
      }
      return null;
    case JCancelLocationAnswer.code:
      logger.error(new StringBuilder("processRequest - : Cancel-Location-Answer: Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
     break;
    case JDeleteSubscriberDataAnswer.code:
      logger.error(new StringBuilder("processRequest - : Delete-Subscriber-Data-Answer: Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
    case JInsertSubscriberDataAnswer.code:
      logger.error(new StringBuilder("processRequest - : Insert-Subscriber-Data-Answer: Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
    case JResetAnswer.code:
      logger.error(new StringBuilder("processRequest - : Reset-Answer: Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
      
    default:
      logger.error(new StringBuilder("processRequest - S6a - Not Supported message: ").append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      return null;
    }
    return null;
  }
  
  @Override
  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a Other\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doAuthenticationInformationRequestEvent(ServerS6aSession session,
      JAuthenticationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a AIR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doPurgeUERequestEvent(ServerS6aSession session, JPurgeUERequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a PUR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doUpdateLocationRequestEvent(ServerS6aSession session, JUpdateLocationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a ULR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doNotifyRequestEvent(ServerS6aSession session, JNotifyRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a NOR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doCancelLocationAnswerEvent(ServerS6aSession session, JCancelLocationRequest request,
      JCancelLocationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a CLA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doInsertSubscriberDataAnswerEvent(ServerS6aSession session, JInsertSubscriberDataRequest request,
      JInsertSubscriberDataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a IDA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doDeleteSubscriberDataAnswerEvent(ServerS6aSession session, JDeleteSubscriberDataRequest request,
      JDeleteSubscriberDataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a DSA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doResetAnswerEvent(ServerS6aSession session, JResetRequest request, JResetAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a RSA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

}
