package com.att.scef.loadTest;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.mme.MME;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.pubsub.api.async.RedisPubSubAsyncCommands;
import com.lambdaworks.redis.pubsub.api.sync.RedisPubSubCommands;

public class LoadTest {
  protected final Logger logger = LoggerFactory.getLogger(MME.class);
  private RedisClient redisClient;

  private StatefulRedisPubSubConnection<String, String> connection;
  private RedisPubSubAsyncCommands<String, String> handler;
  private RedisPubSubCommands<String, String> h;
  
  public static void main(String[] args) {
    String host = "127.0.0.1";
    //String host = "ILTLV937";
    int port = 6379;
    String channel = "MME-Clients";
    int retry = 1000;
    
    
    for (int i = 0; i < args.length; i += 2) {
        if (args[i].equals("--redis-host")) {
            host = args[i+1];
        }
        else if (args[i].equals("--redis-port")) {
            port = Integer.parseInt(args[i+1]);
        }
        else if (args[i].equals("--redis-channel")) {
          channel = args[i+1];
        }
        else if (args[i].equals("--retry")) {
          retry = Integer.parseInt(args[i+1]);
        }
        else if (args[i].equals("--redis-channel")) {
          channel = args[i+1];
        }
    }
    new LoadTest(host, port, channel, retry);
    
  }
  
  public LoadTest(String host, int port, String channel, int retry) {
    this.redisClient = RedisClient.create(RedisURI.Builder.redis(host, port).build());
    this.connection = this.redisClient.connectPubSub(); //RedisCommands();
    RedisPubSubListener<String, String> listener = new RedisPubSubListener<String, String>() {

      @Override
      public void message(String channel, String message) {
        logger.info("LoadTest message 1" );
        // TODO Auto-generated method stub
        
      }

      @Override
      public void message(String pattern, String channel, String message) {
        logger.info("LoadTest message 2" );
        // TODO Auto-generated method stub
        
      }

      @Override
      public void subscribed(String channel, long count) {
        // TODO Auto-generated method stub
        logger.info("LoadTest subscribed" );
        
      }

      @Override
      public void psubscribed(String pattern, long count) {
        // TODO Auto-generated method stub
        logger.info("LoadTest psubscribed" );
        
      }

      @Override
      public void unsubscribed(String channel, long count) {
        // TODO Auto-generated method stub
        logger.info("LoadTest unsubscribed" );
       
      }

      @Override
      public void punsubscribed(String pattern, long count) {
        // TODO Auto-generated method stub
        logger.info("LoadTest punsubscribed" );
        
      }
      
    };
    
    this.connection.addListener(listener);
    
    this.handler = this.connection.async();
    this.h = this.connection.sync();

    Random r = new Random();
    
    StringBuilder sb = new StringBuilder();
    String msg = sb.append("D|10|").append(r.nextInt(10000000)).toString();
    logger.info("Send messages to channel : " + channel);
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < retry; i++) {
        this.h.publish(channel, msg + i);
//      RedisFuture<Long> future = this.handler.publish(channel, msg + i);
    }
    start = System.currentTimeMillis() - start;
    logger.info("Test sent : " + retry + " in : " + start + " milliseconds" );
    
  }
}
