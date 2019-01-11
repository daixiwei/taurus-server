package com.taurus.permanent.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.taurus.core.util.Logger;


/**
 * ip连接过滤
 * @author daixiwei daixiwei15@126.com
 */
public class ConnectionFilter {
	private final Set<String>							addressWhiteList;
	private final Set<String>							bannedAddresses;
	private final ConcurrentMap<String, AtomicInteger>	addressMap;
	private int											maxConnectionsPerIp = 10;
	private Logger 										logger;
	
	public ConnectionFilter() {
		this.addressWhiteList = new HashSet<String>();
		this.bannedAddresses = new HashSet<String>();
		this.addressMap = new ConcurrentHashMap<String, AtomicInteger>();
		logger = Logger.getLogger(ConnectionFilter.class);
	}
	/**
	 * 获取所有黑名单列表
	 */
	public void addBannedAddress(String ipAddress) {
		synchronized (bannedAddresses) {
			bannedAddresses.add(ipAddress);
		}
	}
	
	/**
	 * 获取所有白名单列表
	 */
	public void addWhiteListAddress(String ipAddress) {
		synchronized (this.addressWhiteList) {
			this.addressWhiteList.add(ipAddress);
		}
	}
	
	/**
	 * 获取所有黑名单列表
	 */
	public String[] getBannedAddresses() {
		String[] set = (String[]) null;
		
		synchronized (this.bannedAddresses) {
			set = new String[bannedAddresses.size()];
			set = (String[]) bannedAddresses.toArray(set);
		}
		
		return set;
	}
	
	/**
	 * 获取每个IP最大的连接数
	 */
	public int getMaxConnectionsPerIp() {
		return this.maxConnectionsPerIp;
	}

	/**
	 * 获取白名单列表
	 */
	public String[] getWhiteListAddresses() {
		String[] set = (String[]) null;
		
		synchronized (this.addressWhiteList) {
			set = new String[this.addressWhiteList.size()];
			set = (String[]) this.addressWhiteList.toArray(set);
		}
		
		return set;
	}
	
	public void removeAddress(String ipAddress) {
		synchronized (this.addressMap) {
			AtomicInteger count = (AtomicInteger) this.addressMap.get(ipAddress);
			
			if (count != null) {
				int value = count.decrementAndGet();
				
				if (value == 0)
					this.addressMap.remove(ipAddress);
			}
		}
	}
	
	/**
	 * 移除黑名单IP地址
	 */
	public void removeBannedAddress(String ipAddress) {
		synchronized (this.bannedAddresses) {
			this.bannedAddresses.remove(ipAddress);
		}
	}
	
	/**
	 * 移除白名单IP地址
	 */
	public void removeWhiteListAddress(String ipAddress) {
		synchronized (this.addressWhiteList) {
			this.addressWhiteList.remove(ipAddress);
		}
	}
	
	/**
	 * 获取每个IP最大连接数
	 */
	public void setMaxConnectionsPerIp(int max) {
		this.maxConnectionsPerIp = max;
	}
	
	public boolean validateAndAddAddress(String ipAddress) {
		synchronized (this.addressWhiteList) {
			if (this.addressWhiteList.contains(ipAddress)) {
				return true;
			}
		}
		
		if (isAddressBanned(ipAddress)) {
			logger.warn("Ip Address: " + ipAddress + " is banned!");
			return false;
		}
		
		synchronized (this.addressMap) {
			AtomicInteger count = (AtomicInteger) addressMap.get(ipAddress);
			
			if ((count != null) && (count.intValue() >= maxConnectionsPerIp)) {
				logger.warn("Refused connection.  Ip Address: " + ipAddress + " has reached maximum allowed connections.");
				return false;
			}
			
			if (count == null) {
				count = new AtomicInteger(1);
				this.addressMap.put(ipAddress, count);
			} else {
				count.incrementAndGet();
			}
		}
		return true;
	}
	
	private boolean isAddressBanned(String ip) {
		boolean isBanned = false;
		
		synchronized (this.bannedAddresses) {
			isBanned = this.bannedAddresses.contains(ip);
		}
		
		return isBanned;
	}
}
