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
import org.jdiameter.api.s6t.ServerS6tSession;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationAnswer;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.common.impl.app.s6t.JConfigurationInformationAnswerImpl;
import org.jdiameter.common.impl.app.s6t.JNIDDInformationAnswerImpl;
import org.jdiameter.common.impl.app.s6t.JReportingInformationRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.interfaces.S6tAbstractServer;

public class S6tServer extends S6tAbstractServer {
  protected final Logger logger = LoggerFactory.getLogger(S6tServer.class);
  private HSS hss;

  private String configFile;

  public S6tServer(HSS hssCtx, String configFile) {
    this.hss = hssCtx;
    this.configFile = configFile;
  }

  public void init(String serverID) throws Exception {
    if (logger.isInfoEnabled()) {
      logger.info("Reading config file S6tSessionFactoryImpl: " + this.configFile);
    }
    this.init(new FileInputStream(this.configFile), serverID);
  }

  public void sendRIR() {
    if (logger.isInfoEnabled()) {
      logger.info("Send RIR to SCEF");
    }
    
     try {
       //this.serverS6tSession = this.sessionFactory.getNewAppSession(getApplicationId(), ServerS6tSession.class);
       ServerS6tSession serverS6tSession = this.sessionFactory.getNewAppSession(getApplicationId(), ServerS6tSession.class);
      //rir = new JReportingInformationRequestImpl(super.createRequest(this.serverS6tSession, JReportingInformationRequest.code));
      JReportingInformationRequest rir = 
          new JReportingInformationRequestImpl(super.createRequest(serverS6tSession, JReportingInformationRequest.code));

      AvpSet reqSet = rir.getMessage().getAvps();
      reqSet.addAvp(Avp.DESTINATION_HOST, this.getRemoteRealm(), true);

      serverS6tSession.sendReportingInformationRequest(rir);

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
      logger.info("Sent RIR to SCEF");
    }

  }
  
  public void sendCIAAnswer(ServerS6tSession session, JConfigurationInformationRequest cir, int resultCode) {
    try {
      JConfigurationInformationAnswer cia = new JConfigurationInformationAnswerImpl((Request) cir.getMessage(), resultCode);
      Answer answer = (Answer) cia.getMessage();
      
      updateAnswer(cir.getMessage(), answer, resultCode);
      
      AvpSet set = answer.getAvps();
      
      //TODO
      //Utils.printMessage(logger, this.getStack().getDictionary(), answer, true);

      session.sendConfigurationInformationAnswer(this.s6tSessionFactory.createConfigurationInformationAnswer(answer));
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

  public void sendNIAAnswer(ServerS6tSession session, JNIDDInformationRequest nir, int resultCode) {
    try {
      JNIDDInformationAnswer nia = new JNIDDInformationAnswerImpl((Request) nir.getMessage(), resultCode);

      Answer answer = (Answer) nia.getMessage();
      updateAnswer(nir.getMessage(), answer, resultCode);

      AvpSet set = answer.getAvps();
      //TODO
 
      session.sendNIDDInformationAnswer(this.s6tSessionFactory.createNIDDInformationAnswer(answer));
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
    //logger.info("processRequest");
    int code = request.getCommandCode();
    switch (code) {
    case JConfigurationInformationRequest.code:
    case JNIDDInformationRequest.code:
      try {
        ServerS6tSession serverS6tSession = (ServerS6tSession) this.s6tSessionFactory.getNewSession(request.getSessionId(), ServerS6tSession.class, this.getApplicationId(), (Object[])null);
        ((NetworkReqListener) serverS6tSession).processRequest(request);
      } catch (Exception e) {
        logger.error(e.toString());
        e.printStackTrace();
      }
      return null;
    case JReportingInformationAnswer.code:
      logger.error(new StringBuilder("processRequest - : Reporting-Information-Request: Not Supported message in this state: ")
          .append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      break;
    default:
      logger.error(new StringBuilder("processRequest - S6t - Not Supported message: ").append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      return null;
    }
    return null;
  }

  @Override
  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error(
        "Received \"S6t Other\" event, request[" + request + "], answer[" + answer + "], on session[" + session + "]");
  }

  @Override
  public void doConfigurationInformationRequestEvent(ServerS6tSession session, JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // TODO
    if (logger.isInfoEnabled()) {
      logger.info("Received S6t Configuration-Information-Request (CIR) : " + request);
    }
    hss.handleConfigurationInformationRequestEvent(session, request);
  }

  @Override
  public void doReportingInformationAnswerEvent(ServerS6tSession session, JReportingInformationRequest request,
      JReportingInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // TODO
    if (logger.isInfoEnabled()) {
      logger.info("Received S6t Reporting-Information-Answer (RIA) : " + request);
    }
  }

  @Override
  public void doNIDDInformationRequestEvent(ServerS6tSession session, JNIDDInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    if (logger.isInfoEnabled()) {
      logger.info("Received S6t NIDD-Information-Request (NIR) : " + request);
    }
    hss.handleNIDDInformationRequestEvent(session, request);
  }

  
}
