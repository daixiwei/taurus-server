package com.taurus.core.plugin.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import com.alibaba.druid.pool.DruidDataSource;
import com.taurus.core.plugin.IPlugin;
import com.taurus.core.plugin.database.DataBasePlugin.DatabaseConfig.DbConfig;
import com.taurus.core.plugin.database.DataBasePlugin.DatabaseConfig.PoolConfig;

/**
 * DataBasePlugin
 * 支持多个数据库连接组件
 * @author daixiwei
 *
 */
public class DataBasePlugin implements IPlugin{
	private String id;

	private DatabaseConfig config;
	
	public DataBasePlugin() {
		this.config = new DatabaseConfig();
	}
	
	@Override
	public boolean start() {
		DruidDataSource dbPool = new DruidDataSource();
		for(DbConfig dbconfig : this.config.dbList) {
			dbPool.setName(dbconfig.name);
			dbPool.setUrl(dbconfig.connectionString);
			dbPool.setUsername(dbconfig.userName);
			dbPool.setPassword(dbconfig.password);
			dbPool.setDriverClassName(dbconfig.driverName);
			
			PoolConfig config = this.config.poolConfig;
			dbPool.setInitialSize(config.initialSize);
			dbPool.setMinIdle(config.minIdle);
			dbPool.setMaxActive(config.maxActive);
			dbPool.setMaxWait(config.maxWait);
			dbPool.setTimeBetweenConnectErrorMillis(config.timeBetweenConnectErrorMillis);
			dbPool.setTimeBetweenEvictionRunsMillis(config.timeBetweenEvictionRunsMillis);
			dbPool.setMinEvictableIdleTimeMillis(config.minEvictableIdleTimeMillis);
			
			dbPool.setValidationQuery(config.validationQuery);
			dbPool.setTestWhileIdle(config.testWhileIdle);
			dbPool.setTestOnBorrow(config.testOnBorrow);
			dbPool.setTestOnReturn(config.testOnReturn);
			
			dbPool.setRemoveAbandoned(config.removeAbandoned);
			dbPool.setRemoveAbandonedTimeoutMillis(config.removeAbandonedTimeoutMillis);
			dbPool.setLogAbandoned(config.logAbandoned);
		
			dbPool.setMaxPoolPreparedStatementPerConnectionSize(config.maxPoolPreparedStatementPerConnectionSize);
			
			Db db = new Db(dbconfig.name, dbPool);
			DataBase.addDb(db);
		}
		
		return true;
	}

	@Override
	public boolean stop() {
		Set<String> keys = DataBase.dbMap.keySet();
		for(String key : keys) {
			DataBase.removeDb(key);
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
		this.config.poolConfig.maxActive = Integer.parseInt(pcEm.getChild("maxActive").getTextTrim());
		this.config.poolConfig.initialSize = Integer.parseInt(pcEm.getChild("initialSize").getTextTrim());
		this.config.poolConfig.minIdle = Integer.parseInt(pcEm.getChild("minIdle").getTextTrim());
		this.config.poolConfig.maxWait = Integer.parseInt(pcEm.getChild("maxWait").getTextTrim());
		this.config.poolConfig.testOnBorrow = Boolean.parseBoolean(pcEm.getChild("testOnBorrow").getTextTrim());
		this.config.poolConfig.testOnReturn = Boolean.parseBoolean(pcEm.getChild("testOnReturn").getTextTrim());
		this.config.poolConfig.testWhileIdle = Boolean.parseBoolean(pcEm.getChild("testWhileIdle").getTextTrim());
		this.config.poolConfig.minEvictableIdleTimeMillis = Integer.parseInt(pcEm.getChild("minEvictableIdleTimeMillis").getTextTrim());
		this.config.poolConfig.timeBetweenEvictionRunsMillis = Integer.parseInt(pcEm.getChild("timeBetweenEvictionRunsMillis").getTextTrim());
		this.config.poolConfig.timeBetweenConnectErrorMillis = Integer.parseInt(pcEm.getChild("timeBetweenConnectErrorMillis").getTextTrim());
		this.config.poolConfig.validationQuery = pcEm.getChild("maxActive").getTextTrim();
		this.config.poolConfig.removeAbandoned = Boolean.parseBoolean(pcEm.getChild("removeAbandoned").getTextTrim());
		this.config.poolConfig.removeAbandonedTimeoutMillis = Integer.parseInt(pcEm.getChild("removeAbandonedTimeoutMillis").getTextTrim());
		this.config.poolConfig.logAbandoned = Boolean.parseBoolean(pcEm.getChild("logAbandoned").getTextTrim());
		this.config.poolConfig.maxPoolPreparedStatementPerConnectionSize = Integer.parseInt(pcEm.getChild("maxPoolPreparedStatementPerConnectionSize").getTextTrim());
		
		Element dblistEm = element.getChild("databases");

		Iterator<?> itr = (dblistEm.getChildren()).iterator();
	    while(itr.hasNext()) {
	        Element dbEm = (Element)itr.next();
			DbConfig dbconfig = new DbConfig();
			dbconfig.name = dbEm.getChildTextTrim("name");
			dbconfig.driverName = dbEm.getChildTextTrim("driverName");
			dbconfig.connectionString = dbEm.getChildTextTrim("connectionString");
			dbconfig.userName = dbEm.getChildTextTrim("userName");
			dbconfig.password = dbEm.getChildTextTrim("password");
			this.config.dbList.add(dbconfig);
	    }
		return true;
	}
	
	public static final class DatabaseConfig {
		public volatile PoolConfig		poolConfig	= new PoolConfig();
		public volatile List<DbConfig>	dbList	= new ArrayList<DbConfig>();
		
		public static final class PoolConfig {
			public int		maxActive									= 8;
			public int		initialSize									= 8;
			public int		minIdle										= 0;
			public int		maxWait										= 8;
			public boolean	testOnBorrow								= true;
			public boolean	testOnReturn								= true;
			public boolean	testWhileIdle								= true;
			public int		minEvictableIdleTimeMillis					= 6000;
			public int		timeBetweenEvictionRunsMillis				= 3000;
			public int		timeBetweenConnectErrorMillis				= 1000;
			public String	validationQuery								= "select 1";
			public boolean	removeAbandoned								= true;
			public int		removeAbandonedTimeoutMillis				= 1000;
			public boolean	logAbandoned								= true;
			public int		maxPoolPreparedStatementPerConnectionSize	= 1000;
		}
		
		public static final class DbConfig{
			public String 	name 				="";
			public String	driverName			= "";
			public String	connectionString	= "";
			public String	userName			= "";
			public String	password			= "";
		}
	}
}
