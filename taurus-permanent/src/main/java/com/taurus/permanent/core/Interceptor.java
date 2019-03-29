package com.taurus.permanent.core;

/**
 * Interceptor
 * @author daixiwei
 *
 */
public interface Interceptor {
	void intercept(Action action,Controller controller) throws Exception;
}
