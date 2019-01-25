package com.taurus.core.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * object array entity
 * @author daixiwei	daixiwei15@126.com
 *
 */
public class TArray implements ITArray {
	private List<TDataWrapper>	dataHolder;
	private boolean				isChange	= false;

	public TArray() {
		dataHolder = new ArrayList<TDataWrapper>();
	}

	public static ITArray newFromBinaryData(byte[] bytes) {
		return TDataSerializer.me().binary2array(bytes);
	}

	public static ITArray newFromJsonData(String jsonStr) {
		return TDataSerializer.me().json2array(jsonStr);
	}

	public static ITArray newFromResultSet(ResultSet rs) throws SQLException {
		return TDataSerializer.me().resultSet2array(rs);
	}

	public static TArray newInstance() {
		return new TArray();
	}

	public byte[] toBinary() {
		return TDataSerializer.me().array2binary(this);
	}

	public String toJson() {
		return TDataSerializer.me().array2json(flatten());
	}

	public boolean isNull(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);

		if (wrapper == null) {
			return false;
		}
		return wrapper.getTypeId() == TDataType.NULL;
	}

	public TDataWrapper get(int index) {
		return (TDataWrapper) this.dataHolder.get(index);
	}

	public Boolean getBool(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Boolean) wrapper.getObject() : null;
	}

	public Byte getByte(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Byte) wrapper.getObject() : null;
	}

	public Integer getUByte(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? Integer.valueOf(TDataSerializer.me().getUByte(((Byte) wrapper.getObject()).byteValue())) : null;
	}

	public Short getShort(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Short) wrapper.getObject() : null;
	}

	public Integer getInt(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Integer) wrapper.getObject() : null;
	}

	public Long getLong(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Long) wrapper.getObject() : null;
	}

	public Float getFloat(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Float) wrapper.getObject() : null;
	}

	public Double getDouble(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Double) wrapper.getObject() : null;
	}

	public String getString(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (String) wrapper.getObject() : null;
	}

	public byte[] getByteArray(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (byte[]) wrapper.getObject() : null;
	}

	public ITArray getTArray(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (ITArray) wrapper.getObject() : null;
	}

	public ITObject getTObject(int index) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (ITObject) wrapper.getObject() : null;
	}

	public void addBool(boolean value) {
		addObject(Boolean.valueOf(value), TDataType.BOOL);
	}

	public void addByte(byte value) {
		addObject(Byte.valueOf(value), TDataType.BYTE);
	}

	public void addByteArray(byte[] value) {
		addObject(value, TDataType.BYTE_ARRAY);
	}

	public void addDouble(double value) {
		addObject(Double.valueOf(value), TDataType.DOUBLE);
	}

	public void addFloat(float value) {
		addObject(Float.valueOf(value), TDataType.FLOAT);
	}

	public void addInt(int value) {
		addObject(Integer.valueOf(value), TDataType.INT);
	}

	public void addLong(long value) {
		addObject(Long.valueOf(value), TDataType.LONG);
	}

	public void addNull() {
		addObject(null, TDataType.NULL);
	}

	public void addTArray(ITArray value) {
		addObject(value, TDataType.TARRAY);
	}

	public void addTObject(ITObject value) {
		addObject(value, TDataType.TOBJECT);
	}

	public void addShort(short value) {
		addObject(Short.valueOf(value), TDataType.SHORT);
	}

	public void addString(String value) {
		addObject(value, TDataType.STRING);
	}

	public void add(TDataWrapper wrappedObject) {
		this.dataHolder.add(wrappedObject);
	}

	public Object getAt(int index) {
		Object item = null;
		TDataWrapper wrapper = (TDataWrapper) dataHolder.get(index);

		if (wrapper != null)
			item = wrapper.getObject();
		return item;
	}

	public Iterator<TDataWrapper> iterator() {
		return this.dataHolder.iterator();
	}

	public void del(int index) {
		this.dataHolder.remove(index);
	}

	public int size() {
		return this.dataHolder.size();
	}
	
	public void clear() {
		this.dataHolder.clear();
	}
	
	public String toString() {
		return dataHolder.toString();
	}

	private void addObject(Object value, TDataType typeId) {
		dataHolder.add(new TDataWrapper(typeId, value));
		isChange = true;
	}

	public boolean equals(Object obj) {
		boolean isEquals = isChange;
		isChange = false;
		return isEquals;
	}

	private List<Object> flatten() {
		List<Object> list = new ArrayList<Object>();
		TDataSerializer.me().flattenArray(list, this);
		return list;
	}
}
