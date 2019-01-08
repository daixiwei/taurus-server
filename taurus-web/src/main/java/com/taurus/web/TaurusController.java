package com.taurus.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.core.Controller;
import com.taurus.core.entity.ITObject;
import com.taurus.core.util.Logger;

/**
 * TaurusController
 * @author daixiwei
 *
 */
public class TaurusController extends Controller{

	protected Logger logger;
	
	public TaurusController() {
		logger = Logger.getLogger(getClass());
	}
	
	/**
	 * 响应客户端
	 * @param result
	 * @param data
	 */
	public void responseClient(int result, ITObject data) {
		try {
			TaurusInterceptor.httpResponse(this.getResponse(), result, data);
		} catch (IOException e) {
			logger.error("response client execption!\n",e);
		}
	}
	
	/**
	 * 获取客户端Host
	 * @return
	 */
	public String getRemoteAddr() {
		return getIpAddr(this.getRequest());
	}
	
	private static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
