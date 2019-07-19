package com.taurus.core.events;

import java.util.concurrent.ThreadPoolExecutor;

import com.taurus.core.service.AbstractService;
import com.taurus.core.util.Logger;

/**
 * 事件管理，处理框架内部事件
 * 
 * @author daixiwei daixiwei15@126.com
 *
 */
public final class EventManager extends AbstractService implements IEventDispatcher {
	private final ThreadPoolExecutor	threadPool;
	private final EventDispatcher		dispatcher;
	private final Logger				logger;

	public EventManager(ThreadPoolExecutor threadPool) {
		setName("EventManager");
		logger = Logger.getLogger(EventManager.class);

		this.threadPool = threadPool;
		dispatcher = new EventDispatcher(threadPool);
	}

	public void init(Object o) {
		super.init(o);
		logger.info(this.name + " init.");
	}

	public void destroy(Object o) {
		super.init(o);
		dispatcher.removeAllListener();
		logger.info(this.name + " shut down.");
	}

	public ThreadPoolExecutor getThreadPool() {
		return this.threadPool;
	}

	public void addEventListener(String eventName, IEventListener listener) {
		dispatcher.addEventListener(eventName, listener);
	}

	public boolean hasEventListener(String eventName) {
		return dispatcher.hasEventListener(eventName);
	}

	public void removeEventListener(String eventName, IEventListener listener) {
		dispatcher.removeEventListener(eventName, listener);
	}

	public void dispatchEvent(Event event) {
		dispatcher.dispatchEvent(event);
	}

	@Override
	public void removeAllListener() {
		dispatcher.removeAllListener();
	}

}
