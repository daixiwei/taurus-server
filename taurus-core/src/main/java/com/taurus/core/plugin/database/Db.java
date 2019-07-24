package com.taurus.core.plugin.database;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.taurus.core.entity.ITArray;
import com.taurus.core.entity.ITObject;
import com.taurus.core.entity.TArray;
import com.taurus.core.entity.TDataWrapper;
import com.taurus.core.util.StringUtil;

/**
 * Db 数据库工具
 * @author daixiwei
 *
 */
public class Db {
	protected String name;
	protected DataSource ds;
	
	private static final String STR_NULL = "";
	private static final char   CHAR_COMMA = ',';
	private static final char   CHAR_QUOTES = '\'';
	private static final char   CHAR_UNKNOWN = '?';
	private static final String   TYPE_BOOLEAN = "boolean";
	
	public Db(String name,DataSource ds) {
		this.name = name;
		this.ds = ds;
	}
	
	/**
	 * 数据库   exec  select
	 * @param sql			select 数据
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String , String>> executeQuery(String sql) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rset=null;
		try{
			conn = ds.getConnection();
			if(conn ==null)throw new SQLException("db connection is null!");
			stmt = conn.prepareStatement(sql);
			if(stmt ==null)throw new SQLException(sql +"sql error!");
			rset = stmt.executeQuery();
	
			ResultSetMetaData rsmd = rset.getMetaData();
			int dataSize = rsmd.getColumnCount();
			
			List<Map<String , String>> list = new ArrayList<Map<String , String>>();
			
			while(rset.next()){
				if (rset.isBeforeFirst()) {
					rset.next();
				}
				Map<String , String> map = new HashMap<String , String>();
				for(int i=1;i<=dataSize;i++){
					String columnName = rsmd.getColumnName(i);
					if(StringUtil.isEmpty(columnName))continue;
					Object column = rset.getObject(columnName);
					String columnStr =null;
					if(column==null || StringUtil.isEmpty(column.toString())){
						columnStr = STR_NULL;
					}else{
						columnStr = column.toString();
					}
					map.put(columnName, columnStr);
				}
				list.add(map);
			}
			return list;
		}finally{
			try {
				if(rset!=null)rset.close();
			} finally {
				try {
					if(stmt!=null)stmt.close();
				}finally {
					if(conn!=null)conn.close();
				}
			}
		}
		
	}
	
	private static void writeValueFromSetter(Field field, Object pojo, Object fieldValue) throws Exception {
		String setterName = "set" + StringUtil.capitalize(field.getName());
		Method setterMethod = pojo.getClass().getMethod(setterName, new Class[] { field.getType() });
		setterMethod.invoke(pojo, new Object[] { fieldValue });
	}
	
	private static final void setFieldValue(Object pojo, Field field, Object fieldValue) throws Exception {
		int modifiers = field.getModifiers();
		if ((Modifier.isTransient(modifiers)) || (Modifier.isStatic(modifiers))) {
			return;
		}
		if (Modifier.isPublic(modifiers))
			field.set(pojo, fieldValue);
		else
			writeValueFromSetter(field, pojo, fieldValue);
	}
	
	private static Object readValueFromGetter(String fieldName, String type, Object pojo) throws Exception {
		Object value = null;
		boolean isBool = type.equalsIgnoreCase(TYPE_BOOLEAN);
		String getterName = isBool?"is":"get" + StringUtil.capitalize(fieldName);
		Method getterMethod = pojo.getClass().getMethod(getterName, new Class[0]);
		value = getterMethod.invoke(pojo, new Object[0]);

		return value;
	}

	
	private static final Object getFieldValue(Field field,Object pojo) throws Exception {
		int modifiers = field.getModifiers();
		if ((Modifier.isTransient(modifiers)) || (Modifier.isStatic(modifiers))) {
			return null;
		}
		Object fieldValue = null;

		if (Modifier.isPublic(modifiers)) {
			fieldValue = field.get(pojo);
		} else {
			fieldValue = readValueFromGetter(field.getName(), field.getType().getSimpleName(), pojo);
		}
	
		return fieldValue;
	}
	
	/**
	 * 数据库   exec  select
	 * @param sql			select 数据
	 * @param pojoClazz
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T>  executeQuery(String sql,Class<T> clazz) throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rset=null;
		
		try{
			conn = ds.getConnection();
			if(conn ==null)throw new SQLException("db connection is null!");
			stmt = conn.prepareStatement(sql);
			if(stmt ==null)throw new SQLException(sql +"sql error!");
			rset = stmt.executeQuery();

			List<T> list = new ArrayList<T>();
			
			while(rset.next()){
				if (rset.isBeforeFirst()) {
					rset.next();
				}
				Object pojo = clazz.newInstance();
				Field[ ] fields = clazz.getDeclaredFields( );
				for(Field field:fields) {
					int modifiers = field.getModifiers();
					if ((Modifier.isTransient(modifiers)) || (Modifier.isStatic(modifiers))) {
						continue;
					}
					Object column = null;
					try {
						column = rset.getObject(field.getName());
						if (column == null) {
							continue;
						}
					}catch (Exception e) {
						continue;
					}
					setFieldValue(pojo,field,column);
				}
				list.add((T) pojo);
			}
			return list;
		}finally{
			try {
				if(rset!=null)rset.close();
			} finally {
				try {
					if(stmt!=null)stmt.close();
				}finally {
					if(conn!=null)conn.close();
				}
			}
		}
	}
	
	/**
	 * 数据库   exec  select
	 * @param sql			select 数据
	 * @return
	 * @throws SQLException
	 */
	public ITArray executeQueryByTArray(String sql) throws SQLException{
		Connection conn = null;
		PreparedStatement stmt =null;
		ResultSet rset = null;
		try{
			conn = ds.getConnection();
			if(conn ==null)throw new SQLException("db connection is null!");
			stmt = conn.prepareStatement(sql);
			if(stmt ==null)throw new SQLException(sql +"sql error!");
			rset = stmt.executeQuery();
			ITArray list = TArray.newFromResultSet(rset);
			return list;
		}finally{
			try {
				if(rset!=null)rset.close();
			} finally {
				try {
					if(stmt!=null)stmt.close();
				}finally {
					if(conn!=null)conn.close();
				}
			}
		}
	}
	
	/**
	 * 数据库  		exec sql
	 * @param sql		sql数据
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(String sql)throws SQLException{
		return __executeUpdate(sql);
	}
	
	private int __executeUpdate(String sql)throws SQLException{
		Connection conn = null;
		PreparedStatement stmt =null;
		try{
			conn = ds.getConnection();
			if(conn ==null)throw new SQLException("db connection is null!");
			stmt = conn.prepareStatement(sql);
			if(stmt ==null)throw new SQLException(sql +"sql error!");
			int result =stmt.executeUpdate();
			return result;
		}finally{
			try {
				if(stmt!=null)stmt.close();
			}finally {
				if(conn!=null)conn.close();
			}
		}
	}
	
	/**
	 * 数据库   insert into tableName values()
	 * @param table				表名
	 * @param data				insert数据
	 * @return
	 * @throws SQLException
	 */
	public int insert(String table,ITObject data) throws SQLException{
		if(StringUtil.isEmpty(table)){
			throw new SQLException("table is null!");
		}
		if(data == null){
			throw new SQLException("data is null!");
		}
		StringBuilder valuesql = new StringBuilder();
		StringBuilder keysql = new StringBuilder();
		Set<String> keys = data.getKeys();
		int count =0;
		for (String key : keys) {
			TDataWrapper wrapper = data.get(key);
			if(count>0){
				keysql.append(CHAR_COMMA);
				valuesql.append(CHAR_COMMA);
			}
			count++;
			keysql.append(key);
			switch(wrapper.getTypeId()){
				case SHORT:
				case INT:
				case DOUBLE:
				case FLOAT:
				case BYTE:
				case BOOL:
				case LONG:
					valuesql.append(wrapper.getObject());
					break;
				case STRING:
					valuesql.append(CHAR_QUOTES);
					valuesql.append(wrapper.getObject());
					valuesql.append(CHAR_QUOTES);
					break;
				case TOBJECT:
					ITObject mo = (ITObject)wrapper.getObject();
					valuesql.append(CHAR_QUOTES);
					valuesql.append(mo.toJson());
					valuesql.append(CHAR_QUOTES);
					break;
				case TARRAY:
					ITArray ao = (ITArray)wrapper.getObject();
					valuesql.append(CHAR_QUOTES);
					valuesql.append(ao.toJson());
					valuesql.append(CHAR_QUOTES);
					break;
				default:
					break;
			}
		}

		String sql = String.format("INSERT INTO %s (%s) VALUES(%s)", table,keysql,valuesql);
		return __executeUpdate(sql);
	}
	
	/**
	 * 数据库   insert into tableName values()
	 * @param table				表名
	 * @param data				insert数据
	 * @return
	 * @throws SQLException
	 */
	public <T> int insert(String table,T data) throws Exception{
		if(StringUtil.isEmpty(table)){
			throw new SQLException("table is null!");
		}
		if(data == null){
			throw new SQLException("data is null!");
		}
		StringBuilder valuesql = new StringBuilder();
		StringBuilder keysql = new StringBuilder();
		// 获取类中的全部定义字段
		Field[ ] fields = data.getClass().getDeclaredFields( );
		int count =0;
		for(Field field:fields) {
			Object value = getFieldValue(field,data);
			if(value==null)continue;
			if(count>0){
				keysql.append(CHAR_COMMA);
				valuesql.append(CHAR_COMMA);
			}
			count++;
			keysql.append(field.getName());
			if ((value instanceof Boolean) || (value instanceof Byte) || (value instanceof Short) || (value instanceof Integer) || (value instanceof Long) || 
					(value instanceof Float) || (value instanceof Double)) {
				valuesql.append(value);
			}  else if ((value instanceof String)) {
				valuesql.append(CHAR_QUOTES);
				valuesql.append(value);
				valuesql.append(CHAR_QUOTES);
			}
		}
		String sql = String.format("INSERT INTO %s (%s) VALUES(%s)", table,keysql,valuesql);
		return __executeUpdate(sql);
	}
	
	/**
	 * 数据库   insert into tableName values()
	 * @param table				表名
	 * @param list				insert数据
	 * @return
	 * @throws SQLException
	 */
	public <T> int insertBatch(String table,List<T> list) throws Exception{
		if(StringUtil.isEmpty(table)){
			throw new SQLException("table is null!");
		}
		if(list == null){
			throw new SQLException("list is null!");
		}
		if(list.size() == 0)return 0;

		StringBuilder valuesql = new StringBuilder();
		StringBuilder keysql = new StringBuilder();
		// 获取类中的全部定义字段
		T data = list.get(0);
		Class<?> classz = data.getClass();
		Field[] fields = classz.getDeclaredFields();
		
		int count =0;
		List<Method> methodList = new ArrayList<>();
		for(Field field:fields) {
			int modifiers = field.getModifiers();
			if ((Modifier.isTransient(modifiers)) || (Modifier.isStatic(modifiers))) {
				continue;
			}
			String fieldName = field.getName();
			boolean isBool = field.getType().getSimpleName().equalsIgnoreCase(TYPE_BOOLEAN);
			String getterName = isBool?"is":"get" + StringUtil.capitalize(fieldName);
			Method getterMethod = classz.getMethod(getterName, new Class[0]);
			methodList.add(getterMethod);
			if(count>0){
				keysql.append(CHAR_COMMA);
				valuesql.append(CHAR_COMMA);
			}
			count++;
			keysql.append(field.getName());
			valuesql.append(CHAR_UNKNOWN);
		}
		String sql = String.format("INSERT INTO %s (%s) VALUES(%s)", table,keysql,valuesql);
		Connection conn = null;
		PreparedStatement stmt =null;
		try{
			conn = ds.getConnection();
			if(conn ==null)throw new SQLException("db connection is null!");
			stmt = conn.prepareStatement(sql);
			if(stmt ==null)throw new SQLException(sql +"sql error!");
			conn.setAutoCommit(false);
			for(int i=0;i<list.size();++i) {
				T obj = list.get(i);
				for(int k=1;k<=count;++k) {
					Method method = methodList.get(k-1);
					Object value = method.invoke(obj, new Object[0]);
					stmt.setObject(k, value);
				}
				stmt.addBatch();
			}
			stmt.executeBatch();
			conn.commit();
			return 0;
		}finally{
			try {
				if(stmt!=null)stmt.close();
			}finally {
				if(conn!=null)conn.close();
			}
		}
	}
	
	
	/**
	 * 数据库 update tableName set
	 * @param table			表名
	 * @param data			更新的字段
	 * @param where			sql条件
	 * @return
	 * @throws SQLException
	 */
	public int update(String table,ITObject data,String where)throws SQLException{
		if(StringUtil.isEmpty(table)){
			throw new SQLException("table is null!");
		}
		if(data == null || data.size()==0){
			throw new SQLException("data is null!");
		}
		StringBuilder valuesql = new StringBuilder();
		Set<String> keys = data.getKeys();
		int count =0;
		for (String key : keys) {
			TDataWrapper wrapper = data.get(key);
			if(count>0){
				valuesql.append(CHAR_COMMA);
			}
			count++;
			valuesql.append(key);
			valuesql.append("=");
			switch(wrapper.getTypeId()){
				case SHORT:
				case INT:
				case DOUBLE:
				case FLOAT:
				case BYTE:
				case BOOL:
				case LONG:
					valuesql.append(wrapper.getObject());
					break;
				case STRING:
					valuesql.append(CHAR_QUOTES);
					valuesql.append(wrapper.getObject());
					valuesql.append(CHAR_QUOTES);
					break;
				case TOBJECT:
					ITObject mo = (ITObject)wrapper.getObject();
					valuesql.append(CHAR_QUOTES);
					valuesql.append(mo.toJson());
					valuesql.append(CHAR_QUOTES);
					break;
				case TARRAY:
					ITArray ao = (ITArray)wrapper.getObject();
					valuesql.append(CHAR_QUOTES);
					valuesql.append(ao.toJson());
					valuesql.append(CHAR_QUOTES);
					break;
				default:
					break;
			}
		}

		String sql = String.format("UPDATE %s SET %s ", table,valuesql);
		if(where!=null&&where.length()>0)sql = sql + "where "+where;
		return __executeUpdate(sql);
	}
	
	/**
	 * 数据库 update tableName set
	 * @param table			表名
	 * @param data			更新的字段
	 * @param where			sql条件
	 * @return
	 * @throws Exception
	 */
	public <T> int update(String table,T data,String where)throws Exception{
		if(StringUtil.isEmpty(table)){
			throw new SQLException("table is null!");
		}
		if(data == null){
			throw new SQLException("data is null!");
		}
		StringBuilder valuesql = new StringBuilder();

		// 获取类中的全部定义字段
		Field[] fields = data.getClass().getDeclaredFields( );
		int count =0;
		for(Field field:fields) {
			Object value = getFieldValue(field,data);
			if(value==null)continue;
			if(count>0){
				valuesql.append(CHAR_COMMA);
			}
			count++;
			valuesql.append(field.getName());
			valuesql.append("=");
			if ((value instanceof Boolean) || (value instanceof Byte) || (value instanceof Short) || (value instanceof Integer) || (value instanceof Long) || 
					(value instanceof Float) || (value instanceof Double)) {
				valuesql.append(value);
			}  else if ((value instanceof String)) {
				valuesql.append(CHAR_QUOTES);
				valuesql.append(value);
				valuesql.append(CHAR_QUOTES);
			}
		}
		
		String sql = String.format("UPDATE %s SET %s ", table,valuesql);
		if(where!=null&&where.length()>0)sql = sql + "where "+where;
		return __executeUpdate(sql);
	}
	
	/**
	 * 存储过程调用    call prepareName()
	 * @param prepareName		存储过程名
	 * @param data				数据
	 * @return
	 * @throws SQLException
	 */
	public ITArray prepareCall(String prepareName,ITArray data) throws SQLException{
		return prepareCall(prepareName,data,true);
	}
	
	/**
	 * 存储过程调用    call prepareName()
	 * @param prepareName		存储过程名
	 * @param data				数据
	 * @return
	 * @throws SQLException
	 */
	public void prepareCallNonResult(String prepareName,ITArray data) throws SQLException{
		prepareCall(prepareName,data,false);
	}
	
	/**
	 * 存储过程调用    call prepareName()
	 * @param prepareName		存储过程名
	 * @param data				数据
	 * @return
	 * @throws SQLException
	 */
	private ITArray prepareCall(String prepareName,ITArray data,boolean resultSet) throws SQLException{
		if(StringUtil.isEmpty(prepareName)){
			throw new SQLException("prepare name is null!");
		}
		if(data == null){
			throw new SQLException("data is null!");
		}
		StringBuilder valuesql = new StringBuilder();
		
		int count =0;
		for (int i=0;i<data.size();++i) {
			
			TDataWrapper wrapper = data.get(i);
			if(count>0){
				valuesql.append(CHAR_COMMA);
			}
			count++;
			switch(wrapper.getTypeId()){
				case SHORT:
				case INT:
				case DOUBLE:
				case FLOAT:
				case BYTE:
				case BOOL:
				case LONG:
					valuesql.append(wrapper.getObject());
					break;
				case STRING:
					valuesql.append(CHAR_QUOTES);
					valuesql.append(wrapper.getObject());
					valuesql.append(CHAR_QUOTES);
					break;
				case TOBJECT:
					ITObject mo = (ITObject)wrapper.getObject();
					valuesql.append(CHAR_QUOTES);
					valuesql.append(mo.toJson());
					valuesql.append(CHAR_QUOTES);
					break;
				case TARRAY:
					ITArray ao = (ITArray)wrapper.getObject();
					valuesql.append(CHAR_QUOTES);
					valuesql.append(ao.toJson());
					valuesql.append(CHAR_QUOTES);
					break;
				default:
					break;
			}
		}
		String sql = String.format("{call %s(%s)}", prepareName,valuesql);
		return executeCall(sql,resultSet);
	}
	
	/**
	 * 存储过程调用 
	 * @param sql
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	public ITArray executeCall(String sql,boolean resultSet) throws SQLException{
		Connection conn = null;
		CallableStatement stmt =null;
		ResultSet rset = null;
		try{
			conn = ds.getConnection();
			if(conn ==null)throw new SQLException("db connection is null!");
			stmt = conn.prepareCall(sql);
			if(stmt ==null)throw new SQLException(sql +"sql error!");
			if(resultSet) {
				rset = stmt.executeQuery();
				ITArray list = TArray.newFromResultSet(rset);
				return list;
			}else {
				stmt.executeUpdate();
				return null;
			}
		}finally{
			try {
				if(rset!=null)rset.close();
			} finally {
				try {
					if(stmt!=null)stmt.close();
				}finally {
					if(conn!=null)conn.close();
				}
			}
		}
	}
	
	
	/**
	 * 获取当前DB名称
	 * @return
	 */
	public String getName() {
		return name;
	}
}
