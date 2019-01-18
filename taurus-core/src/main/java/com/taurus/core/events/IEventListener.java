package com.taurus.core.events;

/**
 * 事件监听接口
 * @author daixiwei daixiwei15@126.com
 */
public interface IEventListener {
	
	/**
	 * 
	 * @param event
	 */
	public void handleEvent(Event event);
}
