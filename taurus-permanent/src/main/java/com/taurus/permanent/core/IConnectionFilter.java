package com.taurus.permanent.core;


/**
 * ip连接过滤通用接口
 * @author daixiwei daixiwei15@126.com
 */
public interface IConnectionFilter {
	/**
	 * 添加黑名单IP地址
	 * @param ipAddress
	 */
	public void addBannedAddress(String ipAddress);
	
	/**
	 * 移除黑名单IP地址
	 * @param ipAddress
	 */
	public void removeBannedAddress(String ipAddress);
	
	/**
	 * 获取所有黑名单列表
	 * @return
	 */
	public String[] getBannedAddresses();
	
	
	public boolean validateAndAddAddress(String ipAddress);
	
	public void removeAddress(String ipAddress);
	
	/**
	 * 添加白名单地址
	 * @param ipAddress
	 */
	public void addWhiteListAddress(String ipAddress);
	
	/**
	 * 移除白名单地址
	 * @param ipAddress
	 */
	public void removeWhiteListAddress(String ipAddress);
	
	/**
	 * 获取白名单列表
	 * @return
	 */
	public String[] getWhiteListAddresses();
	
	/**
	 * 获取每个IP最大的连接数
	 * @return
	 */
	public int getMaxConnectionsPerIp();
	
	/**
	 * 设置每个IP最大的连接数
	 * @param max
	 */
	public void setMaxConnectionsPerIp(int max);
}
