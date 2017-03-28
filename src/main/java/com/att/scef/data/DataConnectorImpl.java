package com.att.scef.data;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

public abstract class DataConnectorImpl implements DataConnector {
	protected RedisClient client;
	private static final String DEFAULT_REDIS_ADDRESS = "redis://localhost/";

	protected DataConnectorImpl(RedisURI uri) {
        this.client = RedisClient.create(uri);
	}
	
	protected RedisClient getRedisClient() {
		return this.client;
	}
	
	protected void stop() {
		client.shutdown();
	}
	
	public static String getDefaultAddress() {
		return DEFAULT_REDIS_ADDRESS;
	}
	
}
