package com.taurus.permanent.bitswarm.io;

/**
 * ProcessedPacket
 * @author daixiwei daixiwei15@126.com
 */
public class ProcessedPacket {
	private byte[]			data;
	private PacketReadState	state;
	
	public ProcessedPacket(PacketReadState state, byte[] data) {
		this.state = state;
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public PacketReadState getState() {
		return state;
	}
}
