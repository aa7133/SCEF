package com.att.scef.data;

public class ConnectorImpl {
	private DataConnector dataBase = null;
	
	public Object createDatabase(Class<?> clazz, String host, int port) {
		if (clazz.equals(AsyncDataConnector.class)) {
			dataBase = new AsyncDataConnector(host, port);
		}
		else if (clazz.equals(SyncDataConnector.class)) {
			dataBase = new SyncDataConnector(host, port);
		}
		else if (clazz.equals(ReactiveDataConnector.class)) {
			dataBase = new ReactiveDataConnector(host, port);
		}
		return dataBase.getHandler();
	}

	public Object createDatabase(Class<?> clazz) {
		if (clazz.equals(AsyncDataConnector.class)) {
			dataBase = new AsyncDataConnector();
		}
		else if (clazz.equals(SyncDataConnector.class)) {
			dataBase = new SyncDataConnector();
		}
		else if (clazz.equals(ReactiveDataConnector.class)) {
			dataBase = new ReactiveDataConnector();
		}
		return dataBase.getHandler();
	}


	public void closeDataBase() {
		dataBase.closeDataBase();
	}

}
