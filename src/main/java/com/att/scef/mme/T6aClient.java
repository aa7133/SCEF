package com.att.scef.mme;

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
import org.jdiameter.api.t6a.ClientT6aSession;
import org.jdiameter.api.t6a.ServerT6aSession;
import org.jdiameter.api.t6a.events.JConfigurationInformationAnswer;
import org.jdiameter.api.t6a.events.JConfigurationInformationRequest;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataAnswer;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataAnswer;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.jdiameter.api.t6a.events.JReportingInformationAnswer;
import org.jdiameter.api.t6a.events.JReportingInformationRequest;
import org.jdiameter.common.impl.app.t6a.JMO_DataRequestImpl;
import org.jdiameter.common.impl.app.t6a.JMT_DataAnswerImpl;
import org.jdiameter.common.impl.app.t6a.JReportingInformationRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.interfaces.T6aAbstractClient;
import com.att.scef.utils.MonitoringType;


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
  
  public void sendTDA(ClientT6aSession session, JMT_DataRequest request, int resultCode) {
    if (logger.isInfoEnabled()) {
      logger.info("Sent TDA to SCEF");
    }
    try {
      JMT_DataAnswer tda = new JMT_DataAnswerImpl((Request)request.getMessage(), resultCode);
      Answer answer = (Answer)tda.getMessage();
      //AvpSet set = answer.getAvps();

      session.sendMTDataAnswer(this.t6aSessionFactory.createMT_DataAnswer(answer));
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
  
  public void sendODR(String msisdn, String msg) {
    if (logger.isInfoEnabled()) {
      logger.info("Send ODR to SCEF");
    }
    
    if (msisdn == null || msisdn.length() == 0) {
      logger.error("No MSISDN");
      return;
    }

    try {
      ClientT6aSession clientT6aSession = this.sessionFactory.getNewAppSession(getApplicationId(),
          ClientT6aSession.class);
      JMO_DataRequest odr = new JMO_DataRequestImpl(super.createRequest(clientT6aSession, JMO_DataRequest.code, msisdn, "Bearer-ID1"));

      AvpSet reqSet = odr.getMessage().getAvps();
      

      reqSet.addAvp(Avp.DESTINATION_HOST, this.getDestinationHost(), true);
      
      reqSet.addAvp(Avp.NON_IP_DATA, msg, false);


      clientT6aSession.sendMODataRequest(odr);

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

    if (logger.isInfoEnabled()) {
      logger.info("Sent ODR to SCEF");
    }
  }
  
  public void sendRIR(String msisdn, GMonitoringEventConfig gmon, int monitoringEvent) {
    if (logger.isInfoEnabled()) {
      logger.info("Send RIR from MME to SCEF for device " + msisdn);
    }
    try {
      ClientT6aSession clientT6aSession = this.sessionFactory.getNewAppSession(getApplicationId(), ClientT6aSession.class);
      
      JReportingInformationRequest rir = 
          new JReportingInformationRequestImpl(super.createRequest(clientT6aSession, JReportingInformationRequest.code));

      AvpSet reqSet = rir.getMessage().getAvps();
      reqSet.addAvp(Avp.DESTINATION_HOST, this.getDestinationHost(), true);

      
//       * Monitoring-Event-Report::= <AVP header: 3123 10415> TS- 29-128
      AvpSet monitoringReport = reqSet.addGroupedAvp(Avp.MONITORING_EVENT_REPORT, true, false);
//      { SCEF-Reference-ID }
      monitoringReport.addAvp(Avp.SCEF_REFERENCE_ID, gmon.getScefRefId());
//      [ SCEF-ID ]
      monitoringReport.addAvp(Avp.SCEF_ID, gmon.getScefId(), this.getApplicationId().getVendorId(), true, false, false);
//      [ Monitoring-Type ]
      monitoringReport.addAvp(Avp.MONITORING_TYPE, monitoringEvent);
//      [ Reachability-Information ]
      if (monitoringEvent == MonitoringType.UE_REACHABILITY) {
        monitoringReport.addAvp(Avp.REACHABILITY_INFORMATION, MonitoringType.UE_REACHABILITY_REACHABLE_FOR_DATA);
      }
//      [ EPS-Location-Information ]
      else if (monitoringEvent == MonitoringType.LOCATION_REPORTING) {
        AvpSet epsLocation = reqSet.addGroupedAvp(Avp.EPS_LOCATION_INFORMATION, false, false);
        AvpSet mmeLocation = epsLocation.addGroupedAvp(Avp.MME_LOCATION_INFORMATION, false, false);
        mmeLocation.addAvp(Avp.TRACKING_AREA_IDENTITY, "JUSTDemo123", this.getApplicationId().getVendorId(), false, false, true);
      }
//      [ Communication-Failure-Information ]
      else if (monitoringEvent == MonitoringType.COMMUNICATION_FAILURE) {
        AvpSet commFailure = reqSet.addGroupedAvp(Avp.COMMUNICATION_FAILURE_INFORMATION, this.getApplicationId().getVendorId(), true, false);
        commFailure.addAvp(Avp.CAUSE_TYPE, MonitoringType.CAUSE_TYPE_NAS);
        commFailure.addAvp(Avp.S1AP_CAUSE, MonitoringType.CAUSE_TYPE_NAS);
      }
//      *[ Number-Of-UE-Per-Location-Report ]
      else if (monitoringEvent == MonitoringType.NUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA) {
        AvpSet uePerLocation = reqSet.addGroupedAvp(Avp.NUMBER_OF_UE_PER_LOCATION_REPORT, this.getApplicationId().getVendorId(), true, false);
        AvpSet epsLocation = uePerLocation.addGroupedAvp(Avp.EPS_LOCATION_INFORMATION, true, false);
        AvpSet mmeLocation = epsLocation.addGroupedAvp(Avp.MME_LOCATION_INFORMATION, true, false);
        mmeLocation.addAvp(Avp.TRACKING_AREA_IDENTITY, "JUSTDemo123", this.getApplicationId().getVendorId(), false, false, true);
        
        uePerLocation.addAvp(Avp.UE_COUNT, 2000, true, false);
      }

      clientT6aSession.sendReportingInformationRequest(rir);

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
    case JConfigurationInformationRequest.code:
    case JConnectionManagementRequest.code:
      if (request.isRequest()) {
        if (logger.isInfoEnabled()) {
          logger.info("Got Request for command : " + request.getCommandCode());
        }
        ClientT6aSession clientT6aSession =  (ClientT6aSession)this.t6aSessionFactory
            .getNewSession(request.getSessionId(), ClientT6aSession.class, this.getApplicationId(), (Object[])null);
        ((NetworkReqListener)clientT6aSession).processRequest(request);
        break;
      }
      else {
        if (logger.isInfoEnabled()) {
          logger.info("Got Answer for command : " + request.getCommandCode());
        }
        ServerT6aSession serverT6aSession =  (ServerT6aSession)this.t6aSessionFactory
            .getNewSession(request.getSessionId(), ServerT6aSession.class, this.getApplicationId(), (Object[])null);
        ((NetworkReqListener)serverT6aSession).processRequest(request);
        break;
      }
    case JReportingInformationRequest.code:
    case JMO_DataAnswer.code:
    case JMT_DataRequest.code:
      ClientT6aSession clientT6aSession =  (ClientT6aSession)this.t6aSessionFactory
          .getNewSession(request.getSessionId(), ClientT6aSession.class, this.getApplicationId(), (Object[])null);
      ((NetworkReqListener)clientT6aSession).processRequest(request);
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
