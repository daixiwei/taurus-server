package com.taurus.core.entity;

/**
 * TDataType
 * @author daixiwei	daixiwei15@126.com
 *
 */
public enum TDataType{
	  NULL(0), 
	  BOOL(1), 
	  BYTE(2), 
	  SHORT(3), 
	  INT(4), 
	  LONG(5), 
	  FLOAT(6), 
	  DOUBLE(7), 
	  STRING(8), 
	  BYTE_ARRAY(9), 
	  TARRAY(10), 
	  TOBJECT(11);

	private int typeID;

	private TDataType(int typeID) {
		this.typeID = typeID;
	}

	public static TDataType fromTypeId(int typeId) {
		for (TDataType item : values()) {
			if (item.getTypeID() == typeId) {
				return item;
			}
		}

		throw new IllegalArgumentException("Unknown typeId for MPDataType");
	}

	public int getTypeID() {
		return this.typeID;
	}
}
