package com.att.scef.data;

public interface DataConnector {
	public static final String DEFAULT_REDIS_ADDRESS = "redis://localhost/";

	public void closeDataBase();
	public Object getHandler();
	
}
