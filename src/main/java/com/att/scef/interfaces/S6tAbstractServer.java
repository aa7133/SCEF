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
import org.jdiameter.api.s6t.ClientS6tSession;
import org.jdiameter.api.s6t.ServerS6tSession;
import org.jdiameter.api.s6t.ServerS6tSessionListener;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationAnswer;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.common.impl.app.s6t.S6tSessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.utils.TBase;

public abstract class S6tAbstractServer extends TBase implements ServerS6tSessionListener {
  private final Logger logger = LoggerFactory.getLogger(S6tAbstractServer.class);
  protected ServerS6tSession serverS6tSession;
  protected S6tSessionFactoryImpl s6tSessionFactory;
  private static final long VENDOR_ID = 10415;
  private static final long S6T_AUTH_APPLICATION_ID = 16777345;

  public void init(FileInputStream configStream, String serverId) {
    try {
      super.init(configStream, serverId, ApplicationId.createByAuthAppId(VENDOR_ID, S6T_AUTH_APPLICATION_ID));
      s6tSessionFactory = new S6tSessionFactoryImpl(this.sessionFactory);
      this.sessionFactory.registerAppFacory(ServerS6tSession.class, s6tSessionFactory);
      this.sessionFactory.registerAppFacory(ClientS6tSession.class, s6tSessionFactory);
      s6tSessionFactory.setServerSessionListener(this);
      this.serverS6tSession = this.sessionFactory.getNewAppSession(getApplicationId(), ServerS6tSession.class);

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
/*
  protected Request createRequest(AppSession session, int code) {
    Request r = session.getSessions().get(0).createRequest(code, getApplicationId(), this.getRemoteRealm());

    AvpSet reqSet = r.getAvps();
    AvpSet vendorSpecificApplicationId = reqSet.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID, 0, false, false);
    // 1* [ Vendor-Id ]
    vendorSpecificApplicationId.addAvp(Avp.VENDOR_ID, getApplicationId().getVendorId(), true);
    // 0*1{ Auth-Application-Id }
    vendorSpecificApplicationId.addAvp(Avp.AUTH_APPLICATION_ID, getApplicationId().getAuthAppId(), true);
    // 0*1{ Acct-Application-Id }
    // { Auth-Session-State }
    reqSet.addAvp(Avp.AUTH_SESSION_STATE, 1);
    // { Origin-Host }
    reqSet.removeAvp(Avp.ORIGIN_HOST);
    reqSet.addAvp(Avp.ORIGIN_HOST, this.getFirstPeerFromList(), true);
    return r;
  }
*/
  
  @Override
  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6t Other\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  @Override
  public void doConfigurationInformationRequestEvent(ServerS6tSession session, JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6t CIR\" event, request[" + request + "], on session[" + session + "]");
  }

  @Override
  public void doReportingInformationAnswerEvent(ServerS6tSession session, JReportingInformationRequest request,
      JReportingInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error(
        "Received \"S6t RIA\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  @Override
  public void doNIDDInformationRequestEvent(ServerS6tSession session, JNIDDInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6t NIR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void setServerS6tSession(ServerS6tSession serverS6tSession) {
    this.serverS6tSession = serverS6tSession;
  }

}
