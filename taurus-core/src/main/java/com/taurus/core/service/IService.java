package com.taurus.core.service;

/**
 * 通用服务接口
 * @author daixiwei daixiwei15@126.com
 *
 */
public interface IService {
	
	/**
	 * 初始化Service
	 * @param o
	 */
	public void init(Object o);
	
	/**
	 * 销毁Service
	 * @param o
	 */
	public void destroy(Object o);
	
	/**
	 * 获取Service名称
	 * @return
	 */
	public String getName();
	
	/**
	 * 设置Service名称
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * Service是否激活
	 * @return
	 */
	public boolean isActive();
}
