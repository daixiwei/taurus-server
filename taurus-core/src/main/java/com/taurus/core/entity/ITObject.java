package com.taurus.core.entity;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * object map interface
 * @author daixiwei	daixiwei15@126.com
 *
 */
public interface ITObject {
	public boolean isNull(String key);
	
	public boolean containsKey(String key);
	
	public boolean remove(String key);
	
	public Set<String> getKeys();
	
	public int size();
	
	public Iterator<Map.Entry<String, TDataWrapper>> iterator();
	
	public byte[] toBinary();
	
	public String toJson();
	
	public TDataWrapper get(String key);
	
	public Boolean getBoolean(String key);
	
	public Byte getByte(String key);
	
	public Integer getUByte(String key);
	
	public Short getShort(String key);
	
	public Integer getInt(String key);
	
	public Long getLong(String key);
	
	public Float getFloat(String key);
	
	public Double getDouble(String key);
	
	public String getString(String key);
	
	public byte[] getByteArray(String key);
	
	public ITArray getTArray(String key);
	
	public ITObject getTObject(String key);
	
	public void putNull(String key);
	
	public void putBoolean(String key, boolean value);
	
	public void putByte(String key, byte value);
	
	public void putShort(String key, short value);
	
	public void putInt(String key, int value);
	
	public void putLong(String key, long value);
	
	public void putFloat(String key, float value);
	
	public void putDouble(String key, double value);
	
	public void putString(String key, String value);
	
	public void putByteArray(String key, byte[] data);
	
	public void putTArray(String key, ITArray array);
	
	public void putTObject(String key, ITObject object);
	
	public void put(String key, TDataWrapper wrapper);
	
	public void del(String key);
	
}
