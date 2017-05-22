package com.att.scef.scef;

import static org.jdiameter.client.impl.helpers.Parameters.OwnDiameterURI;

import java.io.FileInputStream;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6t.ClientS6tSession;
import org.jdiameter.api.s6t.events.JConfigurationInformationAnswer;
import org.jdiameter.api.s6t.events.JConfigurationInformationRequest;
import org.jdiameter.api.s6t.events.JNIDDInformationAnswer;
import org.jdiameter.api.s6t.events.JNIDDInformationRequest;
import org.jdiameter.api.s6t.events.JReportingInformationAnswer;
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.jdiameter.common.impl.app.s6t.JReportingInformationAnswerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.gson.GSCEFUserProfile;
import com.att.scef.interfaces.S6tAbstractClient;
import com.att.scef.utils.BCDStringConverter;

public class S6tClient extends S6tAbstractClient {
	protected final Logger logger = LoggerFactory.getLogger(S6tClient.class);
	private SCEF scef;
	//private String remoteRealm = null;
	
	private String configFile;
	
	public S6tClient(SCEF scefCtx, String configFile) {
		this.scef = scefCtx;
		this.configFile = configFile;
	}
	
	public void init(String clientID) throws Exception {
	    if (logger.isInfoEnabled()) {
	      logger.info("Reading config file S6tClient : " + this.configFile);
	    }
		this.init(new FileInputStream(configFile), clientID);
	}


  public String sendCirRequest(GSCEFUserProfile newMsg, GMonitoringEventConfig[] mc, GAESE_CommunicationPattern[] cp) {
    String sessionID = null;
    try {
      Configuration conf = this.getStack().getMetaData().getConfiguration();
      ClientS6tSession session = (ClientS6tSession) this.sessionFactory.getNewAppSession(
          this.getStack().getSessionFactory().getSessionId("S6t-CIR"), this.getApplicationId(), ClientS6tSession.class,
          (Object[]) null);

      String localaddr = conf.getStringValue(OwnDiameterURI.ordinal(), "aaa://127.0.0.1:15868");
      if (logger.isInfoEnabled()) {
        logger.info(new StringBuilder("target realm = ").append(this.getRemoteRealm()).append(", localAddress = ")
            .append(localaddr).append(", Session id =").append(session.getSessionId()).toString());
      }
      Request request = this.createRequest(session, JConfigurationInformationRequest.code);

      AvpSet reqSet = request.getAvps();
      Avp sessionIdAvp = reqSet.getAvp(Avp.SESSION_ID);
      sessionID = sessionIdAvp.getUTF8String();

      reqSet.addAvp(Avp.DESTINATION_HOST, this.getDestinationHost(), true);

      // Add user identity
      AvpSet userIdentifier = reqSet.addGroupedAvp(Avp.USER_IDENTIFIER, getApplicationId().getVendorId(), true, false);

      String extId = newMsg.getExternalId();
      if (extId != null && extId.length() != 0) {
        userIdentifier.addAvp(Avp.EXTERNAL_IDENTIFIER, extId, getApplicationId().getVendorId(), true, false, false);
      }
      String msisdn = newMsg.getMsisdn();
      if (msisdn != null && msisdn.length() != 0) {
        userIdentifier.addAvp(Avp.MSISDN, BCDStringConverter.toBCD(msisdn), getApplicationId().getVendorId(), true,
            false);
      }
      String userNmae = newMsg.getUserName();
      if (userNmae != null && userNmae.length() != 0) {
        userIdentifier.addAvp(Avp.USER_NAME, userNmae, getApplicationId().getVendorId(), true, false, false);
      }

      if (mc != null && mc.length > 0) {
        for (GMonitoringEventConfig m : mc) {
          AvpSet monEvConf = reqSet.addGroupedAvp(Avp.MONITORING_EVENT_CONFIGURATION,
              this.getApplicationId().getVendorId(), true, false);
          monEvConf.addAvp(Avp.MONITORING_TYPE, m.getMonitoringType(), this.getApplicationId().getVendorId(), true,
              false);
          if (m.getScefRefId() == -1) { // delete
            for (long i : m.getScefRefIdForDelition()) {
              monEvConf.addAvp(Avp.SCEF_REFERENCE_ID_FOR_DELETION, i, this.getApplicationId().getVendorId(), true,
                  false);
            }
          } else {
            monEvConf.addAvp(Avp.SCEF_ID, m.getScefId(), this.getApplicationId().getVendorId(), true, false, false);
            monEvConf.addAvp(Avp.SCEF_REFERENCE_ID, m.getScefRefId(), this.getApplicationId().getVendorId(), true,
                false);
          }
        }
      }

      if (cp != null && cp.length > 0) {

      }

      // send the message
      JConfigurationInformationRequest cir = this.getS6tSessionFactory().createConfigurationInformationRequest(request);
      session.sendConfigurationInformationRequest(cir);

      if (logger.isInfoEnabled()) {
        logger.info("Configuration-Information-Request sent to HSS");
      }
      return sessionID;
      
    } catch (IllegalDiameterStateException e) {
      e.printStackTrace();
    } catch (InternalException e) {
      e.printStackTrace();
    } catch (RouteException e) {
      e.printStackTrace();
    } catch (OverloadException e) {
      e.printStackTrace();
    } catch (AvpDataException e) {
      e.printStackTrace();
    }
    return sessionID;
  }

  public void sendNirRequest(GSCEFUserProfile profile) {
    try {
      Configuration conf = this.getStack().getMetaData().getConfiguration();

      String localaddr = conf.getStringValue(OwnDiameterURI.ordinal(), "aaa://127.0.0.1:15868");

      ClientS6tSession session = (ClientS6tSession)this.sessionFactory
          .getNewAppSession(this.getStack().getSessionFactory().getSessionId("S6t-NIR"),
                  this.getApplicationId(), ClientS6tSession.class, (Object[])null);

      if (logger.isInfoEnabled()) {
        logger.info(new StringBuilder("target realm = " ).append(this.getRemoteRealm()).append(", localAddress = ")
            .append(localaddr).append(", Session id =").append(session.getSessionId()).toString());
      }
      
      Request request = this.createRequest(session, JNIDDInformationRequest.code);
 
      AvpSet reqSet = request.getAvps();

      AvpSet userIdentity = reqSet.addGroupedAvp(Avp.USER_IDENTIFIER, this.getApplicationId().getVendorId(),
          true, false);
      String userName = profile.getUserName();
      String externalId = profile.getExternalId();
      String msisdn = profile.getMsisdn();

      boolean userFlag = false;

      if (userName != null && userName.length() != 0) {
        userIdentity.addAvp(Avp.USER_NAME, userName, this.getApplicationId().getVendorId(), true, false, false);
        userFlag = true;
      }

      if (externalId != null && externalId.length() != 0) {
        userIdentity.addAvp(Avp.EXTERNAL_IDENTIFIER, externalId, this.getApplicationId().getVendorId(), true,
            false, false);
        userFlag = true;
      }

      if (msisdn != null && msisdn.length() != 0) {
        userIdentity.addAvp(Avp.MSISDN, BCDStringConverter.toBCD(msisdn), this.getApplicationId().getVendorId(),
            true, false);
        userFlag = true;
      }

      if (userFlag == false) {
        if (logger.isErrorEnabled()) {
          logger.error(
              "No user name, MSISDN or external-id defined for this profile Can't Send NIDD-Information-Request");
        }
        return;
      }

      JNIDDInformationRequest nir = this.getS6tSessionFactory().createNIDDInformationRequest(request);
      session.sendNIDDInformationRequest(nir);

      if (logger.isInfoEnabled()) {
        logger.info("NIDD-Information-Request sent to HSS");
      }
    } catch (IllegalDiameterStateException e) {
      e.printStackTrace();
    } catch (InternalException e) {
      e.printStackTrace();
    } catch (RouteException e) {
      e.printStackTrace();
    } catch (OverloadException e) {
      e.printStackTrace();
    }
  }
  
  public void sendRIA(ClientS6tSession session, JReportingInformationRequest request, int resultCode) {
    try {
      JReportingInformationAnswer ria = new JReportingInformationAnswerImpl((Request)request.getMessage(), resultCode);
      
      Answer answer = (Answer)ria.getMessage();
      AvpSet set = answer.getAvps();

      session.sendReportingInformationAnswer(this.s6tSessionFactory.createReportingInformationAnswer(answer));
      
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
    case JConfigurationInformationRequest.code:
    case JNIDDInformationRequest.code:
      try {
        ClientS6tSession clientS6tSession = (ClientS6tSession) this.s6tSessionFactory
            .getNewSession(request.getSessionId(), ClientS6tSession.class, this.getApplicationId(), (Object[]) null);

        ((NetworkReqListener) clientS6tSession).processRequest(request);
      } catch (Exception e) {
        logger.error(e.toString());
        e.printStackTrace();
      }
      return null;
    case JReportingInformationAnswer.code:
      try {
        ClientS6tSession clientS6tSession = (ClientS6tSession) this.s6tSessionFactory
            .getNewSession(request.getSessionId(), ClientS6tSession.class, this.getApplicationId(), (Object[]) null);

        ((NetworkReqListener) clientS6tSession).processRequest(request);
      } catch (Exception e) {
        logger.error(e.toString());
        e.printStackTrace();
      }
      return null;
    default:
      logger.error(new StringBuilder("processRequest - S6t - Not Supported message: ").append(request.getCommandCode())
          .append(" from interface : ").append(request.getApplicationId()).append(" from Class ")
          .append(request.getClass().getName()).toString());
      return null;
    }
  }

  @Override
  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleOtherEvent(session, request, answer);
  }

  @Override
  public void doConfigurationInformationAnswerEvent(ClientS6tSession session, JConfigurationInformationRequest request,
      JConfigurationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleConfigurationInformationAnswerEvent(session, request, answer);
  }

  @Override
  public void doReportingInformationRequestEvent(ClientS6tSession session, JReportingInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleReportingInformationRequestEvent(session, request);
  }

  @Override
  public void doNIDDInformationAnswerEvent(ClientS6tSession session, JNIDDInformationRequest request,
      JNIDDInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    this.scef.handleNIDDInformationAnswerEvent(session, request, answer);
  }

}
