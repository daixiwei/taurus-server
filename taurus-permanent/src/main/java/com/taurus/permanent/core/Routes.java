package com.taurus.permanent.core;

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
	
	private String baseViewPath = null;
	private List<Route> routeItemList = new ArrayList<Route>();
	
	private boolean clearAfterMapping = true;
	
	/**
	 * Implement this method to add route, add interceptor and set baseViewPath
	 */
	public abstract void config();
	
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
	 * @param viewPath View path for this Controller
	 */
	public Routes add(String controllerKey, Controller controller) {
		routeItemList.add(new Route(controllerKey, controller));
		return this;
	}

	

	
	public String getBaseViewPath() {
		return baseViewPath;
	}
	
	public static List<Routes> getRoutesList() {
		return routesList;
	}
	
	public List<Route> getRouteItemList() {
		return routeItemList;
	}
	
	/**
	 * 配置是否在路由映射完成之后清除内部数据，以回收内存，默认值为 true.
	 * 
	 * 设置为 false 通常用于在系统启动之后，仍然要使用 Routes 的场景，
	 * 例如希望拿到 Routes 生成用于控制访问权限的数据
	 */
	public void setClearAfterMapping(boolean clearAfterMapping) {
		this.clearAfterMapping = clearAfterMapping;
	}
	
	public void clear() {
		if (clearAfterMapping) {
			routesList = null;
			controllerKeySet = null;
			baseViewPath = null;
			routeItemList = null;
		}
	}
	
	public static class Route {
		private String controllerKey;
		private Controller controller;
		
		public Route(String controllerKey, Controller controller) {
			if (StringUtil.isEmpty(controllerKey)) {
				throw new IllegalArgumentException("controllerKey can not be blank");
			}
			if (controller == null) {
				throw new IllegalArgumentException("controllerClass can not be null");
			}
			
			this.controllerKey = processControllerKey(controllerKey);
			this.controller = controller;
		}
		
		private String processControllerKey(String controllerKey) {
			controllerKey = controllerKey.trim();
			if (controllerKeySet.contains(controllerKey)) {
				throw new IllegalArgumentException("controllerKey already exists: " + controllerKey);
			}
			controllerKeySet.add(controllerKey);
			return controllerKey;
		}
		
		
		
		public String getControllerKey() {
			return controllerKey;
		}
		
		public Controller getController() {
			return controller;
		}
		
	}
}
