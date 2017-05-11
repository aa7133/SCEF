package com.att.scef.hss;

import java.io.FileInputStream;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Avp;
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
import org.jdiameter.common.impl.app.s6a.JInsertSubscriberDataRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GCommunicationPatternSet;
import com.att.scef.gson.GHSSUserProfile;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.interfaces.S6aAbstractServer;

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
  
  private void buildUserDataAvp(GHSSUserProfile hssData, AvpSet userData, long vendorId) {
    for (GMonitoringEventConfig mo : hssData.getMonitoringConfig()) {
        boolean isRefId = false;
        AvpSet monitoringEvCon = userData.addGroupedAvp(Avp.MONITORING_EVENT_CONFIGURATION, vendorId,true, false);
        monitoringEvCon.addAvp(Avp.SCEF_ID, mo.getScefId(), vendorId, true, false, true);
        monitoringEvCon.addAvp(Avp.MONITORING_TYPE, mo.getMonitoringType(), vendorId, true, false);

        if (mo.getScefRefId() != 0) {
            isRefId = true;
            monitoringEvCon.addAvp(Avp.SCEF_REFERENCE_ID, mo.getScefRefId());
        }
    
        int[] refFordel = mo.getScefRefIdForDelition();
        if (refFordel != null) {
            for (int i : refFordel) {
                isRefId = true;
                monitoringEvCon.addAvp(Avp.SCEF_REFERENCE_ID_FOR_DELETION, i);
            }
        }

        if (isRefId == false) {
            logger.error("No SCEF-Reference-ID or SCEF-Reference-ID-For-Delition exists event skiped");
            userData.removeAvp(Avp.MONITORING_EVENT_REPORT);
            continue;
        }
        monitoringEvCon.addAvp(Avp.MAXIMUM_NUMBER_OF_REPORTS, mo.getMaximumNumberOfReports(), vendorId, true, false);
    }
    
    for (GAESE_CommunicationPattern ae : hssData.getAESECommunicationPattern()) {
        AvpSet aese = userData.addGroupedAvp(Avp.AESE_COMMUNICATION_PATTERN, vendorId,true, false);
        boolean isRefId = false;
        aese.addAvp(Avp.SCEF_ID, ae.getScefId(), vendorId, true, false, true);
        if (ae.getScefRefId() != 0) {
            isRefId = true;
            aese.addAvp(Avp.SCEF_REFERENCE_ID, ae.getScefRefId());
        }
        int[] refFordel = ae.getScefRefIdForDelition();
        if (refFordel != null) {
            for (int i : refFordel) {
                isRefId = true;
                aese.addAvp(Avp.SCEF_REFERENCE_ID_FOR_DELETION, i);
            }
        }
        if (isRefId == false) {
            logger.error("No SCEF-Reference-ID or SCEF-Reference-ID-For-Delition exists event skiped");
            userData.removeAvp(Avp.AESE_COMMUNICATION_PATTERN);
            continue;
        }
        
        GCommunicationPatternSet[] cps = ae.getCommunicationPatternSet();
        if (cps != null) {
            for (GCommunicationPatternSet cp : cps) {
                AvpSet commPattSet = aese.addGroupedAvp(Avp.COMMUNICATION_PATTERN_SET, vendorId,true, false);
                commPattSet.addAvp(Avp.PERIODIC_COMMUNICATION_INDICATOR, cp.periodicCommunicationIndicator);
                if (cp.periodicCommunicationIndicator == 0) {
                    commPattSet.addAvp(Avp.COMMUNICATION_DURATION_TIME, cp.communicationDurationTime);
                    commPattSet.addAvp(Avp.PERIODIC_TIME, cp.periodictime);
                }
            }
        }
    }
    
}

  
  public void sendIDRRequest(GHSSUserProfile hssData, String mmeAddress) {
    // build  IDR message to mme for update user data
    try {
      if (logger.isInfoEnabled()) {
        logger.info("Send IDR to MME");
      }
      
      JInsertSubscriberDataRequest idr =
          new JInsertSubscriberDataRequestImpl(super.createRequest(this.serverS6aSession, JInsertSubscriberDataRequest.code));

      
      /*
        Request idr = this.createRequest(session, this.s6aAuthApplicationId, JInsertSubscriberDataRequest.code,
                HSS_REALM,
                this.getConfiguration().getStringValue(OwnDiameterURI.ordinal(), "aaa://127.0.0.1:23000"));
      */
        AvpSet reqSet = idr.getMessage().getAvps();
        //idr.getMessage().setRequest(true);

        // add data
        // < Insert-Subscriber-Data-Request> ::= < Diameter Header: 319,
        // REQ, PXY, 16777251 >
        // < Session-Id > by createRequest
        // [ DRMP ]
        // [ Vendor-Specific-Application-Id ] by createRequest
        // { Auth-Session-State } by createRequest
        // { Origin-Host } by createRequest
        // { Origin-Realm } by createRequest
        // { Destination-Host }
        reqSet.addAvp(Avp.DESTINATION_HOST, this.getDestinationHost(), true);
        // { Destination-Realm }
        //added at create request
        //reqSet.addAvp(Avp.DESTINATION_REALM, this.getRemoteRealm(), true);
        // { User-Name }
        //TODO fix imsi later
        //reqSet.addAvp(Avp.USER_NAME, hssData.getIMSI(), true);
        // *[ Supported-Features]
        // { Subscription-Data}

        buildUserDataAvp(hssData, reqSet.addGroupedAvp(Avp.SUBSCRIPTION_DATA,
                                 this.getApplicationId().getVendorId(), true, false),
                                 this.getApplicationId().getVendorId());

        // [ IDR- Flags ]
        // *[ Reset-ID ]
        // *[ AVP ]
        // *[ Proxy-Info ]
        // *[ Route-Record ]
        
        //JInsertSubscriberDataRequest request = this.s6aSessionFactory.createInsertSubscriberDataRequest((Request) idr);

//        ISessionFactory sessionFactory = ((ISessionFactory)this.stack.getSessionFactory());
//        ServerS6aSession serverS6asession = sessionFactory.getNewAppSession(request.getMessage().getSessionId(),
//                   this.getApplicationId(), ServerS6aSession.class, (Object)request);
        this.serverS6aSession.sendInsertSubscriberDataRequest(idr);
        //serverS6asession.sendInsertSubscriberDataRequest(request);

//        ((ServerS6aSession) ((ISessionFactory) this.stack.getSessionFactory()).getNewAppSession(
//            //idr.getMessage().getSessionId(), this.getApplicationId(), ServerS6aSession.class,
//            this.stack.getSessionFactory().getSessionId("S6A-IDR"), this.getApplicationId(), ServerS6aSession.class,
//                           (Object) idr)).sendInsertSubscriberDataRequest(idr);
        if (logger.isInfoEnabled()) {
          logger.info("Sent IDR to MME");
        }

    } catch (InternalException e) {
        e.printStackTrace();
    } catch (IllegalDiameterStateException e) {
        e.printStackTrace();
    } catch (RouteException e) {
        e.printStackTrace();
    } catch (OverloadException e) {
        e.printStackTrace();
    }

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
        ServerS6aSession serverS6aSession = (ServerS6aSession)this.s6aSessionFactory
            .getNewSession(request.getSessionId(), ServerS6aSession.class, this.getApplicationId(), (Object[])null);
        ((NetworkReqListener)serverS6aSession).processRequest(request);
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
