package com.att.scef.data;

import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;

public class SyncDataConnector extends DataConnectorImpl  {
	StatefulRedisConnection<String, String> connection;
	RedisStringCommands<String, String> handler;
	
	public SyncDataConnector(String host, int port) {
		this(RedisURI.Builder.redis(host, port).build());
	}
	
	public SyncDataConnector() {
		this(RedisURI.create(DataConnectorImpl.getDefaultAddress()));
	}
	
	private SyncDataConnector(RedisURI uri) {
		super(uri);
        this.connection = getRedisClient().connect();
        this.handler = connection.sync();
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
