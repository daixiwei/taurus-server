package com.taurus.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taurus.core.entity.ITObject;
import com.taurus.core.routes.IController;
import com.taurus.core.util.Logger;
import com.taurus.core.util.StringUtil;

/**
 * Controller
 * @author daixiwei
 *
 */
public abstract class Controller implements IController{
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
	
	private static final String[] HEADERS = {   
        "X-Forwarded-For",  
        "Proxy-Client-IP",  
        "WL-Proxy-Client-IP",  
        "HTTP_X_FORWARDED_FOR",  
        "HTTP_X_FORWARDED",  
        "HTTP_X_CLUSTER_CLIENT_IP",  
        "HTTP_CLIENT_IP",  
        "HTTP_FORWARDED_FOR",  
        "HTTP_FORWARDED",  
        "HTTP_VIA",  
        "REMOTE_ADDR",  
        "X-Real-IP"  
    };  
	
	private static final String UNKNOWN = "unknown";
	 /** 
     * 判断ip是否为空，空返回true 
     * @param ip 
     * @return 
     */  
	private static boolean isEmptyIp(final String ip){  
        return (StringUtil.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip));  
    }  
	
	private static String getIpAddr(HttpServletRequest request) {
		String ip = StringUtil.Empty;
		 for (String header : HEADERS) {  
            ip = request.getHeader(header);  
            if(!isEmptyIp(ip)) {  
                 break;  
            }  
        }  
        if(isEmptyIp(ip)){  
            ip = request.getRemoteAddr();  
        }  
		return ip;
	}
}
