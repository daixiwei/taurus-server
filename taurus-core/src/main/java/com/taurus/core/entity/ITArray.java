package com.taurus.core.entity;

import java.util.Iterator;

/**
 * object array interface
 * @author daixiwei	daixiwei15@126.com
 *
 */
public interface ITArray {
	
	public Iterator<TDataWrapper> iterator();

	public TDataWrapper get(int index);
	
	public void del(int index);
	
	public int size();
	
	public void clear();
	
	public byte[] toBinary();
	
	public String toJson();
	
	public void addNull();
	
	public void addBool(boolean value);
	
	public void addByte(byte value);
	
	public void addShort(short value);
	
	public void addInt(int value);
	
	public void addLong(long value);
	
	public void addFloat(float value);
	
	public void addDouble(double value);
	
	public void addString(String value);
	
	public void addByteArray(byte[] data);
	
	public void addTArray(ITArray array);
	
	public void addTObject(ITObject object);
	
	public void add(TDataWrapper wrapper);
	
	public boolean isNull(int index);
	
	public Boolean getBool(int index);
	
	public Byte getByte(int index);
	
	public Integer getUByte(int index);
	
	public Short getShort(int index);
	
	public Integer getInt(int index);
	
	public Long getLong(int index);
	
	public Float getFloat(int index);
	
	public Double getDouble(int index);
	
	public String getString(int index);
	
	public byte[] getByteArray(int index);
	
	public ITArray getTArray(int index);
	
	public ITObject getTObject(int index);
	
	
}
