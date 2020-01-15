package com.taurus.core.plugin.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import com.taurus.core.plugin.IPlugin;
import com.taurus.core.plugin.redis.RedisPlugin.RedisConfig.InfoConfig;
import com.taurus.core.util.StringUtil;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * RedisPlugin.
 * RedisPlugin 支持多个 Redis 服务端，只需要创建多个 RedisPlugin 对象
 * 对应这多个不同的 Redis 服务端即可。也支持多个 RedisPlugin 对象对应同一
 */
public class RedisPlugin implements IPlugin{
	private static final int TIMEOUT = 5000;
	private String id;
	private final RedisConfig config;


	
	public RedisPlugin() {
		config = new RedisConfig();
	}
	
	public boolean start() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(config.poolConfig.maxTotal);
		jedisPoolConfig.setMaxIdle(config.poolConfig.maxIdle);
		jedisPoolConfig.setMaxIdle(config.poolConfig.minIdle);
		jedisPoolConfig.setMaxWaitMillis(config.poolConfig.maxWaitMillis);
		jedisPoolConfig.setTestOnBorrow(config.poolConfig.testOnBorrow);
		jedisPoolConfig.setTestOnReturn(config.poolConfig.testOnReturn);
		jedisPoolConfig.setTestWhileIdle(config.poolConfig.testWhileIdle);
		jedisPoolConfig.setNumTestsPerEvictionRun(config.poolConfig.numTestsPerEvictionRun);
		jedisPoolConfig.setMinEvictableIdleTimeMillis(config.poolConfig.minEvictableIdleTimeMillis);
		jedisPoolConfig.setTimeBetweenEvictionRunsMillis(config.poolConfig.timeBetweenEvictionRunsMillis);
		jedisPoolConfig.setSoftMinEvictableIdleTimeMillis(config.poolConfig.softMinEvictableIdleTimeMillis);
		jedisPoolConfig.setBlockWhenExhausted(config.poolConfig.blockWhenExhausted);
		
		for (InfoConfig sis : config.infos) {
			String passwd = StringUtil.isEmpty(sis.password) ? null : sis.password;
			JedisPool jedisPool = new JedisPool(jedisPoolConfig, sis.host, sis.port, TIMEOUT, passwd, sis.database);
			Cache cache = new Cache(sis.name, jedisPool);
			Redis.addCache(cache);
		}
		return true;
	}
	
	public boolean stop() {
		Set<String> keys = Redis.cacheMap.keySet();
		for(String key : keys) {
			Redis.removeCache(key);
		}
		return true;
	}
	


	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean loadConfig(Element element) {
		Element pcEm = element.getChild("poolConfig");
		this.config.poolConfig.maxTotal = Integer.parseInt(pcEm.getChild("maxTotal").getTextTrim());
		this.config.poolConfig.maxIdle = Integer.parseInt(pcEm.getChild("maxIdle").getTextTrim());
		this.config.poolConfig.minIdle = Integer.parseInt(pcEm.getChild("minIdle").getTextTrim());
		this.config.poolConfig.maxWaitMillis = Integer.parseInt(pcEm.getChild("maxWaitMillis").getTextTrim());
		this.config.poolConfig.testOnBorrow = Boolean.parseBoolean(pcEm.getChild("testOnBorrow").getTextTrim());
		this.config.poolConfig.testOnReturn = Boolean.parseBoolean(pcEm.getChild("testOnReturn").getTextTrim());
		this.config.poolConfig.testWhileIdle = Boolean.parseBoolean(pcEm.getChild("testWhileIdle").getTextTrim());
		this.config.poolConfig.numTestsPerEvictionRun = Integer.parseInt(pcEm.getChild("numTestsPerEvictionRun").getTextTrim());
		this.config.poolConfig.minEvictableIdleTimeMillis = Integer.parseInt(pcEm.getChild("minEvictableIdleTimeMillis").getTextTrim());
		this.config.poolConfig.timeBetweenEvictionRunsMillis = Integer.parseInt(pcEm.getChild("timeBetweenEvictionRunsMillis").getTextTrim());
		this.config.poolConfig.softMinEvictableIdleTimeMillis = Integer.parseInt(pcEm.getChild("softMinEvictableIdleTimeMillis").getTextTrim());
		this.config.poolConfig.blockWhenExhausted = Boolean.parseBoolean(pcEm.getChild("blockWhenExhausted").getTextTrim());

		Element dblistEm = element.getChild("infos");

		Iterator<?> itr = (dblistEm.getChildren()).iterator();
	    while(itr.hasNext()) {
	        Element infoEm = (Element)itr.next();
	        InfoConfig infoConfig = new InfoConfig();
			infoConfig.name = infoEm.getAttributeValue("name", StringUtil.Empty);
			infoConfig.host = infoEm.getAttributeValue("host", "127.0.0.1");
			infoConfig.port = Integer.parseInt(infoEm.getAttributeValue("port", "6379"));
			infoConfig.database = Integer.parseInt(infoEm.getAttributeValue("database", "0"));
			infoConfig.password = infoEm.getAttributeValue("password");
			this.config.infos.add(infoConfig);
	    }
		return true;
	}
	
	public static final class RedisConfig{

		public volatile PoolConfig			poolConfig	= new PoolConfig();

		public volatile List<InfoConfig>	infos	= new ArrayList<InfoConfig>();
		
		
		public static final class PoolConfig {
			public int		maxTotal						= 8;
			public int		maxIdle							= 8;
			public int		minIdle							= 0;
			public int		maxWaitMillis					= 8;
			public boolean	testOnBorrow					= true;
			public boolean	testOnReturn					= true;
			public boolean	testWhileIdle					= true;
			public int		numTestsPerEvictionRun			= 3;
			public int		minEvictableIdleTimeMillis		= 6000;
			public int		timeBetweenEvictionRunsMillis	= 3000;
			public int		softMinEvictableIdleTimeMillis	= 1000;
			public boolean	blockWhenExhausted				= true;
		}

		public static final class InfoConfig {
			public String 	name	="";
			public String	host	= "127.0.0.1";
			public int		port	= 6379;
			public String	password = "";
			public int		database = 0;
		}
	}
}


