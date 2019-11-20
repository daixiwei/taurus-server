package com.taurus.core.plugin.redis;

import java.util.UUID;

import redis.clients.jedis.Jedis;

/**
 * RedisLock
 */
public class RedisLock {
	private static final long	DEFAULT_TIME_OUT		= 180000;
	private static final int	EXPIRE					= 10000;
	private static final String	LOCK_SUCCESS			= "OK";
	private static final String	SET_IF_NOT_EXIST		= "NX";
	private static final String	SET_WITH_EXPIRE_TIME	= "PX";
	private static final String checkAndDelScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) " +
            "else " +
            "return 0 " +
            "end";
	
	private Jedis				jedis;
	private String				key;
	private String				lock_value;

	/**
	 * This creates a RedisLock
	 * 
	 * @param key key
	 * @param jedis
	 */
	public RedisLock(String key, Jedis jedis) {
		this.key = key + "{lock}";
		this.jedis = jedis;
		lock_value = UUID.randomUUID().toString()+Thread.currentThread().getId() + System.nanoTime();
	}

	/**
	 * lock(); try { doSomething(); } finally { unlock() }
	 */
	public boolean lock() {
		long time = System.currentTimeMillis();
		try {
			while ((System.currentTimeMillis() - time) < DEFAULT_TIME_OUT) {
				String result = jedis.set(key, lock_value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, EXPIRE);
				if (LOCK_SUCCESS.equals(result)) {
					return true;
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
			jedis.eval(checkAndDelScript, 1, key, lock_value);
		} finally {
			if (closeJedis) {
				this.jedis.close();
			}
		}
	}
}
