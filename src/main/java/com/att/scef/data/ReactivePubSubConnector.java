package com.att.scef.data;

import com.att.scef.scef.SCEF;

import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.pubsub.api.rx.RedisPubSubReactiveCommands;

import rx.Observable;
import com.lambdaworks.redis.api.rx.Success;


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
		Observable<Success> x = this.handler.subscribe(channel);
		
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
