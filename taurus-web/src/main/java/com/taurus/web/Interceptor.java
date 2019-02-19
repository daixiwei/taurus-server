package com.taurus.web;

/**
 * Interceptor
 * @author daixiwei
 *
 */
public interface Interceptor {
	boolean intercept(Action action,Controller controller);
}
