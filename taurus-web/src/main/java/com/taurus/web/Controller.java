package com.taurus.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taurus.core.entity.ITObject;
import com.taurus.core.util.Logger;

/**
 * Controller
 * @author daixiwei
 *
 */
public abstract class Controller {
	private String actionKey;
	private String session;
	private ITObject param;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private volatile boolean isFinish;
	protected Logger logger;
	
	void _init(HttpServletRequest request,HttpServletResponse response,String actionKey,String session,ITObject param){
		this.logger = Logger.getLogger(getClass());
		this.request = request;
		this.response = response;
		this.actionKey = actionKey;
		this.session = session;
		this.param = param;
	}
	
	/**
	 * get action key.
	 * @return
	 */
	public String getActionKey() {
		return actionKey;
	}
	
	/**
	 * 获取session对象
	 * @return
	 */
	public String getSession() {
		return session;
	}

	/**
	 * 获取参数
	 * @return
	 */
	public ITObject getParams() {
		return param;
	}
	
	/**
	 * 动态响应客户端请示
	 * @param result 响应结果 0成功
	 * @param params 数据参数
	 */
	public void sendResponse(int result, ITObject params) {
		if(isFinish) {
			throw new RuntimeException("This response is finish!");
		}
		isFinish = true;
		try {
			WebUtils.httpResponse(this.response, result, params);
		} catch (IOException e) {
			logger.error("response client execption!\n",e);
		}
	}
	

	
	/**
	 * 获取客户端Host
	 * @return
	 */
	public String getRemoteAddr() {
		return getIpAddr(this.request);
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
