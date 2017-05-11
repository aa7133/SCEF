package com.att.scef.mme;

import java.io.FileInputStream;

import org.jdiameter.api.Answer;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6a.ClientS6aSession;
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
import org.jdiameter.common.impl.app.s6a.JInsertSubscriberDataAnswerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.interfaces.S6aAbstractClient;
import com.att.scef.scef.SCEF;

public class S6aClient extends S6aAbstractClient {
  protected final Logger logger = LoggerFactory.getLogger(S6aClient.class);

  private String configFile;
  private MME mme;

  public S6aClient(MME mmeCtx, String configFile) {
    //TODO add the MME context
    this.mme = mmeCtx;
    this.configFile = configFile;
    logger.info(this.configFile);
  }
  
  public void init(String clientID) throws Exception {
    logger.info(clientID);

    this.init(new FileInputStream(configFile), clientID);
  }

  public void sendIDA(ClientS6aSession session, JInsertSubscriberDataRequest idr, int resultCode) {
    try {
      JInsertSubscriberDataAnswer ida = new JInsertSubscriberDataAnswerImpl((Request)idr.getMessage(), resultCode);
      Answer answer = (Answer)ida.getMessage();
      
      AvpSet set = answer.getAvps();
      
      session.sendInsertSubscriberDataAnswer(this.s6aSessionFactory.createInsertSubscriberDataAnswer(answer));
    } catch (InternalException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalDiameterStateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RouteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (OverloadException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
  
  @Override
  public Answer processRequest(Request request) {
    int code = request.getCommandCode();
    switch (code) {
    case JAuthenticationInformationRequest.code:
      logger.error(new StringBuilder("processRequest - : Autentication-Information-Answer: Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
     break;

    case JPurgeUERequest.code:
      logger.error(new StringBuilder("processRequest - : Purge-UE-Answer: Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
     break;

    case JUpdateLocationRequest.code:
      logger.error(new StringBuilder("processRequest - : Update-Location-Answer: Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
     break;

    case JNotifyRequest.code:
      logger.error(new StringBuilder("processRequest - : Notify : Not yet implemented: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
     break;
   case JCancelLocationAnswer.code:
      logger.error(new StringBuilder("processRequest - : Cancel-Location-Request: Not yet implemented: ")
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
    case JInsertSubscriberDataRequest.code:
      try {
        ClientS6aSession clientS6aSession = (ClientS6aSession)this.s6aSessionFactory
            .getNewSession(request.getSessionId(), ClientS6aSession.class, this.getApplicationId(), (Object[])null);
        ((NetworkReqListener)clientS6aSession).processRequest(request);
      } catch (Exception e) {
        logger.error(e.toString());
        e.printStackTrace();
      }
      return null;
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
  public void doCancelLocationRequestEvent(ClientS6aSession session, JCancelLocationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a CLR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doInsertSubscriberDataRequestEvent(ClientS6aSession session, JInsertSubscriberDataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.mme.handleInsertSubscriberDataRequestEvent(session, request);
  }

  @Override
  public void doDeleteSubscriberDataRequestEvent(ClientS6aSession session, JDeleteSubscriberDataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a DSR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doResetRequestEvent(ClientS6aSession session, JResetRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a RSR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doAuthenticationInformationAnswerEvent(ClientS6aSession session, JAuthenticationInformationRequest request, JAuthenticationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a AIA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doPurgeUEAnswerEvent(ClientS6aSession session, JPurgeUERequest request, JPurgeUEAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a PUA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doUpdateLocationAnswerEvent(ClientS6aSession session, JUpdateLocationRequest request, JUpdateLocationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a ULA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  @Override
  public void doNotifyAnswerEvent(ClientS6aSession session, JNotifyRequest request, JNotifyAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a NOA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

}
