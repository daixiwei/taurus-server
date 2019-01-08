package com.taurus.core.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.taurus.core.util.StringUtil;

/**
 * object map entity
 * @author daixiwei	daixiwei15@126.com
 *
 */
public class TObject implements ITObject {
	private Map<String, TDataWrapper>	dataHolder;
	private boolean						isChange;
	
	public static ITObject newFromBinaryData(byte[] bytes) {
		return TDataSerializer.getInstance().binary2object(bytes);
	}
	
	public static ITObject newFromJsonData(String jsonStr) {
		return TDataSerializer.getInstance().json2object(jsonStr);
	}
	
	public static ITObject newFromResultSet(ResultSet rs) throws SQLException{
		return TDataSerializer.getInstance().resultSet2object(rs);
	}
	
	public static TObject newInstance() {
		return new TObject();
	}
	
	public TObject() {
		dataHolder = new ConcurrentHashMap<String, TDataWrapper>();
	}
	
	public Iterator<Map.Entry<String, TDataWrapper>> iterator() {
		return this.dataHolder.entrySet().iterator();
	}
	
	public boolean containsKey(String key) {
		return this.dataHolder.containsKey(key);
	}
	
	public boolean remove(String key) {
		return this.dataHolder.remove(key) != null;
	}
	
	public int size() {
		return this.dataHolder.size();
	}
	
	public byte[] toBinary() {
		return TDataSerializer.getInstance().object2binary(this);
	}
	
	public String toJson() {
		return TDataSerializer.getInstance().object2json(flatten());
	}
	
	public boolean isNull(String key) {
		TDataWrapper wrapper = (TDataWrapper) this.dataHolder.get(key);
		
		if (wrapper == null) {
			return false;
		}
		return wrapper.getTypeId() == TDataType.NULL;
	}
	
	public TDataWrapper get(String key) {
		return (TDataWrapper) this.dataHolder.get(key);
	}
	
	public Boolean getBoolean(String key) {
		TDataWrapper o = (TDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Boolean) o.getObject();
	}
	
	public Byte getByte(String key) {
		TDataWrapper o = (TDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Byte) o.getObject();
	}
	
	public byte[] getByteArray(String key) {
		TDataWrapper o = (TDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (byte[]) o.getObject();
	}
	
	public Double getDouble(String key) {
		TDataWrapper o = (TDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Double) o.getObject();
	}
	
	public Float getFloat(String key) {
		TDataWrapper o = (TDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Float) o.getObject();
	}
	
	public Integer getInt(String key) {
		TDataWrapper o = (TDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Integer) o.getObject();
	}
	
	public Set<String> getKeys() {
		return dataHolder.keySet();
	}
	
	public Long getLong(String key) {
		TDataWrapper o = (TDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Long) o.getObject();
	}
	
	public ITArray getTArray(String key) {
		TDataWrapper o = (TDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (ITArray) o.getObject();
	}
	
	public ITObject getTObject(String key) {
		TDataWrapper o = (TDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (ITObject) o.getObject();
	}
	
	public Short getShort(String key) {
		TDataWrapper o = (TDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Short) o.getObject();
	}
	
	public Integer getUByte(String key) {
		TDataWrapper o = (TDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return Integer.valueOf(TDataSerializer.getInstance().getUByte(((Byte) o.getObject()).byteValue()));
	}
	
	public String getString(String key) {
		TDataWrapper o = (TDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (String) o.getObject();
	}
	
	public void putBoolean(String key, boolean value) {
		putObj(key, Boolean.valueOf(value), TDataType.BOOL);
	}
	
	public void putByte(String key, byte value) {
		putObj(key, Byte.valueOf(value), TDataType.BYTE);
	}
	
	public void putByteArray(String key, byte[] value) {
		putObj(key, value, TDataType.BYTE_ARRAY);
	}
	
	public void putDouble(String key, double value) {
		putObj(key, Double.valueOf(value), TDataType.DOUBLE);
	}
	
	public void putFloat(String key, float value) {
		putObj(key, Float.valueOf(value), TDataType.FLOAT);
	}
	
	public void putInt(String key, int value) {
		putObj(key, Integer.valueOf(value), TDataType.INT);
	}
	
	public void putLong(String key, long value) {
		putObj(key, Long.valueOf(value), TDataType.LONG);
	}
	
	public void putNull(String key) {
		this.dataHolder.put(key, new TDataWrapper(TDataType.NULL, null));
	}
	
	public void putTArray(String key, ITArray value) {
		putObj(key, value, TDataType.TARRAY);
	}
	
	public void putTObject(String key, ITObject value) {
		putObj(key, value, TDataType.TOBJECT);
	}
	
	public void putShort(String key, short value) {
		putObj(key, Short.valueOf(value), TDataType.SHORT);
	}
	
	public void putString(String key, String value) {
		if(value==null)value = StringUtil.Empty;
		putObj(key, value, TDataType.STRING);
	}
	
	public void put(String key, TDataWrapper wrappedObject) {
		putObj(key, wrappedObject, null);
	}
	
	public String toString() {
		return dataHolder.toString();
	}
	
	private void putObj(String key, Object value, TDataType typeId) {
		if (key == null) {
			throw new IllegalArgumentException("MPObject requires a non-null key for a 'put' operation!");
		}
		if (key.length() > 255) {
			throw new IllegalArgumentException("MPObject keys must be less than 255 characters!");
		}
		if (value == null) {
			throw new IllegalArgumentException("MPObject requires a non-null value! If you need to add a null use the putNull() method.");
		}
		if ((value instanceof TDataWrapper))
			dataHolder.put(key, (TDataWrapper) value);
		else
			dataHolder.put(key, new TDataWrapper(typeId, value));
		isChange = true;
	}
	
	public boolean equals(Object obj) {
		boolean isEquals = isChange;
		isChange = false;
		return isEquals;
	}
	
	private Map<String, Object> flatten() {
		Map<String, Object> map = new HashMap<String, Object>();
		TDataSerializer.getInstance().flattenObject(map, this);
		return map;
	}

	@Override
	public void del(String key) {
		dataHolder.remove(key);
	}
}
