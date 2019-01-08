package com.taurus.core.util.task;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务数据基类
 * @author daixiwei daixiwei15@126.com
 *
 */
public class Task {
	private Object				id;
	private Map<Object, Object>	parameters;
	private volatile boolean	active	= true;
	
	public Task() {
		this.parameters = new HashMap<Object, Object>();
	}
	
	public Task(Object id) {
		this();
		this.id = id;
	}
	
	public Task(Object id, Map<Object, Object> mapObj) {
		this.id = id;
		this.parameters = mapObj;
	}
	
	public Object getId() {
		return this.id;
	}
	
	public Map<Object, Object> getParameters() {
		return this.parameters;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
}
