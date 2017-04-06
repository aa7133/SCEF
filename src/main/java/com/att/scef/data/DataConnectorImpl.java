package com.att.scef.data;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

public abstract class DataConnectorImpl implements DataConnector {
	private static RedisClient client = null;
	private static final String DEFAULT_REDIS_ADDRESS = "redis://localhost/";

	protected DataConnectorImpl(RedisURI uri) {
		if (client == null) {
			client = RedisClient.create(uri);
		}
	}
	
	protected RedisClient getRedisClient() {
		return client;
	}
	
	protected void stop() {
		client.shutdown();
		client = null;
	}
	
	public static String getDefaultAddress() {
		return DEFAULT_REDIS_ADDRESS;
	}
	
}
