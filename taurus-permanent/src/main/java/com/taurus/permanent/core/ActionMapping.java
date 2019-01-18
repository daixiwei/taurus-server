package com.taurus.permanent.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taurus.core.util.StringUtil;
import com.taurus.permanent.core.Routes.Route;

public class ActionMapping {
	protected static final String SLASH = "/";
	protected Routes routes;
	protected Map<String, Action> mapping = new HashMap<String, Action>(2048, 0.5F);
	
	public ActionMapping(Routes routes) {
		this.routes = routes;
	}
	
	protected List<Routes> getRoutesList() {
		List<Routes> routesList = Routes.getRoutesList();
		List<Routes> ret = new ArrayList<Routes>(routesList.size() + 1);
		ret.add(routes);
		ret.addAll(routesList);
		return ret;
	}
	
	protected void buildActionMapping() {
		mapping.clear();
		for (Routes routes : getRoutesList()) {
			for (Route route : routes.getRouteItemList()) {
				Class<? extends Controller> controllerClass = route.getController().getClass();
				
				Method[] methods = controllerClass.getDeclaredMethods();
				for (Method method : methods) {
					String methodName = method.getName();
					if (!Modifier.isPublic(method.getModifiers()))
						continue ;
					ActionKey ak = method.getAnnotation(ActionKey.class);
					if (ak == null)continue ;
					String controllerKey = route.getControllerKey();
					String actionKey = ak.value().trim();
					if (StringUtil.isEmpty(actionKey))
						throw new IllegalArgumentException(controllerClass.getName() + "." + methodName + "(): The argument of ActionKey can not be blank.");
					Action action = new Action(controllerKey, actionKey, route.getController(), method, methodName);
					
					actionKey =StringUtil.isEmpty(controllerKey) ? actionKey : (controllerKey + SLASH+actionKey);
					if (mapping.put(actionKey, action) != null) {
						throw new RuntimeException(buildMsg(actionKey, controllerClass, method));
					}
				}
			}
		}
		routes.clear();
	}
	
	protected String buildMsg(String actionKey, Class<? extends Controller> controllerClass, Method method) {
		StringBuilder sb = new StringBuilder("The action \"")
			.append(controllerClass.getName()).append(".")
			.append(method.getName()).append("()\" can not be mapped, ")
			.append("actionKey \"").append(actionKey).append("\" is already in use.");
		
		String msg = sb.toString();
		System.err.println("\nException: " + msg);
		return msg;
	}
	
	/**
	 * Support four types of url
	 */
	public Action getAction(String url) {
		Action action = mapping.get(url);
		if (action != null) {
			return action;
		}
		return action;
	}
	
	public List<String> getAllActionKeys() {
		List<String> allActionKeys = new ArrayList<String>(mapping.keySet());
		Collections.sort(allActionKeys);
		return allActionKeys;
	}
}
