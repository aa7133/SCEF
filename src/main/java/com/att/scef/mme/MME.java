package com.att.scef.mme;


import java.util.concurrent.TimeUnit;

import org.jdiameter.api.AvpSet;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Mode;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.ResultCode;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.s6a.ClientS6aSession;
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
import org.jdiameter.api.t6a.ClientT6aSession;
import org.jdiameter.api.t6a.events.JConfigurationInformationAnswer;
import org.jdiameter.api.t6a.events.JConfigurationInformationRequest;
import org.jdiameter.api.t6a.events.JConnectionManagementAnswer;
import org.jdiameter.api.t6a.events.JConnectionManagementRequest;
import org.jdiameter.api.t6a.events.JMO_DataAnswer;
import org.jdiameter.api.t6a.events.JMO_DataRequest;
import org.jdiameter.api.t6a.events.JMT_DataRequest;
import org.jdiameter.api.t6a.events.JReportingInformationAnswer;
import org.jdiameter.api.t6a.events.JReportingInformationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.data.AsyncDataConnector;
import com.att.scef.data.ConnectorImpl;
import com.att.scef.data.SyncDataConnector;
import com.att.scef.hss.HSS;
import com.lambdaworks.redis.api.async.RedisStringAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;

public class MME {
  protected final Logger logger = LoggerFactory.getLogger(HSS.class);

  private ConnectorImpl syncDataConnector;
  private ConnectorImpl asyncDataConnector;
  private RedisStringAsyncCommands<String, String> asyncHandler;
  private RedisStringCommands<String, String> syncHandler;

  private S6aClient s6aClient;
  private T6aClient t6aClient;
  
  private final static String DEFAULT_S6A_CLIENT_NAME = "S6A-CLIENT";
  private final static String DEFAULT_T6A_CLIENT_NAME = "T6a-CLIENT";


  private final static String DEFAULT_S6A_CONFIG_FILE = "/home/odldev/scef/src/main/resources/mme/config-mme-s6a.xml";
  private final static String DEFAULT_T6A_CONFIG_FILE = "/home/odldev/scef/src/main/resources/mme/config-mme-t6a.xml";
  private final static String DEFAULT_DICTIONARY_FILE = "/home/odldev/scef/src/main/resources/dictionary.xml";
  
  
  public static void main(String[] args) {
    String s6aConfigFile = DEFAULT_S6A_CONFIG_FILE;
    String t6aConfigFile = DEFAULT_T6A_CONFIG_FILE;
    String dictionaryFile = DEFAULT_DICTIONARY_FILE;
    String host = "127.0.0.1";
    int port = 6379;
    String channel = "";
    
    
    for (int i = 0; i < args.length; i += 2) {
        if (args[i].equals("--s6a-conf")) {
            s6aConfigFile = args[i+1];
        }
        else if (args[i].equals("--t6a-conf")) {
          t6aConfigFile = args[i+1];
        }
        else if (args[i].equals("--dir")) {
            dictionaryFile = args[i+1];
        }
        else if (args[i].equals("--redis-host")) {
            host = args[i+1];
        }
        else if (args[i].equals("--redis-port")) {
            port = Integer.parseInt(args[i+1]);
        }
        else if (args[i].equals("--redis-channel")) {
            channel = args[i+1];
        }
    }
    
    new MME(s6aConfigFile, t6aConfigFile, dictionaryFile, host, port, channel);
    
  }
  
  @SuppressWarnings("unchecked")
  public MME(String s6aConfigFile, String t6aConfigFile, String dictionaryFile, String host, int port, String channel) {
      super();
      asyncDataConnector = new ConnectorImpl();
      asyncHandler = (RedisStringAsyncCommands<String, String>)asyncDataConnector.createDatabase(AsyncDataConnector.class, host, port);

      syncDataConnector = new ConnectorImpl();
      syncHandler = (RedisStringCommands<String, String>)syncDataConnector.createDatabase(SyncDataConnector.class, host, port);


      this.s6aClient = new S6aClient(this, s6aConfigFile);
      this.t6aClient = new T6aClient(this, t6aConfigFile);
      

      try {
        this.s6aClient.init(DEFAULT_S6A_CLIENT_NAME);
        this.s6aClient.start(Mode.ANY_PEER, 10, TimeUnit.SECONDS);

      } catch (Exception e) {
          e.printStackTrace();
      }
      try {
        this.t6aClient.init(DEFAULT_T6A_CLIENT_NAME);
        this.t6aClient.start(Mode.ANY_PEER, 10, TimeUnit.SECONDS);

      } catch (Exception e) {
          e.printStackTrace();
      }
      if (logger.isInfoEnabled()) {
        logger.info("=================================== S6a ==============================");
        this.s6aClient.checkConfig();
        logger.info("=================================== T6a ==============================");
        this.t6aClient.checkConfig();
        logger.info("=================================== MME Started ==============================");
      }

  }
  
  public void handleOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a Other\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }
  
  public void handleCancelLocationRequestEvent(ClientS6aSession session, JCancelLocationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a CLR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void handleInsertSubscriberDataRequestEvent(ClientS6aSession session, JInsertSubscriberDataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    if (logger.isInfoEnabled()) {
      logger.info("Got Insert-Subscriber-Information-Request (IDR)");
    }
    AvpSet reqSet = request.getMessage().getAvps();
    
    s6aClient.sendIDA(session, request, ResultCode.SUCCESS);
  }

  public void handleDeleteSubscriberDataRequestEvent(ClientS6aSession session, JDeleteSubscriberDataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a DSR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void handleResetRequestEvent(ClientS6aSession session, JResetRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a RSR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void doAuthenticationInformationAnswerEvent(ClientS6aSession session, JAuthenticationInformationRequest request, JAuthenticationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a AIA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void handlePurgeUEAnswerEvent(ClientS6aSession session, JPurgeUERequest request, JPurgeUEAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a PUA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void handleUpdateLocationAnswerEvent(ClientS6aSession session, JUpdateLocationRequest request, JUpdateLocationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a ULA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void handleNotifyAnswerEvent(ClientS6aSession session, JNotifyRequest request, JNotifyAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"S6a NOA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }
  
  public void handleT6aOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a Other\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void handleConfigurationInformationAnswerEvent(ClientT6aSession session, JConfigurationInformationRequest request,
      JConfigurationInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CIA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void handleConfigurationInformationRequestEvent(ClientT6aSession session, JConfigurationInformationRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CIR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void handleReportingInformationAnswerEvent(ClientT6aSession session, JReportingInformationRequest request,
      JReportingInformationAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a RIA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void handleMO_DataAnswerEvent(ClientT6aSession session, JMO_DataRequest request, JMO_DataAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a ODA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void handleMT_DataRequestEvent(ClientT6aSession session, JMT_DataRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a TDR\" event, request[" + request + "], on session[" + session + "]");
  }

  public void handleConnectionManagementAnswerEvent(ClientT6aSession session, JConnectionManagementRequest request,
      JConnectionManagementAnswer answer)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CMA\" event, request[" + request + "], answer[" + answer + "], on session["
        + session + "]");
  }

  public void handleConnectionManagementRequestEvent(ClientT6aSession session, JConnectionManagementRequest request)
      throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    logger.error("Received \"T6a CMR\" event, request[" + request + "], on session[" + session + "]");
  }

}
