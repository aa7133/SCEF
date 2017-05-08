package com.att.scef.data;

import com.att.scef.scef.SCEF;

public class PubSubConnectorImpl {
	private DataConnector dataBase = null;
	
	public Object createPubSub(Class<?> clazz, String host, int port, SCEF scef, String channel) {
		if (clazz.equals(AsyncPubSubConnector.class)) {
			dataBase = new AsyncPubSubConnector(scef, channel, host, port);
		}
		else if (clazz.equals(SyncPubSubConnector.class)) {
			dataBase = new SyncPubSubConnector(scef, channel, host, port);
		}
		else if (clazz.equals(ReactivePubSubConnector.class)) {
			dataBase = new ReactivePubSubConnector(scef, channel, host, port);
		}
		return dataBase.getHandler();
	}

	public Object createPubSub(Class<?> clazz, SCEF scef, String channel) {
		if (clazz.equals(AsyncPubSubConnector.class)) {
			dataBase = new AsyncPubSubConnector(scef, channel);
		}
		else if (clazz.equals(SyncPubSubConnector.class)) {
			dataBase = new SyncPubSubConnector(scef, channel);
		}
		else if (clazz.equals(ReactivePubSubConnector.class)) {
			dataBase = new ReactivePubSubConnector(scef, channel);
		}

		return dataBase.getHandler();
	}

	public void closePubSub() {
		dataBase.closeDataBase();
	}

}
