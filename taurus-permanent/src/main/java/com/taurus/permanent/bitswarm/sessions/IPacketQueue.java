package com.taurus.permanent.bitswarm.sessions;

import com.taurus.permanent.bitswarm.data.Packet;

/**
 * 网络包队列接口
 * @author daixiwei daixiwei15@126.com
 */
public interface IPacketQueue {
	/**
	 * 返回队列头部的元素
	 * @return
	 */
	public Packet peek();
	
	/**
	 * 移除并返回队列头部的元素
	 * @return
	 */
	public Packet take();

	/**
	 * 队列是否为空
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * 队列是否已满
	 * @return
	 */
	public boolean isFull();

	/**
	 * 获取队列当前大小
	 * @return
	 */
	public int getSize();

	/**
	 * 获取队列最大大小
	 * @return
	 */
	public int getMaxSize();

	/**
	 * 设置队列最大大小
	 * @param size
	 */
	public void setMaxSize(int size);
	
	/**
	 * 队列当前使用百分比
	 * @return
	 */
	public float getPercentageUsed();

	/**
	 * 清理队列
	 */
	public void clear();
	
	/**
	 * 添加一个元素
	 * @param packet
	 */
	public void put(Packet packet);
}
