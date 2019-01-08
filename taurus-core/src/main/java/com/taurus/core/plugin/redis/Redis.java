package com.taurus.core.plugin.redis;

import java.util.concurrent.ConcurrentHashMap;

import com.taurus.core.util.StringUtil;

/**
 * Redis.
 * redis 工具类
 * <pre>
 * 例如：
 * Redis.use().set("key", "value");
 * Redis.use().get("key");
 * </pre>
 */
public class Redis {
	
	static Cache mainCache = null;
	
	static final ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();
	
	static void addCache(Cache cache) {
		if (cache == null)
			throw new IllegalArgumentException("cache can not be null");
		if (cacheMap.containsKey(cache.getName()))
			throw new IllegalArgumentException("The cache name already exists");
		
		cacheMap.put(cache.getName(), cache);
		if (mainCache == null)
			mainCache = cache;
	}
	
	static void removeCache(String cacheName) {
		Cache cache = cacheMap.remove(cacheName);
		if(cache == mainCache) {
			cache.jedisPool.destroy();
			mainCache = null;
		}
	}
	
	/**
	 * 提供一个设置设置主缓存 mainCache 的机会，否则第一个被初始化的 Cache 将成为 mainCache
	 */
	public static void setMainCache(String cacheName) {
		if (StringUtil.isEmpty(cacheName))
			throw new IllegalArgumentException("cacheName can not be blank");
		cacheName = cacheName.trim();
		Cache cache = cacheMap.get(cacheName);
		if (cache == null)
			throw new IllegalArgumentException("the cache not exists: " + cacheName);
		
		Redis.mainCache = cache;
	}
	
	public static Cache use() {
		return mainCache;
	}
	
	public static Cache use(String cacheName) {
		return cacheMap.get(cacheName);
	}
	
}




