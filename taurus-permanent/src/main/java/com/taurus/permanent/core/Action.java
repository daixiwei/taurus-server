package com.taurus.permanent.core;

import java.lang.reflect.Method;

public class Action {
	private final Controller controller;
	private final String controllerKey;
	private final String actionKey;
	private final Method method;
	private final String methodName;
	
	
	public Action(String controllerKey, String actionKey, Controller controller, Method method, String methodName) {
		this.controllerKey = controllerKey;
		this.actionKey = actionKey;
		this.controller = controller;
		this.method = method;
		this.methodName = methodName;
		
	}
	
	public Controller getController() {
		return controller;
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
	
}
