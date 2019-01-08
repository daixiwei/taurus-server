package com.taurus.core.events;

/**
 * 事件数据通用接口
 * @author daixiwei daixiwei15@126.com
 */
public interface IEvent {
	
	/**
	 * 
	 * @return
	 */
	public Object getTarget();
	
	/**
	 * 
	 * @param target
	 */
	public void setTarget(Object target);
	
	/**
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * 
	 * @param param
	 */
	public void setName(String param);
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public Object getParameter(String key);
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void setParameter(String key, Object value);
}
