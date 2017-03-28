package com.att.scef.data;

import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;

public class ReactiveDataConnector extends DataConnectorImpl {
	StatefulRedisConnection<String, String> connection;
	RedisStringReactiveCommands<String, String> handler;
	
	public ReactiveDataConnector(String host, int port) {
		this(RedisURI.Builder.redis(host, port).build());
	}
	
	public ReactiveDataConnector() {
		this(RedisURI.create(DataConnectorImpl.getDefaultAddress()));
	}
	
	private ReactiveDataConnector(RedisURI uri) {
		super(uri);
        this.connection = getRedisClient().connect();
        this.handler = connection.reactive();
	}
	
	@Override
	public Object getHandler() {
		return handler;
	}
	
	@Override
	public void closeDataBase() {
		this.connection.close();
		stop();
	}
}
