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
import org.jdiameter.api.s6a.ClientS6aSession;
import org.jdiameter.api.s6a.ClientS6aSessionListener;
import org.jdiameter.api.s6a.ServerS6aSession;
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
import org.jdiameter.common.impl.app.s6a.S6aSessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.utils.TBase;

public abstract class S6aAbstractClient extends TBase implements ClientS6aSessionListener {
  private final Logger logger = LoggerFactory.getLogger(S6aAbstractClient.class);
  private ClientS6aSession clientS6aSession;
  protected S6aSessionFactoryImpl s6aSessionFactory;
  private static final long VENDOR_ID = 10415;
  private static final long AUTH_APPLICATION_ID = 16777251;

  public void init(FileInputStream configStream, String clientID) {
    try {
      super.init(configStream, clientID, ApplicationId.createByAuthAppId(VENDOR_ID, AUTH_APPLICATION_ID));
      this.s6aSessionFactory = new S6aSessionFactoryImpl(this.sessionFactory);
      this.sessionFactory.registerAppFacory(ServerS6aSession.class, s6aSessionFactory);
      this.sessionFactory.registerAppFacory(ClientS6aSession.class, s6aSessionFactory);

      s6aSessionFactory.setClientSessionListener(this);

      this.clientS6aSession = this.sessionFactory.getNewAppSession(this.sessionFactory.getSessionId("S6a"),
          getApplicationId(), ClientS6aSession.class, (Object[]) null);
    } catch (InternalException e) {
      e.printStackTrace();

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

  public void start(Mode mode, long timeOut, TimeUnit timeUnit)
      throws IllegalDiameterStateException, InternalException {
    stack.start(mode, timeOut, timeUnit);
  }

  public String getSessionId() {
    return this.clientS6aSession.getSessionId();
  }

  public ClientS6aSession getSession() {
    return this.clientS6aSession;
  }

  public S6aSessionFactoryImpl getS6aSessionFactory() {
    return s6aSessionFactory;
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
