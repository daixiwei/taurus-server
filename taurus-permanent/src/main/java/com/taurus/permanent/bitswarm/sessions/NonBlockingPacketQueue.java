package com.taurus.permanent.bitswarm.sessions;

import java.util.LinkedList;

import com.taurus.permanent.bitswarm.data.Packet;

/**
 * 非阻塞网络包队列
 * @author daixiwei daixiwei15@126.com
 */
public final class NonBlockingPacketQueue implements IPacketQueue {
	private final LinkedList<Packet> queue;
	private int maxSize;

	public NonBlockingPacketQueue(int maxSize) {
		this.queue = new LinkedList<Packet>();
		this.maxSize = maxSize;
	}

	public void clear() {
		synchronized (this.queue) {
			queue.clear();
		}
	}

	public int getSize() {
		return queue.size();
	}

	public int getMaxSize() {
		return maxSize;
	}

	public boolean isEmpty() {
		return queue.size() == 0;
	}

	public boolean isFull() {
		return queue.size() >= maxSize;
	}

	public float getPercentageUsed() {
		if (this.maxSize == 0) {
			return 0.0F;
		}
		return queue.size() * 100 / maxSize;
	}

	public Packet peek() {
		Packet packet = null;

		synchronized (this.queue) {
			if (!isEmpty()) {
				packet = queue.get(0);
			}
		}
		return packet;
	}

	public void put(Packet packet){
		if (isFull()) {
			throw new RuntimeException("packet is full!");
		}
		
		synchronized (queue) {
			queue.addLast(packet);
		}
	}

	public void setMaxSize(int size) {
		maxSize = size;
	}

	public Packet take() {
		Packet packet = null;

		synchronized (queue) {
			packet = queue.removeFirst();
		}

		return packet;
	}

}
