package com.taurus.core.plugin.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jdom.Element;

import com.taurus.core.plugin.IPlugin;
import com.taurus.core.plugin.database.DataBasePlugin.DatabaseConfig.DbConfig;
import com.taurus.core.plugin.database.DataBasePlugin.DatabaseConfig.PoolConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
		
		for(DbConfig dbconfig : this.config.dbList) {
			PoolConfig config = this.config.poolConfig;
			
			HikariConfig hk_config = new HikariConfig();
			hk_config.setPoolName(dbconfig.name);
			hk_config.setJdbcUrl(dbconfig.jdbcUrl);
			hk_config.setUsername(dbconfig.userName);
			hk_config.setPassword(dbconfig.password);
			hk_config.setDriverClassName(dbconfig.driverName);
			hk_config.setMaximumPoolSize(config.maxPool);
			hk_config.setMinimumIdle(config.minIdle);
			hk_config.setConnectionTimeout(config.connectionTimeout);
			hk_config.setIdleTimeout(config.idleTimeout);
			hk_config.setMaxLifetime(config.maxLifetime);
			hk_config.setConnectionTestQuery(config.validationQuery);
			hk_config.setDataSourceProperties(config.props);
//			hk_config.addDataSourceProperty("cachePrepStmts", config.cachePrepStmts);
//			hk_config.addDataSourceProperty("prepStmtCacheSize", config.prepStmtCacheSize);
//			hk_config.addDataSourceProperty("prepStmtCacheSqlLimit", config.prepStmtCacheSqlLimit);

			HikariDataSource dbPool = new HikariDataSource(hk_config);
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
		this.config.poolConfig.maxPool = Integer.parseInt(pcEm.getChild("maxPool").getTextTrim());
		this.config.poolConfig.minIdle = Integer.parseInt(pcEm.getChild("minIdle").getTextTrim());
		this.config.poolConfig.maxLifetime = Integer.parseInt(pcEm.getChild("maxLifetime").getTextTrim());
		this.config.poolConfig.validationQuery = pcEm.getChild("validationQuery").getTextTrim();
		this.config.poolConfig.connectionTimeout = Integer.parseInt(pcEm.getChild("connectionTimeout").getTextTrim());
		this.config.poolConfig.idleTimeout = Integer.parseInt(pcEm.getChild("idleTimeout").getTextTrim());
		Element propsEm = pcEm.getChild("props");
		List<?>  props = propsEm.getChildren();
		for(Object obj : props) {
			Element pEm = (Element) obj;
			this.config.poolConfig.props.put(pEm.getName(), pEm.getTextTrim());
		}
//		props.getChildren();
//		this.config.poolConfig.cachePrepStmts = Boolean.parseBoolean(pcEm.getChild("cachePrepStmts").getTextTrim());
//		this.config.poolConfig.prepStmtCacheSize = Integer.parseInt(pcEm.getChild("prepStmtCacheSize").getTextTrim());
//		this.config.poolConfig.prepStmtCacheSqlLimit = Integer.parseInt(pcEm.getChild("prepStmtCacheSqlLimit").getTextTrim());
		
		Element dblistEm = element.getChild("databases");

		Iterator<?> itr = (dblistEm.getChildren()).iterator();
	    while(itr.hasNext()) {
	        Element dbEm = (Element)itr.next();
			DbConfig dbconfig = new DbConfig();
			dbconfig.name = dbEm.getChildTextTrim("name");
			dbconfig.driverName = dbEm.getChildTextTrim("driverName");
			dbconfig.jdbcUrl = dbEm.getChildTextTrim("jdbcUrl");
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
			public int		maxPool					= 8;
			public int		minIdle					= 8;
			public int		maxLifetime				= 6000;
			public int		connectionTimeout		= 3000;
			public int		idleTimeout				= 60000;
			public String	validationQuery			= "select 1";
			
			public Properties props = new Properties();
//			public boolean	cachePrepStmts			= true;
//			public int		prepStmtCacheSize		= 250;
//			public int		prepStmtCacheSqlLimit	= 2048;
		}
		
		public static final class DbConfig{
			public String 	name 				="";
			public String	driverName			= "";
			public String	jdbcUrl				= "";
			public String	userName			= "";
			public String	password			= "";
		}
	}
}
