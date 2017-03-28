package com.att.scef.data;


import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisStringAsyncCommands;

public class AsyncDataConnector extends DataConnectorImpl {
	private StatefulRedisConnection<String, String> connection;
	private RedisStringAsyncCommands<String, String> handler;
	
	public AsyncDataConnector(String host, int port) {
		this(RedisURI.Builder.redis(host, port).build());
	}
	
	public AsyncDataConnector() {
		this(RedisURI.create(DataConnectorImpl.getDefaultAddress()));
	}
	
	private AsyncDataConnector(RedisURI uri) {
		super(uri);
        this.connection = getRedisClient().connect();
        this.handler = connection.async();
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
