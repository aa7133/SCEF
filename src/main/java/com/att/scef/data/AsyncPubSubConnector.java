package com.att.scef.data;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.scef.scef.SCEF;
import com.att.scef.scef.ScefPubSubListener;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.pubsub.api.async.RedisPubSubAsyncCommands;


public class AsyncPubSubConnector extends DataConnectorImpl {
	protected final Logger logger = LoggerFactory.getLogger(AsyncPubSubConnector.class);

	private StatefulRedisPubSubConnection<String, String> connection;
	private RedisPubSubAsyncCommands<String, String> handler;

	public AsyncPubSubConnector(SCEF scef, String channel, String host, int port) {
		this(scef, channel, RedisURI.Builder.redis(host, port).build());
	}
	
	public AsyncPubSubConnector(SCEF scef, String channel) {
		this(scef, channel, RedisURI.create(DataConnectorImpl.getDefaultAddress()));
	}
	
	private AsyncPubSubConnector(SCEF scef, String channel, RedisURI uri) {
		super(uri);
		this.connection = getRedisClient().connectPubSub();
		
		ScefPubSubListener<String, String> listner = new ScefPubSubListener<String, String>() {
			@Override
			public void unsubscribed(String channel, long count) {
			}
			
			@Override
			public void subscribed(String channel, long count) {
			}
			
			@Override
			public void punsubscribed(String pattern, long count) {
			}
			
			@Override
			public void psubscribed(String pattern, long count) {
			}
			
			@Override
			public void message(String pattern, String channel, String message) {
			}
			
			@Override
			public void message(String channel, String message) {
				//TODO 
				if (logger.isInfoEnabled()) {
					logger.info(new StringBuffer("Got Message from Redis PubSub on channel :").append(channel)
							.append(" Message = ").append(message).toString());
				}
				this.scefContext.sendDiamterMessages(message);
			}
		};

		// add the SCEF object that create the class to be able to operate messages
		listner.setScefContext(scef);
		
		this.connection.addListener(listner);

		this.handler = this.connection.async();
		
		RedisFuture<Void> future = this.handler.subscribe(channel);
		try {
			future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	@Override
	public void closeDataBase() {
		this.connection.close();
		stop();
	}

	@Override
	public Object getHandler() {
		return this.handler;
	}

}
