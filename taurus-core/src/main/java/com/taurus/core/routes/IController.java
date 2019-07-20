package com.taurus.core.routes;

import com.taurus.core.entity.ITObject;

/**
 * Controller
 * @author daixiwei
 *
 */
public interface IController {
	
	
	/**
	 * get action key.
	 * @return
	 */
	public String getActionKey() ;
	
	/**
	 * 获取session对象
	 * @return
	 */
	public Object getSession() ;

	/**
	 * 获取参数
	 * @return
	 */
	public ITObject getParams() ;
	
	/**
	 * 动态响应客户端请示
	 * @param result 响应结果 0成功
	 * @param params 数据参数
	 */
	public void sendResponse(int result, ITObject params);
	
}
