package com.taurus.core.plugin.redis;

import redis.clients.jedis.Jedis;

/**
 * RedisLock
 */
public class RedisLock {  
	private static final String LOCKED = "TRUE";
	private static final long DEFAULT_TIME_OUT = 60000;
	private static final int EXPIRE = 60;  
	private static final String LOCKED_KEY = "_lock";
  
    private Jedis jedis;
    private String key;  
    private boolean locked = false;  
  
    /** 
     * This creates a RedisLock 
     * @param key key 
     * @param jedis 
     */  
    public RedisLock(String key, Jedis jedis) {  
        this.key = key + LOCKED_KEY;  
        this.jedis = jedis;  
    }  
  
    /**
     * lock(); 
     * try { 
     *      doSomething(); 
     * } finally { 
     *      unlock()
     * } 
     * @param timeout  毫秒
     * @return
     */
    public boolean lock(long timeout) {  
        long nano = System.currentTimeMillis();
        try {  
            while ((System.currentTimeMillis() - nano) < timeout) {  
                if (this.jedis.setnx(this.key, LOCKED) == 1) {  
                    this.jedis.expire(this.key, EXPIRE);  
                    this.locked = true;  
                    return this.locked;  
                }  
                Thread.sleep(3);  
            }  
        } catch (Exception e) {  
            throw new RuntimeException("Locking error", e);  
        }  
        return false;  
    }  
 
  
    /** 
     * lock(); 
     * try { 
     *      doSomething(); 
     * } finally { 
     *      unlock()
     * } 
     */  
    public boolean lock() {  
        return lock(DEFAULT_TIME_OUT);  
    }  
  
    /** 
     * lock(); 
     * try { 
     *      doSomething(); 
     * } finally { 
     *      unlock()
     * } 
     */  
    public void unlock() {  
    	unlock(true); 
    }  
    
    /** 
     * lock(); 
     * try { 
     *      doSomething(); 
     * } finally { 
     *      unlock()
     * } 
     */  
    public void unlock(boolean closeJedis) {  
        try {  
            if (this.locked) {  
                this.jedis.del(this.key);  
            }  
        } finally {  
        	if(closeJedis) {
        		this.jedis.close();
        	}
        }  
    }  
}  
