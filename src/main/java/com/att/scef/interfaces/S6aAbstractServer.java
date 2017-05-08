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
import org.jdiameter.api.s6a.ServerS6aSession;
import org.jdiameter.api.s6a.ServerS6aSessionListener;
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
import org.jdiameter.common.impl.app.s6a.S6aSessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.utils.TBase;

public abstract class S6aAbstractServer extends TBase implements ServerS6aSessionListener{

  private final Logger logger = LoggerFactory.getLogger(S6tAbstractServer.class);
  protected ServerS6aSession serverS6aSession;
  protected S6aSessionFactoryImpl s6aSessionFactory;
  private static final long VENDOR_ID = 10415;
  private static final long S6T_AUTH_APPLICATION_ID = 16777345;

  public void init(FileInputStream configStream, String serverId) {
    try {
      super.init(configStream, serverId, ApplicationId.createByAuthAppId(VENDOR_ID, S6T_AUTH_APPLICATION_ID));
      s6aSessionFactory = new S6aSessionFactoryImpl(this.sessionFactory);
      this.sessionFactory.registerAppFacory(ServerS6aSession.class, s6aSessionFactory);
      this.sessionFactory.registerAppFacory(ClientS6aSession.class, s6aSessionFactory);
      s6aSessionFactory.setServerSessionListener(this);
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
