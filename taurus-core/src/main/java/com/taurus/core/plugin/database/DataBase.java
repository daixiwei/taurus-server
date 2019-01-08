package com.taurus.core.plugin.database;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.taurus.core.util.StringUtil;

/**
 * DataBase 数据库工具
 * <pre>
 * DataBase.use().executeQueryByMPArray("SELECT * FROM table");
 * </pre>
 * @author daixiwei
 *
 */
public class DataBase {
	static Db mainDb = null;
	
	static final ConcurrentMap<String, Db> dbMap = new ConcurrentHashMap<String, Db>();
	
	static void addDb(Db db) {
		if (db == null)
			throw new IllegalArgumentException("cache can not be null");
		if (dbMap.containsKey(db.getName()))
			throw new IllegalArgumentException("The cache name already exists");
		
		dbMap.put(db.getName(), db);
		if (mainDb == null)
			mainDb = db;
	}
	
	static void removeDb(String dbName) {
		Db db = dbMap.remove(dbName);
		if(db == mainDb) {
			db.ds.close();
			mainDb = null;
		}
	}
	
	/**
	 * set mainDb
	 */ 
	public static void setMainDb(String dbName) {
		if (StringUtil.isEmpty(dbName))
			throw new IllegalArgumentException("dbName can not be blank");
		dbName = dbName.trim();
		Db db = dbMap.get(dbName);
		if (db == null)
			throw new IllegalArgumentException("the db not exists: " + dbName);
		
		DataBase.mainDb = db;
	}
	
	public static Db use() {
		return mainDb;
	}
	
	public static Db use(String dbName) {
		return dbMap.get(dbName);
	}
}
