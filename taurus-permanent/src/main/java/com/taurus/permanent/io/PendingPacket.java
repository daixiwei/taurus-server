package com.taurus.permanent.io;

/**
 * PendingPacket
 * @author daixiwei daixiwei15@126.com
 */
public class PendingPacket {
	private Object			buffer;
	private int				expectedLen	= -1;
	
	public PendingPacket() {

	}
	

	
	public Object getBuffer() {
		return buffer;
	}
	
	public void setBuffer(Object buffer) {
		this.buffer = buffer;
	}
	
	public int getExpectedLen() {
		return this.expectedLen;
	}
	
	public void setExpectedLen(int len) {
		this.expectedLen = len;
	}
	
	public String toString() {
		return  buffer.toString();
	}
}
