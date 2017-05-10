package com.att.scef.mme;

import java.util.concurrent.TimeUnit;

import org.jdiameter.api.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.data.AsyncDataConnector;
import com.att.scef.data.ConnectorImpl;
import com.att.scef.data.SyncDataConnector;
import com.att.scef.hss.HSS;
import com.att.scef.interfaces.S6aClient;
import com.att.scef.interfaces.S6aServer;
import com.att.scef.interfaces.S6tServer;
import com.att.scef.interfaces.T6aClient;
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

        this.t6aClient.init(DEFAULT_T6A_CLIENT_NAME);
        this.t6aClient.start(Mode.ANY_PEER, 10, TimeUnit.SECONDS);
      } catch (Exception e) {
          e.printStackTrace();
      }

      logger.info("=================================== MME started ==============================");
  }


}
