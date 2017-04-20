package com.att.scef.data;

import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisStringAsyncCommands;

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
