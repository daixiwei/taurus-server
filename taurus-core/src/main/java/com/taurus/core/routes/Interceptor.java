package com.taurus.core.routes;

/**
 * Interceptor
 * @author daixiwei
 *
 */
public interface Interceptor {
	void intercept(Action action,IController controller) throws Exception;
}
