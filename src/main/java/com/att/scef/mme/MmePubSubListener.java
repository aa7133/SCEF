package com.att.scef.mme;

import com.lambdaworks.redis.pubsub.RedisPubSubListener;

public abstract class MmePubSubListener<K, V> implements RedisPubSubListener<K, V> {
  protected MME mmeContext = null;

  public void setMmeContext(MME mme) {
    this.mmeContext = mme;
}

}
