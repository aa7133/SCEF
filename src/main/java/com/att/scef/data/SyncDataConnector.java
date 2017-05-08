package com.att.scef.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisStringCommands;

public class SyncDataConnector extends DataConnectorImpl  {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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
