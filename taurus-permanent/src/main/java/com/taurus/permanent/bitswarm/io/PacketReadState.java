package com.taurus.permanent.bitswarm.io;

/**
 * PacketReadState
 * @author daixiwei daixiwei15@126.com
 */
public enum PacketReadState {
	/**
	 * 等待新包读取
	 */
	WAIT_NEW_PACKET, 
	/**
	 * 等待读取包大小
	 */
	WAIT_DATA_SIZE, 
	/**
	 * 等待读取碎片包
	 */
	WAIT_DATA_SIZE_FRAGMENT, 
	WAIT_DATA;
}
