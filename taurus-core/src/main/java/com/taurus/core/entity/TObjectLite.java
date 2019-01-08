package com.taurus.core.entity;

/**
 * TObjectLite
 * @author daixiwei	daixiwei15@126.com
 *
 */
public final class TObjectLite extends TObject {
	public static TObject newInstance() {
		return new TObjectLite();
	}
	
	@Override
	public Byte getByte(String key) {
		Integer i = super.getInt(key);
		
		return i != null ? Byte.valueOf(i.byteValue()) : null;
	}
	
	@Override
	public Short getShort(String key) {
		Integer i = super.getInt(key);
		
		return i != null ? Short.valueOf(i.shortValue()) : null;
	}
	
	@Override
	public Float getFloat(String key) {
		Double d = super.getDouble(key);
		
		return d != null ? Float.valueOf(d.floatValue()) : null;
	}
}
