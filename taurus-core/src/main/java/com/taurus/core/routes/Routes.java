package com.taurus.core.routes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.taurus.core.util.StringUtil;

/**
 * Routes
 * @author daixiwei
 *
 */
public abstract class Routes {
	
	private static List<Routes> routesList = new ArrayList<Routes>();
	private static Set<String> controllerKeySet = new HashSet<String>();
	
	private List<Route> routeItemList = new ArrayList<Route>();
	private Interceptor interceptor;
	private boolean addSlash = true;
	
	/**
	 * Implement this method to add route, add interceptor and set baseViewPath
	 */
	public abstract void config();
	
	/**
	 * 设置拦截器
	 * @param interceptor
	 */
	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	/**
	 * Add Routes
	 */
	public Routes add(Routes routes) {
		routes.config();
		routesList.add(routes);
		return this;
	}
	
	/**
	 * Add route
	 * @param controllerKey A key can find controller
	 * @param controller Controller Class
	 */
	public Routes add(String controllerKey, Class<? extends IController> controller) {
		routeItemList.add(new Route(controllerKey, controller,this.addSlash));
		return this;
	}
	
	public static List<Routes> getRoutesList() {
		return routesList;
	}
	
	public List<Route> getRouteItemList() {
		return routeItemList;
	}
	
	public Interceptor getInterceptor() {
		return interceptor;
	}
	
	public void setAddSlash(boolean add) {
		this.addSlash =add;
	}
	
	
	public static class Route {
		private String controllerKey;
		private Class<? extends IController> controller;
		
		public Route(String controllerKey, Class<? extends IController> controller,boolean addSlash) {
			if (StringUtil.isEmpty(controllerKey)) {
				throw new IllegalArgumentException("controllerKey can not be blank");
			}
			if (controller == null) {
				throw new IllegalArgumentException("controllerClass can not be null");
			}
			
			this.controllerKey = processControllerKey(controllerKey,addSlash);
			this.controller = controller;
		}
		
		private String processControllerKey(String controllerKey,boolean addSlash) {
			controllerKey = controllerKey.trim();
			if(addSlash) {
				if (!controllerKey.startsWith("/")) {
					controllerKey = "/" + controllerKey;
				}
			}
			if (controllerKeySet.contains(controllerKey)) {
				throw new IllegalArgumentException("controllerKey already exists: " + controllerKey);
			}
			controllerKeySet.add(controllerKey);
			return controllerKey;
		}
		
		
		
		public String getControllerKey() {
			return controllerKey;
		}
		
		public Class<? extends IController> getController() {
			return controller;
		}
		
	}
}
