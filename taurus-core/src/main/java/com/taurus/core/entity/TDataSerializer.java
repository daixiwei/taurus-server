package com.taurus.core.entity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.taurus.core.util.Logger;
import com.taurus.core.util.StringUtil;
import com.taurus.core.util.json.JSONUtils;

/**
 * TDataSerializer
 * @author daixiwei	daixiwei15@126.com
 *
 */
public class TDataSerializer {
	private static TDataSerializer	instance			= new TDataSerializer();
	private static int				BUFFER_CHUNK_SIZE	= 512;

	private static Logger			logger				= Logger.getLogger(TDataSerializer.class);

	public static TDataSerializer getInstance() {
		return instance;
	}

	private TDataSerializer() {
	}

	public int getUByte(byte b) {
		return 0xFF & b;
	}

	public ITArray binary2array(byte[] data) {
		if (data.length < 3) {
			throw new IllegalStateException("Can't decode an TArray. Byte data is insufficient. Size: " + data.length + " bytes");
		}
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
		buffer.flip();
		return decodeTArray(buffer);
	}

	private ITArray decodeTArray(ByteBuffer buffer) {
		ITArray array = TArray.newInstance();
		byte headerBuffer = buffer.get();
		if (headerBuffer != TDataType.TARRAY.getTypeID()) {
			throw new IllegalStateException("Invalid DataType. Expected: " + TDataType.TARRAY.getTypeID() + ", found: " + headerBuffer);
		}
		short size = buffer.getShort();
		if (size < 0) {
			throw new IllegalStateException("Can't decode TArray. Size is negative = " + size);
		}

		try {
			for (int i = 0; i < size; i++) {
				TDataWrapper decodedObject = decodeObject(buffer);

				if (decodedObject != null)
					array.add(decodedObject);
				else {
					throw new IllegalStateException("TArray item is null! index: " + i);
				}
			}
		} catch (RuntimeException codecError) {
			throw new IllegalArgumentException(codecError.getMessage());
		}
		return array;
	}

	public ITObject binary2object(byte[] data) {
		if (data.length < 3) {
			throw new IllegalStateException("Can't decode an TObject. Byte data is insufficient. Size: " + data.length + " bytes");
		}
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
		buffer.flip();
		return decodeTObject(buffer);
	}

	private ITObject decodeTObject(ByteBuffer buffer) {
		ITObject tobj = TObject.newInstance();
		byte headerBuffer = buffer.get();
		if (headerBuffer != TDataType.TOBJECT.getTypeID()) {
			throw new IllegalStateException("Invalid DataType. Expected: " + TDataType.TOBJECT.getTypeID() + ", found: " + headerBuffer);
		}
		short size = buffer.getShort();
		if (size < 0) {
			throw new IllegalStateException("Can't decode TObject. Size is negative = " + size);
		}

		try {
			for (int i = 0; i < size; i++) {
				short keySize = buffer.getShort();
				if ((keySize < 0) || (keySize > 255)) {
					throw new IllegalStateException("Invalid TObject key length. Found = " + keySize);
				}
				byte[] keyData = new byte[keySize];
				buffer.get(keyData, 0, keyData.length);
				String key = new String(keyData);
				TDataWrapper decodedObject = decodeObject(buffer);
				if (decodedObject != null)
					tobj.put(key, decodedObject);
				else {
					throw new IllegalStateException("Could not decode value for key: " + keyData);
				}
			}
		} catch (RuntimeException codecError) {
			throw new IllegalArgumentException(codecError.getMessage());
		}
		return tobj;
	}

	/**
	 * 
	 * @param jsonStr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ITObject json2object(String jsonStr) {
		if (jsonStr.length() < 2) {
			throw new IllegalStateException("Can't decode TObject. JSON String is too short. Len: " + jsonStr.length());
		}

		Object o = JSONUtils.parse(jsonStr);

		return decodeTObject((HashMap<String, Object>) o);
	}

	/**
	 * 
	 * @param jsonStr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ITArray json2array(String jsonStr) {
		if (jsonStr.length() < 2) {
			throw new IllegalStateException("Can't decode TObject. JSON String is too short. Len: " + jsonStr.length());
		}
		Object jsa = JSONUtils.parse(jsonStr);
		return decodeTArray((List<Object>) jsa);
	}

	private ITArray decodeTArray(List<Object> jsa) {
		ITArray array = TArrayLite.newInstance();

		for (Object value : jsa) {
			TDataWrapper decodedObject = decodeJsonObject(value);
			if (decodedObject != null)
				array.add(decodedObject);
			else {
				throw new IllegalStateException("(json2sfarray) Could not decode value for object: " + value);
			}
		}
		return array;
	}

	private ITObject decodeTObject(HashMap<String, Object> jso) {

		ITObject object = TObjectLite.newInstance();
		for (Entry<String, Object> entry : jso.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			TDataWrapper decodedObject = decodeJsonObject(value);
			if (decodedObject != null)
				object.put(key, decodedObject);
			else {
				throw new IllegalStateException("(json2tobj) Could not decode value for key: " + key);
			}
		}
		return object;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private TDataWrapper decodeJsonObject(Object o) {

		if ((o instanceof Integer)) {
			return new TDataWrapper(TDataType.INT, o);
		}
		if ((o instanceof Long)) {
			return new TDataWrapper(TDataType.LONG, o);
		}
		if ((o instanceof Double)) {
			return new TDataWrapper(TDataType.DOUBLE, o);
		}
		if ((o instanceof Boolean)) {
			return new TDataWrapper(TDataType.BOOL, o);
		}
		if ((o instanceof String)) {
			return new TDataWrapper(TDataType.STRING, o);
		}
		if ((o instanceof HashMap)) {
			HashMap jso = (HashMap) o;
//			if (jso.size() == 0) {
//				return new TDataWrapper(TDataType.NULL, null);
//			}
			return new TDataWrapper(TDataType.TOBJECT, decodeTObject(jso));
		}
		if ((o instanceof List)) {
			return new TDataWrapper(TDataType.TARRAY, decodeTArray((List) o));
		}

		throw new IllegalArgumentException(String.format("Unknown DataType! %s", o == null ? "null" : o.getClass()));
	}

	public TObject resultSet2object(ResultSet rset) throws SQLException {
		ResultSetMetaData metaData = rset.getMetaData();
		TObject sfso = new TObject();

		if (rset.isBeforeFirst()) {
			rset.next();
		}
		for (int col = 1; col <= metaData.getColumnCount(); col++) {
			String colName = metaData.getColumnLabel(col);
			int type = metaData.getColumnType(col);
			
			Object rawDataObj = rset.getObject(col);
			if (rawDataObj == null) {
				continue;
			}
			
			if (type == Types.NULL) {
				sfso.putNull(colName);
			} else if (type == Types.BOOLEAN) {
				sfso.putBoolean(colName, rset.getBoolean(col));
			} else if (type == Types.DATE) {
				sfso.putLong(colName, rset.getDate(col).getTime());
			} else if ((type == Types.FLOAT) || (type == Types.DOUBLE) || (type == Types.DECIMAL) || (type == Types.REAL)) {
				sfso.putDouble(colName, rset.getDouble(col));
			} else if ((type == Types.INTEGER) || (type == Types.TINYINT) || (type == Types.SMALLINT)) {
				sfso.putInt(colName, rset.getInt(col));
			} else if ((type == Types.CHAR) || (type == Types.VARCHAR) || (type == Types.LONGVARCHAR)) {
				sfso.putString(colName, rset.getString(col));
			} else if ((type == Types.NCHAR) || (type == Types.NVARCHAR) || (type == Types.LONGNVARCHAR)) {
				sfso.putString(colName, rset.getNString(col));
			} else if (type == Types.TIMESTAMP) {
				sfso.putLong(colName, rset.getTimestamp(col).getTime());
			} else if (type == Types.BIGINT) {
				sfso.putLong(colName, rset.getLong(col));
			} else if (type == Types.LONGVARBINARY) {
				byte[] binData = getBlobData(colName, rset.getBinaryStream(col));

				if (binData != null) {
					sfso.putByteArray(colName, binData);
				}
			} else if (type == Types.BLOB) {
				Blob blob = rset.getBlob(col);
				sfso.putByteArray(colName, blob.getBytes(0L, (int) blob.length()));
			} else {
				logger.info("Skipping Unsupported SQL TYPE: " + type + ", Column:" + colName);
			}
		}

		return sfso;
	}

	private byte[] getBlobData(String colName, InputStream stream) {
		BufferedInputStream bis = new BufferedInputStream(stream);
		byte[] bytes = (byte[]) null;
		try {
			bytes = new byte[bis.available()];
			bis.read(bytes);
		} catch (IOException ex) {
			logger.warn("SFSObject serialize error. Failed reading BLOB data for column: " + colName);
		} finally {
			try {
				bis.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		return bytes;
	}

	public TArray resultSet2array(ResultSet rset) throws SQLException {
		TArray array = new TArray();

		while (rset.next()) {
			array.addTObject(resultSet2object(rset));
		}

		return array;
	}

	public byte[] object2binary(ITObject object) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_SIZE);
		buffer.put((byte) TDataType.TOBJECT.getTypeID());
		buffer.putShort((short) object.size());
		return obj2bin(object, buffer);
	}

	private byte[] obj2bin(ITObject object, ByteBuffer buffer) {
		Set<String> keys = object.getKeys();
		for (String key : keys) {
			TDataWrapper wrapper = object.get(key);
			buffer = encodeTObjectKey(buffer, key);
			buffer = encodeObject(buffer, wrapper);
		}

		int pos = buffer.position();
		byte[] result = new byte[pos];
		buffer.flip();
		buffer.get(result, 0, pos);
		return result;
	}

	public byte[] array2binary(ITArray array) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_SIZE);
		buffer.put((byte) TDataType.TARRAY.getTypeID());
		buffer.putShort((short) array.size());
		return arr2bin(array, buffer);
	}

	private byte[] arr2bin(ITArray array, ByteBuffer buffer) {
		Iterator<TDataWrapper> iter = array.iterator();
		while (iter.hasNext()) {
			TDataWrapper wrapper = (TDataWrapper) iter.next();
			buffer = encodeObject(buffer, wrapper);
		}

		int pos = buffer.position();
		byte[] result = new byte[pos];
		buffer.flip();
		buffer.get(result, 0, pos);
		return result;
	}

	public String object2json(Map<String, Object> map) {
		return JSONUtils.toJSONString(map);
	}

	public String array2json(List<Object> array) {
		return JSONUtils.toJSONString(array);
	}

	public void flattenObject(Map<String, Object> map, ITObject obj) {
		for (Iterator<Entry<String, TDataWrapper>> it = obj.iterator(); it.hasNext();) {
			Entry<String, TDataWrapper> entry = it.next();
			String key = (String) entry.getKey();
			TDataWrapper value = (TDataWrapper) entry.getValue();
			if (value.getTypeId() == TDataType.TOBJECT) {
				Map<String, Object> newMap = new HashMap<String, Object>();
				map.put(key, newMap);
				flattenObject(newMap, (ITObject) value.getObject());
			} else if (value.getTypeId() == TDataType.TARRAY) {
				List<Object> newList = new ArrayList<Object>();
				map.put(key, newList);
				flattenArray(newList, (ITArray) value.getObject());
			} else {
				map.put(key, value.getObject());
			}
		}
	}

	public void flattenArray(List<Object> array, ITArray tarray) {
		for (Iterator<TDataWrapper> it = tarray.iterator(); it.hasNext();) {
			TDataWrapper value = (TDataWrapper) it.next();
			if (value.getTypeId() == TDataType.TOBJECT) {
				Map<String, Object> newMap = new HashMap<String, Object>();
				array.add(newMap);
				flattenObject(newMap, (TObject) value.getObject());
			} else if (value.getTypeId() == TDataType.TARRAY) {
				List<Object> newList = new ArrayList<Object>();
				array.add(newList);
				flattenArray(newList, (ITArray) value.getObject());
			} else {
				array.add(value.getObject());
			}
		}
	}

	private TDataWrapper decodeObject(ByteBuffer buffer) throws RuntimeException {
		TDataWrapper decodedObject = null;
		byte headerByte = buffer.get();

		if (headerByte == TDataType.NULL.getTypeID()) {
			decodedObject = binDecode_NULL(buffer);
		} else if (headerByte == TDataType.BOOL.getTypeID()) {
			decodedObject = binDecode_BOOL(buffer);
		} else if (headerByte == TDataType.BYTE.getTypeID()) {
			decodedObject = binDecode_BYTE(buffer);
		} else if (headerByte == TDataType.BYTE_ARRAY.getTypeID()) {
			decodedObject = binDecode_BYTE_ARRAY(buffer);
		} else if (headerByte == TDataType.SHORT.getTypeID()) {
			decodedObject = binDecode_SHORT(buffer);
		} else if (headerByte == TDataType.INT.getTypeID()) {
			decodedObject = binDecode_INT(buffer);
		} else if (headerByte == TDataType.LONG.getTypeID()) {
			decodedObject = binDecode_LONG(buffer);
		} else if (headerByte == TDataType.FLOAT.getTypeID()) {
			decodedObject = binDecode_FLOAT(buffer);
		} else if (headerByte == TDataType.DOUBLE.getTypeID()) {
			decodedObject = binDecode_DOUBLE(buffer);
		} else if (headerByte == TDataType.STRING.getTypeID()) {
			decodedObject = binDecode_STRING(buffer);
		} else if (headerByte == TDataType.TARRAY.getTypeID()) {
			buffer.position(buffer.position() - 1);
			decodedObject = new TDataWrapper(TDataType.TARRAY, decodeTArray(buffer));
		} else if (headerByte == TDataType.TOBJECT.getTypeID()) {
			buffer.position(buffer.position() - 1);
			ITObject tobj = decodeTObject(buffer);
			TDataType type = TDataType.TOBJECT;
			Object finalTObj = tobj;
			decodedObject = new TDataWrapper(type, finalTObj);
		} else {
			throw new RuntimeException("Unknow DataType ID: " + headerByte);
		}
		return decodedObject;
	}

	private ByteBuffer encodeObject(ByteBuffer buffer, TDataWrapper wrapper) {
		TDataType typeId = wrapper.getTypeId();
		Object object = wrapper.getObject();

		switch (typeId) {
			case NULL:
				buffer = binEncode_NULL(buffer);
				break;
			case BOOL:
				buffer = binEncode_BOOL(buffer, (Boolean) object);
				break;
			case BYTE:
				buffer = binEncode_BYTE(buffer, (Byte) object);
				break;
			case SHORT:
				buffer = binEncode_SHORT(buffer, (Short) object);
				break;
			case INT:
				buffer = binEncode_INT(buffer, (Integer) object);
				break;
			case LONG:
				buffer = binEncode_LONG(buffer, (Long) object);
				break;
			case FLOAT:
				buffer = binEncode_FLOAT(buffer, (Float) object);
				break;
			case DOUBLE:
				buffer = binEncode_DOUBLE(buffer, (Double) object);
				break;
			case STRING:
				buffer = binEncode_STRING(buffer, (String) object);
				break;
			case BYTE_ARRAY:
				buffer = binEncode_BYTE_ARRAY(buffer, (byte[]) object);
				break;
			case TARRAY:
				buffer = addData(buffer, array2binary((TArray) object));
				break;
			case TOBJECT:
				buffer = addData(buffer, object2binary((TObject) object));
				break;
			default:
				throw new IllegalArgumentException("Unrecognized type in TObject serialization: " + typeId);
		}

		return buffer;
	}

	private TDataWrapper binDecode_NULL(ByteBuffer buffer) {
		return new TDataWrapper(TDataType.NULL, null);
	}

	private TDataWrapper binDecode_BOOL(ByteBuffer buffer) throws RuntimeException {
		byte boolByte = buffer.get();
		Boolean bool = null;
		if (boolByte == 0)
			bool = new Boolean(false);
		else if (boolByte == 1)
			bool = new Boolean(true);
		else {
			throw new RuntimeException("Error decoding Bool type. Illegal value: " + bool);
		}
		return new TDataWrapper(TDataType.BOOL, bool);
	}

	private TDataWrapper binDecode_BYTE(ByteBuffer buffer) {
		byte boolByte = buffer.get();
		return new TDataWrapper(TDataType.BYTE, Byte.valueOf(boolByte));
	}

	private TDataWrapper binDecode_SHORT(ByteBuffer buffer) {
		short shortValue = buffer.getShort();
		return new TDataWrapper(TDataType.SHORT, Short.valueOf(shortValue));
	}

	private TDataWrapper binDecode_INT(ByteBuffer buffer) {
		int intValue = buffer.getInt();
		return new TDataWrapper(TDataType.INT, Integer.valueOf(intValue));
	}

	private TDataWrapper binDecode_LONG(ByteBuffer buffer) {
		long longValue = buffer.getLong();
		return new TDataWrapper(TDataType.LONG, Long.valueOf(longValue));
	}

	private TDataWrapper binDecode_FLOAT(ByteBuffer buffer) {
		float floatValue = buffer.getFloat();
		return new TDataWrapper(TDataType.FLOAT, Float.valueOf(floatValue));
	}

	private TDataWrapper binDecode_DOUBLE(ByteBuffer buffer) {
		double doubleValue = buffer.getDouble();
		return new TDataWrapper(TDataType.DOUBLE, Double.valueOf(doubleValue));
	}

	private TDataWrapper binDecode_STRING(ByteBuffer buffer) throws RuntimeException {
		int strLen = buffer.getInt();
		if (strLen < 0) {
			throw new RuntimeException("Error decoding String. Negative size: " + strLen);
		}

		byte[] strData = new byte[strLen];
		buffer.get(strData, 0, strLen);
		String decodedString = new String(strData);
		return new TDataWrapper(TDataType.STRING, decodedString);
	}

	private TDataWrapper binDecode_BYTE_ARRAY(ByteBuffer buffer) throws RuntimeException {
		int arraySize = buffer.getInt();
		if (arraySize < 0) {
			throw new RuntimeException("Error decoding typed array size. Negative size: " + arraySize);
		}

		byte[] byteData = new byte[arraySize];
		buffer.get(byteData, 0, arraySize);
		return new TDataWrapper(TDataType.BYTE_ARRAY, byteData);
	}

	private ByteBuffer binEncode_NULL(ByteBuffer buffer) {
		return addData(buffer, new byte[1]);
	}

	private ByteBuffer binEncode_BOOL(ByteBuffer buffer, Boolean value) {
		byte[] data = new byte[2];
		data[0] = (byte) TDataType.BOOL.getTypeID();
		data[1] = (byte) (value.booleanValue() ? 1 : 0);
		return addData(buffer, data);
	}

	private ByteBuffer binEncode_BYTE(ByteBuffer buffer, Byte value) {
		byte[] data = new byte[2];
		data[0] = (byte) TDataType.BYTE.getTypeID();
		data[1] = value.byteValue();
		return addData(buffer, data);
	}

	private ByteBuffer binEncode_SHORT(ByteBuffer buffer, Short value) {
		ByteBuffer buf = ByteBuffer.allocate(3);
		buf.put((byte) TDataType.SHORT.getTypeID());
		buf.putShort(value.shortValue());
		return addData(buffer, buf.array());
	}

	private ByteBuffer binEncode_INT(ByteBuffer buffer, Integer value) {
		ByteBuffer buf = ByteBuffer.allocate(5);
		buf.put((byte) TDataType.INT.getTypeID());
		buf.putInt(value.intValue());
		return addData(buffer, buf.array());
	}

	private ByteBuffer binEncode_LONG(ByteBuffer buffer, Long value) {
		ByteBuffer buf = ByteBuffer.allocate(9);
		buf.put((byte) TDataType.LONG.getTypeID());
		buf.putLong(value.longValue());
		return addData(buffer, buf.array());
	}

	private ByteBuffer binEncode_FLOAT(ByteBuffer buffer, Float value) {
		ByteBuffer buf = ByteBuffer.allocate(5);
		buf.put((byte) TDataType.FLOAT.getTypeID());
		buf.putFloat(value.floatValue());
		return addData(buffer, buf.array());
	}

	private ByteBuffer binEncode_DOUBLE(ByteBuffer buffer, Double value) {
		ByteBuffer buf = ByteBuffer.allocate(9);
		buf.put((byte) TDataType.DOUBLE.getTypeID());
		buf.putDouble(value.doubleValue());
		return addData(buffer, buf.array());
	}

	private ByteBuffer binEncode_STRING(ByteBuffer buffer, String value) {
		if (StringUtil.isEmpty(value)) {
			ByteBuffer buf = ByteBuffer.allocate(5);
			buf.put((byte) TDataType.STRING.getTypeID());
			buf.putInt(0);
			return addData(buffer, buf.array());
		}
		byte[] stringBytes = value.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(5 + stringBytes.length);
		buf.put((byte) TDataType.STRING.getTypeID());
		buf.putInt(stringBytes.length);
		buf.put(stringBytes);
		return addData(buffer, buf.array());
	}

	private ByteBuffer binEncode_BYTE_ARRAY(ByteBuffer buffer, byte[] value) {
		ByteBuffer buf = ByteBuffer.allocate(5 + value.length);
		buf.put((byte) TDataType.BYTE_ARRAY.getTypeID());
		buf.putInt(value.length);
		buf.put(value);
		return addData(buffer, buf.array());
	}

	private ByteBuffer encodeTObjectKey(ByteBuffer buffer, String value) {
		ByteBuffer buf = ByteBuffer.allocate(2 + value.length());
		buf.putShort((short) value.length());
		buf.put(value.getBytes());
		return addData(buffer, buf.array());
	}

	private ByteBuffer addData(ByteBuffer buffer, byte[] newData) {
		if (buffer.remaining() < newData.length) {
			int newSize = BUFFER_CHUNK_SIZE;
			if (newSize < newData.length) {
				newSize = newData.length;
			}
			ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + newSize);
			buffer.flip();
			newBuffer.put(buffer);
			buffer = newBuffer;
		}
		buffer.put(newData);
		return buffer;
	}

}
