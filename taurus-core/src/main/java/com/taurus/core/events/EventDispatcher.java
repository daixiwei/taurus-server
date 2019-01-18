package com.taurus.core.events;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadPoolExecutor;

import com.taurus.core.util.Logger;

/**   
 *  EventDispatcher 事件派发器类，负责进行事件的发送和侦听。
*/
public class EventDispatcher implements IEventDispatcher {
	
	private final Map<String, Set<IEventListener>>	listenersByEvent;
	private ThreadPoolExecutor 						threadPool;
	
	public EventDispatcher(){
		listenersByEvent = new ConcurrentHashMap<String, Set<IEventListener>>();
	}
	
	public EventDispatcher(ThreadPoolExecutor threadPool){
		this();
		this.threadPool = threadPool;
	}
	
	@Override
	public synchronized void addEventListener(String eventName, IEventListener listener) {
		Set<IEventListener> listeners = listenersByEvent.get(eventName);
		if (listeners == null) {
			listeners = new CopyOnWriteArraySet<IEventListener>();
			listenersByEvent.put(eventName, listeners);
		}
		
		listeners.add(listener);
	}
	
	@Override
	public boolean hasEventListener(String eventName) {
		boolean found = false;
		Set<IEventListener> listeners = listenersByEvent.get(eventName);
		if ((listeners != null) && (listeners.size() > 0)) {
			found = true;
		}
		return found;
	}
	
	@Override
	public synchronized void removeEventListener(String eventName, IEventListener listener) {
		Set<IEventListener> listeners = listenersByEvent.get(eventName);
		if (listeners != null)
			listeners.remove(listener);
	}
	
	@Override
	public void dispatchEvent(Event event) {
		Set<IEventListener> listeners = (Set<IEventListener>) listenersByEvent.get(event.getName());
		if ((listeners != null) && (listeners.size() > 0)) {
			for (IEventListener listenerObj : listeners) {
				if(threadPool!=null){
					threadPool.execute(new EventRunner(listenerObj,event));
				}else{
					listenerObj.handleEvent(event);
				}
				
			}
		}
	}

	@Override
	public void removeAllListener() {
		listenersByEvent.clear();
	}
	
	private static final class EventRunner implements Runnable {
		private final IEventListener	listener;
		private final Event			event;
		
		public EventRunner(IEventListener listener, Event event) {
			this.listener = listener;
			this.event = event;
		}
		
		public void run() {
			try {
				listener.handleEvent(event);
			} catch (Exception e) {
				Logger.getLogger(getClass()).warn("Error in event handler: " + e + ", Event: " + event + " Listener: " + listener);
			}
		}
	}
}
