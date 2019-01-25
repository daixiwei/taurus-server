package com.taurus.core.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event.
 * @author daixiwei daixiwei15@126.com
 */
public class Event {
	
	protected Object				target;
	protected String				name;
	protected Map<String, Object>	paramMap;
	
	public Event(String name) {
		this.name = name;
	}
	
	public Event(String name, Object source) {
		this.target = source;
		this.name = name;
	}
	
	public Object getTarget() {
		return this.target;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setTarget(Object target) {
		this.target = target;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Object getParameter(String key) {
		Object tem = null;
		
		if (paramMap != null) {
			tem = paramMap.get(key);
		}
		return tem;
	}
	
	public void setParameter(String key, Object value) {
		if (paramMap == null) {
			paramMap = new ConcurrentHashMap<String, Object>();
		}
		paramMap.put(key, value);
	}
	
	public String toString() {
		return "Event { Name:" + name + ", Source: " + target + ", Params: " + paramMap + " }";
	}
}
