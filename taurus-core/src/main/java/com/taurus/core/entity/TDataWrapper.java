package com.taurus.core.entity;

/**
 * TDataWrapper
 * @author daixiwei	daixiwei15@126.com
 *
 */
public class TDataWrapper {
	private TDataType	typeId;
	private Object		object;
	
	public TDataWrapper(TDataType typeId, Object object) {
		this.typeId = typeId;
		this.object = object;
	}
	
	public TDataType getTypeId() {
		return typeId;
	}
	
	public Object getObject() {
		return object;
	}
	
	public String toString() {
		return object.toString();
	}
}
