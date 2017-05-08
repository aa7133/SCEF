package com.att.scef.data;


import com.att.scef.scef.SCEF;

import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.pubsub.api.rx.RedisPubSubReactiveCommands;



public class ReactivePubSubConnector extends DataConnectorImpl {
	private StatefulRedisPubSubConnection<String, String> connection;
	private RedisPubSubReactiveCommands<String, String> handler;

	public ReactivePubSubConnector(SCEF scef, String channel, String host, int port) {
		this(scef, channel, RedisURI.Builder.redis(host, port).build());
	}
	
	public ReactivePubSubConnector(SCEF scef, String channel) {
		this(scef, channel, RedisURI.create(DataConnectorImpl.getDefaultAddress()));
	}

	
	private ReactivePubSubConnector(SCEF scef, String channel, RedisURI uri) {
		super(uri);

		this.connection = getRedisClient().connectPubSub();
		this.handler = connection.reactive();
		this.handler.subscribe(channel).doOnNext(msg -> System.out.println("message = " + msg))
		                                .doOnCompleted(() -> System.out.println("Completed"))
		                                .subscribe();
		this.handler.observeChannels().doOnNext(channelMsg -> {String msg = channelMsg.getMessage();
		                                                       System.out.println("Message = " + msg);
		                                                       //TODO in the future here we need to put the real work
		                                                       })
		                               .subscribe();
		
	}

	public void mySmallTest(String msg) {
		System.out.println("message = " + msg);
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
