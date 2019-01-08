package com.taurus.web;

import com.jfinal.config.Routes;
import com.jfinal.core.Controller;

/**
 * TaurusRoutes
 * @author daixiwei
 *
 */
public class TaurusRoutes extends Routes{
	private static final String PATH = "/taurus-web";
	@Override
	public void config() {
		this.addInterceptor(new TaurusInterceptor());
	}

	public Routes add(String controllerKey, Class<? extends Controller> controllerClass, String viewPath) {
		if(!TaurusController.class.isAssignableFrom(controllerClass)) {
			throw new RuntimeException("You controller does not extends TaurusController! "+controllerClass);
		}
		controllerKey = PATH + controllerKey;
		return super.add(controllerKey, controllerClass, viewPath);
	}
}
