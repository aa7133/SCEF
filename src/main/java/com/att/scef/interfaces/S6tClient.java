package com.att.scef.interfaces;

import static org.jdiameter.client.impl.helpers.Parameters.OwnDiameterURI;
import static org.jdiameter.client.impl.helpers.Parameters.RealmEntry;
import static org.jdiameter.client.impl.helpers.Parameters.RealmTable;
import static org.jdiameter.server.impl.helpers.Parameters.RealmName;

import java.io.FileInputStream;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
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
import org.jdiameter.api.s6t.events.JReportingInformationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.gson.GSCEFUserProfile;
import com.att.scef.scef.SCEF;
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
		this.init(new FileInputStream(configFile), clientID);
	}


  public void sendCirRequest(GSCEFUserProfile newMsg, GMonitoringEventConfig[] mc, GAESE_CommunicationPattern[] cp) {
    try {
      Configuration conf = this.getStack().getMetaData().getConfiguration();
      ClientS6tSession session = (ClientS6tSession) this.sessionFactory
          .getNewAppSession(this.getStack().getSessionFactory().getSessionId("S6t-CIR"),
              this.getApplicationId(), ClientS6tSession.class, (Object[]) null);
      
      String localaddr = conf.getStringValue(OwnDiameterURI.ordinal(), "aaa://127.0.0.1:15868");
      if (logger.isInfoEnabled()) {
        logger.info(new StringBuilder("target realm = " ).append(this.getRemoteRealm()).append(", localAddress = ")
            .append(localaddr).append(", Session id =").append(session.getSessionId()).toString());
      }
      Request request = this.createRequest(session, JConfigurationInformationRequest.code, this.getRemoteRealm(), localaddr);

      AvpSet reqSet = request.getAvps();
      // Add user identity 
      AvpSet userIdentifier = reqSet.addGroupedAvp(Avp.USER_IDENTIFIER, getApplicationId().getVendorId(), true, false);
      
      String extId = newMsg.getExternalId();
      if (extId != null && extId.length() != 0) {
        userIdentifier.addAvp(Avp.EXTERNAL_IDENTIFIER, extId, getApplicationId().getVendorId(), true, false, false);
      }
      String msisdn = newMsg.getMsisdn();
      if (msisdn != null && msisdn.length() != 0) {
        userIdentifier.addAvp(Avp.MSISDN, BCDStringConverter.toBCD(msisdn), getApplicationId().getVendorId(), true, false);
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
            for (int i : m.getScefRefIdForDelition()) {
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
      
      Request request = this.createRequest(session, JNIDDInformationRequest.code, this.getRemoteRealm(), localaddr);
 
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
	
	@Override
	public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		logger.error("Received \"S6t Other\" event, request[" + request + "], answer[" + answer + "], on session["
				+ session + "]");
	}


	@Override
	public void doConfigurationInformationAnswerEvent(ClientS6tSession session,
			JConfigurationInformationRequest request, JConfigurationInformationAnswer answer)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

		StringBuffer str = new StringBuffer("");
		boolean session_id = false;
		boolean auth_sessin_state = false;
		boolean orig_host = false;
		boolean orig_relm = false;
		try {
			for (Avp a : answer.getMessage().getAvps()) {
				switch (a.getCode()) {
				case Avp.SESSION_ID:
					session_id = true;
					str.append("SESSION_ID : ").append(a.getUTF8String()).append("\n");
					break;
				case Avp.DRMP:
					str.append("\tDRMP : ").append(a.getUTF8String()).append("\n");
					break;
				case Avp.RESULT_CODE:
					str.append("\tRESULT_CODE : ").append(a.getInteger32()).append("\n");
					break;
				case Avp.EXPERIMENTAL_RESULT:
					str.append("\tEXPERIMENTAL_RESULT : ").append(a.getInteger32()).append("\n");
					break;
				case Avp.AUTH_SESSION_STATE:
					auth_sessin_state = true;
					break;

				case Avp.ORIGIN_HOST:
					orig_host = true;
					break;
				case Avp.ORIGIN_REALM:
					orig_relm = true;
                    break;
				case Avp.OC_SUPPORTED_FEATURES:
					break;
				case Avp.OC_OLR:
					break;
				case Avp.SUPPORTED_FEATURES: // grouped
					break;
				case Avp.USER_IDENTIFIER:
					break;
				case Avp.MONITORING_EVENT_REPORT: // grouped
					break;
				case Avp.MONITORING_EVENT_CONFIG_STATUS: // Grouped
					break;
				case Avp.AESE_COMMUNICATION_PATTERN_CONFIG_STATUS: // Grouped
					break;
				case Avp.SUPPORTED_SERVICES: // Grouped
					break;
				case Avp.S6T_HSS_CAUSE:
					break;
				case Avp.FAILED_AVP: // Grouped
					break;
				case Avp.PROXY_INFO: // Grouped
					break;
				case Avp.ROUTE_RECORD: // Grouped
					break;
				default: // got Extra AVP'S
					break;
				}

			}
		} catch (AvpDataException e) {
			e.printStackTrace();
		}
        if (!session_id || !auth_sessin_state || !orig_host || !orig_relm) {
        	logger.error("Configuration-Information-Answer (CIA) - mandatory paramters are missing"); 
        }
		logger.info(str.toString());
	}
	
	@Override
	public void doReportingInformationRequestEvent(ClientS6tSession session, JReportingInformationRequest request)
			throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
		//TODO 
		logger.error("doReportingInformationRequestEvent not yet implemented \"S6t RIR\" event, request[" 
		                + request + "], on session[" + session + "]");
	}

  @Override
  public void doNIDDInformationAnswerEvent(ClientS6tSession session, JNIDDInformationRequest request,
      JNIDDInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    boolean session_id = false;
    boolean auth_sessin_state = false;
    boolean orig_host = false;
    boolean orig_relm = false;

    StringBuffer str = new StringBuffer("");
    try {
      for (Avp a : answer.getMessage().getAvps()) {
        switch (a.getCode()) {
        case Avp.SESSION_ID:
          session_id = true;
          str.append("SESSION_ID : ").append(a.getUTF8String()).append("\n");
          break;
        case Avp.DRMP:
          str.append("\tDRMP : ").append(a.getUTF8String()).append("\n");
          break;
        case Avp.RESULT_CODE:
          str.append("\tRESULT_CODE : ").append(a.getInteger32()).append("\n");
          break;
        case Avp.EXPERIMENTAL_RESULT:
          str.append("\tEXPERIMENTAL_RESULT : ").append(a.getInteger32()).append("\n");
          break;
        case Avp.AUTH_SESSION_STATE:
          auth_sessin_state = true;
          break;

        case Avp.ORIGIN_HOST:
          orig_host = true;
          break;
        case Avp.ORIGIN_REALM:
          orig_relm = true;
          break;
        case Avp.OC_SUPPORTED_FEATURES:
          break;
        case Avp.OC_OLR:
          break;
        case Avp.SUPPORTED_FEATURES: // grouped
          break;
        case Avp.USER_IDENTIFIER:
          break;
        case Avp.NIDD_AUTHORIZATION_RESPONSE: // grouped
          break;
        case Avp.FAILED_AVP: // Grouped
          break;
        case Avp.PROXY_INFO: // Grouped
          break;
        case Avp.ROUTE_RECORD: // Grouped
          break;
        default: // got Extra AVP'S
          break;
        }

      }
    } catch (AvpDataException e) {
      e.printStackTrace();
    }
    if (!session_id || !auth_sessin_state || !orig_host || !orig_relm) {
      logger.error("NIDD-Information-Answer (NIA) - mandatory paramters are missing"); 
  }
    logger.info(str.toString());
  }


}
