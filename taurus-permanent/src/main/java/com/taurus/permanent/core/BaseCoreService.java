package com.taurus.permanent.core;

import java.util.concurrent.atomic.AtomicInteger;

import com.taurus.core.events.EventDispatcher;
import com.taurus.core.service.IService;

/**
 * BaseCoreService
 * @author daixiwei daixiwei15@126.com
 */
public abstract class BaseCoreService extends EventDispatcher implements IService {
	private static final AtomicInteger serviceId = new AtomicInteger(0);
	private static final String DEFAULT_NAME = "Service-";
	protected String name;
	protected volatile boolean active = false;

	public void init(Object o) {
		name = getServiceId();
		active = true;
	}

	public void destroy(Object o) {
		active = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public String toString() {
		return "[Core Service]: " + name + ", State: " + (isActive() ? "active" : "not active");
	}

	protected static String getServiceId() {
		return DEFAULT_NAME + serviceId.getAndIncrement();
	}
}
