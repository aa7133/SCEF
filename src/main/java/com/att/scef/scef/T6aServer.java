package com.att.scef.scef;

import java.io.FileInputStream;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
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
import org.jdiameter.common.impl.app.t6a.JMO_DataAnswerImpl;
import org.jdiameter.common.impl.app.t6a.JMT_DataRequestImpl;
import org.jdiameter.common.impl.app.t6a.JReportingInformationAnswerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.gson.GSCEFUserProfile;
import com.att.scef.interfaces.T6aAbstractServer;

public class T6aServer extends T6aAbstractServer {
  protected final Logger logger = LoggerFactory.getLogger(T6aServer.class);
  private SCEF scef;

  private String configFile;

  public T6aServer(SCEF scefCtx, String configFile) {
    this.scef = scefCtx;
    this.configFile = configFile;
  }

  public void init(String serverID) throws Exception {
    if (logger.isInfoEnabled()) {
      logger.info("Reading config file T6aSessionFactoryImpl: " + this.configFile);
    }
    this.init(new FileInputStream(this.configFile), serverID);
  }
  
  public void sendODA(ServerT6aSession session, JMO_DataRequest request, int resultCode) {
    try {
      JMO_DataAnswer oda = new JMO_DataAnswerImpl((Request)request.getMessage(), resultCode);
      Answer answer = (Answer)oda.getMessage();
    
      //AvpSet set = answer.getAvps();

      session.sendMO_DataAnswer(this.t6aSessionFactory.createMO_DataAnswer(answer));
      
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
  
  public void sendRIA(ServerT6aSession session, JReportingInformationRequest request, int resultCode) {
    try {
      JReportingInformationAnswer ria = new JReportingInformationAnswerImpl((Request)request.getMessage(), resultCode);
      Answer answer = (Answer)ria.getMessage();
      
      session.sendReportingInformationAnswer(this.t6aSessionFactory.createReportingInformationAnswer(answer));
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
  
  public String sendTDRRequest(GSCEFUserProfile userProfile, String message) {
    try {
          ServerT6aSession serverT6aSession = this.sessionFactory.getNewAppSession(getApplicationId(),
              ServerT6aSession.class);
          JMT_DataRequest tdr = new JMT_DataRequestImpl(super.createRequest(serverT6aSession, JMT_DataRequest.code, 
              userProfile.getMsisdn(), "Bearer-fake"));
          AvpSet reqSet = tdr.getMessage().getAvps();
          Avp sessionIdAvp = reqSet.getAvp(Avp.SESSION_ID);
          String sessionId = sessionIdAvp.getUTF8String();
          
          reqSet.addAvp(Avp.DESTINATION_HOST, this.getDestinationHost(), true);
          
          reqSet.addAvp(Avp.NON_IP_DATA, message, false);


          serverT6aSession.sendMT_DataRequest(tdr);
          
          return sessionId;

    } catch (InternalException e) {
      e.printStackTrace();
    } catch (IllegalDiameterStateException e) {
      e.printStackTrace();
    } catch (RouteException e) {
      e.printStackTrace();
    } catch (OverloadException e) {
      e.printStackTrace();
    } catch (AvpDataException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  @Override
  public Answer processRequest(Request request) {
    int code = request.getCommandCode();
    switch (code) {
    case JMO_DataRequest.code:
    case JReportingInformationRequest.code:
    case JConnectionManagementRequest.code:
            try {
        ServerT6aSession serverT6aSession = (ServerT6aSession)this.t6aSessionFactory
            .getNewSession(request.getSessionId(), ServerT6aSession.class, this.getApplicationId(), (Object[])null);
        ((NetworkReqListener)serverT6aSession).processRequest(request);
      } catch (Exception e) {
        logger.error(e.toString());
        e.printStackTrace();
      }
      return null;
    case JConfigurationInformationAnswer.code:
      logger.error(new StringBuilder("processRequest - : Configuration-Information-Answer : Not yet implemented: ")
          .append(request.getCommandCode()).append(" from interface : ").append(request.getApplicationId())
          .append(" from Class ").append(request.getClass().getName()).toString());
      break;
    case JMT_DataAnswer.code:
      logger.error(new StringBuilder("processRequest - : MT-Data-Answer: Not yet implemented: ")
          .append(request.getCommandCode()).append(" from interface : ").append(request.getApplicationId())
          .append(" from Class ").append(request.getClass().getName()).toString());
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
    this.scef.handleT6aOtherEvent(session, request, answer);
  }

  @Override
  public void doSendConfigurationInformationAnswerEvent(ServerT6aSession session,
      JConfigurationInformationRequest request, JConfigurationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleT6aConfigurationInformationAnswerEvent(session, request, answer);
  }

  @Override
  public void doSendConfigurationInformationRequestEvent(ServerT6aSession session,
      JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleT6aConfigurationInformationRequestEvent(session, request);
  }

  @Override
  public void doSendReportingInformationRequestEvent(ServerT6aSession session, JReportingInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleT6aReportingInformationRequestEvent(session, request);
  }

  @Override
  public void doSendMO_DataRequestEvent(ServerT6aSession session, JMO_DataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleT6aMO_DataRequestEvent(session, request);
  }

  @Override
  public void doSendMT_DataAnswertEvent(ServerT6aSession session, JMT_DataRequest request, JMT_DataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleT6aMT_DataAnswertEvent(session, request, answer);
  }

  @Override
  public void doSendConnectionManagementAnswertEvent(ServerT6aSession session, JConnectionManagementRequest request,
      JConnectionManagementAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleT6aConnectionManagementAnswertEvent(session, request, answer);
  }

  @Override
  public void doSendConnectionManagementRequestEvent(ServerT6aSession session, JConnectionManagementRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleT6aConnectionManagementRequestEvent(session, request);
  }

}
