package org.fastutil.mainland.util.cache;

import java.io.Serializable;

/**
 * 基於java緩存實現
 * 
 * @author Administrator
 *
 * @param <V>
 */
public class CacheEntity<V extends Object> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private V value;// value值
	private int expireTime = 1;// 有效時間，單位秒
	private long expireAt;// 失效時間

	public CacheEntity() {
		expireAt = System.currentTimeMillis() + expireTime * 1000;
	}

	public CacheEntity(V value) {
		this.value = value;
		expireAt = System.currentTimeMillis() + expireTime * 1000;
	}

	public CacheEntity(V value, int seconds) {
		this.value = value;
		if (seconds > 0) {
			this.expireTime = seconds;
		}
		expireAt = System.currentTimeMillis() + expireTime * 1000;
	}

	public CacheEntity(V value, long expireAt) {
		this.value = value;
		if (expireAt > System.currentTimeMillis()) {
			this.expireAt = expireAt;
		} else {
			this.expireAt = System.currentTimeMillis() + expireTime * 1000;
		}
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		if (expireTime > 0) {
			this.expireTime = expireTime;
			this.expireAt = System.currentTimeMillis() + this.expireTime * 1000;
		}
	}

	public long getExpireAt() {
		return expireAt;
	}

	public void setExpireAt(long expireAt) {
		if (expireAt > System.currentTimeMillis())
			this.expireAt = expireAt;
	}

	public long ttl() {
		return expireAt - System.currentTimeMillis();
	}

	public String toString() {
		// TODO Auto-generated method stub
		return value != null ? value.toString() : "";
	}
}
