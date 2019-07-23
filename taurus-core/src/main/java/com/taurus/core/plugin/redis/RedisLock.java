package com.taurus.core.plugin.redis;

import redis.clients.jedis.Jedis;

/**
 * RedisLock
 */
public class RedisLock {
	private static final String	LOCKED					= "0";
	private static final long	DEFAULT_TIME_OUT		= 6000;
	private static final int	EXPIRE					= 4000;
	private static final String	LOCK_SUCCESS			= "OK";
	private static final String	SET_IF_NOT_EXIST		= "NX";
	private static final String	SET_WITH_EXPIRE_TIME	= "PX";

	private Jedis				jedis;
	private String				key;
	private volatile boolean	locked					= false;

	/**
	 * This creates a RedisLock
	 * 
	 * @param key key
	 * @param jedis
	 */
	public RedisLock(String key, Jedis jedis) {
		this.key = key + "$lock";
		this.jedis = jedis;
	}

	/**
	 * lock(); try { doSomething(); } finally { unlock() }
	 */
	public boolean lock() {
		long time = System.currentTimeMillis();
		try {
			while ((System.currentTimeMillis() - time) < DEFAULT_TIME_OUT) {
				String result = jedis.set(key, LOCKED, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, EXPIRE);
				if (LOCK_SUCCESS.equals(result)) {
					this.locked = true;
					return this.locked;
				}
				Thread.sleep(5);
			}
		} catch (Exception e) {
			throw new RuntimeException("Locking error", e);
		}
		return false;
	}

	/**
	 * lock(); try { doSomething(); } finally { unlock() }
	 */
	public void unlock() {
		unlock(true);
	}

	/**
	 * lock(); try { doSomething(); } finally { unlock() }
	 */
	public void unlock(boolean closeJedis) {
		try {
			if (this.locked) {
				this.jedis.del(this.key);
			}
		} finally {
			if (closeJedis) {
				this.jedis.close();
			}
		}
	}
}
