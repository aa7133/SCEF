package com.att.scef.mme;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
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
import com.att.scef.gson.GMmeUserProfile;
import com.att.scef.gson.GMonitoringEventConfig;
import com.att.scef.utils.LocationInformationConfiguration;
import com.att.scef.utils.UE_ReachabilityConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.async.RedisStringAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.pubsub.api.async.RedisPubSubAsyncCommands;

public class MME {
  protected final Logger logger = LoggerFactory.getLogger(MME.class);

  private ConnectorImpl syncDataConnector;
  private ConnectorImpl asyncDataConnector;
 
  private RedisClient redisClient;
  private RedisStringAsyncCommands<String, String> asyncHandler;
  private RedisStringCommands<String, String> syncHandler;

  private StatefulRedisPubSubConnection<String, String> connection;
  private RedisPubSubAsyncCommands<String, String> handler;

  private S6aClient s6aClient;
  private T6aClient t6aClient;
  
  private final static String DEFAULT_S6A_CLIENT_NAME = "S6A-CLIENT";
  private final static String DEFAULT_T6A_CLIENT_NAME = "T6a-CLIENT";


  private final static String DEFAULT_S6A_CONFIG_FILE = "/home/odldev/scef/src/main/resources/mme/config-mme-s6a.xml";
  private final static String DEFAULT_T6A_CONFIG_FILE = "/home/odldev/scef/src/main/resources/mme/config-mme-t6a.xml";
  private final static String DEFAULT_DICTIONARY_FILE = "/home/odldev/scef/src/main/resources/dictionary.xml";

  private final static String DEFAULT_PROFILE_PREFIX = "MME-USER-";
  private final static int ACTIVE = 1;
  private final static int NOT_ACTIVE = 0;
  
  public static void main(String[] args) {
    String s6aConfigFile = DEFAULT_S6A_CONFIG_FILE;
    String t6aConfigFile = DEFAULT_T6A_CONFIG_FILE;
    String dictionaryFile = DEFAULT_DICTIONARY_FILE;
    String host = "127.0.0.1";
    int port = 6379;
    String channel = "MME-Clients";
    
    
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
      
      //Redis pub sub
      this.redisClient = RedisClient.create(RedisURI.Builder.redis(host, port).build());
      this.connection = this.redisClient.connectPubSub();

      MmePubSubListener<String, String> listner = this.getListner(); 

      // subscribe the listener for the pub sub
      this.connection.addListener(listner);

      this.handler = this.connection.async();
      
      // connect to the publisher
      RedisFuture<Void> future = this.handler.subscribe(channel);
      
      try {
          future.get();
      } catch (InterruptedException e) {
          e.printStackTrace();
      } catch (ExecutionException e) {
          e.printStackTrace();
      }
      
      
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
  
  private void handleMmeMessages(String channel, String message) {
    if (logger.isInfoEnabled()) {
      logger.info(new StringBuffer("message = \"").append(message).append("\" for channel = ")
          .append(channel).toString());
    }
    String[] paramters = message.trim().split("\\|");
    for (String s : paramters) {
      logger.info(s);
    }
    String msisdn = paramters[1];
    if (msisdn == null || msisdn.length() == 0) {
      logger.error("empty msisdn");
      return;
    }
    String userData = this.syncHandler.get(DEFAULT_PROFILE_PREFIX + msisdn);
   
    switch (paramters[0].toUpperCase()) {
    case "D":
      String msg = paramters[2];
      this.t6aClient.sendODR(msisdn, msg);
      break;
    case "A":
      int activeState = Integer.parseInt(paramters[2]);
      changeActiveState(msisdn, userData, activeState);
      break;
    case "M":
      int monitorEvent = Integer.parseInt(paramters[2]);
      if (monitorEvent > 7 || monitorEvent < 0) {
        logger.error("monitoring not in range use number between 0 - 7");
      }
      sendMonitoringEvent(msisdn, userData, monitorEvent);
      break;
    default:
      StringBuffer sb = new StringBuffer();
      logger.error("Wrong command request" + paramters[0] + " for message : "+ message);
      sb.append("send data from device \"D|msisdn|message\"\n")
           .append("Change device State \"A|msisdn|[0,1]\"  0 - deactivate device 1 - activate device\n")
           .append("Send Monitoring Event \"M|msisdn|[0-7]\"  \n\t\tLOSS_OF_CONNECTIVITY (0)\n\t\tUE_REACHABILITY (1)\n\t\t")
           .append("LOCATION_REPORTING (2)\n\t\tCHANGE_OF_IMSI_IMEI(SV)_ASSOCIATION (3)\n\t\tROAMING_STATUS (4)\n\t\t")
           .append("COMMUNICATION_FAILURE (5)\n\t\tAVAILABILITY_AFTER_DDN_FAILURE (6)\n\t\tNUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA (7)");
      logger.info(sb.toString());
      
    }
  }

  private void sendMonitoringEvent(String msisdn, String userData, int monitoringEvent) {
    if (userData == null) {
      logger.error("sendMonitoringEvent User not exists : " + msisdn);
      return;
    }
    GMmeUserProfile userProfile = new Gson().fromJson(new JsonParser().parse(userData), GMmeUserProfile.class);
    if (userProfile.getActive() == NOT_ACTIVE) {
      logger.info("user not in active state");
      return;
    }
    GMonitoringEventConfig gmon = null;
    for (GMonitoringEventConfig g : userProfile.getMonitoringEvents()) {
      if (g.getMonitoringType() == monitoringEvent) {
        gmon = g;
      }
    }
    if (gmon == null) {
      logger.info(new StringBuffer("The requested monitoring flag : ").append(monitoringEvent)
          .append("Was not set for the user : ").append(msisdn).toString());
      return;
    }
    this.t6aClient.sendRIR(msisdn, gmon, monitoringEvent);       
  }

  private void changeActiveState(String msisdn, String userData, int activeState) {
    if (userData == null) {
      logger.error("User not exists" + msisdn);
      return;
    }
    RedisFuture<String> future = null;
    Runnable listener = new Runnable() {
      @Override
      public void run() {
        logger.info("MME active state changed");
      }
    };

    GMmeUserProfile userProfile = new Gson().fromJson(new JsonParser().parse(userData), GMmeUserProfile.class);
    userProfile.setActive(activeState);
    future = this.asyncHandler.set(msisdn, new Gson().toJson(userProfile));

    future.thenRun(listener);    
  }
  

  private void handleMmeMessages(String pattern, String channel, String message) {
    if (logger.isInfoEnabled()) {
      logger.info(new StringBuffer("message = \"").append(message).append("\" for channel = ")
          .append(channel).append(" pattern = ").append(pattern).toString());
    }
  }

  private MmePubSubListener<String, String> getListner() {
    MmePubSubListener<String, String> listener = new MmePubSubListener<String, String>() {
      @Override
      public void message(String channel, String message) {
        if (logger.isInfoEnabled()) {
          logger.info(new StringBuffer("Got Message from Redis PubSub on channel : ").append(channel)
                  .append(" Message = ").append(message).toString());
        }
        this.mmeContext.handleMmeMessages(channel, message);
      }

      @Override
      public void message(String pattern, String channel, String message) {
        //TODO 
        if (logger.isInfoEnabled()) {
            logger.info(new StringBuffer("Got Message from Redis PubSub with pattern : ").append(pattern).append(" on channel : ").append(channel)
                    .append(" Message = ").append(message).toString());
        }
        this.mmeContext.handleMmeMessages(pattern, channel, message);
      }

      @Override
      public void subscribed(String channel, long count) {
        if (logger.isInfoEnabled()) {
          logger.info(new StringBuffer("subscribed from Redis PubSub channel : ").append(channel).append(" count :").append(count)
                  .toString());
        }
      }

      @Override
      public void psubscribed(String pattern, long count) {
        if (logger.isInfoEnabled()) {
          logger.info(new StringBuffer("psubscribed from Redis PubSub pattern : ").append(pattern).append(" count :").append(count)
                  .toString());
        }
      }

      @Override
      public void unsubscribed(String channel, long count) {
        if (logger.isInfoEnabled()) {
          logger.info(new StringBuffer("unsubscribed from Redis PubSub channel : ").append(channel).append(" count :").append(count)
                  .toString());
        }
      }

      @Override
      public void punsubscribed(String pattern, long count) {
        if (logger.isInfoEnabled()) {
          logger.info(new StringBuffer("punsubscribed from Redis PubSub pattern : ").append(pattern).append(" count :").append(count)
                  .toString());
        }
      }
    };
    listener.setMmeContext(this);
    return listener;
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

    RedisFuture<String> future = null;
    Runnable listener = new Runnable() {
      @Override
      public void run() {
        logger.info("MME data inserted");
      }
    };

    try {
      Avp msisdnAvp = reqSet.getAvp(Avp.USER_NAME);
      if (msisdnAvp == null) {
          logger.error("Missing user name in Insert Subscriber Data request (IDR)");
        this.s6aClient.sendIDA(session, request, ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
        return;
      }
      String msisdn = msisdnAvp.getUTF8String();
      String userData = this.syncHandler.get(DEFAULT_PROFILE_PREFIX + msisdn);   

      AvpSet subsSet = reqSet.getAvp(Avp.SUBSCRIPTION_DATA).getGrouped();
      
      AvpSet monitoringEventConfig = subsSet.getAvps(Avp.MONITORING_EVENT_CONFIGURATION);
      StringBuffer sb = new StringBuffer("MONITORING_EVENT_CONFIGURATION list :");
      
      List<GMonitoringEventConfig> monitoringConfigList = new ArrayList<GMonitoringEventConfig>();
      boolean skipFlag = false;
      
      for (Avp mon : monitoringEventConfig) {
        GMonitoringEventConfig gmon = new GMonitoringEventConfig();
        sb.append("\n");
        for (Avp a: mon.getGrouped()) {
          if(a.getCode() == Avp.SCEF_ID) {
            gmon.setScefId(a.getDiameterIdentity());
            sb.append("SCEF_ID = ").append(gmon.getScefId()).append("\n");
          }
          else if (a.getCode() == Avp.SCEF_REFERENCE_ID) {
            gmon.setScefRefId(a.getInteger32());
            sb.append("SCEF_REFERENCE_ID = ").append(gmon.getScefRefId()).append("\n");
          }
          else if (a.getCode() == Avp.MONITORING_TYPE) {
            gmon.setMonitoringType(a.getInteger32());
            sb.append("MONITORING_TYPE = ").append(gmon.getMonitoringType()).append("\n");
          }
          else if (a.getCode() == Avp.SCEF_REFERENCE_ID_FOR_DELETION) {
            skipFlag = true;
            sb.append("SCEF_REFERENCE_ID_FOR_DELETION = ").append(a.getUnsigned32()).append("\n");
          }
          else if (a.getCode() == Avp.MAXIMUM_NUMBER_OF_REPORTS) {
            gmon.setMaximumNumberOfReports(a.getInteger32());
            sb.append("MAXIMUM_NUMBER_OF_REPORTS = ").append(gmon.getMaximumNumberOfReports()).append("\n");
          }
          else if (a.getCode() == Avp.MONITORING_DURATION) {
            gmon.setMonitoringDuration(new String(a.getOctetString()));
            sb.append("MONITORING_DURATION = ").append(gmon.getMonitoringDuration()).append("\n");
          }
          else if (a.getCode() == Avp.CHARGED_PARTY) {
            gmon.setChargedParty(a.getUTF8String());
            sb.append("CHARGED_PARTY = ").append(gmon.getChargedParty()).append("\n");
          }
          else if (a.getCode() == Avp.MAXIMUM_DETECTION_TIME) {
            gmon.setMaximumDetectionTime(a.getInteger32());
            sb.append("MAXIMUM_DETECTION_TIME = ").append(gmon.getMaximumDetectionTime()).append("\n");
          }
          else if (a.getCode() == Avp.UE_REACHABILITY_CONFIGURATION) {
            gmon.setUEReachabilityConfiguration(UE_ReachabilityConfiguration.extractFromAvpSingle(a));
            sb.append("UE_REACHABILITY_CONFIGURATION = ").append(gmon.getUEReachabilityConfiguration()).append("\n");
          }
          else if (a.getCode() == Avp.LOCATION_INFORMATION_CONFIGURATION) {
            gmon.setLocationInformationConfiguration(LocationInformationConfiguration.extractFromAvpSingle(a));
            sb.append("LOCATION_INFORMATION_CONFIGURATION = ").append(gmon.getLocationInformationConfiguration()).append("\n");
          }
          else if (a.getCode() == Avp.ASSOCIATION_TYPE) {
            gmon.setAssociationType(a.getInteger32());
            sb.append("ASSOCIATION_TYPE = ").append(gmon.getAssociationType()).append("\n");
          }
        }
        if (!skipFlag) {
          monitoringConfigList.add(gmon);
        }
      }
      
      GMonitoringEventConfig[] monitoringConfigArray = new GMonitoringEventConfig[monitoringConfigList.size()];
      int i = 0;
      for (GMonitoringEventConfig g : monitoringConfigList) {
        monitoringConfigArray[i++] = g;
      }
      
      GMmeUserProfile userProfile = null;
      if (userData == null) {
        userProfile = new GMmeUserProfile();
        userProfile.setActive(ACTIVE);
      }
      else {
        userProfile = new Gson().fromJson(new JsonParser().parse(userData), GMmeUserProfile.class);
      }
      userProfile.setMonitoringEvents(monitoringConfigArray);
      String buf = new Gson().toJson(userProfile);
      future = this.asyncHandler.set(DEFAULT_PROFILE_PREFIX + msisdn, buf /*new Gson().toJson(userProfile)*/);
      logger.info("saving user :" + DEFAULT_PROFILE_PREFIX + msisdn + "--" + buf);
      
      if (logger.isInfoEnabled()) {
        logger.info(sb.toString());
      }
    } catch (AvpDataException e) {
        e.printStackTrace();
    }
    
    this.s6aClient.sendIDA(session, request, ResultCode.SUCCESS);
    
    //TODO add user data to data base based on MSISDN
    future.thenRun(listener);
    
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
    if (logger.isInfoEnabled()) {
      logger.info("Got MO-Data-Answer (ODA)");
    }
    AvpSet reqSet = request.getMessage().getAvps();
    AvpSet ansSet = answer.getMessage().getAvps();
    
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
