package com.att.scef.data;

import com.att.scef.scef.SCEF;
import com.att.scef.utils.ScefPubSubListener;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.pubsub.api.async.RedisPubSubAsyncCommands;


public class AsyncPubSubConnector extends DataConnectorImpl {
	private StatefulRedisPubSubConnection<String, String> connection;
	private RedisPubSubAsyncCommands<String, String> handler;

	public AsyncPubSubConnector(SCEF scef, String host, int port) {
		this(scef, RedisURI.Builder.redis(host, port).build());
	}
	
	public AsyncPubSubConnector(SCEF scef) {
		this(scef, RedisURI.create(DataConnectorImpl.getDefaultAddress()));
	}
	
	private AsyncPubSubConnector(SCEF scef, RedisURI uri) {
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
				this.scefContext.sendConfigurationInformationRequest();
			}
		};

		// add the SCEF object that create the class to be able to operate messages
		listner.setScefContext(scef);
		
		this.connection.addListener(listner);

		this.handler = this.connection.async();
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
