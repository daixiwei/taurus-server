package com.taurus.web;

import java.lang.reflect.Method;

/**
 * Action.
 * @author daixiwei
 *
 */
public class Action {
	private final Class<? extends Controller> controllerClass;
	private final String controllerKey;
	private final String actionKey;
	private final Method method;
	private final String methodName;
	private final Interceptor interceptor;
	
	public Action(String controllerKey, String actionKey, Class<? extends Controller> controller, Method method, String methodName,Interceptor interceptor) {
		this.controllerKey = controllerKey;
		this.actionKey = actionKey;
		this.controllerClass = controller;
		this.method = method;
		this.methodName = methodName;
		this.interceptor = interceptor;
	}
	
	public Class<? extends Controller> getControllerClass() {
		return controllerClass;
	}
	
	public String getControllerKey() {
		return controllerKey;
	}
	
	public String getActionKey() {
		return actionKey;
	}
	
	public Method getMethod() {
		return method;
	}

	
	public String getMethodName() {
		return methodName;
	}
	
	public Interceptor getInterceptor() {
		return interceptor;
	}
	
}
