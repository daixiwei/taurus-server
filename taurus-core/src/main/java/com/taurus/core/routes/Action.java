package com.taurus.core.routes;

import java.lang.reflect.Method;

/**
 * Action.
 * @author daixiwei
 *
 */
public class Action {
	private final Class<? extends IController> controllerClass;
	private final String controllerKey;
	private final String actionKey;
	private final Method method;
	private final String methodName;
	private final Interceptor interceptor;
	private final ActionKey actionKeyObj;
	
	public Action(String controllerKey, String actionKey, Class<? extends IController> controller, Method method, String methodName,Interceptor interceptor,ActionKey actionKeyObj) {
		this.controllerKey = controllerKey;
		this.actionKey = actionKey;
		this.controllerClass = controller;
		this.method = method;
		this.methodName = methodName;
		this.interceptor = interceptor;
		this.actionKeyObj = actionKeyObj;
	}
	
	public Class<? extends IController> getControllerClass() {
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
	
	public ActionKey getActionKeyObj() {
		return actionKeyObj;
	}
	
	@Override
	public String toString() {
		return actionKey;
	}
}
