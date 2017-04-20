/**
 * This class is to enhance the RedisPubSubListener in a way that will allow to use the messages in the correct context
 * since we are in different thread when we get the RedisPubSubListener call backs
 * 
 */
package com.att.scef.utils;

import com.att.scef.scef.SCEF;
import com.lambdaworks.redis.pubsub.RedisPubSubListener;

public abstract class ScefPubSubListener<K, V> implements RedisPubSubListener<K, V> {
	protected SCEF scefContext = null;
	
	public void setScefContext(SCEF scef) {
		this.scefContext = scef;
	}

}
