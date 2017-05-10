package com.att.scef.interfaces;

import java.io.FileInputStream;

import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.events.JAuthenticationInformationAnswer;
import org.jdiameter.api.s6a.events.JAuthenticationInformationRequest;
import org.jdiameter.api.s6a.events.JCancelLocationRequest;
import org.jdiameter.api.s6a.events.JDeleteSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JInsertSubscriberDataRequest;
import org.jdiameter.api.s6a.events.JNotifyAnswer;
import org.jdiameter.api.s6a.events.JNotifyRequest;
import org.jdiameter.api.s6a.events.JPurgeUEAnswer;
import org.jdiameter.api.s6a.events.JPurgeUERequest;
import org.jdiameter.api.s6a.events.JResetRequest;
import org.jdiameter.api.s6a.events.JUpdateLocationAnswer;
import org.jdiameter.api.s6a.events.JUpdateLocationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.mme.MME;
import com.att.scef.scef.SCEF;

public class S6aClient extends S6aAbstractClient {
  protected final Logger logger = LoggerFactory.getLogger(S6tClient.class);

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

  
  @Override
  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a Other\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void doCancelLocationRequestEvent(ClientS6aSession session, JCancelLocationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a CLR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void doInsertSubscriberDataRequestEvent(ClientS6aSession session, JInsertSubscriberDataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a IDR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void doDeleteSubscriberDataRequestEvent(ClientS6aSession session, JDeleteSubscriberDataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a DSR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void doResetRequestEvent(ClientS6aSession session, JResetRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a RSR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void doAuthenticationInformationAnswerEvent(ClientS6aSession session, JAuthenticationInformationRequest request, JAuthenticationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a AIA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void doPurgeUEAnswerEvent(ClientS6aSession session, JPurgeUERequest request, JPurgeUEAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a PUA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void doUpdateLocationAnswerEvent(ClientS6aSession session, JUpdateLocationRequest request, JUpdateLocationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a ULA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void doNotifyAnswerEvent(ClientS6aSession session, JNotifyRequest request, JNotifyAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a NOA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

}
