package com.taurus.core.plugin.redis;

import java.util.Random;

import redis.clients.jedis.Jedis;

/**
 * RedisLock
 * @author daixiwei
 *
 */
public class RedisLock {  
  
    public static final String LOCKED = "TRUE";  
    public static final long MILLI_NANO_CONVERSION = 1000 * 1000L;  
    public static final long DEFAULT_TIME_OUT = 1000;  
    public static final Random RANDOM = new Random();  
    public static final int EXPIRE = 3 * 60;  
  
    private Jedis jedis;
    private String key;  
    private boolean locked = false;  
  
    /** 
     * This creates a RedisLock 
     * @param key key 
     * @param shardedJedisPool 
     */  
    public RedisLock(String key, Jedis jedis) {  
        this.key = key + "_lock";  
        this.jedis = jedis;  
    }  
  
    /** 
     * lock(); 
     * try { 
     *      doSomething(); 
     * } finally { 
     *      unlock()
     * } 
     */  
    public boolean lock(long timeout) {  
        long nano = System.nanoTime();  
        timeout *= MILLI_NANO_CONVERSION;  
        try {  
            while ((System.nanoTime() - nano) < timeout) {  
                if (this.jedis.setnx(this.key, LOCKED) == 1) {  
                    this.jedis.expire(this.key, EXPIRE);  
                    this.locked = true;  
                    return this.locked;  
                }  
                Thread.sleep(3, RANDOM.nextInt(500));  
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
    public boolean lock(long timeout, int expire) {  
        long nano = System.nanoTime();  
        timeout *= MILLI_NANO_CONVERSION;  
        try {  
            while ((System.nanoTime() - nano) < timeout) {  
                if (this.jedis.setnx(this.key, LOCKED) == 1) {  
                    this.jedis.expire(this.key, expire);  
                    this.locked = true;  
                    return this.locked;  
                }  
                Thread.sleep(3, RANDOM.nextInt(500));  
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
        try {  
            if (this.locked) {  
                this.jedis.del(this.key);  
            }  
        } finally {  
            this.jedis.close();
        }  
    }  
}  
