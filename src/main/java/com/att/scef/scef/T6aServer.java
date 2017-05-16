package com.att.scef.scef;

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
import org.jdiameter.api.t6a.ServerT6aSession;
import org.jdiameter.api.t6a.events.JConfigurationInformationAnswer;
import org.jdiameter.api.t6a.events.JConfigurationInformationRequest;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataAnswer;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataAnswer;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.jdiameter.api.t6a.events.JReportingInformationRequest;
import org.jdiameter.common.impl.app.t6a.JMO_DataAnswerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
      AvpSet set = answer.getAvps();

      session.sendMO_DataAnswer(this.t6aSessionFactory.createMO_DataAnswer(answer));
      
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
    case JMO_DataRequest.code:
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
    case JConnectionManagementRequest.code:
      // both request and response can be happened
      logger.error(new StringBuilder("processRequest - : Connection-Manager-Request: Not yet implemented: ")
          .append(request.getCommandCode()).append(" from interface : ").append(request.getApplicationId())
          .append(" from Class ").append(request.getClass().getName()).toString());
      break;
    case JMT_DataAnswer.code:
      logger.error(new StringBuilder("processRequest - : MT-Data-Answer: Not yet implemented: ")
          .append(request.getCommandCode()).append(" from interface : ").append(request.getApplicationId())
          .append(" from Class ").append(request.getClass().getName()).toString());
      break;
    case JReportingInformationRequest.code:
      logger.error(new StringBuilder("processRequest - : Reporting-Information-Request: Not yet implemented: ")
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
    this.scef.handleSendConfigurationInformationAnswerEvent(session, request, answer);
  }

  @Override
  public void doSendConfigurationInformationRequestEvent(ServerT6aSession session,
      JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleSendConfigurationInformationRequestEvent(session, request);
  }

  @Override
  public void doSendReportingInformationRequestEvent(ServerT6aSession session, JReportingInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleSendReportingInformationRequestEvent(session, request);
  }

  @Override
  public void doSendMO_DataRequestEvent(ServerT6aSession session, JMO_DataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleSendMO_DataRequestEvent(session, request);
  }

  @Override
  public void doSendMT_DataAnswertEvent(ServerT6aSession session, JMT_DataRequest request, JMT_DataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleSendMT_DataAnswertEvent(session, request, answer);
  }

  @Override
  public void doSendConnectionManagementAnswertEvent(ServerT6aSession session, JConnectionManagementRequest request,
      JConnectionManagementAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleSendConnectionManagementAnswertEvent(session, request, answer);
  }

  @Override
  public void doSendConnectionManagementRequestEvent(ServerT6aSession session, JConnectionManagementRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleSendConnectionManagementRequestEvent(session, request);
  }

}
