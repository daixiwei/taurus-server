package com.taurus.core.entity;

/**
 * TArrayLite
 * @author daixiwei	daixiwei15@126.com
 *
 */
public class TArrayLite extends TArray {
	public static TArrayLite newInstance() {
		return new TArrayLite();
	}

	public Byte getByte(int index) {
		Integer i = super.getInt(index);

		return i != null ? Byte.valueOf(i.byteValue()) : null;
	}

	public Short getShort(int index) {
		Integer i = super.getInt(index);
		return i != null ? Short.valueOf(i.shortValue()) : null;
	}

	public Float getFloat(int index) {
		Double d = super.getDouble(index);
		return d != null ? Float.valueOf(d.floatValue()) : null;
	}
}
