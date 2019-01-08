package com.taurus.core.events;

/**
 * 事件派发器通用接口
 * @author daixiwei daixiwei15@126.com
 */
public interface IEventDispatcher {
	
	/**
	 * 添加指定类型的事件监听
	 * @param eventName
	 * @param listener
	 */
	public void addEventListener(String eventName, IEventListener listener);
	
	/**
	 * 检查指定类型事件监听器是否存在
	 * @param eventName
	 * @return
	 */
	public boolean hasEventListener(String eventName);
	
	/**
	 * 删除指定的事件监听
	 * @param eventName
	 * @param listener
	 */
	public void removeEventListener(String eventName, IEventListener listener);
	
	/**
	 * 删除所有的事件监听
	 */
	public void removeAllListener();
	
	/**
	 * 
	 * @param event
	 */
	public void dispatchEvent(IEvent event);
}
